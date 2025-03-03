package com.example.filearchiver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class FileListActivity extends AppCompatActivity {
    private ArrayList<Uri> fileUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);

        ListView fileListView = findViewById(R.id.fileListView);
        Button btnArchive = findViewById(R.id.archiveButton);

        fileUris = getIntent().getParcelableArrayListExtra("selectedFiles");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                getFileNames());
        fileListView.setAdapter(adapter);

        // Обработчик кнопки архивировации
        btnArchive.setOnClickListener(v -> {
            // Логика архивации
        });
    }


    private ArrayList<String> getFileNames() {
        ArrayList<String> names = new ArrayList<>();
        if (fileUris != null) {
            for (Uri uri : fileUris) {
                names.add(uri.getLastPathSegment());
            }
        }
        return names;
    }
}

