package com.fffz.amessenger.sample;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PlayService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
