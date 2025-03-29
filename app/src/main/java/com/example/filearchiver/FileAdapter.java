package com.example.filearchiver;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<FileItem> files;

    public FileAdapter(List<FileItem> files) {
        this.files = files;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem file = files.get(position);
        holder.fileName.setText(file.getName());
        holder.fileSize.setText(file.getSize());
        holder.fileIcon.setImageResource(file.getIconResId());

        if (file.isArchived()) {
            holder.fileStatus.setText("• Archived");
            holder.fileStatus.setVisibility(View.VISIBLE);
            holder.fileName.setAlpha(0.5f);
            holder.fileIcon.setAlpha(0.5f);
            holder.fileSize.setAlpha(0.5f);
        }
    }

    public void setItemArchived(int position, boolean isArchived) {
        if (position >= 0 && position < files.size()) {
            files.get(position).setArchived(isArchived);
            notifyItemChanged(position);
        }
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public int findItemPosition(Uri fileUri) {
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).getUri().equals(fileUri)) {
                return i;
            }
        }
        return -1;
    }

    public void removeItem(int position) {
        files.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, files.size() - position);
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        TextView fileSize;
        TextView fileStatus;
        ImageView fileIcon;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            fileSize = itemView.findViewById(R.id.fileSize);
            fileIcon = itemView.findViewById(R.id.fileIcon);
            fileStatus = itemView.findViewById(R.id.fileStatus);
        }
    }

    public void setFileItems(List<FileItem> fileItems) {
        this.files = fileItems;
    }

    public List<FileItem> getFileItems(){
        return this.files;
    }
}
