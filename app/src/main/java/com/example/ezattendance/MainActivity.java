package com.example.ezattendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton fab;
    RecyclerView recyclerView;
    ClassAdapter classAdapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<ClassItem>classItems=new ArrayList<>();

    Toolbar toolbar;
    DatabaseHelper databaseHelper;

    EditText class_edit;
    EditText subject_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper=new DatabaseHelper(this);

        fab=findViewById(R.id.fab_main);
        fab.setOnClickListener(v -> showDialog());

        loadData();

        recyclerView=findViewById(R.id.recylerview);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        classAdapter=new ClassAdapter(this,classItems);
        recyclerView.setAdapter(classAdapter);
        classAdapter.setOnItemClickListener(position -> gotoItemActivity(position));
        setToolbar();
    }

    private void loadData() {
        Cursor cursor = databaseHelper.getClassTable();

        classItems.clear();

        // Check if the column index is valid (not -1)
        int columnIndexId = cursor.getColumnIndex(DatabaseHelper.C_ID);
        int columnIndexClassName = cursor.getColumnIndex(DatabaseHelper.CLASS_NAME_KEY);
        int columnIndexSubjectName = cursor.getColumnIndex(DatabaseHelper.SUBJECT_NAME_KEY);

        while (cursor.moveToNext()) {
            // Check if the column index is valid before using it
            if (columnIndexId != -1 && columnIndexClassName != -1 && columnIndexSubjectName != -1) {
                int id = cursor.getInt(columnIndexId);
                String className = cursor.getString(columnIndexClassName);
                String subjectName = cursor.getString(columnIndexSubjectName);

                classItems.add(new ClassItem(id, className, subjectName));
            } else {
                // Handle the case where one of the column indexes is -1
                // You might want to log an error, throw an exception, or handle it in a way that makes sense for your application
                // For now, let's just log a message
                Log.e("loadData", "One or more column indexes are -1");
            }
        }
    }

    private void setToolbar() {
        toolbar=findViewById(R.id.toolbar);
        TextView tittle=toolbar.findViewById(R.id.title_toolbar);
        TextView subtitle=toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back=toolbar.findViewById(R.id.back);
        ImageButton save=toolbar.findViewById(R.id.save);

        tittle.setText("EZAttendance");
        subtitle.setVisibility(View.GONE);
        back.setVisibility(View.INVISIBLE);
        save.setVisibility(View.INVISIBLE);
    }

    private void gotoItemActivity(int position) {
        Intent intent=new Intent(this,StudentActivity.class);
        intent.putExtra("className",classItems.get(position).getClassName());
        intent.putExtra("subjectName",classItems.get(position).getSubjectName());
        intent.putExtra("position",position);
        startActivity(intent);
    }

    private void showDialog(){
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        View view= LayoutInflater.from(this).inflate(R.layout.add_class_dialog,null);
        builder.setView(view);
        AlertDialog dialog=builder.create();
        dialog.show();;
        class_edit=view.findViewById(R.id.class_edit);
        subject_edit=view.findViewById(R.id.subject_edit);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button cancel=view.findViewById(R.id.cancel_btn);
        Button add= view.findViewById(R.id.add_btn);

        cancel.setOnClickListener(v -> dialog.dismiss());
        add.setOnClickListener(v -> {
            addClass();
            dialog.dismiss();
        });



}

    private void addClass() {
        String className= class_edit.getText().toString();
        String subjectName =subject_edit.getText().toString();
        long cid=databaseHelper.addClass(className,subjectName);
        ClassItem classItem= new ClassItem(cid,className, subjectName);
        classItems.add(classItem);
        classAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case 0:
                showUpdateDialog(item.getGroupId());
                break;
            case 1:
                deleteClass(item.getGroupId());
        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(int position) {
        MyDialog dialog=new MyDialog();
        dialog.show(getSupportFragmentManager(),MyDialog.CLASS_UPDATE_DIALOG);
        dialog.setListener((className,subjectName)->updateClass(position,className,subjectName));
    }

    private void updateClass(int position, String className, String subjectName) {
        databaseHelper.updateClass(classItems.get(position).getCid(),className,subjectName);
        classItems.get(position).setClassName(className);
        classItems.get(position).setSubjectName(subjectName);
        classAdapter.notifyItemChanged(position);
    }

    private void deleteClass(int position) {
        databaseHelper.deleteClass(classItems.get(position).getCid());
        classItems.remove(position);
        classAdapter.notifyItemRemoved(position);
    }
}