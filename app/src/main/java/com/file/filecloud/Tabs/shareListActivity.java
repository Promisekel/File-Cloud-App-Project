package com.file.filecloud.Tabs;

import android.R.color;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.file.filecloud.Dashboard;
import com.file.filecloud.Firebase.NetworkConnection;
import com.file.filecloud.ModelClass.ModelPeers;
import com.file.cloud.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class
shareListActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private Toolbar toolbar;
    private TextView displayTv;
    private String fuser, timestamp, fileName, fileUri, type, uid;
    private RecyclerView peersRv;
    private AdapterShare adapterShare;
    private RelativeLayout placeholder;
    private ArrayList<ModelPeers> modelPeer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.peersactivity);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        fuser = auth.getUid();

        Intent intent = getIntent();
        timestamp = "" + intent.getStringExtra("timestamp");
        uid = "" + intent.getStringExtra("uid");
        fileName = "" + intent.getStringExtra("fileName");
        fileUri = "" + intent.getStringExtra("fileUri");
        type = "" + intent.getStringExtra("type");

        toolbar = findViewById(R.id.toolbar);
        peersRv = findViewById(R.id.peersRv);
        displayTv = findViewById(R.id.displayTv);
        placeholder = findViewById(R.id.placeholder);
        displayTv.setText("Share with...");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .9), (int) (height * .7));
        loadPeers();

    }

    private void loadPeers() {
        modelPeer = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(fuser).child("Peers")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        modelPeer.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.exists()) {
                                placeholder.setVisibility(View.GONE);
                                ModelPeers model = ds.getValue(ModelPeers.class);
                                modelPeer.add(model);
                            } else {
                                placeholder.setVisibility(View.VISIBLE);
                            }
                        }

                        adapterShare = new AdapterShare(shareListActivity.this, modelPeer);
                        adapterShare.notifyDataSetChanged();
                        peersRv.setAdapter(adapterShare);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void searchPeers(final String query) {
        modelPeer = new ArrayList<>();
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelPeer.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.exists()) {
                        placeholder.setVisibility(View.GONE);
                        ModelPeers model = ds.getValue(ModelPeers.class);
                        if (!model.getUid().equals(fUser.getUid())) {
                            if (model.getFullName().toLowerCase().contains(query.toLowerCase())) {
                                modelPeer.add(model);

                            }
                        }

                    } else {
                        placeholder.setVisibility(View.VISIBLE);
                    }

                }
                adapterShare = new AdapterShare(shareListActivity.this, modelPeer);
                adapterShare.notifyDataSetChanged();
                peersRv.setAdapter(adapterShare);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    protected void onStart() {
        CheckName();
        super.onStart();

    }

    private void CheckName() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String firstName = "" + ds.child("firstName").getValue();
                    if (firstName.isEmpty()) {
                        verifyInfoDialog();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void verifyInfoDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.warning_dialog, null);
        final Button msgeBtn = view.findViewById(R.id.msgeBtn);
        TextView msgeTv = view.findViewById(R.id.msgeTv);
        Button cancelBtn = view.findViewById(R.id.cancelBtn);
        cancelBtn.setVisibility(View.VISIBLE);
        msgeTv.setText("Username is empty, please provide your username before sharing any file");
        msgeBtn.setText("Provide");

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(view);

        final android.app.AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();

        msgeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNameUpdateDialog(dialog);

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showNameUpdateDialog(final android.app.AlertDialog dialog) {
        View view = LayoutInflater.from(this).inflate(R.layout.register_pin_dialog, null);
        final Button setpinBtn = view.findViewById(R.id.setpinBtn);
        final EditText firstName = view.findViewById(R.id.pinEt);
        final EditText surName = view.findViewById(R.id.setpassEt);
        final TextView dialogTitle = view.findViewById(R.id.dialogTitle);

        dialogTitle.setText("Update current name");
        firstName.setHint("Enter your firstName");
        surName.setHint("Enter your SurName");
        setpinBtn.setText("Done");
        firstName.setInputType(InputType.TYPE_CLASS_TEXT);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(view);

        final android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();

        setpinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fname = firstName.getText().toString().trim();
                final String sname = surName.getText().toString().trim();
                if (!TextUtils.isEmpty(fname)) {
                    if (!TextUtils.isEmpty(sname)) {
                        updateUserName(fname, sname, alertDialog, dialog);
                    } else {
                        surName.setHint("Provide your surname");
                        surName.setHintTextColor(Color.parseColor("#880808"));
                    }
                } else {
                    firstName.setHint("Provide your first name");
                    firstName.setHintTextColor(Color.parseColor("#880808"));
                }
            }
        });
    }

    private void updateUserName(final String fname, final String sname, final android.app.AlertDialog dialog, final android.app.AlertDialog alertDialog) {
        final ProgressDialog progress = new ProgressDialog(shareListActivity.this);
        progress.setMessage("updating name please wait...");
        progress.show();
        progress.setCanceledOnTouchOutside(false);
        progress.setCancelable(false);
        final HashMap<String, Object> name = new HashMap<>();
        name.put("firstName", fname);
        name.put("surName", sname);
        name.put("fullName", fname + " " + sname);


        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(user.getUid()).updateChildren(name).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progress.dismiss();
                    alertDialog.dismiss();
                    dialog.dismiss();
                    dialog.dismiss();
                    Toast.makeText(shareListActivity.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progress.dismiss();
                alertDialog.dismiss();
                dialog.dismiss();
                dialog.dismiss();
                Toast.makeText(shareListActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    /*private void updateUserName(final String fname, final String sname, final android.app.AlertDialog dialog, final android.app.AlertDialog alertDialog) {
        final ProgressDialog progress = new ProgressDialog(shareListActivity.this);
        progress.setMessage("updating name please wait...");
        progress.show();
        progress.setCanceledOnTouchOutside(false);
        progress.setCancelable(false);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(user.getUid()).child("fullName").setValue(fname + " " + sname).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    dialog.dismiss();
                    final HashMap<String, Object> name = new HashMap<>();
                    name.put("firstName", fname);
                    name.put("surName", sname);
                    name.put("fullName", sname);
                    final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.child(user.getUid()).updateChildren(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progress.dismiss();
                                alertDialog.dismiss();
                                dialog.dismiss();
                                Toast.makeText(shareListActivity.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progress.dismiss();
                            alertDialog.dismiss();
                            dialog.dismiss();
                            Toast.makeText(shareListActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                alertDialog.dismiss();
                progress.dismiss();
                Toast.makeText(shareListActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem item1 = menu.findItem(R.id.logout).setVisible(false);

        // return true so that the menu pop up is opened
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                displayTv.setVisibility(View.GONE);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

public class AdapterShare extends RecyclerView.Adapter<AdapterShare.ViewHolder> {

    private Context context;
    private ArrayList<ModelPeers> modelPeers;

    public AdapterShare(Context context, ArrayList<ModelPeers> modelPeers) {
        this.context = context;
        this.modelPeers = modelPeers;
    }

    @NonNull
    @Override
    public shareListActivity.AdapterShare.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_peers, parent, false);
        return new shareListActivity.AdapterShare.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final shareListActivity.AdapterShare.ViewHolder holder, int position) {
        final ModelPeers model = modelPeers.get(position);
        final String uid = model.getUid();
        final String firstName = model.getFirstName();
        final String surName = model.getSurName();
        final String fullName = model.getSurName();


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String image = "" + ds.child("image").getValue();
                    String fullName = "" + ds.child("fullName").getValue();
                    holder.nameTv.setText(fullName);
                    try {
                        Picasso.get().load(image).placeholder(R.drawable.ic_user_face).into(holder.avatarIv);
                    } catch (Exception e) {

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenuOptions(uid, model, holder.moreBtn);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkConnection.isNetworkAvailable(shareListActivity.this)){
                    new androidx.appcompat.app.AlertDialog.Builder(shareListActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("No Internet Connection")
                            .setMessage("Restore Internet connectivity and try again")
                            .setPositiveButton("Setup", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        startActivity(new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY));
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }else {
                shareFile(model);}
            }
        });
    }

    private void shareFile(final ModelPeers model) {
        View view = LayoutInflater.from(shareListActivity.this).inflate(R.layout.custom_dialog, null);
        final TextView messageTv = view.findViewById(R.id.messageTv);
        final Button VerifyBtn = view.findViewById(R.id.VerifyBtn);
        final ProgressBar progress = view.findViewById(R.id.progress);
        TextView messageTitleTv = view.findViewById(R.id.messageTitleTv);
        messageTitleTv.setVisibility(View.GONE);


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(model.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String fullName = "" + ds.child("fullName").getValue();
                    messageTv.setText("Share file with " + fullName + "?");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        VerifyBtn.setText("Share");

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(shareListActivity.this);
        builder.setView(view);

        final android.app.AlertDialog dialog = builder.create();
        dialog.show();

        VerifyBtn.setOnClickListener(v -> {
            VerifyBtn.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            final String timestamp = String.valueOf(System.currentTimeMillis());
            final HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("sender", user.getUid());
            hashMap.put("fileName", fileName);
            hashMap.put("fileUri", fileUri);
            hashMap.put("timestamp", timestamp);
            if (type.equals("PDF")) {
                hashMap.put("type", "PDF");
                hashMap.put("fileName", fileName + ".pdf");
            }
            if (type.equals("DOC")) {
                hashMap.put("type", "DOC");
            }
            if (type.equals("PPT")) {
                hashMap.put("type", "PPT");
            }
            if (type.equals("AUDIO")) {
                hashMap.put("type", "AUDIO");
            }
            if (type.equals("VIDEO")) {
                hashMap.put("type", "VIDEO");
            }
            if (type.equals("EXCEL")) {
                hashMap.put("type", "EXCEL");
            }
            if (type.equals("PHOTO")) {
                hashMap.put("type", "PHOTO");
            }
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(model.getUid()).child("Received Files").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(aVoid -> {
                        dialog.dismiss();
                        progress.setVisibility(View.GONE);
                        Toast.makeText(context, "File Shared Successfully", Toast.LENGTH_LONG).show();

                    }).addOnFailureListener(e -> Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_LONG).show());
        });
    }

    private void openMenuOptions(final String uid, final ModelPeers model, ImageButton morebtn) {
        final PopupMenu popupMenu = new PopupMenu(context, morebtn, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Share");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 1) {
                    shareFile(model);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return modelPeers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView avatarIv;
        private TextView nameTv;
        private ImageButton moreBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
        }
    }
}
}