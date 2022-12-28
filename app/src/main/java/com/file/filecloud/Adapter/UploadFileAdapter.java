package com.file.filecloud.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.file.cloud.R;

import java.util.List;

public class UploadFileAdapter extends RecyclerView.Adapter<UploadFileAdapter.ViewHolder>{

    public List<String> fileNameList,status;

    public UploadFileAdapter(List<String> fileNameList, List<String> status) {
        this.fileNameList = fileNameList;
        this.status = status;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_file_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String filename = fileNameList.get(position);
        /*if (filename.length()>20)
            filename=filename.substring(0,20)+"...";*/

        holder.fileName.setText(filename);

        String fileStatus = status.get(position);


        if (fileStatus.equals("loading")){
            holder.progress.setVisibility(View.VISIBLE);
            holder.done.setVisibility(View.GONE);
        }

        else{
            holder.progress.setVisibility(View.GONE);
            holder.done.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return fileNameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView fileName;
        public ImageView avatarIv,done;
        public ProgressBar progress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            fileName = itemView.findViewById(R.id.fileName);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            done = itemView.findViewById(R.id.done);
            progress = itemView.findViewById(R.id.progress);
        }
    }
}
