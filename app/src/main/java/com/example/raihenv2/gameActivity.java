package com.example.raihenv2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class gameActivity extends Fragment {

    public static TextView txt_score, txt_best_score, txt_score_over, select_input;
    public static RelativeLayout rl_game_over;
    public static Button btn_start, btn_back;
    private GameView gv;
    private MediaPlayer mediaPlayer;
    private SpeechRecognizer sr;
    private Intent intent;
    private RadioButton rbTouch, rbVoice;
    private RadioGroup radioGroup;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        Constants.SCREEN_WIDTH = dm.widthPixels;
        Constants.SCREEN_HEIGHT = dm.heightPixels;
        View view = inflater.inflate(R.layout.activity_game, container, false);
        txt_score = view.findViewById(R.id.txt_score);
        txt_best_score = view.findViewById(R.id.txt_best_score);
        txt_score_over = view.findViewById(R.id.txt_score_over);
        rl_game_over = view.findViewById(R.id.rl_game_over);
        btn_start = view.findViewById(R.id.btn_start);
        btn_back = view.findViewById(R.id.btn_back);
        select_input = view.findViewById(R.id.select_mode);
        gv = view.findViewById(R.id.gv);
        rbTouch = view.findViewById(R.id.rbTouch);
        rbVoice = view.findViewById(R.id.rbVoice);
        radioGroup = view.findViewById(R.id.play_mode);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbTouch) {
                    mediaPlayer.start();
                    Constants.GAME_MODE = 0;
                } else {
                    mediaPlayer.pause();
                    Constants.GAME_MODE = 1;
                }
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.CURRENT_PAGE = 0;
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new HomeFragment()).commit();
            }
        });
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FUCK", Constants.GAME_MODE + "");
                gv.setStart(true);
                if (rbTouch.isChecked()) {
                    Constants.GAME_MODE = 0;
                } else if (rbVoice.isChecked()) {
                    Constants.GAME_MODE = 1;
                }

                txt_score.setVisibility(View.VISIBLE);
                select_input.setVisibility(View.INVISIBLE);
                btn_start.setVisibility(View.INVISIBLE);
                btn_back.setVisibility(View.INVISIBLE);
                radioGroup.setVisibility(View.INVISIBLE);
                if (Constants.GAME_MODE == 1) {
                    mediaPlayer.pause();
                    sr = SpeechRecognizer.createSpeechRecognizer(getActivity());
                    sr.setRecognitionListener(new listener());
                    startRec();
                }
                Log.d("FUCK", Constants.GAME_MODE + "");
            }
        });
        rl_game_over.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_start.setVisibility(View.VISIBLE);
                select_input.setVisibility(View.VISIBLE);
                btn_back.setVisibility(View.VISIBLE);
                radioGroup.setVisibility(View.VISIBLE);
                rl_game_over.setVisibility(View.INVISIBLE);
                gv.setStart(false);
                gv.reset();
                if (Constants.GAME_MODE == 1) {
                    sr.destroy();
                }
            }
        });
        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.sillychipsong);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        requestRecordAudioPermission();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mediaPlayer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mediaPlayer.pause();
    }

    private void startRec() {

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
        //intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 20000000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 50000000);
        //intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
        sr.startListening(intent);
    }

    private void requestRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String requiredPermission = Manifest.permission.RECORD_AUDIO;

            // If the user previously denied this permission then show a message explaining why
            // this permission is needed
            if (getActivity().checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 101);
            }
        }
    }

    class listener implements RecognitionListener
    {
        public void onReadyForSpeech(Bundle params)
        {
            //Log.d("TAG", "onReadyForSpeech");
        }
        public void onBeginningOfSpeech()
        {
            //Log.d("TAG", "onBeginningOfSpeech");
        }
        public void onRmsChanged(float rmsdB)
        {
            //Log.d("TAG", "onRmsChanged" + rmsdB);
            if (rmsdB > 8) {
                gv.setDropping();
            }
        }
        public void onBufferReceived(byte[] buffer)
        {
            Log.d("TAG", "onBufferReceived");
        }
        public void onEndOfSpeech()
        {
            //Log.d("TAG", "onEndofSpeech");
        }
        public void onError(int error)
        {
            //Log.d("TAG",  "error " +  error);

            sr.startListening(intent);
        }
        public void onResults(Bundle results)
        {
            startRec();
        }
        public void onPartialResults(Bundle partialResults)
        {
            //Log.d("TAG", "onPartialResults");
        }
        public void onEvent(int eventType, Bundle params)
        {
            //Log.d("TAG", "onEvent " + eventType);
        }
    }
}