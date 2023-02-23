package com.file.filecloud;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.file.cloud.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;

public class UploadFile extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    int requestCode;
    Intent data;
    String fileUri;
    String uid;
    private ProgressDialog progress;
    private static final int RESULT_CODE_VIDEO = 1;
    private static final int RESULT_CODE_MUSIC = 2;
    private static final int RESULT_CODE_PDF = 3;
    private static final int RESULT_CODE_PPT = 4;
    private static final int RESULT_CODE_DOC = 5;
    private static final int RESULT_CODE_EXCEL = 6;
    private static final int RESULT_CODE_PHOTO = 7;
    private static final int PROFILE_RESULT_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_single_file_dialog);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uid = auth.getUid();

        final TextView fileStatus = findViewById(R.id.fileStatus);
        final TextView fileName = findViewById(R.id.fileName);
        ImageView closeBtn = findViewById(R.id.closeBtn);
        final ProgressBar progress = findViewById(R.id.progress);
        final ImageView ic_done = findViewById(R.id.ic_done);

        Intent intent = getIntent();

        try {
            data = Intent.getIntent(intent.getStringExtra("data"));
            requestCode = Integer.parseInt(intent.getStringExtra("requestCode"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


        fileUri = String.valueOf(data.getData());
        final String filename = getFileName(Uri.parse(fileUri), data);

        fileName.setText(filename);
        fileStatus.setText("Uploading (1) File");

        ic_done.setVisibility(GONE);
        progress.setVisibility(VISIBLE);
        String filePathName = "";

        if (requestCode == RESULT_CODE_MUSIC) {
            filePathName = "MUSIC_SINGLE_FILES/" + filename;
        }
        if (requestCode == RESULT_CODE_PDF) {
            filePathName = "PDF_SINGLE_FILES/" + filename;
        }
        if (requestCode == RESULT_CODE_PPT) {
            filePathName = "PPT_SINGLE_FILES/" + filename;
        }
        if (requestCode == RESULT_CODE_DOC) {
            filePathName = "DOC_SINGLE_FILES/" + filename;
        }
        if (requestCode == RESULT_CODE_EXCEL) {
            filePathName = "EXCEL_SINGLE_FILES/" + filename;
        }


        StorageReference storageReference = FirebaseStorage.getInstance().getReference(uid).child(filePathName);
        storageReference.putFile(Uri.parse(fileUri))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {

                            String timestamp = String.valueOf(System.currentTimeMillis());
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uid", uid);
                            hashMap.put("fileName", filename);
                            hashMap.put("fileUri", downloadUri);
                            hashMap.put("timestamp", timestamp);

                            if (requestCode == RESULT_CODE_MUSIC) {
                                hashMap.put("type", "AUDIO");
                            }
                            if (requestCode == RESULT_CODE_PDF) {
                                hashMap.put("type", "PDF");
                                hashMap.put("fileName", filename);
                            }
                            if (requestCode == RESULT_CODE_PPT) {
                                hashMap.put("type", "PPT");
                            }
                            if (requestCode == RESULT_CODE_DOC) {
                                hashMap.put("type", "DOC");
                            }
                            if (requestCode == RESULT_CODE_EXCEL) {
                                hashMap.put("type", "EXCEL");
                            }

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(uid).child("Files").child(timestamp)
                                    .updateChildren(hashMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isComplete()){
                                                progress.setVisibility(GONE);
                                                ic_done.setVisibility(VISIBLE);
                                                fileStatus.setText("Upload Successful...");
                                                Toast.makeText(UploadFile.this, "" + filename + "Successfully Uploaded", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progress.setVisibility(GONE);
                                            ic_done.setImageResource(R.drawable.fingerprint_dialog_error_to_fp);
                                            Toast.makeText(UploadFile.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();

                                        }
                                    });
                        }


                        Toast.makeText(UploadFile.this, " " + filename + " Successfully Uploaded", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadFile.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void uploadSingleFile(final int requestCode, Intent data) {


    }


    private String getFileName(Uri fileUri, Intent data) {
        String path = new File(data.getData().getPath()).getAbsolutePath();
        String result = null;

        if (path != null) {
            fileUri = data.getData();

            String filename;
            Cursor cursor = getContentResolver().query(fileUri, null, null, null, null);

            if (cursor == null) filename = fileUri.getPath();
            else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                filename = cursor.getString(idx);
                cursor.close();
            }

            result = filename.substring(0, filename.lastIndexOf("."));
        }
        /*String result = null;
        if (fileUri.getScheme().equals("context")) {
            Cursor cursor = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                cursor = getContentResolver().query(fileUri, null, null, null);
            }
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = fileUri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);

            }
        }*/
        return result;
    }
}