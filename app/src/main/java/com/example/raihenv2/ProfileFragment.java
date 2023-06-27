package com.example.raihenv2;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE = 100;

    ImageView img;

    private CircleImageView profileImageView;
    private DatabaseReference databaseReference;
    private FirebaseAuth myAuth;
    private Uri imageUri;
    private String myUri = "";
    private StorageTask uploadTask;
    private StorageReference storageReference;
    private StorageReference profileRef;
    private TextToSpeech textToSpeech;

    static TextView sepCounter, profile_uname, profile_email, profile_uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.activity_profile, container, false);
        img = view.findViewById(R.id.profile_image);

        try {
            myAuth = FirebaseAuth.getInstance();
            //databaseReference = FirebaseDatabase.getInstance().getReference().child("User");
            storageReference = FirebaseStorage.getInstance().getReference();
            //profileRef = storageReference.child("users/" + myAuth.getCurrentUser().getUid() + "/profile.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }

        profile_email = view.findViewById(R.id.profile2);
        profile_uname = view.findViewById(R.id.profile_uname);
        profile_uid = view.findViewById(R.id.profile4);

        loadProfilePicture();
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

        sepCounter = view.findViewById(R.id.profileCounter);
        sepCounter.setText(String.valueOf(MainActivity.seppukuCounter));

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        TextView notif = (TextView) view.findViewById(R.id.profile3);
        notif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifSetting();
            }
        });

        return view;
    }

    private void loadProfilePicture() {
        ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
        File directory = cw.getDir("SomethingRepo", Context.MODE_PRIVATE);
        File file = new File(directory, "UniqueFileName.jpg");

        try {
            profileRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    img.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
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
                img.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
            } else {
                img.setImageDrawable(getResources().getDrawable(R.drawable.jotaro));
            }
        }

        try {
            FirebaseDatabase.getInstance().getReference("list").child(FirebaseAuth.getInstance().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    String name = task.getResult().getValue().toString();

                    FirebaseDatabase.getInstance().getReference("users")
                            .child(name)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.getResult().getValue() != null) {
                                        profile_uname.setText(task.getResult().child("uname").getValue().toString());
                                        profile_uid.setText(task.getResult().child("uid").getValue().toString());
                                        profile_email.setText(task.getResult().child("email").getValue().toString());
                                    } else {
                                        Toast.makeText(getContext(), "Data couldn't be found", Toast.LENGTH_SHORT);
                                    }
                                }
                            });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            //img.setImageURI(imageUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getActivity().getContentResolver(), imageUri);

                ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
                File directory = cw.getDir("SomethingRepo", Context.MODE_PRIVATE);
                File file = new File(directory, "UniqueFileName" + ".jpg");
                file.delete();
                if (!file.exists()) {
                    Log.d("path", file.toString());
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.flush();
                        fos.close();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        try {
            StorageReference fileRef = storageReference.child("users/" + myAuth.getCurrentUser().getUid() + "/profile.jpg");
            fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(img);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notifSetting() {
        Log.d("Notif", "click Notif");
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                android.R.style.Theme_Holo_Dialog_MinWidth,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Log.d("Notif", "set setting Notif");
                        myAlarm(hourOfDay, minute);

                    }
                }, 24, 0, true
        );
        timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //timePickerDialog.updateTime(t2h,t2m);
        timePickerDialog.show();
    }

    public void myAlarm(int hr, int min) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hr);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTime().compareTo(new Date()) < 0) {
            Log.d("Notif", calendar.getTime().compareTo(new Date()) + "///" + new Date() + "////" + calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        Log.d("Notif", "////" + calendar.getTime());
        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        intent.putExtra("test", "success or you just suck");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getActivity().getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) this.getContext().getSystemService(Context.ALARM_SERVICE);//getContext().getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            Log.d("Notif", "got and set alarm ");
        } else {
            Log.d("Notif", "got no notif");
        }
    }

    public void TexttoSpeechButton(View view)
    {
        if(!textToSpeech.isSpeaking()) {
            textToSpeech.speak("This is the Profile Page,  You can view your account information and change your profile picture here.     Press on the image on the center-top of the page to change the image,    Notification Settings allow you to choose at what time you get notified for your day to day events", TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else {
            Toast.makeText(getActivity(), "System Busy", Toast.LENGTH_SHORT).show();
        }
    }
}


