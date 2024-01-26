package com.example.ezattendance;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassviewHolder> {
    ArrayList<ClassItem> classItems;
    Context context;


    private OnItemClickListener onItemClickListener;
    public interface OnItemClickListener{
        void onClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ClassAdapter(Context context, ArrayList<ClassItem> classItems) {
        this.classItems = classItems;
        this.context=context;
    }


    public static class ClassviewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView className;
        TextView subjectName;
        public ClassviewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            className=itemView.findViewById(R.id.class_textview);
            subjectName=itemView.findViewById(R.id.subject_textview);
            itemView.setOnClickListener(v -> onItemClickListener.onClick(getAdapterPosition()) );
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.add(getAdapterPosition(),0, 0,"EDIT");
            contextMenu.add(getAdapterPosition(),1,0,"DELETE");

        }
    }

    @NonNull
    @Override
    public ClassviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView =LayoutInflater.from(parent.getContext()).inflate(R.layout.class_item,parent,false);
        return new ClassviewHolder(itemView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassviewHolder holder, int position) {
        holder.className.setText(classItems.get(position).getClassName());
        holder.subjectName.setText(classItems.get(position).getSubjectName());
    }

    @Override
    public int getItemCount() {
        return classItems.size();
    }
}
