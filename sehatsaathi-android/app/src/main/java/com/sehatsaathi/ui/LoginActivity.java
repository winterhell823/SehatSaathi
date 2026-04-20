package com.sehatsaathi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

import com.sehatsaathi.R;

public class LoginActivity extends AppCompatActivity {

    private Button btnFingerprintLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnFingerprintLogin = findViewById(R.id.btnFingerprintLogin);

        btnFingerprintLogin.setOnClickListener(v -> showBiometricPrompt());
    }

    private void showBiometricPrompt() {
        BiometricManager biometricManager = BiometricManager.from(this);

        int result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
        );

        if (result == BiometricManager.BIOMETRIC_SUCCESS) {
            Executor executor = ContextCompat.getMainExecutor(this);

            BiometricPrompt biometricPrompt = new BiometricPrompt(
                    this,
                    executor,
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(
                                @NonNull BiometricPrompt.AuthenticationResult authResult) {
                            super.onAuthenticationSucceeded(authResult);
                            Toast.makeText(LoginActivity.this, "Fingerprint verified", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        }

                        @Override
                        public void onAuthenticationError(int errorCode,
                                                          @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            Toast.makeText(LoginActivity.this, errString, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Toast.makeText(LoginActivity.this, "Fingerprint not recognized", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            BiometricPrompt.PromptInfo promptInfo =
                    new BiometricPrompt.PromptInfo.Builder()
                            .setTitle("SehatSaathi Login")
                            .setSubtitle("Use your fingerprint to continue")
                            .setNegativeButtonText("Cancel")
                            .build();

            biometricPrompt.authenticate(promptInfo);

        } else if (result == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            Toast.makeText(this, "No fingerprint enrolled on this device", Toast.LENGTH_LONG).show();
        } else if (result == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE) {
            Toast.makeText(this, "This device has no biometric hardware", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Biometric authentication unavailable", Toast.LENGTH_LONG).show();
        }
    }
}
