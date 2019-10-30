package com.fffz.amessenger.sample;

import android.app.Application;

import com.fffz.amessenger.AMessenger;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AMessenger.getInstance().init(this);
        if (Util.isMainProcess(this)) {
            AMessenger.getInstance().register(new MainMessengerHandler(), AMessengerWhat.SHOW_MESSAGE);
        } else if (Util.isPlayerProcess(this)) {
            AMessenger.getInstance().register(new PlayerMessengerHandler(), AMessengerWhat.LOG_IN, AMessengerWhat.LOG_OUT);
        } else if (Util.isDownloadProcess(this)) {
            AMessenger.getInstance().register(new DownloadMessengerHandler(), AMessengerWhat.LOG_IN, AMessengerWhat.LOG_OUT);
        }
    }
}