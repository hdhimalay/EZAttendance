package com.example.ezattendance;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SheetListActivity extends AppCompatActivity {
    private ListView sheetList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listItems = new ArrayList<>();
    private long classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet_list);

        classId = getIntent().getLongExtra("cid", -1);
        loadListItems();

        sheetList = findViewById(R.id.sheetList);
        adapter = new ArrayAdapter<>(this, R.layout.sheet_list, R.id.date_list_item, listItems);
        sheetList.setAdapter(adapter);

        sheetList.setOnItemClickListener((adapterView, view, position, l) -> {
            Log.d("SheetListActivity", "Item clicked at position: " + position);
            openSheetActivity(position);
            generatePDF(listItems.get(position));
        });

    }

    private void openSheetActivity(int position) {
        long[] idArray = getIntent().getLongArrayExtra("idArray");
        int[] rollArray = getIntent().getIntArrayExtra("rollArray");
        String[] nameArray = getIntent().getStringArrayExtra("nameArray");

        Intent intent = new Intent(this, SheetActivity.class);
        intent.putExtra("idArray", idArray);
        intent.putExtra("rollArray", rollArray);
        intent.putExtra("nameArray", nameArray);
        intent.putExtra("month", listItems.get(position));

        startActivity(intent);
    }

    private void loadListItems() {
        try (Cursor cursor = new DatabaseHelper(this).getDistinctMonths(classId)) {
            while (cursor.moveToNext()) {
                String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DATE_KEY));
                listItems.add(date.substring(0, 3) + "." + date.substring(date.length() - 4));
            }
        }
    }

    private void generatePDF(String month) {
        Log.d("SheetListActivity", "Generating PDF for month: " + month);
        Document document = new Document();

        try {
            String filePath = getExternalFilesDir(null) + "/" + month + "_attendance_sheet.pdf";
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Add title
            document.add(new Paragraph("Attendance Sheet for " + month));

            // Create a table with columns: Roll Number, Name, Date, Status
            PdfPTable table = new PdfPTable(4);
            table.addCell("Roll Number");
            table.addCell("Name");
            table.addCell("Date");
            table.addCell("Status");

            // Retrieve attendance data for the given month
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            Cursor cursor = dbHelper.getAttendanceDataForMonth(classId, month);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        int rollNumber = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.STUDENT_ROLL_KEY));
                        String studentName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.STUDENT_NAME_KEY));
                        String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DATE_KEY));
                        String status = cursor.getString(cursor.getColumnIndex(DatabaseHelper.STATUS_KEY));

                        // Add a row for each entry in the attendance data
                        Log.d("SheetListActivity", "Adding row: Roll=" + rollNumber + ", Name=" + studentName + ", Date=" + date + ", Status=" + status);
                        table.addCell(String.valueOf(rollNumber));
                        table.addCell(studentName);
                        table.addCell(date);
                        table.addCell(status);

                    } while (cursor.moveToNext());
                } else {
                    Log.d("SheetListActivity", "Cursor is empty for month: " + month);
                }

                cursor.close();
            } else {
                Log.e("SheetListActivity", "Cursor is null for month: " + month);
            }

            document.add(table);
            document.close();

            Log.d("SheetListActivity", "PDF generated successfully at: " + filePath);

            // Show notification
            showNotification("PDF Generated", "PDF loaded successfully", filePath);

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            Log.e("SheetListActivity", "Error generating PDF: " + e.getMessage());
        }
    }




    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channel_id", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String message, String filePath) {
        createNotificationChannel();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(this, "com.example.ezattendance.fileprovider", file);

        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }







}
