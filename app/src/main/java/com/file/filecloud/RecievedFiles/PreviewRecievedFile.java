package com.file.filecloud.RecievedFiles;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.file.filecloud.PinVerification;
import com.file.cloud.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class PreviewRecievedFile extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ImageView closeBtn;
    private WebView previewWeb;
    private TextView fileName;
    private String uid,timestamp,filename, fileUri,Uri, file;
    private ProgressBar progress, progress2;
    ImageView refreshBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_file);

        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();

        Intent intent = getIntent();
        uid = intent.getStringExtra("myUid");
        timestamp = intent.getStringExtra("timestamp");
        fileUri = intent.getStringExtra("fileUri");
        file = intent.getStringExtra("fileName");

        previewWeb = findViewById(R.id.previewWeb);
        fileName = findViewById(R.id.fileName);
        progress = findViewById(R.id.progress);
        progress2 = findViewById(R.id.progress2);
        refreshBar = findViewById(R.id.refreshBar);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .9), (int) (height * .7));

        previewWeb.getSettings().setAppCacheEnabled(true);
        previewWeb.getSettings().setDatabaseEnabled(true);
        previewWeb.getSettings().setDomStorageEnabled(true);
        previewWeb.getSettings().setSupportZoom(true);
        previewWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        previewWeb.getSettings().setBuiltInZoomControls(true);

        fileName.setText(file);

        loadDocument();

       /* DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(user.getUid()).child("Received Files").orderByChild("timestamp").equalTo(timestamp)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Uri = "" + ds.child("fileUri").getValue();
                            filename = "" + ds.child("fileName").getValue();

                            fileName.setText(filename);
                            previewWeb.getSettings().setJavaScriptEnabled(true);

                            previewWeb.setWebViewClient(new WebViewClient() {
                                @Override
                                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                    super.onPageStarted(view, url, favicon);
                                    progress.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    super.onPageFinished(view, url);
                                    progress.setVisibility(View.GONE);

                                }
                            });

                            try {
                                Uri = URLEncoder.encode(Uri, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            previewWeb.loadUrl("https://docs.google.com/gview?embedded=true&url=" + Uri);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });*/

        refreshBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadPage();
            }
        });
    }

    private void loadDocument()
    {

        previewWeb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progress.setVisibility(View.GONE);

            }
        });

        try {
            fileUri = URLEncoder.encode(fileUri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        previewWeb.loadUrl("https://docs.google.com/gview?embedded=true&url=" + fileUri);
    }

    private void reloadPage()
    {
        progress2.setVisibility(View.VISIBLE);
        refreshBar.setVisibility(View.INVISIBLE);
         DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(user.getUid()).child("Received Files").orderByChild("timestamp").equalTo(timestamp)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Uri = "" + ds.child("fileUri").getValue();
                            filename = "" + ds.child("fileName").getValue();

                            fileName.setText(filename);
                            previewWeb.getSettings().setJavaScriptEnabled(true);

                            previewWeb.setWebViewClient(new WebViewClient() {
                                @Override
                                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                    super.onPageStarted(view, url, favicon);
                                    refreshBar.setVisibility(View.INVISIBLE);
                                    progress.setVisibility(View.VISIBLE);
                                    progress2.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    super.onPageFinished(view, url);
                                    progress.setVisibility(View.GONE);
                                    progress2.setVisibility(View.INVISIBLE);
                                    refreshBar.setVisibility(View.VISIBLE);

                                }
                            });

                            try {
                                Uri = URLEncoder.encode(Uri, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            previewWeb.loadUrl("https://docs.google.com/gview?embedded=true&url=" + Uri);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    protected void onStop()
    {
        super.onStop();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();
        if (!isScreenOn) {
            startActivity(new Intent(PreviewRecievedFile.this, PinVerification.class));
        }
    }

}
