package com.dosoft.livingflamealert;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class LFAlertApp extends Application {
    private UiUpdateListener uiUpdateListener;
    private MainActivity mainActivity;

    public UiUpdateListener getUiUpdateListener() {
        return uiUpdateListener;
    }

    public void setUiUpdateListener(UiUpdateListener uiUpdateListener) {
        this.uiUpdateListener = uiUpdateListener;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupPeriodicCall();
    }

    private void setupPeriodicCall() {
        // Replace the following with the desired interval in milliseconds
        long intervalMillis = Math.round(Math.random() * 15 * 1000); // 15 seconds

        Intent intent = new Intent(this, RestCallReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalMillis, pendingIntent);
    }
}