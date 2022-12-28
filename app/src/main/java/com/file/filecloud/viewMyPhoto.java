package com.file.filecloud;




import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.file.filecloud.Tabs.shareListActivity;
import com.file.cloud.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class viewMyPhoto extends AppCompatActivity {

    private ImageView photoIv;
    private Toolbar toolbar;
    private String uid, timestamp, fileName,photo, PHOTO;
    private TextView downloadTv, shareTv, deleteTv;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_photo);


        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();

        photoIv=findViewById(R.id.photoIv);
        toolbar=findViewById(R.id.toolbar);
        deleteTv=findViewById(R.id.deleteTv);
        downloadTv=findViewById(R.id.downloadTv);
        shareTv=findViewById(R.id.shareTv);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        Intent intent = getIntent();
        uid = intent.getStringExtra("myUid");
        timestamp = intent.getStringExtra("timestamp");
        fileName = intent.getStringExtra("fileName");
        photo = intent.getStringExtra("fileUri");
        PHOTO = intent.getStringExtra("type");

        try {
            Picasso.get().load(photo).into(photoIv);

        }catch (Exception e){
            photoIv.setImageResource(R.drawable.ic_image_icon);
        }

        downloadTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(DOWNLOAD_SERVICE);
                    Uri uri = Uri.parse(photo);
                    DownloadManager.Request request = new DownloadManager.Request(uri);

                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS,"/File Cloud/File Cloud Images/"+"Fc_"+timestamp+".jpg");
                    downloadManager.enqueue(request);

                    Toast toast = Toast.makeText(viewMyPhoto.this,"Downloading to Documents/File Cloud/",Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();

            }
        });

        deleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(viewMyPhoto.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Send to trash?")
                        .setMessage("Are you sure you want to move this file to trash?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                    final ProgressDialog progress = new ProgressDialog(viewMyPhoto.this);
                                    progress.setMessage("Deleting...");
                                    progress.setCancelable(false);
                                    progress.setCanceledOnTouchOutside(false);
                                    progress.show();
                                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("Files");
                                    Query query = dbRef.orderByChild("timestamp").equalTo(timestamp);
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                ds.getRef().removeValue();
                                                String Timestamp = String.valueOf(System.currentTimeMillis());

                                                Long time = System.currentTimeMillis();  //get time in millis
                                                long cutoffDeleteTime = time+TimeUnit.MILLISECONDS.convert(30,TimeUnit.DAYS);///calculates next auto delete time in minutes
                                                String cutoffDeleteTimer = Long.toString(cutoffDeleteTime);

                                                HashMap<String, Object> hashMap = new HashMap<>();
                                                hashMap.put("uid", uid);
                                                hashMap.put("fileName", timestamp+".jpg");
                                                hashMap.put("fileUri", photo);
                                                hashMap.put("timestamp", timestamp);
                                                hashMap.put("deleteTimestamp", Timestamp);
                                                hashMap.put("type", "PHOTO");
                                                hashMap.put("cutoffTimer", cutoffDeleteTimer);
                                                hashMap.put("deleteTypeReceived", "No");

                                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                                ref.child(user.getUid()).child("Trash").child(Timestamp)
                                                        .updateChildren(hashMap)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                progress.dismiss();
                                                                Toast.makeText(viewMyPhoto.this, "Moved Successfully...", Toast.LENGTH_LONG).show();
                                                                viewMyPhoto.this.finish();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(viewMyPhoto.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });


                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });


                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        shareTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(viewMyPhoto.this, shareListActivity.class);
                intent.putExtra("myUid", uid);
                intent.putExtra("timestamp", timestamp);
                intent.putExtra("fileName", fileName);
                intent.putExtra("fileUri", photo);
                intent.putExtra("type", PHOTO);
                startActivity(intent);

            }
        });
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return super.onNavigateUp();
    }
}