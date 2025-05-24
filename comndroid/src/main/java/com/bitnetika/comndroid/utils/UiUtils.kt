package com.bitnetika.comndroid.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.CheckResult
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.ResourcesCompat.ID_NULL
import androidx.core.content.res.getDrawableOrThrow
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.bitnetika.comndroid.listeners.OnTapListener
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce

fun Context.currentLocale(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        resources.configuration.locales[0]?.language ?: LOCALE_EN
    } else {
        resources.configuration.locale?.language ?: LOCALE_EN
    }
}

fun Context?.dimenSize(@DimenRes dimenRes: Int?): Int {
    return runCatching {
        this?.resources?.getDimensionPixelSize(dimenRes.orDefault(ID_NULL))
    }
        .getOrNull()
        .orZero()
}

fun Context?.attrDimenSize(@AttrRes attrRes: Int?): Int {
    this ?: return ID_NULL

    return runCatching {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes.orDefault(ID_NULL), typedValue, true)
        return@runCatching TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
    }
        .getOrNull()
        .orZero()
}

fun Context?.pxToDp(pixels: Int): Float {
    val density = this?.resources?.displayMetrics?.density.orZero()
    return runCatching { pixels.orZero() / density }.getOrNull().orZero()
}

fun Context?.compatColor(@ColorRes colorRes: Int?): Int {
    this ?: return Color.TRANSPARENT

    return runCatching { ResourcesCompat.getColor(resources, colorRes.orDefault(ID_NULL), theme) }
        .getOrNull()
        ?: Color.TRANSPARENT
}

fun Context?.attrColor(@AttrRes attrRes: Int?): Int {
    this ?: return Color.TRANSPARENT

    return runCatching {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes.orDefault(ID_NULL), typedValue, true)
        return@runCatching typedValue.data
    }
        .getOrNull()
        ?: Color.TRANSPARENT
}

fun Context?.compatDrawable(@DrawableRes drawableRes: Int?): Drawable? {
    this ?: return null

    return runCatching {
        ResourcesCompat.getDrawable(resources, drawableRes.orDefault(ID_NULL), theme)
    }
        .getOrNull()
}

fun Context?.attrDrawable(@AttrRes drawableAttr: Int?, styleRes: Int = 0): Drawable? {
    this ?: return null

    return runCatching {
        val value = TypedValue()
        if (styleRes != 0) {
            theme.resolveAttribute(styleRes, value, false)
        }

        val style = value.data
        val attributes = intArrayOf(drawableAttr.orDefault(ID_NULL))
        val array = obtainStyledAttributes(style, attributes)

        val drawable: Drawable? = try {
            array.getDrawableOrThrow(0)
        } catch (ex: Exception) {
            null
        }

        array.recycle()
        return@runCatching drawable
    }
        .getOrNull()
}

fun Context?.selectableItemBgRes(): Int {
    this ?: return ID_NULL

    return runCatching {
        val outValue = TypedValue()
        theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        return@runCatching outValue.resourceId
    }
        .getOrNull()
        .orZero()
}

/**
 * Makes the entire string clickable with uniform styling
 */
