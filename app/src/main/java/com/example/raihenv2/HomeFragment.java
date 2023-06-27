package com.example.raihenv2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    ImageView profileImage;
    ProgressDialog progressDialog;

    //private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth myAuth;
    private StorageReference profileRef;
    private TextToSpeech textToSpeech;


    public static Boolean today;
    public static Boolean tom;
    public static RadioButton manish;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home, container, false);

        profileImage = (ImageView) view.findViewById(R.id.home_image);
        manish = view.findViewById(R.id.nothing);
        textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                textToSpeech.setLanguage(Locale.US);

            }
        });
        //Start of feedback
        ImageView TTS = view.findViewById(R.id.speak);
        TTS.setOnClickListener(this::TexttoSpeechButton);

        //End of feedback

        manish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    progressDialog.dismiss();
                }
            }
        });

        try {
            myAuth = FirebaseAuth.getInstance();
            //databaseReference = FirebaseDatabase.getInstance().getReference().child("User");
            storageReference = FirebaseStorage.getInstance().getReference();
            profileRef = storageReference.child("users/" + myAuth.getCurrentUser().getUid() + "/profile.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadProfilePicture();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        manish.setChecked(false);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        fill();
    }

    private void loadProfilePicture() {
        ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
        File directory = cw.getDir("SomethingRepo", Context.MODE_PRIVATE);
        File file = new File(directory, "UniqueFileName.jpg");
        try {
            profileRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    profileImage.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
                    Toast.makeText(getActivity(),"Profile picture restored from cloud!", Toast.LENGTH_SHORT);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(),"Can't find custom profile picture, loading default profile picture!", Toast.LENGTH_SHORT);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (file.exists()) {
                profileImage.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
            } else {
                profileImage.setImageDrawable(getResources().getDrawable(R.drawable.jotaro));
            }
        }
    }
    public void fill(){

        String currentDate;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        currentDate = sdf.format(cal.getTime());
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(currentDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DATE, 1);
        String nextDate;
        nextDate = sdf.format(c.getTime());

        KalenderFragment kal = new KalenderFragment();
        kal.fillEvents(getView() , currentDate, "today");
        KalenderFragment kal2 = new KalenderFragment();
        kal2.fillEvents(getView(), nextDate, "nextday");

    }
    public void TexttoSpeechButton(View view)
    {
        if(!textToSpeech.isSpeaking()) {
            textToSpeech.speak("This is the Home Page,You can view your profile picture and upcoming events here.     This button will provide hints on all pages,    Double tap to activate voice for navigation", TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else {
            Toast.makeText(getActivity(), "System Busy", Toast.LENGTH_SHORT).show();
        }
    }
}
