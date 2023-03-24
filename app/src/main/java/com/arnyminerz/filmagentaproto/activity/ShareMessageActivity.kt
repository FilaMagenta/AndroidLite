package com.arnyminerz.filmagentaproto.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ShareMessageActivity: AppCompatActivity() {
    companion object {
        const val EXTRA_MESSAGE = "message"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val message = intent.getStringExtra(EXTRA_MESSAGE)
        if (message != null) {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
            }
            startActivity(intent)
        }
        setResult(Activity.RESULT_OK)
        finish()
    }
}