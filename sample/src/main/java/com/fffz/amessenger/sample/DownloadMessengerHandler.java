package com.fffz.amessenger.sample;

import android.util.Log;

import com.fffz.amessenger.AHandler;
import com.fffz.amessenger.AMessage;

public class DownloadMessengerHandler extends AHandler {
    @Override
    public void handleMessage(AMessage aMessage) {
        if (aMessage.what == AMessengerWhat.LOG_IN) {
            Log.d("AMessenger", "log in " + aMessage.getParcelable("user", User.CREATOR));
        } else if (aMessage.what == AMessengerWhat.LOG_OUT) {
            Log.d("AMessenger", "log out");
        }
    }
}
