package com.rakib.biometric_authentication

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private var cancellationSignal: CancellationSignal? = null

    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() =
            @RequiresApi(Build.VERSION_CODES.P)
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errorString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errorString)
                    notifyUser("Authentication Error: $errorString")
                }

                override fun onAuthenticationSucceeded(authenticationResult: BiometricPrompt.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(authenticationResult)
                    notifyUser("Authentication Succeeded!")
                    // Execute Intended Secure Task
                }
            }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkBiometricSupport()

        val authenticateButton = findViewById<Button>(R.id.authenticateButton)

        authenticateButton.setOnClickListener {
            val promptInfo: BiometricPrompt = BiometricPrompt.Builder(this)
                .setTitle("Biometric Authentication")
                .setSubtitle("Login using biometric credential.")
                .setDescription("Fingerprint Authentication")
                .setNegativeButton(
                    "Cancel",
                    this.mainExecutor
                ) { _, _ ->
                }.build()

            promptInfo.authenticate(
                getCancellationSignal(),
                mainExecutor,
                authenticationCallback
            )
        }
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun checkBiometricSupport(): Boolean {
        val keyguardManager: KeyguardManager =
            getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!keyguardManager.isKeyguardSecure) {
            notifyUser("No Fingerprint enrolled.")
            return false
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.USE_BIOMETRIC
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notifyUser("Permission not granted.")
            return false
        }

        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            true
        } else true
    }

    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()

        cancellationSignal?.setOnCancelListener {
            notifyUser("Authentication is cancelled by the user.")
        }

        return cancellationSignal as CancellationSignal
    }
}