package com;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import net.ariflaksito.studentsdata.R;
import com.adapter.StudentAdapter;
import com.db.DbHelper;
import com.model.Student;

import java.util.ArrayList;

public class ListStudentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private ArrayList<Student> studentsArrayList;
    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_students);

        recyclerView = (RecyclerView) findViewById(R.id.rview);
        adapter = new StudentAdapter(this);

        dbHelper = new DbHelper(this);
        studentsArrayList = dbHelper.getAllUsers();
        adapter.setListStudents(studentsArrayList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ListStudentsActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        studentsArrayList = dbHelper.getAllUsers();
        adapter.setListStudents(studentsArrayList);
        adapter.notifyDataSetChanged();
    }
}