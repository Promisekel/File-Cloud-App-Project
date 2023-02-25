package com.file.filecloud.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.format.DateFormat;
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
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.file.filecloud.Firebase.NetworkConnection;
import com.file.filecloud.ModelClass.UserTrashModel;
import com.file.cloud.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class UserTrashAdapter extends RecyclerView.Adapter<UserTrashAdapter.HolderUserTrash>
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private  static final int REQUEST_CODE =1000 ;
    private Context context;
    private ArrayList<UserTrashModel> trashModel;
    private FirebaseAuth auth;
    private FirebaseUser user;


    public UserTrashAdapter(Context context, ArrayList<UserTrashModel> trashModel) {
        this.context = context;
        this.trashModel = trashModel;

    }

    @NonNull
    @Override
    public HolderUserTrash onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_trash,parent,false);

        return new HolderUserTrash(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderUserTrash holder, final int position) {
        final UserTrashModel model = trashModel.get(position);
        final String fileName = model.getFileName();
        final String type = model.getType();
        final String timestamp = model.getTimestamp();
        final String deleteTimestamp = model.getDeleteTimestamp();
        final String uid = model.getUid();
        final String fileUri =  model.getFileUri();
        final String cutoffDeleteTime = model.getCutoffTimer();
        final String DeleteTypeReceived =model.getDeleteTypeReceived();

        holder.fileName.setText(fileName);
        auth=FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        final DatabaseReference currentRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("Trash");

        Long time = System.currentTimeMillis();  //get time in millis

        Long deleteCutoffTime = Long.valueOf(String.valueOf(cutoffDeleteTime));///convert cutoffDeleteTimer(SString) in DB to Long
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(String.valueOf(deleteCutoffTime)));
        final String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        if (time>deleteCutoffTime){////compare Deletes items that are older than 30days
            currentRef.child(deleteTimestamp).removeValue();
        }

        switch (type) {
            case "PDF":
                holder.avatarIv.setBackgroundResource(R.drawable.ic_pdf_icon);
                holder.restore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Restore File?")
                                .setMessage("Are you sure you want to restore this file?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
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
                                        restore(timestamp, uid, type, deleteTimestamp, fileName, fileUri, DeleteTypeReceived);}
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });
                holder.download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast toast = Toast.makeText(context, "Restore file, first before downloading", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                    }
                });

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Delete Permanently?")
                                .setMessage("Are you sure you want to Permanently delete this file?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deletePermanently(deleteTimestamp, uid);
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });
                break;
            case "DOC":
                holder.avatarIv.setBackgroundResource(R.drawable.ic_doc_icon);
                holder.restore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Restore File?")
                                .setMessage("Are you sure you want to restore this file?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
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
                                        restore(timestamp, uid, type, deleteTimestamp, fileName, fileUri, DeleteTypeReceived);}
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Delete Permanently?")
                                .setMessage("Are you sure you want to Permanently delete this file?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deletePermanently(deleteTimestamp, uid);
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();

                    }
                });

                holder.download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast toast = Toast.makeText(context, "Restore file first before downloading", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                });


                break;
            case "EXCEL":
                holder.avatarIv.setBackgroundResource(R.drawable.ic_excel_icon);

                holder.restore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Restore File?")
                                .setMessage("Are you sure you want to restore this file?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
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
                                        restore(timestamp, uid, type, deleteTimestamp, fileName, fileUri, DeleteTypeReceived);}
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Delete Permanently?")
                                .setMessage("Are you sure you want to Permanently delete this file?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deletePermanently(deleteTimestamp, uid);
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();

                    }
                });

                holder.download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast toast = Toast.makeText(context, "Restore file first before downloading", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                });

                break;
        /*else if (type.equals("AUDIO")){
            holder.avatarIv.setBackgroundResource(R.drawable.ic_music_icon);

            holder.restore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Restore File?")
                            .setMessage("Are you sure you want to restore this file?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    restore(timestamp,uid,type,deleteTimestamp,fileName,fileUri);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Delete Permanently?")
                            .setMessage("Are you sure you want to Permanently delete this file?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deletePermanently(deleteTimestamp,uid);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();

                }
            });

            holder.download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast = Toast.makeText(context,"Restore file first before downloading",Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
            });


        }*/
            case "PPT":
                holder.avatarIv.setBackgroundResource(R.drawable.ic_ppt_icon);

                holder.restore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Restore File?")
                                .setMessage("Are you sure you want to restore this file?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
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
                                        restore(timestamp, uid, type, deleteTimestamp, fileName, fileUri, DeleteTypeReceived);}
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Delete Permanently?")
                                .setMessage("Are you sure you want to Permanently delete this file?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deletePermanently(deleteTimestamp, uid);
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();

                    }
                });
                holder.download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast toast = Toast.makeText(context, "Restore file first before downloading", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                });


                break;
       /* else if (type.equals("VIDEO")){
            holder.avatarIv.setBackgroundResource(R.drawable.ic_video_icon);

            holder.restore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Restore File?")
                            .setMessage("Are you sure you want to restore this file?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    restore(timestamp,uid,type,deleteTimestamp,fileName,fileUri);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Delete Permanently?")
                            .setMessage("Are you sure you want to Permanently delete this file?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deletePermanently(deleteTimestamp,uid);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();

                }
            });

            holder.download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast = Toast.makeText(context,"Restore file first before downloading",Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
            });

        }*/
            case "PHOTO":
                holder.fileName.setText("Image_" + fileName);
                holder.avatarIv.setBackgroundResource(R.drawable.ic_image_icon);

                holder.restore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Restore File?")
                                .setMessage("Are you sure you want to restore this file?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
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
                                        restore(timestamp, uid, type, deleteTimestamp, fileName, fileUri, DeleteTypeReceived);}
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Delete Permanently?")
                                .setMessage("Are you sure you want to Permanently delete this file?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deletePermanently(deleteTimestamp, uid);

                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();

                    }
                });

                holder.download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast toast = Toast.makeText(context, "Restore file first before downloading", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();


                    }
                });
                break;
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDropDown(model,holder.moreBtn,timestamp,fileName,deleteTimestamp);
            }
        });
    }

    private void deletePermanently(String deleteTimestamp, String uid) {
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
            progress.setMessage("Deleting...");
            progress.setCancelable(false);
            progress.setCanceledOnTouchOutside(false);
            progress.show();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("Trash");
            Query query = dbRef.orderByChild("deleteTimestamp").equalTo(deleteTimestamp);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        ds.getRef().removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progress.dismiss();
                                        Toast.makeText(context, "Deleted Successfully...", Toast.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });}

    }

    private void restore(final String timestamp, final String uid, final String type, final String deleteTimestamp, final String fileName, final String fileUri, String DeleteTypeReceived) {
        final ProgressDialog progress = new ProgressDialog(context);progress.setMessage("Restoring...");
            progress.setCancelable(false);
            progress.setCanceledOnTouchOutside(false);
            progress.show();

            if (DeleteTypeReceived.equals("No")){
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Trash");
                Query query = dbRef.orderByChild("deleteTimestamp").equalTo(deleteTimestamp);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ds.getRef().removeValue();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uid", uid);
                            hashMap.put("fileName", fileName);
                            hashMap.put("fileUri", fileUri);
                            hashMap.put("timestamp", timestamp);
                            hashMap.put("deleteTimestamp", deleteTimestamp);

                            if (type.equals("AUDIO")){
                                hashMap.put("type", "AUDIO");
                            }
                            if (type.equals("PDF")){
                                hashMap.put("type", "PDF");
                            }
                            if (type.equals("PPT")){
                                hashMap.put("type", "PPT");
                            }
                            if (type.equals("DOC")){
                                hashMap.put("type", "DOC");
                            }
                            if (type.equals("EXCEL")){
                                hashMap.put("type", "EXCEL");
                            }
                            if (type.equals("PHOTO")){
                                hashMap.put("type", "PHOTO");
                            }

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(uid).child("Files").child(timestamp)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progress.dismiss();
                                            Toast.makeText(context, "Restored Successfully...", Toast.LENGTH_LONG).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }else {
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("Trash");
                Query query = dbRef.orderByChild("deleteTimestamp").equalTo(deleteTimestamp);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ds.getRef().removeValue();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uid", uid);
                            hashMap.put("fileName", fileName);
                            hashMap.put("fileUri", fileUri);
                            hashMap.put("timestamp", timestamp);
                            hashMap.put("deleteTimestamp", deleteTimestamp);

                            if (type.equals("AUDIO")){
                                hashMap.put("type", "AUDIO");
                            }
                            if (type.equals("PDF")){
                                hashMap.put("type", "PDF");
                            }
                            if (type.equals("PPT")){
                                hashMap.put("type", "PPT");
                            }
                            if (type.equals("DOC")){
                                hashMap.put("type", "DOC");
                            }
                            if (type.equals("EXCEL")){
                                hashMap.put("type", "EXCEL");
                            }
                            if (type.equals("PHOTO")){
                                hashMap.put("type", "PHOTO");
                            }

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(user.getUid()).child("Received Files").child(deleteTimestamp)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progress.dismiss();
                                            Toast.makeText(context, "Restored Successfully...", Toast.LENGTH_LONG).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
    }

    private void openDropDown(final UserTrashModel model, ImageButton dropDown, final String timestamp, final String fileName, final String deleteTimestamp) {
        final PopupMenu popupMenu = new PopupMenu(context, dropDown, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "File Details");

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        final String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    View view = LayoutInflater.from(context).inflate(R.layout.trash_file_details, null);
                    final TextView fileNmae = view.findViewById(R.id.fileName);
                    final TextView dateDeleted = view.findViewById(R.id.timestamp);

                    fileNmae.setText(fileName);


                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(deleteTimestamp));
                    final String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    dateDeleted.setText(dateTime);


                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                    builder.setView(view);

                    final android.app.AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();

    }

    @Override
    public int getItemCount() {
        return trashModel.size();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Permission Granted Successfully Proceed to download", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(context, "Permission Denied...", Toast.LENGTH_LONG).show();

            }
        }
    }

    public static class HolderUserTrash extends RecyclerView.ViewHolder {

        private ImageView avatarIv;
        private TextView fileName,restore,download, delete;
        private ImageButton moreBtn;
        public HolderUserTrash(@NonNull View itemView) {
            super(itemView);

            avatarIv=itemView.findViewById(R.id.avatarIv);
            fileName=itemView.findViewById(R.id.fileName);
            restore=itemView.findViewById(R.id.restore);
            download=itemView.findViewById(R.id.download);
            delete=itemView.findViewById(R.id.delete);
            moreBtn=itemView.findViewById(R.id.moreBtn);

        }
    }
}