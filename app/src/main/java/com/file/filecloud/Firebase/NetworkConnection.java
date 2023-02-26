package com.file.filecloud.Firebase;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class NetworkConnection {
    ////CHECK NETWORK AVAILABILITY
    public static boolean isNetworkAvailable(Context con) {
        try {
            ConnectivityManager cm = (ConnectivityManager) con
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        return false;
    }
}