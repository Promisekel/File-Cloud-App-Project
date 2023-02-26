 package com.file.filecloud.Adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.file.filecloud.Firebase.NetworkConnection;
import com.file.filecloud.ModelClass.ModelReceivedFiles;
import com.file.filecloud.ModelClass.UserTrashModel;
import com.file.filecloud.RecievedFiles.PreviewRecievedFile;
import com.file.filecloud.Tabs.shareListActivity;
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
import java.util.concurrent.TimeUnit;

import static androidx.core.app.ActivityCompat.requestPermissions;


public class ReceivedFileAdapter extends RecyclerView.Adapter<ReceivedFileAdapter.HolderPDF>
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_CODE =1000 ;
    private Context context;
    private ArrayList<ModelReceivedFiles> modelreceivedFiles;

    public ReceivedFileAdapter(Context context, ArrayList<ModelReceivedFiles> modelreceivedFiles) {
        this.context = context;
        this.modelreceivedFiles = modelreceivedFiles;


    }

    @NonNull
    @Override
    public HolderPDF onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_row,parent,false);
        return new HolderPDF(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderPDF holder, final int position) {
        final ModelReceivedFiles model = modelreceivedFiles.get(position);
        final String fileName = model.getFileName();
        final String type = model.getType();
        final String timestamp = model.getTimestamp();
        final String fileUri = model.getFileUri();
        final String uid = model.getSender();

        holder.fileName.setText(fileName);

        /*if (type.equals("AUDIO")){
            holder.preview.setText("PLAY");
        }*/

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRenameDialog(timestamp,type,fileName,uid,holder.moreBtn);

            }
        });

        switch (type) {
            case "PDF":
                holder.avatarIv.setImageResource(R.drawable.ic_pdf_icon);

                holder.download.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onClick(View v) {

                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions((AppCompatActivity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                        } else {

                                startDownload(fileUri, fileName, uid, timestamp, type);
                        }
                    }
                });

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Send to trash?")
                                .setMessage("Are you sure you want to move this file to trash?")
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
                                        begindelete(uid, timestamp, fileName, fileUri, type);}
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });

                holder.preview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, PreviewRecievedFile.class);
                        intent.putExtra("timestamp", timestamp);
                        intent.putExtra("fileUri", fileUri);
                        intent.putExtra("fileName", fileName);
                        context.startActivity(intent);
                    }
                });
                holder.share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareFile(context, uid, timestamp, fileName, fileUri, type);

                    }
                });


                break;
            case "EXCEL":
                holder.avatarIv.setImageResource(R.drawable.ic_excel_icon);


                holder.preview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, PreviewRecievedFile.class);
                        intent.putExtra("timestamp", timestamp);
                        intent.putExtra("fileUri", fileUri);
                        intent.putExtra("fileName", fileName);
                        context.startActivity(intent);
                    }
                });
                holder.download.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onClick(View v) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions((AppCompatActivity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                        } else {
                            startDownload(fileUri, fileName, uid, timestamp, type);
                        }
                    }
                });

                holder.share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareFile(context, uid, timestamp, fileName, fileUri, type);

                    }
                });

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Send to trash?")
                                .setMessage("Are you sure you want to move this file to trash?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        begindelete(uid, timestamp, fileName, fileUri, type);
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });


                break;
            case "PPT":
                holder.avatarIv.setImageResource(R.drawable.ic_ppt_icon);


                holder.preview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, PreviewRecievedFile.class);
                        intent.putExtra("timestamp", timestamp);
                        intent.putExtra("fileUri", fileUri);
                        intent.putExtra("fileName", fileName);

                        context.startActivity(intent);
                    }
                });
                holder.download.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onClick(View v) {

                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions((AppCompatActivity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                        } else {
                            startDownload(fileUri, fileName, uid, timestamp, type);
                        }
                    }
                });

                holder.share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareFile(context, uid, timestamp, fileName, fileUri, type);
                    }
                });


                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Send to trash?")
                                .setMessage("Are you sure you want to move this file to trash?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        begindelete(uid, timestamp, fileName, fileUri, type);
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });

                break;
            case "DOC":
                holder.avatarIv.setImageResource(R.drawable.ic_doc_icon);

                holder.preview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, PreviewRecievedFile.class);
                        intent.putExtra("timestamp", timestamp);
                        intent.putExtra("fileUri", fileUri);
                        intent.putExtra("fileName", fileName);
                        context.startActivity(intent);
                    }
                });

                holder.download.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onClick(View v) {

                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions((AppCompatActivity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                        } else {
                            startDownload(fileUri, fileName, uid, timestamp, type);
                        }
                    }
                });

                holder.share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareFile(context, uid, timestamp, fileName, fileUri, type);
                    }
                });

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Send to trash?")
                                .setMessage("Are you sure you want to move this file to trash?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        begindelete(uid, timestamp, fileName, fileUri, type);
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });

                break;
        }

    }

    /*private void openPeerDialog(HolderPDF holder, final String type, final String uid, final String timestamp, final String fileName, final String fileUri) {
        View view = LayoutInflater.from(context).inflate(R.layout.peersactivity, null);
        final ImageView peersIconIv = view.findViewById(R.id.peersIconIv);
        final RecyclerView peersRv = view.findViewById(R.id.peersRv);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setView(view);

        final android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Share?")
                        .setMessage("Are you sure you want to share this file with ")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ///share(uid,timestamp,fileName,fileUri,type);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

    }*/
        ////OPEN RENAME RECEIVED FILE DIALOG
    private void openRenameDialog(final String timestamp, final String type, final String fileName, final String uid, ImageButton moreBtn) {
        final PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Edit File name");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "View Details");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == 0) {
                    View view = LayoutInflater.from(context).inflate(R.layout.edit_file_name, null);
                    final Button uploadBtn = view.findViewById(R.id.uploadBtn);
                    final EditText fileNamEt = view.findViewById(R.id.fileNamEt);

                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                    builder.setView(view);

                    final android.app.AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    fileNamEt.setText(fileName);
                    fileNamEt.setInputType(InputType.TYPE_CLASS_TEXT);

                    uploadBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                if (!TextUtils.isEmpty(fileName))

                                {
                                    String fileName = fileNamEt.getText().toString().trim();

                                    switch (type) {
                                        case "DOC":
                                        case "PDF":
                                        case "PPT":
                                        case "AUDIO":
                                        case "VIDEO":
                                        case "EXCEL":
                                            sendFile(timestamp, fileName);
                                            break;
                                    }
                                }
                                else{
                                    Toast.makeText(context, "Please Enter a file name", Toast.LENGTH_SHORT).show();

                                }

                        }

                        private void sendFile(String timestamp, String fileName) {
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
                            final ProgressDialog progress= new ProgressDialog(context);
                            progress.setMessage("Updating file name...");
                            progress.show();
                            progress.setCanceledOnTouchOutside(false);
                            progress.setCancelable(false);
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("fileName", fileName);
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            FirebaseUser user=auth.getCurrentUser();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(user.getUid()).child("Received Files").child(timestamp).updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progress.dismiss();
                                            alertDialog.dismiss();
                                            Toast.makeText(context,"File name updated successfully", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progress.dismiss();
                                            alertDialog.dismiss();
                                            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_LONG).show();

                                        }
                                    });}
                        }
                    });

                }
                if (id == 1){
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(uid)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds: snapshot.getChildren()){
                                        String nicki = "" + ds.child("nicki").getValue();

                                        View view = LayoutInflater.from(context).inflate(R.layout.file_detail_layout, null);
                                        final TextView file = view.findViewById(R.id.fileName);
                                        final TextView dateSent = view.findViewById(R.id.timestamp);
                                        final TextView sender = view.findViewById(R.id.sender);

                                        file.setText(fileName);
                                        sender.setText(nicki);

                                        Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                        calendar.setTimeInMillis(Long.parseLong(timestamp));
                                        final String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                                        dateSent.setText(dateTime);


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


        ///DOWNLOAD RECEIVED FILES DB
    private void startDownload(final String fileUri, final String fileName, String uid, String timestamp, final String type) {
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
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Received Files");
            Query query = dbRef.orderByChild("timestamp").equalTo(timestamp);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
                    Uri uri = Uri.parse(fileUri);
                    DownloadManager.Request request = new DownloadManager.Request(uri);

                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    switch (type) {
                        case "PDF":
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS, "/File Cloud/File Cloud Documents_" + fileName + ".pdf");
                            downloadManager.enqueue(request);
                            break;
                        case "DOC":
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS, "/File Cloud/File Cloud Documents_" + fileName + ".docx");
                            downloadManager.enqueue(request);
                            break;
                        case "EXCEL":
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS, "/File Cloud/File Cloud Documents_" + fileName + ".xlx");
                            downloadManager.enqueue(request);
                            break;
                        case "PPT":
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS, "/File Cloud/File Cloud Documents_" + fileName + ".ppt");
                            downloadManager.enqueue(request);
                            break;
                    }
                    Toast toast = Toast.makeText(context, "Downloading to Documents/File Cloud/", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

        ////DELETE RECEIVED FILE FROM DB
    private void  begindelete(final String uid, final String timestamp, final String fileName, final String fileUri, final String type) {
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
            progress.setMessage("moving...");
            progress.setCancelable(false);
            progress.setCanceledOnTouchOutside(false);
            progress.show();

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            final FirebaseUser user = firebaseAuth.getCurrentUser();

            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("Received Files");
            Query query = dbRef.orderByChild("timestamp").equalTo(timestamp);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        ds.getRef().removeValue();
                        String Timestamp = String.valueOf(System.currentTimeMillis());


                        Long time = System.currentTimeMillis();  //get time in millis
                        long cutoffDeleteTime = time + TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS);///calculates next auto delete time in DAYS

                        String cutoffDeleteTimer = Long.toString(cutoffDeleteTime);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("uid", uid);
                        hashMap.put("fileName", fileName);
                        hashMap.put("fileUri", fileUri);
                        hashMap.put("timestamp", timestamp);
                        hashMap.put("deleteTimestamp", Timestamp);
                        hashMap.put("cutoffTimer", cutoffDeleteTimer);
                        hashMap.put("deleteTypeReceived", "Yes");

                        if (type.equals("AUDIO")) {
                            hashMap.put("type", "AUDIO");
                        }
                        if (type.equals("PDF")) {
                            hashMap.put("type", "PDF");
                        }
                        if (type.equals("PPT")) {
                            hashMap.put("type", "PPT");
                        }
                        if (type.equals("DOC")) {
                            hashMap.put("type", "DOC");
                        }
                        if (type.equals("EXCEL")) {
                            hashMap.put("type", "EXCEL");
                        }

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(user.getUid()).child("Trash").child(Timestamp)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progress.dismiss();
                                        Toast.makeText(context, "Moved Successfully...", Toast.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    /////SHARE RECEIVED FILE WITH PEER
    private void shareFile(Context context, String uid, String timestamp, String fileName, String fileUri, String type) {
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
        Intent intent = new Intent(context, shareListActivity.class);
        intent.putExtra("myUid", uid);
        intent.putExtra("timestamp", timestamp);
        intent.putExtra("fileName", fileName);
        intent.putExtra("fileUri", fileUri);
        intent.putExtra("type", type);
        context.startActivity(intent);}

    }

    @Override
    public int getItemCount() {
        return modelreceivedFiles.size();
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

    public static class HolderPDF extends RecyclerView.ViewHolder {

        private ImageView avatarIv;
        private TextView fileName,preview,download,delete, share;
        private ImageButton moreBtn;


        public HolderPDF(@NonNull View itemView) {
            super(itemView);
            avatarIv=itemView.findViewById(R.id.avatarIv);
            fileName=itemView.findViewById(R.id.fileName);
            preview=itemView.findViewById(R.id.preview);
            download=itemView.findViewById(R.id.download);
            delete=itemView.findViewById(R.id.delete);
            share=itemView.findViewById(R.id.share);
            moreBtn=itemView.findViewById(R.id.moreBtn);
        }
    }
}
