package com.fffz.amessenger.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.fffz.amessenger.AMessage;
import com.fffz.amessenger.AMessenger;

public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_log_in).setOnClickListener(this);
        findViewById(R.id.btn_log_out).setOnClickListener(this);
        startService(new Intent(this, PlayService.class));
        startService(new Intent(this, DownloadService.class));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_log_in) {
            User user = new User(1, "aaa");
            AMessage aMessage = new AMessage(AMessengerWhat.LOG_IN);
            aMessage.putParcelable("user", user);
            AMessenger.getInstance().sendMessage(aMessage);
        } else if (id == R.id.btn_log_out) {
            AMessenger.getInstance().sendEmptyMessage(AMessengerWhat.LOG_OUT);
        }
    }

}