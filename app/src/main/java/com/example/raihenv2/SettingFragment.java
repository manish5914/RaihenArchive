package com.example.raihenv2;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.Locale;

public class SettingFragment extends Fragment {
    private TextToSpeech textToSpeech;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_setting, container, false);

        textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                textToSpeech.setLanguage(Locale.US);

            }
        });
        //Sta
        //Start of feedback
        ImageView TTS = view.findViewById(R.id.speak);
        TTS.setOnClickListener(this::TexttoSpeechButton);

        //End of feedback

        TextView textView = view.findViewById(R.id.about_us);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/dQw4w9WgXcQ"));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage("com.google.android.youtube");
                startActivity(intent);
            }
        });
        ToggleButton toggleButton;
        toggleButton =view.findViewById(R.id.toggleButton);
        if (Constants.BIOMETRIC_VAL) {
            toggleButton.setChecked(true);
        }
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
                File directory = cw.getDir("SomethingRepo", Context.MODE_PRIVATE);
                File file = new File(directory, "empty.txt");
                if (isChecked) {
                    Constants.BIOMETRIC_VAL = true;
                    file.mkdirs();
                } else {
                    Constants.BIOMETRIC_VAL = false;
                    file.delete();
                }
            }
        });
        
        TextView feedback = view.findViewById(R.id.feedback);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Feedback.class);
                startActivity(intent);
            }
        });
        return view;
    }

    public void TexttoSpeechButton(View view)
    {
        if(!textToSpeech.isSpeaking()) {
            textToSpeech.speak("This is the Settings Page,   Here you can  enable or disable biometrics identification.        You can also learn more about the developers and provide a feedback to help them make the app better", TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else {
            Toast.makeText(getActivity(), "System Busy", Toast.LENGTH_SHORT).show();
        }
    }
}
