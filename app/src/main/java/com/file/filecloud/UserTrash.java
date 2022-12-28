package com.file.filecloud;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.file.filecloud.Adapter.UserTrashAdapter;
import com.file.filecloud.ModelClass.UserTrashModel;
import com.file.cloud.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class UserTrash extends AppCompatActivity {

    private FirebaseAuth auth;
    private ActionBar actionBar;
    private RecyclerView trashRv;
    private ArrayList<UserTrashModel> trashModel;
    private UserTrashAdapter trashAdapter;
    private ImageView trashPH;
    String uid;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_trash);


        auth= FirebaseAuth.getInstance();
        uid=auth.getUid();


        actionBar=getSupportActionBar();
        actionBar.setTitle("Trash");
        actionBar.setSubtitle("Items older than 30days deletes automatically ");

        trashRv = findViewById(R.id.trashRv);
        trashPH = findViewById(R.id.trashPH);


        loadTrash();
    }
    private void loadTrash() {
        trashModel = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("Trash").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        trashModel.clear();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                if (ds.exists()){
                                    trashPH.setVisibility(View.GONE);
                                    UserTrashModel model = ds.getValue(UserTrashModel.class);
                                    trashModel.add(model);
                                }else {
                                    trashPH.setVisibility(View.VISIBLE);
                                    trashAdapter.notifyDataSetChanged();
                                }

                            }

                            trashAdapter = new UserTrashAdapter(UserTrash.this,trashModel);
                            trashAdapter.notifyDataSetChanged();
                            trashRv.setAdapter(trashAdapter);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void searchLoadTrash(final String query) {
        trashModel = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("Trash").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trashModel.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String type =""+ds.child("type").getValue();
                    switch (type) {
                        case "PDF":
                        case "PHOTO":
                        case "EXCEL":
                        case "DOC":
                        case "AUDIO":
                        case "PPT":
                        case "VIDEO": {
                            UserTrashModel model = ds.getValue(UserTrashModel.class);
                            if (model.getFileName().toLowerCase().contains(query.toLowerCase())) {
                                trashModel.add(model);
                            }
                            break;
                        }
                    }

                }
                trashAdapter = new UserTrashAdapter(UserTrash.this,trashModel);
                trashRv.setAdapter(trashAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.logout).setVisible(false);
        menu.findItem(R.id.aboutApp).setVisible(false);


        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) search.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchLoadTrash(query);
                }
                else {
                    loadTrash();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {

                if (!TextUtils.isEmpty(query)){
                    searchLoadTrash(query);
                }
                else {
                    loadTrash();
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();
        if (!isScreenOn) {
            startActivity(new Intent(UserTrash.this, PinVerification.class));
        }
    }


}

