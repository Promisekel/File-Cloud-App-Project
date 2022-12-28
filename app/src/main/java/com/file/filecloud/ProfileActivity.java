package com.file.filecloud;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.file.filecloud.Tabs.viewprofilephoto;
import com.file.cloud.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    FirebaseUser user;
    ImageView avatarIv;
    String uid;
    ProgressDialog progress;
    String profilePhoto;
    private static final int PROFILE_RESULT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        uid = auth.getUid();
        user = auth.getCurrentUser();

        final TextView userMailTv = findViewById(R.id.userMailTv);
        final TextView userPhoneTv = findViewById(R.id.userPhoneTv);
        final TextView userNameTv = findViewById(R.id.userNameTv);
        final FloatingActionButton editBtn = findViewById(R.id.editBtn);
        avatarIv = findViewById(R.id.avatarIv);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .9), (int) (height * .7));

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String fullName = "" + ds.child("fullName").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String email = "" + ds.child("email").getValue();
                    String image = "" + ds.child("image").getValue();

                    userNameTv.setText(fullName);
                    userPhoneTv.setText(phone);
                    userMailTv.setText(email);
                    try {
                        Picasso.get().load(image).placeholder(R.drawable.ic_user_face).into(avatarIv);

                    } catch (Exception e) {

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEdithProfileDialog(editBtn);
            }
        });
        avatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, viewprofilephoto.class));
            }
        });
        progress = new ProgressDialog(this);
    }

    /*private boolean checkIfUsernameExits(DataSnapshot snapshot, String value) {
        UserModel user = new UserModel();
        for (DataSnapshot ds : snapshot.getChildren()) {
            user.setNicki(ds.getValue(UserModel.class).getNicki());
            if (user.equals(value)) {
                Toast.makeText(this, "" + user, Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        Toast.makeText(this, "no much", Toast.LENGTH_SHORT).show();
        return false;

    }*/

    private void showEdithProfileDialog(FloatingActionButton editBtn) {
        String[] options = {"Edit Name", "Edit Phone", "Edit Profile Photo"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog
                .Builder(this);
        builder.setTitle("Choose Action");
        builder.setItems(options,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
        if (which == 0)
        {
                    ////edit name clicked
                    progress.setMessage("Updating Name");
                    showNameUpdateDialog();
                }
        else if (which == 1)
        {
                    ////edit name clicked
                    progress.setMessage("Updating Phone");
                    showNamePhoneUpdateDialog("phone");
                }
        else if (which == 2)
        {
                    ////edit Profile clicked
                    progress.setMessage("Updating Profile Picture");
                    profilePhoto = "image";
                    pickCoverFromGallery();
                }
            }
        });builder.create().show();
    }

    @SuppressLint("SetTextI18n")
    private void showNameUpdateDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.register_pin_dialog, null);
        final Button setpinBtn = view.findViewById(R.id.setpinBtn);
        final EditText firstName = view.findViewById(R.id.pinEt);
        final EditText surName = view.findViewById(R.id.setpassEt);
        final TextView dialogTitle = view.findViewById(R.id.dialogTitle);

        dialogTitle.setText("Update current name");
        firstName.setHint("Enter your firstName");
        surName.setHint("Enter your SurName");
        setpinBtn.setText("Done");
        firstName.setInputType(InputType.TYPE_CLASS_TEXT);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(view);

        final android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();

        setpinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fname = firstName.getText().toString().trim();
                final String sname = surName.getText().toString().trim();
                if (!TextUtils.isEmpty(fname)) {
                        if (!TextUtils.isEmpty(sname)){
                            updateUserName(fname, sname,alertDialog);
                        }else {
                            surName.setHint("Provide your surname");
                            surName.setHintTextColor(Color.parseColor("#880808"));
                        }

                } else {
                    firstName.setHint("Provide your first name");
                    firstName.setHintTextColor(Color.parseColor("#880808"));
                }
            }
        });
    }

    private void updateUserName(final String fname, final String sname, final android.app.AlertDialog alertDialog) {
        final ProgressDialog progress = new ProgressDialog(ProfileActivity.this);
        progress.setMessage("updating name please wait...");
        progress.show();
        progress.setCanceledOnTouchOutside(false);
        progress.setCancelable(false);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("fullName").setValue(fname + " " + sname).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    final HashMap<String, Object> name = new HashMap<>();
                    name.put("firstName", fname);
                    name.put("surName", sname);
                    final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.child(user.getUid()).updateChildren(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progress.dismiss();
                                alertDialog.dismiss();
                                Toast.makeText(ProfileActivity.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progress.dismiss();
                            alertDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                alertDialog.dismiss();
                progress.dismiss();
                Toast.makeText(ProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void pickCoverFromGallery() {
        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Please Select Multiple Files"), PROFILE_RESULT_CODE);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void showNamePhoneUpdateDialog(final String key) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Update " + key);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);

        final EditText editText = new EditText(this);
        editText.setHint("Enter " + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                final String value = editText.getText().toString().trim();

                if (!TextUtils.isEmpty(value)) {
                        final ProgressDialog progress = new ProgressDialog(ProfileActivity.this);
                        progress.show();
                        progress.setCanceledOnTouchOutside(false);
                        progress.setCancelable(false);
                        uploadKey(key, value, progress, dialog);

                } else {
                    Toast.makeText(ProfileActivity.this, "Please Enter " + key, Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();


    }

    private void uploadKey(final String key, String value, final ProgressDialog progress, final DialogInterface dialog) {
        final HashMap<String, Object> result = new HashMap<>();
        result.put(key, value);
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).updateChildren(result)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            progress.dismiss();
                            Toast.makeText(ProfileActivity.this, key + " Updated Successfully", Toast.LENGTH_SHORT).show();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progress.dismiss();
                Toast.makeText(ProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PROFILE_RESULT_CODE && resultCode == RESULT_OK && data != null) {
                Uri ImageUri = data.getData();

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            }


            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK) {
                        progress.setMessage("Updating Profile...");
                        progress.setCanceledOnTouchOutside(false);
                        progress.setCanceledOnTouchOutside(false);
                        progress.show();

                        Uri resultUri = result.getUri();

                        uploadProfilePhoto(resultUri);

                }


            }
        }
    }

    private void uploadProfilePhoto(Uri resultUri) {
        String filePathName = "PROFILE_IMAGES/";
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(uid).child(filePathName);
        storageReference.putFile(resultUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            final HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("image", downloadUri);
                            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(uid).updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progress.dismiss();
                                            Toast.makeText(ProfileActivity.this, "Profile Updated successfully", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progress.dismiss();
                                            Toast.makeText(ProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();

                                        }
                                    });

                        } else {
                            progress.dismiss();
                            Toast.makeText(ProfileActivity.this, "Error Occured", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progress.show();
                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();
        if (!isScreenOn) {
            startActivity(new Intent(ProfileActivity.this, PinVerification.class));
        }
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        finish();
    }



}

