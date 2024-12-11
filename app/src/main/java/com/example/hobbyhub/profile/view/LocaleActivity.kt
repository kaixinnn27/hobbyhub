package com.example.hobbyhub.profile.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hobbyhub.LandingActivity
import com.example.hobbyhub.R
import com.example.hobbyhub.databinding.ActivityLocaleBinding
import com.example.hobbyhub.utility.LocaleManager

class LocaleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocaleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocaleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupLanguageSelector()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupLanguageSelector() {
        val radioGroup = binding.radioGroupLanguages

        val currentLanguage = LocaleManager.getLocale(this)

        when (currentLanguage) {
            "en" -> binding.radioButtonEnglish.isChecked = true
            "zh" -> binding.radioButtonChinese.isChecked = true
            "ms" -> binding.radioButtonMalay.isChecked = true
            else -> binding.radioButtonEnglish.isChecked = true  // Default to English if no match
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedLanguage = when (checkedId) {
                R.id.radioButtonEnglish -> "en"
                R.id.radioButtonChinese -> "zh"
                R.id.radioButtonMalay -> "ms"
                else -> "en"
            }

            if (selectedLanguage == currentLanguage) {
                return@setOnCheckedChangeListener
            }

            LocaleManager.setLocale(this@LocaleActivity, selectedLanguage)
            restartApp()
        }
    }

    private fun restartApp() {
        val intent = Intent(applicationContext, LandingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}