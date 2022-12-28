package com.file.filecloud.Tabs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.file.cloud.R;
import com.squareup.picasso.Picasso;

public class ViewProfile extends AppCompatActivity {

    private ImageView photoIv;
    private Toolbar toolbar;
    private String uid, timestamp, fileName,imageUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_profile);

        photoIv=findViewById(R.id.photoIv);
        toolbar=findViewById(R.id.toolbar);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uuid");
        imageUid = intent.getStringExtra("imageUid");
        timestamp = intent.getStringExtra("timestamp");
        fileName = intent.getStringExtra("fileName");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        try {
            Picasso.get().load(imageUid).into(photoIv);

        }catch (Exception e){
            photoIv.setImageResource(R.drawable.ic_image_icon);
        }




    }
}