package com.example.filearchiver;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class FileListActivity extends AppCompatActivity {

    static {
        System.loadLibrary("filearchiver");
    }
    private ArrayList<Uri> fileUris;
    private FileAdapter fileAdapter;
    private ProgressBar progressBar;
    private TextView progressText;
    private Button btnArchive;

    private int totalFiles; // Общее количество файлов
    private int filesArchived = 0; // Количество заархивированных файлов


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);

        fileUris = getIntent().getParcelableArrayListExtra("selectedFiles");
        totalFiles = fileUris != null ? fileUris.size() : 0;

        // Настройка RecyclerView
        RecyclerView fileRecyclerView = findViewById(R.id.fileRecyclerView);
        fileRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileAdapter = new FileAdapter(getFileItems());
        fileRecyclerView.setAdapter(fileAdapter);

        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        btnArchive = findViewById(R.id.archiveButton);

        // Обработчик кнопки архивировации
        btnArchive.setOnClickListener(v -> {
            btnArchive.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            progressText.setVisibility(View.VISIBLE);
            progressText.setText("File " +  "0/" + totalFiles);
            filesArchived = 0;

            // Логика архивации
            if (fileUris != null && !fileUris.isEmpty()) {
                for (Uri fileUri : fileUris) {
                    compressSelectedFile(fileUri);
                }
            } else {
                // Обработка случая, когда список пустой
                Toast.makeText(this, "Файлы не выбраны", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Метод для обновления прогресса
    public void updateFileProgress() {
        filesArchived++;
        int progress = (int) ((filesArchived * 100.0) / totalFiles); // Прогресс в процентах
        progressBar.setProgress(progress);
        progressText.setText("File " +  filesArchived + "/" + totalFiles);
        if (filesArchived == totalFiles) {
            progressBar.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            btnArchive.setVisibility(View.VISIBLE);
        }
    }


    // Метод для создания списка FileItem
    private ArrayList<FileItem> getFileItems() {
        ArrayList<FileItem> fileItems = new ArrayList<>();
        if (fileUris != null) {
            for (Uri uri : fileUris) {
                String fileName = FileUtils.getFileName(this, uri);
                String fileSize = FileUtils.getFileSize(this, uri);
                fileItems.add(new FileItem(fileName, fileSize, R.drawable.ic_image)); // Иконка для всех файлов
            }
        }
        return fileItems;
    }


    // Нативный метод для архивации файла
    public native boolean compressFile(
            String inputFilePath,
            String outputFilePath
    );


    // Метод для вызова архивации
    private void compressSelectedFile(Uri fileUri) {
        // Получаем путь к выбранному файлу
        String inputFilePath = FileUtils.getPath(this, fileUri);
        if (inputFilePath == null) {
            Toast.makeText(this, "Не удалось получить путь к файлу", Toast.LENGTH_SHORT).show();
            return;
        }

        // Получаем имя файла
        String fileName = FileUtils.getFileName(this, fileUri);
        if (fileName == null) {
            fileName = "archive";
        } else {
            int lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex != -1) {
                fileName = fileName.substring(0, lastDotIndex);
            }
        }

        // Путь для сохранения архива
        String outputFilePath = getFilesDir() + "/" + fileName + ".zip";

        // Вызываем нативный метод для архивации
        boolean success = compressFile(inputFilePath, outputFilePath);

        if (success) {
            Toast.makeText(this, "Файл успешно заархивирован: " + outputFilePath, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ошибка при архивации файла", Toast.LENGTH_SHORT).show();
        }
    }
}