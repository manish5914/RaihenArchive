package com.example.raihenv2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class NotificationHelper {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public String currentDate;
    private String currentUserUid = mAuth.getCurrentUser().getUid();
        private Context mContext;
        private static final String NOTIFICATION_CHANNEL_ID = "10001";

        NotificationHelper(Context context) {
            mContext = context;
        }

        void createNotification()
        {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            currentDate = sdf.format(cal.getTime());


            Intent intent = new Intent(mContext , MainActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext,
                    0 /* Request code */, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);


         /*   NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setContentTitle("Title")
                    .setContentText("Content")
                    .setAutoCancel(false)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setContentIntent(resultPendingIntent);
            */
            Intent intentN = new Intent(mContext, MainActivity.class);
            intentN.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intentN, 0);
            try{
                FirebaseDatabase.getInstance().getReference("Events").child(currentUserUid).child("stats").keepSynced(true);
                FirebaseDatabase.getInstance().getReference("Events").child(currentUserUid).child("stats").child(currentDate).child("numberEvents").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        Notification.Builder mbuilder = new Notification.Builder(mContext, "raihen");
                        mbuilder.setSmallIcon(R.drawable.raihen_logo);
                        mbuilder.setContentTitle("Events for Tomorrow");
                        Long num = Long.valueOf(0);
                        if(task.getResult().getValue() != null)
                        {
                            num = (long)task.getResult().getValue();
                        }
                        mbuilder.setContentText("There are " + num +" events tomorrow.");
                        mbuilder.setPriority(Notification.PRIORITY_DEFAULT);
                        mbuilder.setContentIntent(pendingIntent);
                        mbuilder.setAutoCancel(true);
                        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                        {
                            int importance = NotificationManager.IMPORTANCE_HIGH;
                            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "raihen", importance);
                            assert mNotificationManager != null;
                            mbuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                            mNotificationManager.createNotificationChannel(notificationChannel);
                        }
                        assert mNotificationManager != null;
                        mNotificationManager.notify(0 /* Request Code */, mbuilder.build());
                    }
                });

            }
            catch (Exception e)
            {
                Log.d("Notif", e+"");
            }
        }

}
