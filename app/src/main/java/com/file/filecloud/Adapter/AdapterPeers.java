package com.file.filecloud.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
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

import com.file.filecloud.Dashboard;
import com.file.filecloud.Firebase.NetworkConnection;
import com.file.filecloud.ModelClass.ModelPeers;
import com.file.filecloud.Tabs.viewpeerphoto;
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

public class AdapterPeers extends RecyclerView.Adapter<AdapterPeers.HolderPeers> {


    private Context context;
    private ArrayList<ModelPeers> modelPeers;

    public AdapterPeers(Context context, ArrayList<ModelPeers> modelPeers) {
        this.context = context;
        this.modelPeers = modelPeers;
    }

    @NonNull
    @Override
    public HolderPeers onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_peers,parent,false);
        return new AdapterPeers.HolderPeers(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderPeers holder, int position) {
        final ModelPeers model = modelPeers.get(position);
        final String fullName = model.getFullName();
        final String uid = model.getUid();
        final String avatar = model.getImage();


        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenuOptions(uid,holder.moreBtn,fullName);
            }
        });

        holder.avatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, viewpeerphoto.class);
                intent.putExtra("myUid", uid);
                context.startActivity(intent);

            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String fullName = "" + ds.child("fullName").getValue();
                    String image = "" + ds.child("image").getValue();

                    holder.nameTv.setText(fullName);

                    try {
                        Picasso.get().load(image).placeholder(R.drawable.ic_user_face).into(holder.avatarIv);

                    }catch (Exception e){

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void openMenuOptions(final String uid, ImageButton morebtn, String nicki) {
        final PopupMenu popupMenu = new PopupMenu(context, morebtn, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE, 0, 1, "View contact");
        popupMenu.getMenu().add(Menu.NONE, 1, 2, "Remove");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
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

                } if (id == 1){

                    if (!NetworkConnection.isNetworkAvailable(context)){
                        new androidx.appcompat.app.AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("No Internet Connection")
                                .setMessage("Restore Internet connectivity and try again")
                                .setPositiveButton("Setup", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            context.startActivity(new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY));
                                        }
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }else {
                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Remove?")
                                .setMessage("Remove peer from list?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        removePeer(uid, dialog);
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }

                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void removePeer(final String uid, final DialogInterface dialog) {
        if (!NetworkConnection.isNetworkAvailable(context)){
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("No Internet Connection")
                    .setMessage("Restore Internet connectivity and try again")
                    .setPositiveButton("Setup", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                context.startActivity(new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY));
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }else {
            final ProgressDialog progress = new ProgressDialog(context);
            progress.setMessage("Removing... ");
            progress.show();
            progress.setCanceledOnTouchOutside(false);
            progress.setCancelable(false);
            final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(fUser.getUid()).child("Peers").child(uid).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ref.child(uid).child("Peers").child(fUser.getUid()).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast toast = Toast.makeText(context, "Removed successfully", Toast.LENGTH_LONG);
                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                toast.show();
                                                progress.dismiss();
                                                dialog.dismiss();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast toast = Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_LONG);
                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                toast.show();
                                                progress.dismiss();
                                                dialog.dismiss();
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
    }

    @Override
    public int getItemCount() {
        return modelPeers.size();
    }


    public static class HolderPeers extends RecyclerView.ViewHolder{

        private ImageView avatarIv;
        private TextView nameTv;
        private ImageButton moreBtn;

        public HolderPeers(@NonNull View itemView) {
            super(itemView);

            avatarIv=itemView.findViewById(R.id.avatarIv);
            nameTv=itemView.findViewById(R.id.nameTv);
            moreBtn=itemView.findViewById(R.id.moreBtn);

        }
    }
}
