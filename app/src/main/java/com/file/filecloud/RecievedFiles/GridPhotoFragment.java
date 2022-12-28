package com.file.filecloud.RecievedFiles;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.file.filecloud.Adapter.RecievedPhotoAdapter;
import com.file.filecloud.ModelClass.Files_Model;
import com.file.cloud.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class GridPhotoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseAuth auth;
    String uid;
    private RecyclerView photoRv;
    private ArrayList<Files_Model> filesModel;
    private RecievedPhotoAdapter recievedPhotoAdapter;
    private ImageView placeholder;

    public GridPhotoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_gridphoto, container, false);

        auth= FirebaseAuth.getInstance();
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
        reference.child(uid).child("Received Files")
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
                        recievedPhotoAdapter = new RecievedPhotoAdapter(getActivity(), filesModel);
                        photoRv.setAdapter(recievedPhotoAdapter);

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
        menu.findItem(R.id.logout).setVisible(false);
        menu.findItem(R.id.aboutApp).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


}