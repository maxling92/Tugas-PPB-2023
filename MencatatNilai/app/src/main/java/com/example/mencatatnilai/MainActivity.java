package com.example.mencatatnilai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText nama ,nrp;
    private Button simpan ,ambil;
    private SQLiteDatabase db1;
    private SQLiteOpenHelper Opendb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nama = (EditText) findViewById(R.id.nama);
        nrp = (EditText) findViewById(R.id.nrp);
        simpan = (Button) findViewById(R.id.simpan);
        ambil = (Button) findViewById(R.id.ambil);
        simpan.setOnClickListener(operasi);
        ambil.setOnClickListener(operasi);

        Opendb = new SQLiteOpenHelper(this,"db.sql",null,1) {
            @Override
            public void onCreate(SQLiteDatabase db) {}

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
        };
        db1=Opendb.getWritableDatabase();
        db1.execSQL("create table if not exists mhs(nama TEXT, nrp TEXT);");
    }
    @Override
    protected void onStop(){
        db1.close();
        Opendb.close();
        super.onStop();
    }

    View.OnClickListener operasi = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.ambil){
                ambil();
            }
            else if (view.getId() == R.id.simpan) {
                simpan();
            }
        }


    };
    private void simpan()
    {
        ContentValues datamhs = new ContentValues();

        datamhs.put("nama",nama.getText().toString());
        datamhs.put("nrp",nrp.getText().toString());
        db1.insert("mahasiswa",null,datamhs);
        Toast.makeText(this,"Data telah disimpan",Toast.LENGTH_LONG).show();
    }

    private void ambil(){
        Cursor cur = db1.rawQuery("SELECT * FROM mahasiswa WHERE nrp='" +
                nrp.getText().toString() + "'", null);

        if(cur.getCount() >0){
            Toast.makeText(this,"Data Ditemukan Sejumlah " +
                    cur.getCount(),Toast.LENGTH_LONG).show();
            cur.moveToFirst();
            int columnIndex = cur.getColumnIndex("nama");
            if (columnIndex != -1) {
                nama.setText(cur.getString(columnIndex));
            }
        }
        else
            Toast.makeText(this,"Data Tidak Ada", Toast.LENGTH_LONG).show();
    }
}