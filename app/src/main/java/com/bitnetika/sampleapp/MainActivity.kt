package com.bitnetika.sampleapp

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bitnetika.comndroid.utils.isEquals
import com.bitnetika.comndroid.utils.makeClickable

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val isEquals = "Some text".isEquals("Another text")
        findViewById<TextView>(R.id.termsTextView)?.apply {
            setText(
                "By clicking on text, you agree to the terms and policies of the app".makeClickable(
                    context,
                    "terms" to { Toast.makeText(context, "Terms clicked", Toast.LENGTH_SHORT).show() },
                    "policies" to { Toast.makeText(context, "Policies clicked", Toast.LENGTH_SHORT).show() },
                    defaultClickAction = { Toast.makeText(context, "Default click", Toast.LENGTH_SHORT).show() },
                    colorRes = R.color.purple_200,
                    showUnderline = true
                )
            )
            movementMethod = LinkMovementMethod.getInstance()
        }

    }
}
