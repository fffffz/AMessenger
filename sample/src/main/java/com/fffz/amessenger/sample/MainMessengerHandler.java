package com.fffz.amessenger.sample;

import android.util.Log;

import com.fffz.amessenger.AHandler;
import com.fffz.amessenger.AMessage;

public class MainMessengerHandler extends AHandler {
    @Override
    public void handleMessage(AMessage aMessage) {
        if (aMessage.what == AMessengerWhat.SHOW_MESSAGE) {
            Log.d("AMessenger", "show message");
        }
    }
}
