package com.example.ezattendance;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.Calendar;


public class SheetActivity extends AppCompatActivity {

    private int rowSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet);
        long[] idArray = getIntent().getLongArrayExtra("idArray");

        rowSize = idArray.length + 1;

        showTable();
    }

    private void showTable() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        long[] idArray = getIntent().getLongArrayExtra("idArray");
        int[] rollArray = getIntent().getIntArrayExtra("rollArray");
        String[] nameArray = getIntent().getStringArrayExtra("nameArray");
        String month = getIntent().getStringExtra("month");

        int DAY_IN_MONTH = getDayInMonth(month);

        TableRow[] rows = new TableRow[rowSize];
        TextView[] roll_textview = new TextView[rowSize];
        TextView[] name_textview = new TextView[rowSize];
        TextView[][] status_textview = new TextView[rowSize][DAY_IN_MONTH + 1];

        for (int i = 0; i < rowSize; i++) {
            roll_textview[i] = new TextView(this);
            name_textview[i] = new TextView(this);
            for (int j = 1; j <= DAY_IN_MONTH; j++) {
                status_textview[i][j] = new TextView(this);
            }
        }

        //header
        roll_textview[0].setText("Roll");
        roll_textview[0].setTypeface(roll_textview[0].getTypeface(), Typeface.BOLD);
        name_textview[0].setText("Name");
        name_textview[0].setTypeface(name_textview[0].getTypeface(), Typeface.BOLD);
        for (int i = 1; i <= DAY_IN_MONTH; i++) {
            status_textview[0][i].setText(String.valueOf(i));
            status_textview[0][i].setTypeface(status_textview[0][i].getTypeface(), Typeface.BOLD);

        }

        for (int i = 1; i < rowSize; i++) {
            roll_textview[i].setText(String.valueOf(rollArray[i - 1]));
            name_textview[i].setText(nameArray[i - 1]);

            for (int j = 1; j <= DAY_IN_MONTH; j++) {
                String day = month.substring(0, 3) + " " + String.format("%02d", j) + ", " + month.substring(4);

                LocalDate localDate = LocalDate.of(Integer.parseInt(month.substring(4)), getMonthIndex(month.substring(0, 3)), j);
                String formattedDate = localDate.format(DateTimeFormatter.ofPattern("MM.dd.yyyy"));

                String status = databaseHelper.getStatus(idArray[i - 1], formattedDate);
                status_textview[i][j].setText(status);
            }



        }

        for (int i = 0; i < rowSize; i++) {
            rows[i] = new TableRow(this);

            if (i % 2 == 0) {
                rows[i].setBackgroundColor(Color.parseColor("#EEEEEE"));
            } else {
                rows[i].setBackgroundColor(Color.parseColor("#E4E4E4"));

            }

            roll_textview[i].setPadding(16, 16, 16, 16);
            name_textview[i].setPadding(16, 16, 16, 16);

            rows[i].addView(roll_textview[i]);
            rows[i].addView(name_textview[i]);

            for (int j = 1; j <= DAY_IN_MONTH; j++) {
                status_textview[i][j].setPadding(16, 16, 16, 16);
                rows[i].addView(status_textview[i][j]);
            }

            tableLayout.addView(rows[i]);
        }
        tableLayout.setShowDividers(TableLayout.SHOW_DIVIDER_MIDDLE);


    }


    private int getDayInMonth(String month) {

        try {
            String[] parts = month.split("\\.");
            int year = Integer.parseInt(parts[1]);
            int monthIndex = getMonthIndex(parts[0].toUpperCase()); // Call a helper method to get the month index

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthIndex);
            calendar.set(Calendar.DAY_OF_MONTH, 1);

            return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Return 0 in case of an error
        }
    }

    private int getMonthIndex(String monthAbbreviation) {
        String[] monthsAbbreviations = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};

        for (int i = 0; i < monthsAbbreviations.length; i++) {
            if (monthsAbbreviations[i].equalsIgnoreCase(monthAbbreviation)) {
                return i;
            }
        }

        // Handle the case when the month abbreviation is not recognized
        throw new IllegalArgumentException("Invalid month abbreviation: " + monthAbbreviation);
    }


}





