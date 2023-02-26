package com.file.filecloud.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.file.filecloud.ModelClass.Files_Model;
import com.file.filecloud.viewMyPhoto;
import com.file.cloud.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PhotohAdapter extends RecyclerView.Adapter<PhotohAdapter.HolderPhoto> {


    private final Context context;
    private ArrayList<Files_Model> filesModel;

    public PhotohAdapter(Context context, ArrayList<Files_Model> filesModel) {
        this.context = context;
        this.filesModel = filesModel;
    }

    @NonNull
    @Override
    public HolderPhoto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.photo_row,parent,false);
        return new HolderPhoto(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderPhoto holder, final int position) {
        Files_Model model = filesModel.get(position);
        final String photo = model.getFileUri();
        final String uid =model.getUid();
        final String fileName = model.getFileName();
        final String timestamp = model.getTimestamp();
        final String type = model.getType();


            //////LOAD PROFILE IMAGE
            try {
                Picasso.get().load(photo).placeholder(R.drawable.ic_image_icon).into(holder.photoIv);

            }catch (Exception e){
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            ////VIEW PROFILE IMAGE
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, viewMyPhoto.class);
            intent.putExtra("myUid", uid);
            intent.putExtra("timestamp", timestamp);
            intent.putExtra("fileName", fileName);
            intent.putExtra("fileUri", photo);
            intent.putExtra("type", type);
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return filesModel.size();
    }



    public static class HolderPhoto extends RecyclerView.ViewHolder {

        private ImageView photoIv;

        public HolderPhoto(@NonNull View itemView) {
            super(itemView);

            photoIv=itemView.findViewById(R.id.photoIv);

        }
    }
}
