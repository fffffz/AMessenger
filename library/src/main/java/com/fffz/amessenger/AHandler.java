package com.fffz.amessenger;

import android.os.Handler;
import android.os.Message;

public class AHandler extends Handler {
    @Override
    public final void dispatchMessage(Message msg) {
        super.dispatchMessage(msg);
    }

    @Override
    public final void handleMessage(Message msg) {
        handleMessage(new AMessage(msg));
    }

    public void handleMessage(AMessage aMessage) {

    }

}