package com.example.formpenilaian;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText editTextNilaiTes1;
    private EditText editTextNilaiTes2;
    private EditText editTextNilaiETS;
    private EditText editTextNilaiEAS;
    private EditText editTextNilaiHuruf;

    private Button buttonInputData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize your views
        editTextNilaiTes1 = findViewById(R.id.editTextNilaiTes1);
        editTextNilaiTes2 = findViewById(R.id.editTextNilaiTes2);
        editTextNilaiETS = findViewById(R.id.editTextNilaiETS);
        editTextNilaiEAS = findViewById(R.id.editTextNilaiEAS);
        editTextNilaiHuruf = findViewById(R.id.editTextNilaiHuruf);

        buttonInputData = findViewById(R.id.buttonInputData);

        // Set click listener for the "Input Data" button
        buttonInputData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateAndDisplayResult();
            }
        });
    }

    private void calculateAndDisplayResult() {
        // Retrieve values from EditText fields
        String nilaiTes1Str = editTextNilaiTes1.getText().toString();
        String nilaiTes2Str = editTextNilaiTes2.getText().toString();
        String nilaiETSStr = editTextNilaiETS.getText().toString();
        String nilaiEASStr = editTextNilaiEAS.getText().toString();

        // Check if any field is empty
        if (nilaiTes1Str.isEmpty() || nilaiTes2Str.isEmpty() || nilaiETSStr.isEmpty() || nilaiEASStr.isEmpty()) {
            Toast.makeText(this, "Please enter all scores", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse values to double
        double nilaiTes1 = Double.parseDouble(nilaiTes1Str);
        double nilaiTes2 = Double.parseDouble(nilaiTes2Str);
        double nilaiETS = Double.parseDouble(nilaiETSStr);
        double nilaiEAS = Double.parseDouble(nilaiEASStr);

        // Calculate average (Nilai Rata)
        double nilaiRata = (nilaiTes1 + nilaiTes2 + nilaiETS + nilaiEAS) / 4.0;

        // Display the average in the corresponding EditText field
        String nilaiRataStr = String.format("%.2f", nilaiRata);
        editTextNilaiHuruf.setText(nilaiRataStr);

        // Convert Nilai Rata to Nilai Huruf
        String nilaiHuruf;
        if (nilaiRata >= 0 && nilaiRata <= 20) {
            nilaiHuruf = "E";
        } else if (nilaiRata > 20 && nilaiRata <= 40) {
            nilaiHuruf = "D";
        } else if (nilaiRata > 40 && nilaiRata <= 60) {
            nilaiHuruf = "C";
        } else if (nilaiRata > 60 && nilaiRata <= 70) {
            nilaiHuruf = "BC";
        } else if (nilaiRata > 70 && nilaiRata <= 80) {
            nilaiHuruf = "B";
        } else if (nilaiRata > 80 && nilaiRata <= 90) {
            nilaiHuruf = "AB";
        } else {
            nilaiHuruf = "A";
        }

        // Display the result in the corresponding EditText field
        editTextNilaiHuruf.setText(nilaiHuruf);
    }
}