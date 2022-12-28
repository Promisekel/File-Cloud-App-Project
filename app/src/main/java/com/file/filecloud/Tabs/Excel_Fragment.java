package com.file.filecloud.Tabs;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.file.filecloud.AboutApp;
import com.file.filecloud.Adapter.FileTapAdapter;
import com.file.filecloud.LoginActivity;
import com.file.filecloud.ModelClass.Files_Model;
import com.file.cloud.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class Excel_Fragment extends Fragment {

    private FirebaseAuth auth;
    private RecyclerView excelRv;
    private ArrayList<Files_Model> filesModels;
    private FileTapAdapter fileTapAdapter;
    String uid;
    private ImageView placeholder;


    public Excel_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_excel, container, false);

        auth= FirebaseAuth.getInstance();
        uid=auth.getUid();

        excelRv = view.findViewById(R.id.excelRv);
        placeholder = view.findViewById(R.id.placeholder);

        loadExcels();

        return  view;
    }

    private void loadExcels() {
        filesModels = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("Files")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        filesModels.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            Files_Model model = ds.getValue(Files_Model.class);
                            if (model.getType().equals("EXCEL") ){
                                if (ds.exists()){
                                    placeholder.setVisibility(View.GONE);
                                    filesModels.add(model);
                                }else {
                                    placeholder.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                        fileTapAdapter = new FileTapAdapter(getActivity(),filesModels);
                        excelRv.setAdapter(fileTapAdapter);
                        fileTapAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
    private void searchExcels(final String query) {
        filesModels = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("Files")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        filesModels.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String type =""+ds.child("type").getValue();
                            if (type.equals("EXCEL")){
                                Files_Model model = ds.getValue(Files_Model.class);
                                if (model.getFileName().toLowerCase().contains(query.toLowerCase())) {
                                    filesModels.add(model);
                                }
                            }
                        }
                        fileTapAdapter = new FileTapAdapter(getActivity(),filesModels);
                        excelRv.setAdapter(fileTapAdapter);
                        fileTapAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu,menu);

        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    searchExcels(query);
                } else {
                    loadExcels();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String query) {

                if (!TextUtils.isEmpty(query)) {
                    searchExcels(query);
                } else {
                    loadExcels();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            final Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            new AlertDialog.Builder(getActivity())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Logout?")
                    .setMessage("Are you sure you want to Logout?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            auth.signOut();
                            startActivity(loginIntent);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();

            }else if (id==R.id.aboutApp){
            startActivity(new Intent(getActivity(), AboutApp.class));
        }
        return super.onOptionsItemSelected(item);
    }

}
