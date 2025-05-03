package com.bitnetika.comndroid.utils

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Base64
import androidx.annotation.ColorInt
import androidx.core.net.toUri
import okhttp3.Request

fun Boolean?.orFalse(): Boolean = this ?: false

fun Int?.orZero(): Int = this ?: 0

fun Int?.orDefault(default: Int): Int = this ?: default

fun Long?.orZero(): Long = this ?: 0

fun Long?.orDefault(default: Long): Long = this ?: default

fun Float?.orZero(): Float = this ?: 0F

fun Float?.orDefault(default: Float): Float = this ?: default

fun Double?.orZero(): Double = this ?: 0.0

fun Double?.orDefault(default: Double): Double = this ?: default

fun String?.orDefault(default: String): String = this ?: default

fun String?.isEquals(other: String?): Boolean {
    return equals(other, true)
}

fun CharSequence?.hasContains(other: String?): Boolean {
    if (this == null || other == null) {
        return false
    }

    return contains(other, true)
}

fun String?.encodeBase64(): String? {
    this ?: return null
    return Base64.encodeToString(encodeToByteArray(), Base64.NO_WRAP)
}

fun String.toHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}

fun String?.toUriOrNull(): Uri? {
    this ?: return null

    return try {
        this.toUri()
    } catch (ex: Exception) {
        null
    }
}

fun String.toColor(@ColorInt default: Int = Color.TRANSPARENT): Int {
    return try {
        Color.parseColor(this)
    } catch (ex: Exception) {
        default
    }
}

fun <T> Bundle?.getParcelableData(key: String, klass: Class<T>): T? {
    this ?: return null

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelable(key, klass)
        } else {
            getParcelable(key)
        }
    } catch (ex: Exception) {
        null
    }
}

fun <T> Intent?.getParcelableData(key: String, klass: Class<T>): T? {
    this ?: return null

    return try {
        getParcelableExtra(key)
    } catch (ex: Exception) {
        null
    }
}

fun Request.Builder.setHeader(name: String?, value: String?) {
    if (name.isNullOrBlank() || value.isNullOrBlank()) {
        return
    }

    this.header(name, value)
}

fun createQueryMap(vararg params: Pair<String, Any?>): Map<String, String> {
    return params.mapNotNull { (key, value) ->
        val valuableString = value?.toString()
        return@mapNotNull if (valuableString.isNullOrBlank()) null else key to valuableString
    }
        .toMap()
}
