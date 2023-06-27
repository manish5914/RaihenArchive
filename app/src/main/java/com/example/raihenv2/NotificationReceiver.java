package com.example.raihenv2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Notif", "Got a notif");
        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.createNotification();

    }
}
