package com.example.hobbyhub.utility

import androidx.biometric.BiometricPrompt

interface BiometricAuthListener {
    fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult)
    fun onBiometricAuthenticateError(errorCode: Int, errString: String)
    fun onBiometricAuthenticateFailed()
}