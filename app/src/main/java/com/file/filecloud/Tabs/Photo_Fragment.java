package com.file.filecloud.Tabs;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.filecloud.AboutApp;
import com.file.filecloud.Adapter.PhotohAdapter;
import com.file.filecloud.LoginActivity;
import com.file.filecloud.ModelClass.Files_Model;
import com.file.cloud.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class Photo_Fragment extends Fragment {

    private FirebaseAuth auth;
    String uid;
    private RecyclerView photoRv;
    private ArrayList<Files_Model> filesModel;
    private PhotohAdapter photohAdapter;
    private ImageView placeholder;
    FirebaseUser user;

    public Photo_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        auth= FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        uid=auth.getUid();

        photoRv = view.findViewById(R.id.photoRv);
        placeholder = view.findViewById(R.id.placeholder);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),3, LinearLayoutManager.VERTICAL,false);
        photoRv.setLayoutManager(gridLayoutManager);

        loadPhoto();

        return view;
    }

    private void loadPhoto() {
        filesModel = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(user.getUid()).child("Files")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        filesModel.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Files_Model model = ds.getValue(Files_Model.class);
                             if (model.getType().equals("PHOTO")) {
                                if (ds.exists()){
                                    placeholder.setVisibility(View.GONE);
                                    filesModel.add(model);
                                }else {
                                    placeholder.setVisibility(View.VISIBLE);
                                }

                            }
                        }
                        photohAdapter = new PhotohAdapter(getActivity(), filesModel);
                        photoRv.setAdapter(photohAdapter);

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
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item1 = menu.findItem(R.id.search).setVisible(false);

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