fun String.makeClickable(
    context: Context?,
    clickAction: ((View) -> Unit)? = null,
    @ColorRes colorRes: Int? = null,
    showUnderline: Boolean = false
): SpannableString {
    if (isEmpty()) {
        return SpannableString(EMPTY_STRING)
    }

    val color = colorRes?.let { context.compatColor(it) }
        ?: context.attrColor(com.google.android.material.R.attr.colorSecondary)

    return SpannableString(this).apply {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View)  {
                clickAction?.invoke(widget)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = color
                ds.isUnderlineText = showUnderline
            }
        }
        setSpan(clickableSpan, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

/**
 * Makes specific substrings clickable with optional fallback
 * @param clickableParts Pairs of substring and click handler
 * @param defaultClickAction Fallback when no substrings match
 */
fun String.makeClickable(
    context: Context?,
    vararg clickableParts: Pair<String, (View) -> Unit> = emptyArray(),
    defaultClickAction: ((View) -> Unit)? = null,
    @ColorRes colorRes: Int? = null,
    showUnderline: Boolean = false
): SpannableString {
    if (isEmpty()) {
        return SpannableString(EMPTY_STRING)
    }

    val color = colorRes?.let { context.compatColor(it) }
        ?: context.attrColor(com.google.android.material.R.attr.colorSecondary)

    val spannable = SpannableString(this)
    var hasClickableSpans = false

    clickableParts.forEach { (substring, onClick) ->
        substring
            .takeIf { it.isNotBlank() && contains(it) }
            ?.let {
                val start = indexOf(it)
                spannable.setSpan(
                    object : ClickableSpan() {
                        override fun onClick(widget: View) = onClick(widget)
                        override fun updateDrawState(ds: TextPaint) {
                            ds.color = color
                            ds.isUnderlineText = showUnderline
                        }
                    },
                    start,
                    start + it.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                hasClickableSpans = true
            }
    }

    return if (hasClickableSpans) {
        spannable
    } else {
        makeClickable(context, defaultClickAction, colorRes, showUnderline)
    }
}

fun Context?.showToast(message: String?, duration: Int = Toast.LENGTH_LONG) {
    if (this == null || message.isNullOrBlank()) {
        return
    }

    Toast.makeText(this, message, duration).show()
}

fun Context?.showToast(@StringRes messageRes: Int?, duration: Int = Toast.LENGTH_LONG) {
    this ?: return

    val message = runCatching { getString(messageRes.orDefault(ID_NULL)) }.getOrNull()
    showToast(message, duration)
}

fun DialogFragment.setPercentileWidth(percentage: Float) {
    try {
        val percent = percentage / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = Rect(0, 0, dm.widthPixels, dm.heightPixels)
        val percentileWidth = rect.width() * percent

        dialog?.window?.setLayout(percentileWidth.toInt(), WRAP_CONTENT)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun DialogFragment.makeFullScreenDialog() {
    try {
        dialog?.window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun Activity.showSoftKeyboard(editText: EditText?) {
    editText ?: return

    try {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun Activity.hideSoftKeyboard() {
    try {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(findViewById<View?>(android.R.id.content).windowToken, 0)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun Fragment.hideSoftKeyboard() {
    activity?.hideSoftKeyboard()
}

private fun View.isTouchInView(event: MotionEvent): Boolean {
    val location = IntArray(2)
    getLocationOnScreen(location)
    return event.rawX >= location[0]
            && event.rawX <= location[0] + width
            && event.rawY >= location[1]
            && event.rawY <= location[1] + height
}

fun ViewGroup.setupKeyboardAutoHide(activity: Activity?) {
    setOnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            val focusedView = activity?.currentFocus
            if (focusedView is EditText) {
                if (!focusedView.isTouchInView(event)) {
                    activity.hideSoftKeyboard()
                    focusedView.clearFocus()
                }
            }
        }

        // Handle click detection for accessibility
        if (event.action == MotionEvent.ACTION_UP) {
            v.performClick()
        }
        false // Don't consume event
    }
}

@OptIn(FlowPreview::class)
@CheckResult
fun TextView.doOnTextChange(debounceMillis: Long = 300L): Flow<CharSequence> {
    return callbackFlow {
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // empty-implementation
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty-implementation
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { trySend(it) }
            }
        }

        trySend(text)
        addTextChangedListener(textWatcher)
        awaitClose { removeTextChangedListener(textWatcher) }
    }
        .debounce(debounceMillis)
        .conflate()
}

fun Context?.openAppSettings() {
    this ?: return

    val appUri = Uri.parse("package:${packageName}")
    val launchIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appUri)
    launchIntent.addCategory(Intent.CATEGORY_DEFAULT)
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        if (launchIntent.resolveActivity(packageManager) != null) {
            startActivity(launchIntent)
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun View.setOnTapListener(interval: Long = 1000, onTap: (view: View?) -> Unit) {
    setOnClickListener(OnTapListener(interval, onTap))
}

fun Context?.copyToClipboard(text: String, listener: (() -> Unit)? = null) {
    this ?: return
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return

    val clip = ClipData.newPlainText(text, text)
    clipboard.setPrimaryClip(clip)
    listener?.invoke()
}

fun Context?.openInPlayStore() {
    this ?: return

    try {
        val appUrl = "https://play.google.com/store/apps/details?id=${packageName}"
        val appUri = appUrl.toUriOrNull() ?: return

        val updateIntent = Intent(Intent.ACTION_VIEW, appUri)
        startActivity(updateIntent)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}
