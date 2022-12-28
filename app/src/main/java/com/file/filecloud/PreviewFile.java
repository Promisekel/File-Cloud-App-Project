package com.file.filecloud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.file.cloud.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class PreviewFile extends AppCompatActivity {
    private ImageView closeBtn;
    private WebView previewWeb;
    private TextView fileName;
    private String uid, timestamp, filename, fileUrl, file;
    private String Uri;
    private ProgressBar progress, progress2;
    ImageView refreshBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_file);
        previewWeb = findViewById(R.id.previewWeb);
        fileName = findViewById(R.id.fileName);
        progress = findViewById(R.id.progress);
        progress2 = findViewById(R.id.progress2);
        refreshBar = findViewById(R.id.refreshBar);


        Intent intent = getIntent();
        uid = intent.getStringExtra("myUid");
        timestamp = intent.getStringExtra("timestamp");
        fileUrl = intent.getStringExtra("fileUrl");
        file = intent.getStringExtra("fileName");

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .9), (int) (height * .7));

        previewWeb.getSettings().setAppCacheEnabled(true);
        previewWeb.getSettings().setDatabaseEnabled(true);
        previewWeb.getSettings().setDomStorageEnabled(true);
        previewWeb.getSettings().setSupportZoom(true);
        previewWeb.getSettings().setBuiltInZoomControls(true);
        previewWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        previewWeb.getSettings().setJavaScriptEnabled(true);

        fileName.setText(file);

        loadDocument();

       /* DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Files");
        ref.orderByChild("timestamp").equalTo(timestamp)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            Uri = ""+ds.child("fileUri").getValue();
                            filename = ""+ds.child("fileName").getValue();

                            fileName.setText(filename);
                            previewWeb.getSettings().setJavaScriptEnabled(true);

                            previewWeb.setWebViewClient(new WebViewClient(){
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
                                Uri= URLEncoder.encode(Uri, "UTF-8");
                            }
                            catch (UnsupportedEncodingException e){
                                e.printStackTrace();
                            }
                            previewWeb.loadUrl("https://docs.google.com/gview?embedded=true&url="+Uri);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });*/

        //////////////////////////////////////SWIPERESHRESHLAYER////////////////////////////////////////////////////////////

        refreshBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadPage();

            }
        });

    }

    private void loadDocument() {

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
            fileUrl = URLEncoder.encode(fileUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        previewWeb.loadUrl("https://docs.google.com/gview?embedded=true&url=" + fileUrl);
    }

    private void reloadPage() {
        progress2.setVisibility(View.VISIBLE);
        refreshBar.setVisibility(View.INVISIBLE);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Files");
        ref.orderByChild("timestamp").equalTo(timestamp)
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
    protected void onStop() {
        super.onStop();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();
        if (!isScreenOn) {
            startActivity(new Intent(PreviewFile.this, PinVerification.class));
        }
    }
}
