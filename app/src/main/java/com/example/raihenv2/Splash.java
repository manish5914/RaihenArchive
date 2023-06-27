package com.example.raihenv2;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.concurrent.Executor;

public class Splash extends AppCompatActivity {
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        executor = ContextCompat.getMainExecutor(this);
        ConstraintLayout biometricLoginButton = findViewById(R.id.biometric_login);
        biometricPrompt = new BiometricPrompt(Splash.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Touch on screen to start biometric login.", Toast.LENGTH_LONG).show();
                if(errString.equals("No fingerprints enrolled."))
                {
                    Toast.makeText(getApplicationContext(), "Skipping Biometric Login" ,Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Splash.this, LoginActivity.class));
                    finish();
                }else if(errString.equals("This device does not have a fingerprint sensor"))
                {
                    Toast.makeText(getApplicationContext(), "Skipping Biometric Login" ,Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Splash.this, LoginActivity.class));
                    finish();
                }
            }
            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                biometricLoginButton.setOnClickListener(null);
                Toast.makeText(getApplicationContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Splash.this, LoginActivity.class));
                finish();
            }

        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Cancel")
                .setConfirmationRequired(true)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();

        ImageView text1 = findViewById(R.id.icon_text1);
        ImageView text2 = findViewById(R.id.icon_text2);
        ImageView text3 = findViewById(R.id.icon_text3);
        ImageView text4 = findViewById(R.id.icon_text4);
        ImageView text5 = findViewById(R.id.icon_text5);
        ImageView text6 = findViewById(R.id.icon_text6);


        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadein);
        Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadein);
        Animation animation3 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadein);
        Animation animation4 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadein);
        Animation animation5 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadein);
        Animation animation6 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fadein);

        animation1.setDuration(500);
        animation2.setDuration(500);
        animation3.setDuration(500);
        animation4.setDuration(500);
        animation5.setDuration(500);
        animation6.setDuration(500);

        animation2.setStartOffset(400);
        animation3.setStartOffset(800);
        animation4.setStartOffset(1200);
        animation5.setStartOffset(1600);
        animation6.setStartOffset(2000);

        text1.startAnimation(animation1);
        text2.startAnimation(animation2);
        text3.startAnimation(animation3);
        text4.startAnimation(animation4);
        text5.startAnimation(animation5);
        text6.startAnimation(animation6);



        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
                    public void run() {
                try {
                    biometricLoginButton.setOnClickListener(view -> { biometricPrompt.authenticate(promptInfo);
                    });
                    ContextWrapper cw = new ContextWrapper(getApplicationContext());
                    File directory = cw.getDir("SomethingRepo", Context.MODE_PRIVATE);
                    File file = new File(directory, "empty.txt");

                    if (file.exists()) {
                        Constants.BIOMETRIC_VAL = true;
                        biometricPrompt.authenticate(promptInfo);
                    } else {
                        startActivity(new Intent(Splash.this, LoginActivity.class));
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },3000);


    }

}