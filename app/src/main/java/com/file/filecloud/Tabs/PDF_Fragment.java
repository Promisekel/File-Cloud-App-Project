package com.file.filecloud.Tabs;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
public class PDF_Fragment extends Fragment {

    private FirebaseAuth auth;
    private RecyclerView pdfRv;
    private ArrayList<Files_Model> filesModels;
    private FileTapAdapter fileTapAdapter;
    String uid;
    private ImageView placeholder;



    public PDF_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pdf, container, false);

        auth= FirebaseAuth.getInstance();
        uid=auth.getUid();

        pdfRv = view.findViewById(R.id.pdfRv);
        placeholder = view.findViewById(R.id.placeholder);


        loadPDFs();


        return view;
    }

    private void loadPDFs() {
        filesModels = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("Files")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        filesModels.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            Files_Model model = ds.getValue(Files_Model.class);
                            if (model.getType().equals("PDF")){
                                if (ds.exists()){
                                    placeholder.setVisibility(View.GONE);
                                    filesModels.add(model);
                                }else {
                                    placeholder.setVisibility(View.VISIBLE);
                                }

                            }
                        }
                        fileTapAdapter = new FileTapAdapter(getActivity(),filesModels);
                        pdfRv.setAdapter(fileTapAdapter);
                        fileTapAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void searchPDFs(final String query) {
        filesModels = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("Files")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        filesModels.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String type =""+ds.child("type").getValue();
                            if (type.equals("PDF")){
                                Files_Model model = ds.getValue(Files_Model.class);
                                if (model.getFileName().toLowerCase().contains(query.toLowerCase())) {
                                    filesModels.add(model);
                                }
                            }
                        }
                        fileTapAdapter = new FileTapAdapter(getActivity(),filesModels);
                        pdfRv.setAdapter(fileTapAdapter);
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
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    searchPDFs(query);
                } else {
                    loadPDFs();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {

                if (!TextUtils.isEmpty(query)) {
                    searchPDFs(query);
                } else {
                    loadPDFs();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.aboutApp){
            startActivity(new Intent(getActivity(), AboutApp.class));
        }else if (id == R.id.logout) {

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



        }
        return super.onOptionsItemSelected(item);
    }

}
