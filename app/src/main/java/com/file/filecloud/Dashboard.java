package com.file.filecloud;

import android.Manifest;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.viewpager.widget.ViewPager;

import com.anstrontechnologies.corehelper.AnstronCoreHelper;
import com.file.filecloud.Adapter.TabAdapter;
import com.file.filecloud.Adapter.UploadFileAdapter;
import com.file.filecloud.RecievedFiles.RecievedFiles;
import com.file.filecloud.Tabs.DOC_Fragment;
import com.file.filecloud.Tabs.Excel_Fragment;
import com.file.filecloud.Tabs.PDF_Fragment;
import com.file.filecloud.Tabs.PPT_Fragment;
import com.file.filecloud.Tabs.Photo_Fragment;
import com.file.cloud.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

import static androidx.recyclerview.widget.RecyclerView.GONE;
import static androidx.recyclerview.widget.RecyclerView.OnClickListener;
import static androidx.recyclerview.widget.RecyclerView.VISIBLE;

public class Dashboard extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabAdapter adapter;
    private ImageButton menuBtn;
    private ActionBar actionbar;
    private TextView userTv;
    private String uid, name;
    private ImageView profileIv;
    private Uri fileUri;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;


    private ProgressDialog progress;
    private static final int RESULT_CODE_VIDEO = 1;
    private static final int RESULT_CODE_MUSIC = 2;
    private static final int RESULT_CODE_PDF = 3;
    private static final int RESULT_CODE_PPT = 4;
    private static final int RESULT_CODE_DOC = 5;
    private static final int RESULT_CODE_EXCEL = 6;
    private static final int RESULT_CODE_PHOTO = 7;
    private static final int PROFILE_RESULT_CODE = 100;
    private List<String> filenameList, status, icon;
    private UploadFileAdapter uploadFileAdapter;
    private AnstronCoreHelper coreHelper;
    private ArrayList<Uri> ImageUris;
    int position = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uid = auth.getUid();



        actionbar = getSupportActionBar();
        actionbar.setTitle("File Cloud");

        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tab);
        menuBtn = findViewById(R.id.menuBtn);
        userTv = findViewById(R.id.userTv);
        profileIv = findViewById(R.id.profileIv);


        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragemt(new Photo_Fragment(), "Photo");
        adapter.addFragemt(new PDF_Fragment(), "PDF");
        adapter.addFragemt(new DOC_Fragment(), "DOC");
        adapter.addFragemt(new Excel_Fragment(), "EXCEL");
        adapter.addFragemt(new PPT_Fragment(), "PPT");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);


        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {

                    String image = "" + ds.child("image").getValue();
                    String firstName = "" + ds.child("firstName").getValue();

                    if (firstName.isEmpty()) {
                        userTv.setText("Me");

                    } else {
                        userTv.setText(firstName);
                    }

                    try {
                        Picasso.get().load(image).placeholder(R.drawable.ic_user_face).into(profileIv);

                    } catch (Exception e) {

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        progress = new ProgressDialog(this);
        filenameList = new ArrayList<>();
        ImageUris = new ArrayList<>();
        status = new ArrayList<>();
        uploadFileAdapter = new UploadFileAdapter(filenameList, status);
        coreHelper = new AnstronCoreHelper(this);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenMenu(menuBtn);
            }
        });

        profileIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard.this, ProfileActivity.class));
            }
        });
    }


    private void OpenMenu(ImageButton menuBtn) {

        final PopupMenu popupMenu = new PopupMenu(Dashboard.this, menuBtn, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE, 0, 1, "View Profile");
        popupMenu.getMenu().add(Menu.NONE, 1, 2, "Trash");
        popupMenu.getMenu().add(Menu.NONE, 2, 3, "View Peers");
        popupMenu.getMenu().add(Menu.NONE, 3, 4, "Received Files");


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    startActivity(new Intent(Dashboard.this, ProfileActivity.class));

                }
                if (id == 1) {
                    startActivity(new Intent(Dashboard.this, UserTrash.class));
                }
                if (id == 2) {
                    startActivity(new Intent(Dashboard.this, PeersActivity.class));

                }
                if (id == 3) {
                    startActivity(new Intent(Dashboard.this, RecievedFiles.class));

                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        finish();
    }

    /*private void checkUserStatus() {

        if (user != null && user.isEmailVerified()) {
            startActivity(new Intent(Dashboard.this, PinVerification.class));
            finish();

        } else {
            startActivity(new Intent(Dashboard.this, LoginActivity.class));
            finish();
        }

    }*/

    @Override
    protected void onStop() {
        super.onStop();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();
        if (!isScreenOn) {
            startActivity(new Intent(Dashboard.this, PinVerification.class));
        }
    }

    public void choosePhoto(final View view) {
        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        final PopupMenu popupMenu = new PopupMenu(Dashboard.this, view, Gravity.END);
                        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Select a single file");
                        ///popupMenu.getMenu().add(Menu.NONE, 1, 0, "Select multiple files");

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                int id = item.getItemId();
                                if (id == 0) {
                                    Intent intent = new Intent();
                                    intent.setType("image/*");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Please Select a single image"), RESULT_CODE_PHOTO);
                                }
                                /*if (id == 1) {
                                    Intent intent = new Intent();
                                    intent.setType("image/*");
                                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Please Select Multiple images"), RESULT_CODE_PHOTO);
                                }*/
                                return false;
                            }
                        });
                        popupMenu.show();

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

    public void chooseExcel(final View view) {
        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse)
                    {
                        final PopupMenu popupMenu = new PopupMenu(Dashboard.this, view, Gravity.END);
                        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Select a single file");
                        ///popupMenu.getMenu().add(Menu.NONE, 1, 0, "Select multiple files");

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                int id = item.getItemId();
                                if (id == 0) {
                                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                                    intent.setType("*/*");
                                    String[] mimetypes = {"application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
                                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                                    startActivityForResult(Intent.createChooser(intent, "Please Select a single File"), RESULT_CODE_EXCEL);
                                }
                                if (id == 1) {
                                    Intent intent = new Intent();
                                    intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Please Select Multiple Files"), RESULT_CODE_EXCEL);
                                }
                                return false;
                            }
                        });
                        popupMenu.show();

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

    public void chooseDoc(final View view) {
        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        final PopupMenu popupMenu = new PopupMenu(Dashboard.this, view, Gravity.END);
                        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Select a single file");
                        //popupMenu.getMenu().add(Menu.NONE, 1, 0, "Select multiple files");

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                int id = item.getItemId();
                                if (id == 0) {
                                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                                    intent.setType("*/*");
                                    String[] mimetypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword"};
                                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                                    startActivityForResult(Intent.createChooser(intent, "Please Select a single File"), RESULT_CODE_DOC);
                                }
                                if (id == 1) {
                                    Intent intent = new Intent();
                                    intent.setType("application/msword");
                                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Please Select Multiple Files"), RESULT_CODE_DOC);
                                }
                                return false;
                            }
                        });
                        popupMenu.show();

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

    public void choosePPT(final View view) {
        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        final PopupMenu popupMenu = new PopupMenu(Dashboard.this, view, Gravity.END);
                        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Select a single file");
                        //popupMenu.getMenu().add(Menu.NONE, 1, 0, "Select multiple files");

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                int id = item.getItemId();
                                if (id == 0) {
                                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                                    intent.setType("*/*");
                                    String[] mimetypes = {"application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation"};
                                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                                    startActivityForResult(Intent.createChooser(intent, "Please Select a single File"), RESULT_CODE_PPT);
                                }
                                if (id == 1) {
                                    Intent intent = new Intent();
                                    intent.setType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
                                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Please Select Multiple Files"), RESULT_CODE_PPT);
                                }
                                return false;
                            }
                        });
                        popupMenu.show();

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

    public void choosePDF(final View view) {
        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {

                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        final PopupMenu popupMenu = new PopupMenu(Dashboard.this, view, Gravity.END);
                        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Select a single file");
                        //popupMenu.getMenu().add(Menu.NONE, 1, 0, "Select multiple files");

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                int id = item.getItemId();
                                if (id == 0) {
                                    Intent intent = new Intent();
                                    intent.setType("application/pdf");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Please Select a single File"), RESULT_CODE_PDF);
                                }
                                if (id == 1) {
                                    Intent intent = new Intent();
                                    intent.setType("application/pdf");
                                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Please Select Multiple Files"), RESULT_CODE_PDF);
                                }
                                return false;
                            }
                        });
                        popupMenu.show();

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

    /*public void chooseMusic(final View view) {
        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        final PopupMenu popupMenu = new PopupMenu(Dashboard.this, view, Gravity.END);
                        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Select a single file");
                        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Select multiple files");


                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                int id = item.getItemId();
                                if (id == 0) {
                                    Intent intent = new Intent();
                                    intent.setType("audio/*");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Please Select Multiple Files"), RESULT_CODE_MUSIC);
                                }
                                if (id == 1) {
                                    Intent intent = new Intent();
                                    intent.setType("audio/*");
                                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Please Select Multiple Files"), RESULT_CODE_MUSIC);
                                }
                                return false;
                            }
                        });
                        popupMenu.show();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

    }*/

    /*public void chooseVideo(final View view) {
        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                        final PopupMenu popupMenu = new PopupMenu(Dashboard.this, view, Gravity.END);
                        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Select a single file");
                        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Select multiple files");


                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                int id = item.getItemId();
                                if (id == 0) {
                                    Intent intent = new Intent();
                                    intent.setType("video/*");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Please Select a single File"), RESULT_CODE_VIDEO);
                                }
                                if (id == 1) {
                                    Intent intent = new Intent();
                                    intent.setType("video/*");
                                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Please Select Multiple Files"), RESULT_CODE_VIDEO);
                                }
                                return false;
                            }
                        });
                        popupMenu.show();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_CODE_MUSIC && resultCode == RESULT_OK) {

            if (data.getClipData() != null) {
                //uploadMultipleFiles(requestCode, data);

            } else if (data.getData() != null) {
                uploadSingleFile(requestCode, data);
            }

        } else if (requestCode == RESULT_CODE_PDF && resultCode == RESULT_OK) {

            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                if (clipData.getItemCount() > 5) {
                    Toast.makeText(this, "You can't select more than 5 documents at a time", Toast.LENGTH_LONG).show();
                } else {
                   // uploadMultipleFiles(requestCode, data);
                }


            } else if (data.getData() != null) {
                uploadSingleFile(requestCode, data);

            }

        } else if (requestCode == RESULT_CODE_PPT && resultCode == RESULT_OK) {

            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                if (clipData.getItemCount() > 5) {
                    Toast.makeText(this, "You can't select more than 5 documents at a time", Toast.LENGTH_LONG).show();
                } else {
                    //uploadMultipleFiles(requestCode, data);
                }
            } else if (data.getData() != null) {
                uploadSingleFile(requestCode, data);

            }

        }
        if (requestCode == RESULT_CODE_DOC && resultCode == RESULT_OK) {

            if (data.getClipData() != null) {
               /// uploadMultipleFiles(requestCode, data);

            } else if (data.getData() != null) {
                uploadSingleFile(requestCode, data);

            }

        }
        if (requestCode == RESULT_CODE_EXCEL && resultCode == RESULT_OK) {

            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                if (clipData.getItemCount() > 5) {
                    Toast.makeText(this, "You cant select more than 5 documents at a time", Toast.LENGTH_LONG).show();
                } else {
                   /// uploadMultipleFiles(requestCode, data);
                }

            } else if (data.getData() != null) {
                uploadSingleFile(requestCode, data);

            }

        }
        if (requestCode == PROFILE_RESULT_CODE && resultCode == RESULT_OK) {

            if (data.getData() != null) {
                uploadSingleFile(requestCode, data);

            }

        }
        if (requestCode == RESULT_CODE_PHOTO && resultCode == RESULT_OK) {

            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                if (clipData.getItemCount() > 5) {
                    Toast.makeText(this, "You can't upload more than 5 images at a time", Toast.LENGTH_LONG).show();
                } else {
                    ///openUploadMultiplePhotoDialog(data, requestCode);
                }
            } else if (data.getData() != null) {
                Uri image_uri = data.getData();
                displayImagePick(image_uri, requestCode, data);
            }

        }
    }

    private void displayImagePick(final Uri image_uri, int requestCode, Intent data) {
        final String filename = getFileName(image_uri, data);

        View view = LayoutInflater.from(this).inflate(R.layout.image_pick_layout, null);
        final ImageView pImageIv = view.findViewById(R.id.pImageIv);
        FloatingActionButton fab = view.findViewById(R.id.fab);
        final Button sendloadBtn = view.findViewById(R.id.sendloadBtn);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        pImageIv.setImageURI(image_uri);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Please Select a single image"), RESULT_CODE_PHOTO);
            }
        });

        sendloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendloadBtn.setEnabled(false);
                if (pImageIv != null) {
                    sendloadBtn.setEnabled(false);
                    openUploadSinglePhoto(image_uri, dialog, filename, pImageIv);
                } else {
                    Toast.makeText(Dashboard.this, "Image Box is Empty...", Toast.LENGTH_LONG).show();
                }


            }
        });
    }

    private void openUploadSinglePhoto(Uri image_uri, final AlertDialog dialog, final String filename, ImageView pImageIv) {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Uploading...");
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        progress.show();


        String timestamp = String.valueOf(System.currentTimeMillis());

        String filePathName = "PHOTO_SINGLE_FILES/" + timestamp;
        Bitmap bitmap = ((BitmapDrawable) pImageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();


        StorageReference storageReference = FirebaseStorage.getInstance().getReference(uid).child(filePathName);
        storageReference.putBytes(data)
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
                            hashMap.put("type", "PHOTO");

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(uid).child("Files").child(timestamp)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progress.dismiss();
                                            dialog.dismiss();
                                            Toast.makeText(Dashboard.this, "Successfully Uploaded...", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progress.dismiss();
                                            Toast.makeText(Dashboard.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();

                                        }
                                    });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Dashboard.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

   /* private void openUploadMultiplePhotoDialog(Intent data, int requestCode) {
        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
            fileUri = data.getClipData().getItemAt(i).getUri();
            ImageUris.add(fileUri);
            position = 0;

        }
        final String filename = getFileName(fileUri, data);

        View view = LayoutInflater.from(Dashboard.this).inflate(R.layout.row_multi_image_picker_dialog, null);
        final ImageSwitcher imageSwitcher = view.findViewById(R.id.imageSwitcher);
        final Button previousBtn = view.findViewById(R.id.previousBtn);
        Button nextBtn = view.findViewById(R.id.nextBtn);
        Button sendBtn = view.findViewById(R.id.sendBtn);
        FloatingActionButton fab = view.findViewById(R.id.fab);

        if (position == 0) {
            previousBtn.setVisibility(GONE);
        } else {
            previousBtn.setVisibility(VISIBLE);
        }

        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                PhotoView imageView = new PhotoView(getApplicationContext());
                return imageView;
            }
        });


        imageSwitcher.setImageURI(ImageUris.get(0));


        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Dashboard.this);
        builder.setView(view);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();
        dialog.show();

        previousBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position > 0) {
                    position--;
                    imageSwitcher.setImageURI(ImageUris.get(position));
                } else {
                    Toast.makeText(Dashboard.this, "No Previous Images", Toast.LENGTH_LONG).show();
                }
            }
        });

        nextBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position < ImageUris.size() - 1) {
                    position++;
                    imageSwitcher.setImageURI(ImageUris.get(position));
                    previousBtn.setVisibility(VISIBLE);

                } else {
                    Toast.makeText(Dashboard.this, "No more Images", Toast.LENGTH_SHORT).show();
                }

            }
        });

        sendBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageSwitcher != null) {
                    UploadMultiplePhoto(position, ImageUris, fileUri, dialog, filename);
                } else {
                    Toast.makeText(Dashboard.this, "Image Box is Empty...", Toast.LENGTH_LONG).show();
                }


            }
        });

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Please Select Multiple images"), RESULT_CODE_PHOTO);
            }
        });


    }*/

    /*private void UploadMultiplePhoto(int position, ArrayList<Uri> imageUris, Uri fileUris, final AlertDialog dialog, final String filename) {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Uploading...");
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        progress.show();


        String timestamp = String.valueOf(System.currentTimeMillis());

        String filePathName = "PHOTO_MULTI_FILES/" + timestamp;


        for (position = 0; position < imageUris.size(); position++) {
            Uri IndividualImage = imageUris.get(position);
            File file = new File(SiliCompressor.with(this)
                    .compress(FileUtils.getPath(this, IndividualImage), new File(this.getCacheDir(), "temp")));
            Uri uri = Uri.fromFile(file);


            StorageReference storageReference = FirebaseStorage.getInstance().getReference(uid).child(filePathName);
            storageReference.child(coreHelper.getFileNameFromUri(uri)).putFile(uri)
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
                                hashMap.put("type", "PHOTO");

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(uid).child("Files").child(timestamp)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progress.dismiss();
                                                dialog.dismiss();
                                                Toast.makeText(Dashboard.this, "Successfully Uploaded...", Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progress.dismiss();
                                                Toast.makeText(Dashboard.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();

                                            }
                                        });
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Dashboard.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


    }*/

    private void uploadSingleFile(final int requestCode, Intent data) {

        fileUri = data.getData();
        final String filename = getFileName(fileUri, data);


        View view = LayoutInflater.from(Dashboard.this).inflate(R.layout.upload_single_file_dialog, null);
        final TextView fileStatus = view.findViewById(R.id.fileStatus);
        final TextView fileName = view.findViewById(R.id.fileName);
        ImageView closeBtn = view.findViewById(R.id.closeBtn);
        final ProgressBar progress = view.findViewById(R.id.progress);
        final ImageView ic_done = view.findViewById(R.id.ic_done);

        ic_done.setVisibility(GONE);
        progress.setVisibility(VISIBLE);

        fileName.setText(filename);
        fileStatus.setText("Uploading (1) File");

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Dashboard.this);
        builder.setView(view);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();
        dialog.show();

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
        storageReference.putFile(fileUri)
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
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progress.setVisibility(GONE);
                                            ic_done.setVisibility(VISIBLE);
                                            fileStatus.setText("Upload Successful...");
                                            Toast.makeText(Dashboard.this, "" + filename + "Successfully Uploaded", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progress.setVisibility(GONE);
                                            ic_done.setImageResource(R.drawable.fingerprint_dialog_error_to_fp);
                                            Toast.makeText(Dashboard.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();

                                        }
                                    });
                        }


                        Toast.makeText(Dashboard.this, " " + filename + " Successfully Uploaded", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Dashboard.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    /*private void uploadMultipleFiles(final int requestCode, Intent data) {
        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
            fileUri = data.getClipData().getItemAt(i).getUri();
            final String filename = getFileName(fileUri, data);


            View view = LayoutInflater.from(Dashboard.this).inflate(R.layout.upload_file_dialog, null);
            final RecyclerView uploadFileRv = view.findViewById(R.id.uploadFileRv);
            final TextView fileNumber = view.findViewById(R.id.fileNumber);
            ImageView closeBtn = view.findViewById(R.id.closeBtn);
            uploadFileRv.setAdapter(uploadFileAdapter);
            filenameList.add(filename);
            status.add("loading");
            uploadFileAdapter.notifyDataSetChanged();

            uploadFileRv.setHasFixedSize(true);
            uploadFileRv.setAdapter(uploadFileAdapter);
            fileNumber.setText("Uploading(" + filenameList.size() + ") Files");

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Dashboard.this);
            builder.setView(view);
            builder.setCancelable(false);

            final AlertDialog dialog = builder.create();
            dialog.show();


            String filePathName = "";

            if (requestCode == RESULT_CODE_MUSIC) {
                filePathName = "MUSIC_MULTIPLE_FILES/" + filename;
            }
            if (requestCode == RESULT_CODE_PDF) {
                filePathName = "PDF_MULTIPLE_FILES/" + filename;
            }
            if (requestCode == RESULT_CODE_PPT) {
                filePathName = "PPT_MULTIPLE_FILES/" + filename;
            }
            if (requestCode == RESULT_CODE_DOC) {
                filePathName = "DOC_MULTIPLE_FILES/" + filename;
            }
            if (requestCode == RESULT_CODE_EXCEL) {
                filePathName = "EXCEL_MULTIPLE_FILES/" + filename;
            }


            final int index = i;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(uid).child(filePathName);
            storageReference.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            String downloadUri = uriTask.getResult().toString();
                            if (uriTask.isSuccessful()) {
                                uploadToDB(downloadUri, index, filename, uid, fileUri, fileNumber, requestCode);
                            }


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Dashboard.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });

            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        }

    }*/

    /*private void uploadToDB(String downloadUri, final int index, final String filename, String uid, Uri fileUri, final TextView fileNumber, int requestCode) {
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
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        status.remove(index);
                        status.add(index, "done");
                        uploadFileAdapter.notifyDataSetChanged();
                        fileNumber.setText("Upload Successful...");
                        Toast.makeText(Dashboard.this, "" + filename + "Successfully Uploaded", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progress.dismiss();
                        Toast.makeText(Dashboard.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();

                    }
                });
    }*/

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

    /*private void authenticate() {
        View view = LayoutInflater.from(Dashboard.this).inflate(R.layout.verifyfingerprint
                , null);
        final TextView authenticateTv = view.findViewById(R.id.authenticateTv);
        final Button authBtn = view.findViewById(R.id.authBtn);
        final ImageView authIv = view.findViewById(R.id.authIv);

        keyguardManager =
                (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        fingerprintManager =
                (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        executor = ContextCompat.getMainExecutor(this);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Dashboard.this);
        builder.setView(view);
        final android.app.AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Login using Fingerprint Authentication")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt = new BiometricPrompt(Dashboard.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                authenticateTv.setTextColor(Color.parseColor("#F12011"));
                authIv.setImageResource(R.drawable.ic_cancel);
                authenticateTv.setText("Authentication error: " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                authenticateTv.setTextColor(Color.parseColor("#3EE210"));
                authIv.setImageResource(R.drawable.ic_verified02);
                authenticateTv.setText("Authentication Successful...!");
                dialog.dismiss();


            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                authenticateTv.setTextColor(Color.parseColor("#F12011"));
                authIv.setImageResource(R.drawable.ic_failed);
                authenticateTv.setText("Authentication Failed...!");
            }
        });

        authBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                biometricPrompt.authenticate(promptInfo);
            }
        });
    }};*/
}




