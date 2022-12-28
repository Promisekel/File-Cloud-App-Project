package com.file.filecloud.Tabs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.file.cloud.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class viewpeerphoto extends AppCompatActivity {

    private ImageView photoIv;
    private Toolbar toolbar;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpeerphoto);

        Intent intent = getIntent();
        uid = intent.getStringExtra("myUid");

        photoIv=findViewById(R.id.photoIv);
        toolbar=findViewById(R.id.toolbar);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String imageUid = "" + ds.child("image").getValue();
                    try {
                        Picasso.get().load(imageUid).placeholder(R.drawable.ic_user_face).into(photoIv);

                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_user_face).into(photoIv);

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}