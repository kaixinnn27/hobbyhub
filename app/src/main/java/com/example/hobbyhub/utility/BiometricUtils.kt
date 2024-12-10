package com.example.hobbyhub.utility

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.appcompat.app.AppCompatActivity

object BiometricUtils {

    /*
     * Check whether the device is capable of biometric authentication
     */
    private fun hasBiometricCapability(context: Context): Int {
        return BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
    }

    /**
     * Check if the biometric system is ready for authentication
     */
    fun isBiometricReady(context: Context): Boolean {
        return hasBiometricCapability(context) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Build the biometric prompt information
     */
    private fun setBiometricPromptInfo(
        title: String,
        subtitle: String,
        description: String,
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
    }

    /**
     * Initialize the biometric prompt
     */
    private fun initBiometricPrompt(
        activity: AppCompatActivity,
        listener: BiometricAuthListener
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                listener.onBiometricAuthenticateError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                listener.onBiometricAuthenticateFailed()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                listener.onBiometricAuthenticateSuccess(result)
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }

    /**
     * Display the biometric prompt
     */
    fun showBiometricPrompt(
        title: String = "Biometric Authentication",
        subtitle: String = "Authenticate using biometric credentials.",
        description: String = "Use your fingerprint or device credentials to continue.",
        activity: AppCompatActivity,
        listener: BiometricAuthListener,
        cryptoObject: BiometricPrompt.CryptoObject? = null,
    ) {
        val promptInfo = setBiometricPromptInfo(title, subtitle, description)
        val biometricPrompt = initBiometricPrompt(activity, listener)

        if (cryptoObject == null) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            biometricPrompt.authenticate(promptInfo, cryptoObject)
        }
    }
}
