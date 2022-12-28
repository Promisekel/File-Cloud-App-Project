package com.file.filecloud;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.file.filecloud.Adapter.AdapterPeers;
import com.file.filecloud.Adapter.AdapterSearchPeers;
import com.file.filecloud.ModelClass.ModelPeers;
import com.file.cloud.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class PeersActivity extends AppCompatActivity {

    private String uid,timestamp,filename,fileUri;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private RecyclerView peersRv;
    private Toolbar toolbar;
    private TextView displayTv;
    private RelativeLayout placeholder;
    private AdapterSearchPeers adapterSearchPeers;
    private AdapterPeers adapterPeers;
    private ArrayList<ModelPeers> modelPeer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.peersactivity);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uid = auth.getUid();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .9), (int) (height * .7));

        peersRv = findViewById(R.id.peersRv);
        toolbar = findViewById(R.id.toolbar);
        displayTv =findViewById(R.id.displayTv);
        placeholder =findViewById(R.id.placeholder);
        displayTv.setText("PEERS");
        setSupportActionBar(toolbar);
       Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
       loadPeers();

    }

    private void loadPeers() {
        modelPeer = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).child("Peers")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        modelPeer.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.exists()){
                                placeholder.setVisibility(View.GONE);
                                ModelPeers model = ds.getValue(ModelPeers.class);
                                modelPeer.add(model);
                            }else {
                                placeholder.setVisibility(View.VISIBLE);
                            }

                        }

                        adapterPeers = new AdapterPeers(PeersActivity.this, modelPeer);
                        adapterPeers.notifyDataSetChanged();
                        peersRv.setAdapter(adapterPeers);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void searchPeers(final String query) {
        check();
        modelPeer = new ArrayList<>();
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        modelPeer.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.exists()){
                                placeholder.setVisibility(View.GONE);
                                ModelPeers model = ds.getValue(ModelPeers.class);
                                if (!model.getUid().equals(fUser.getUid())){
                                    if (model.getFullName().toLowerCase().contains(query.toLowerCase())){
                                        modelPeer.add(model);

                                    }
                                }

                            }else {
                                placeholder.setVisibility(View.VISIBLE);
                            }


                        }
                        adapterSearchPeers = new AdapterSearchPeers(PeersActivity.this, modelPeer);
                        adapterPeers.notifyDataSetChanged();
                        peersRv.setAdapter(adapterSearchPeers);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void check() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem item1 = menu.findItem(R.id.logout).setVisible(false);

        // return true so that the menu pop up is opened
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (!TextUtils.isEmpty(query)) {
                    searchPeers(query);
                } else {
                    loadPeers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (!TextUtils.isEmpty(query)) {
                    searchPeers(query);
                } else {
                    loadPeers();
                }
                return false;
            }
        });
        return true; }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}