package com.file.filecloud.Firebase;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class DataPersistance extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);


    }

}
