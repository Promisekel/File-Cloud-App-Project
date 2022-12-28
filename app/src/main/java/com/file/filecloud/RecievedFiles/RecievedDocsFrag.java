package com.file.filecloud.RecievedFiles;

import static androidx.core.view.MenuItemCompat.getActionView;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
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

import com.file.filecloud.Adapter.ReceivedFileAdapter;
import com.file.filecloud.ModelClass.ModelReceivedFiles;
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
 * Use the {@link RecievedDocsFrag#newInstance} factory method to
 * create an instance of this fragment.102
 */
public class RecievedDocsFrag extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseAuth auth;
    private ActionBar actionBar;
    private RecyclerView receivedRv;
    private ArrayList<ModelReceivedFiles> recievedFiles;
    private ReceivedFileAdapter receivedFileAdapter;
    private ImageView placeholder;
    String uid;

    public RecievedDocsFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecievedDocsFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static RecievedDocsFrag newInstance(String param1, String param2) {
        RecievedDocsFrag fragment = new RecievedDocsFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragt_recieved_docs, container, false);



        auth= FirebaseAuth.getInstance();
        uid=auth.getUid();

        receivedRv = view.findViewById(R.id.receivedRv);
        placeholder = view.findViewById(R.id.placeholder);

        receivedFiles();
        return view;
    }
    private void receivedFiles()  {
        recievedFiles = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("Received Files").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recievedFiles.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String type =""+ds.child("type").getValue();
                    if (ds.exists()){
                        switch (type) {
                            case "PDF":
                            case "DOC":
                            case "EXCEL":
                            case "AUDIO":
                            case "PPT": {
                                ModelReceivedFiles model = ds.getValue(ModelReceivedFiles.class);
                                placeholder.setVisibility(View.GONE);
                                recievedFiles.add(model);

                                break;
                            }
                        }
                    }else {
                        placeholder.setVisibility(View.VISIBLE);
                    }


                }

                receivedFileAdapter = new ReceivedFileAdapter(getActivity(),recievedFiles);
                receivedFileAdapter.notifyDataSetChanged();
                receivedRv.setAdapter(receivedFileAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void searchLoadReceived(final String query) {
        recievedFiles = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("Received Files").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recievedFiles.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String type =""+ds.child("type").getValue();
                    if (ds.exists()){
                        if (type.equals("PDF")){
                            placeholder.setVisibility(View.GONE);
                            ModelReceivedFiles model = ds.getValue(ModelReceivedFiles.class);
                            if (model.getFileName().toLowerCase().contains(query.toLowerCase())) {
                                recievedFiles.add(model);
                            }
                            else {
                                placeholder.setVisibility(View.VISIBLE);
                            }
                        }else if (type.equals("DOC")){
                            ModelReceivedFiles model = ds.getValue(ModelReceivedFiles.class);
                            if (model.getFileName().toLowerCase().contains(query.toLowerCase())) {
                                recievedFiles.add(model);
                            }else {
                                placeholder.setVisibility(View.VISIBLE);
                            }
                        } else if (type.equals("EXCEL")){
                            ModelReceivedFiles model = ds.getValue(ModelReceivedFiles.class);
                            if (model.getFileName().toLowerCase().contains(query.toLowerCase())) {
                                recievedFiles.add(model);
                            }else {
                                placeholder.setVisibility(View.VISIBLE);
                            }
                        } else if (type.equals("AUDIO")){
                            ModelReceivedFiles model = ds.getValue(ModelReceivedFiles.class);
                            if (model.getFileName().toLowerCase().contains(query.toLowerCase())) {
                                recievedFiles.add(model);
                            }else {
                                placeholder.setVisibility(View.VISIBLE);
                            }
                        } else if (type.equals("PPT")){
                            ModelReceivedFiles model = ds.getValue(ModelReceivedFiles.class);
                            if (model.getFileName().toLowerCase().contains(query.toLowerCase())) {
                                recievedFiles.add(model);
                            }else {
                                placeholder.setVisibility(View.VISIBLE);
                            }
                        }
                        else if (type.equals("VIDEO")){
                            ModelReceivedFiles model = ds.getValue(ModelReceivedFiles.class);
                            if (model.getFileName().toLowerCase().contains(query.toLowerCase())) {
                                recievedFiles.add(model);
                            }else {
                                placeholder.setVisibility(View.VISIBLE);
                            }
                        }
                        else if (type.equals("PHOTO")){
                            ModelReceivedFiles model = ds.getValue(ModelReceivedFiles.class);
                            if (model.getFileName().toLowerCase().contains(query.toLowerCase())) {
                                recievedFiles.add(model);

                            }else {
                                placeholder.setVisibility(View.VISIBLE);
                            }
                        }
                    }



                }
                receivedFileAdapter = new ReceivedFileAdapter(getActivity(),recievedFiles);
                receivedRv.setAdapter(receivedFileAdapter);
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

        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    searchLoadReceived(query);
                } else {
                    receivedFiles();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {

                if (!TextUtils.isEmpty(query)) {
                    searchLoadReceived(query);
                } else {
                    receivedFiles();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}