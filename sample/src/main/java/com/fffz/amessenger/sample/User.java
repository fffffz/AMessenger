package com.fffz.amessenger.sample;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private long id;
    private String name;

    public User(long id, String name) {
        this.id = id;
        this.name = name;
    }

    protected User(Parcel in) {
        id = in.readLong();
        name = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
    }

    @Override
    public String toString() {
        return "user{id=" + id + ",name=" + name + "}";
    }
}