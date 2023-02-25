package com.file.filecloud.RecievedFiles;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.file.filecloud.Adapter.TabAdapter;
import com.file.filecloud.PinVerification;
import com.file.cloud.R;
import com.google.android.material.tabs.TabLayout;

public class RecievedFiles extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabAdapter adapter;
    private ActionBar actionbar;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recieved_files);


        actionbar = getSupportActionBar();
        actionbar.setTitle("Received Files");


        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tab);


        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragemt(new RecievedDocsFrag(), "DOCUMENTS");
        adapter.addFragemt(new GridPhotoFragment(), "Photo");


        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    protected void onStop() {
        super.onStop();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();
        if (!isScreenOn) {
            startActivity(new Intent(RecievedFiles.this, PinVerification.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();
        if (!isScreenOn) {
            startActivity(new Intent(RecievedFiles.this, PinVerification.class));
        }
    }
}