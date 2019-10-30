package com.fffz.amessenger;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class HubService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return AMessenger.getInstance().getMessenger().getBinder();
    }

}