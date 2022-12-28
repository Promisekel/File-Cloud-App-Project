package com.file.filecloud.Tabs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.file.cloud.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class viewprofilephoto extends AppCompatActivity {

    private ImageView photoIv;
    ImageButton binDp;
    private Toolbar toolbar;
    FirebaseAuth auth;
    FirebaseUser user;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R .layout.viewprofilephoto);

        auth=FirebaseAuth.getInstance();
        uid=auth.getUid();
        user=auth.getCurrentUser();

        photoIv=findViewById(R.id.photoIv);
        toolbar=findViewById(R.id.toolbar);;
        binDp=findViewById(R.id.binDp);


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String imageUid = "" + ds.child("image").getValue();
                    if (imageUid !=null){
                        try {
                            Picasso.get().load(imageUid).into(photoIv);
                            binDp.setVisibility(View.VISIBLE);

                        } catch (Exception e) {
                            Picasso.get().load(R.drawable.ic_user_face).into(photoIv);
                        }
                    }else{
                        binDp.setVisibility(View.VISIBLE);
                        Picasso.get().load(R.drawable.ic_user_face).into(photoIv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(viewprofilephoto.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage("Are you sure you want to Remove your profile picture?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                beginDelete();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

    }

    private void beginDelete() {
        final ProgressDialog progressDialog = new ProgressDialog(viewprofilephoto.this);
        progressDialog.setMessage("Removing Profile Picture...");
        progressDialog.show();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("image", "");
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.child(user.getUid()).updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {

                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(viewprofilephoto.this, "Moved Successfully...", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(viewprofilephoto.this, "Unsuccessfully try again", Toast.LENGTH_LONG).show();

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}