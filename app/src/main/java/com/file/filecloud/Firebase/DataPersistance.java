package com.file.filecloud.Firebase;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class DataPersistance extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        ////CREATE CACHE DATA
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

}
