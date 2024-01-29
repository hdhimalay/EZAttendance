package com.example.ezattendance;

import static java.lang.reflect.Array.get;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class StudentActivity extends AppCompatActivity {
    Toolbar toolbar;
    private String className;
    private String subjectName;
    private int position;
    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private DatabaseHelper databaseHelper;
    private long cid;
    private MyCalender calender;
    private TextView subtitle;

    private ArrayList<StudentItem> studentItems=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        calender=new MyCalender();
        databaseHelper=new DatabaseHelper(this);
        Intent intent=getIntent();
        className=intent.getStringExtra("className");
        subjectName=intent.getStringExtra("subjectName");
        position=intent.getIntExtra("position",-1);
        cid=intent.getLongExtra("cid",-1);
        setToolbar();
        loadData();

        recyclerView =findViewById(R.id.student_recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter= new StudentAdapter(this, studentItems);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(position->changeStatus(position) );
        loadStatusData();
    }

    private void loadData() {
        Cursor cursor = databaseHelper.getStudentTable(cid);
        studentItems.clear();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.S_ID));
            int roll = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.STUDENT_ROLL_KEY));
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.STUDENT_NAME_KEY));

            studentItems.add(new StudentItem(id, roll, name));
        }
//        int columnIndexId = cursor.getColumnIndex(DatabaseHelper.S_ID);
//        int columnIndexRoll = cursor.getColumnIndex(DatabaseHelper.STUDENT_ROLL_KEY);
//        int columnIndexName = cursor.getColumnIndex(DatabaseHelper.STUDENT_NAME_KEY);
//
//        while (cursor.moveToNext()) {
//            // Check if the column index is valid before using it
//            if (columnIndexId != -1 & columnIndexRoll != -1 && columnIndexName != -1) {
//                long id = cursor.getLong(columnIndexId);
//                int roll = cursor.getInt(columnIndexRoll);
//                String name = cursor.getString(columnIndexName);
//
//                studentItems.add(new StudentItem(id, roll, name));
//            } else {
//                // Handle the case where one of the column indexes is -1
//                // You might want to log an error, throw an exception, or handle it in a way that makes sense for your application
//                // For now, let's just log a message
//                Log.e("loadData", "One or more column indexes are -1");
//            }
//        }
        cursor.close();

    }

    private void changeStatus(int position) {
        String status= studentItems.get(position).getStatus();
        if(status.equals("P"))status="A";
        else status="P";

        studentItems.get(position).setStatus(status);
        adapter.notifyItemChanged(position);
    }

    private void setToolbar() {
        toolbar=findViewById(R.id.toolbar);
        TextView tittle=toolbar.findViewById(R.id.title_toolbar);
        ImageButton back=toolbar.findViewById(R.id.back);
        ImageButton save=toolbar.findViewById(R.id.save);
        save.setOnClickListener(v->saveStatus());

        tittle.setText(className);
        subtitle=toolbar.findViewById(R.id.subtitle_toolbar);

        subtitle.setText(subjectName+" | "+calender.getDate());
        back.setOnClickListener(v -> onBackPressed() );
        toolbar.inflateMenu(R.menu.student_menu);
        toolbar.setOnMenuItemClickListener(menuItem-> onMenuItemClick(menuItem));

    }

    private void saveStatus() {
        for (StudentItem studentItem : studentItems) {
            String status = studentItem.getStatus();

            // Check if the current status is "P" or "A" before updating
            if ("P".equals(status) || "A".equals(status)) {
                // Update the status
                long value = databaseHelper.updateStatus(studentItem.getSid(), calender.getDate(), status);

                // If the status is not updated (value == -1), insert the new status
                if (value == -1) {
                    databaseHelper.addStatus(studentItem.getSid(), cid, calender.getDate(), status);
                }else {
                    // Add a new status
                    databaseHelper.addStatus(studentItem.getSid(), cid, calender.getDate(), status);
                }
            }
        }

        // Show a toast message indicating that the status has been saved
        showToast("Status saved successfully");
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    }

    private void loadStatusData(){
        for(StudentItem studentItem: studentItems) {
            String status = databaseHelper.getStatus(studentItem.getSid(),calender.getDate());
            if(status!=null) studentItem.setStatus(status);
            else studentItem.setStatus("");
        }
        adapter.notifyDataSetChanged();
    }

    private boolean onMenuItemClick(MenuItem menuItem) {

        if(menuItem.getItemId() == R.id.add_student){
            showAddStudentDialog();
        }if(menuItem.getItemId() == R.id.show_calender){
            showCalender();
        }if(menuItem.getItemId() == R.id.show_attendance_sheet){
            openSheetList();
        }
        return true;
    }

    private void openSheetList() {
        long[] idArray=new long[studentItems.size()];
        int[] rollArray=new int[studentItems.size()];
        String[] nameArray=new String[studentItems.size()];

        //loops
        for(int i=0;i<idArray.length;i++){
            idArray[i]=studentItems.get(i).getSid();
        }for(int i=0;i<rollArray.length;i++){
            rollArray[i]=studentItems.get(i).getRoll();
        }for(int i=0;i<nameArray.length;i++){
            nameArray[i]=studentItems.get(i).getName();
        }

        Intent intent =new Intent(this, SheetListActivity.class);
        intent.putExtra("cid",cid);
        intent.putExtra("idArray",idArray);
        intent.putExtra("rollArray",rollArray);
        intent.putExtra("nameArray", nameArray);
        startActivity(intent);
    }

    private void showCalender() {

        calender.show(getSupportFragmentManager(),"");
        calender.setOnCalenderOkClickListener(this::onCalenderOkClicked);
    }

    private void onCalenderOkClicked(int year, int month, int day) {
        calender.setDate(year,month,day);
        subtitle.setText(subjectName+" | "+calender.getDate());
        loadStatusData();
    }

    private void showAddStudentDialog() {
        MyDialog dialog=new MyDialog();
        dialog.show(getSupportFragmentManager(),MyDialog.STUDENT_ADD_DIALOG);
        dialog.setListener((roll,name)-> addStudent(roll,name));

    }

    private void addStudent(String roll_string, String name) {
        int roll=Integer.parseInt(roll_string);
        long sid=databaseHelper.addStudent(cid, roll, name);
        StudentItem studentItem=new StudentItem(sid, roll, name);
        studentItems.add(studentItem);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        //int position = item.getOrder(); // Assuming the order represents the position in your list

        switch (item.getItemId()){
            case 0:
                showUpdateStudentDialog(item.getGroupId());
                break;
            case 1:
                deleteStudent(item.getGroupId());
        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateStudentDialog(int position) {
        MyDialog dialog=new MyDialog(studentItems.get(position).getRoll(),studentItems.get(position).getName());
        dialog.show(getSupportFragmentManager(),MyDialog.STUDENT_UPDATE_DIALOG);
        dialog.setListener((roll_string,name)->updateStudent(position,name));
    }

    private void updateStudent(int position, String name) {
        databaseHelper.updateStudent(studentItems.get(position).getSid(),name);
        studentItems.get(position).setName(name);
        adapter.notifyItemChanged(position);
    }

    private void deleteStudent(int position) {
        databaseHelper.deleteStudent(studentItems.get(position).getSid());
        studentItems.remove(position);
        adapter.notifyItemRemoved(position);
    }
}