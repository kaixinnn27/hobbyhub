package com.example.hobbyhub

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hobbyhub.utility.LocaleHelper
import com.example.hobbyhub.utility.LocaleManager

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val userLocale = LocaleManager.getLocale(newBase)
        val contextWithLocale = LocaleHelper.setLocale(newBase, userLocale)
        super.attachBaseContext(contextWithLocale)
    }
}
