package com.fffz.amessenger;

import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;

public class AMessage {

    public final int what;
    public final Bundle data;

    public AMessage(int what) {
        this.what = what;
        data = new Bundle();
    }

    AMessage(Message msg) {
        what = msg.what;
        data = msg.getData();
    }

    public void putString(String key, String value) {
        data.putString(key, value);
    }

    public void putInt(String key, int value) {
        data.putInt(key, value);
    }

    public void putLong(String key, long value) {
        data.putLong(key, value);
    }

    public void putBoolean(String key, boolean value) {
        data.putBoolean(key, value);
    }

    public void putParcelable(String key, Parcelable value) {
        data.putByteArray(key, Util.marshall(value));
    }

    public String getString(String key) {
        return data.getString(key);
    }

    public int getInt(String key) {
        return data.getInt(key);
    }

    public long getLong(String key) {
        return data.getLong(key);
    }

    public boolean getBoolean(String key) {
        return data.getBoolean(key);
    }

    public <T extends Parcelable> T getParcelable(String key, Parcelable.Creator<T> creator) {
        byte[] byteArray = data.getByteArray(key);
        if (byteArray == null) {
            return null;
        }
        return Util.unmarshall(byteArray, creator);
    }

    Message obtainMessage() {
        Message msg = Message.obtain();
        msg.what = what;
        msg.setData(data);
        return msg;
    }

}