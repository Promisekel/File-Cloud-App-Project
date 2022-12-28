package com.file.filecloud.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

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

public class AdapterSearchPeers extends RecyclerView.Adapter<AdapterSearchPeers.HolderPeers> {


    private final Context context;
    private final ArrayList<ModelPeers> modelPeers;

    public AdapterSearchPeers(Context context, ArrayList<ModelPeers> modelPeers) {
        this.context = context;
        this.modelPeers = modelPeers;
    }

    @NonNull
    @Override
    public HolderPeers onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_peers, parent, false);
        return new AdapterSearchPeers.HolderPeers(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderPeers holder, int position) {
        final ModelPeers model = modelPeers.get(position);
        final String fullName = model.getFullName();
        final String uid = model.getUid();
        final String avatar = model.getImage();

        holder.nameTv.setText(fullName);

        try {
            Picasso.get().load(avatar).placeholder(R.drawable.ic_user_face).into(holder.avatarIv);

        } catch (Exception e) {
        }

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenuOptions(uid, holder.moreBtn, fullName);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Connect?")
                        .setMessage("Do you want to connect with this peer and share files?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                connect(uid);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

    }

    private void connect(final String uid) {
        final ProgressDialog progress = new ProgressDialog(context);
        progress.setMessage("Connecting... ");
        progress.show();
        progress.setCanceledOnTouchOutside(false);
        progress.setCancelable(false);
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        final HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("uid", "" + uid);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(fUser.getUid()).child("Peers").child(uid).setValue(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            final HashMap<Object, String> hashMap2 = new HashMap<>();
                            hashMap2.put("uid", "" + fUser.getUid());
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(uid).child("Peers").child(fUser.getUid()).setValue(hashMap2)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast toast = Toast.makeText(context, "connected successfully", Toast.LENGTH_LONG);
                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                            toast.show();
                                            progress.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast toast = Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                    progress.dismiss();
                                }
                            });

                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                progress.dismiss();
            }
        });

    }

    private void openMenuOptions(final String uid, ImageButton morebtn, String nicki) {
        final PopupMenu popupMenu = new PopupMenu(context, morebtn, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE, 0, 1, "Connect");
        popupMenu.getMenu().add(Menu.NONE, 1, 2, "View contact");


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    new AlertDialog.Builder(context)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Connect?")
                            .setMessage("Do you want to connect with this peer and share files?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    connect(uid);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();


                }
                if (id == 1) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(uid)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds: snapshot.getChildren()){
                                        String fullName = "" + ds.child("fullName").getValue();
                                        String phone = "" + ds.child("phone").getValue();
                                        String email = "" + ds.child("email").getValue();

                                        View view = LayoutInflater.from(context).inflate(R.layout.viewpeerdetails, null);
                                        final TextView name = view.findViewById(R.id.nicki);
                                        final TextView contact = view.findViewById(R.id.contact);
                                        final TextView mail = view.findViewById(R.id.mail);

                                        mail.setText(email);

                                        if (fullName.isEmpty()){
                                            name.setText("Name not Provided Yet");
                                            name.setTextColor(Color.GRAY);
                                        }
                                        else {
                                            name.setText(fullName);
                                        }
                                        if (phone.isEmpty()){
                                            contact.setText("Contact not Provided Yet");
                                            contact.setTextColor(Color.GRAY);
                                        }else{
                                            contact.setText(phone);
                                        }

                                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                                        builder.setView(view);

                                        final android.app.AlertDialog dialog = builder.create();
                                        dialog.show();

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

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


    public static class HolderPeers extends RecyclerView.ViewHolder {

        private ImageView avatarIv;
        private TextView nameTv;
        private ImageButton moreBtn;

        public HolderPeers(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);

        }
    }
}
