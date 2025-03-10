package com.example.filearchiver;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FileListActivity extends AppCompatActivity {

    static {
        System.loadLibrary("filearchiver"); // Загружаем нативную библиотеку
    }
    private ArrayList<Uri> fileUris;
    private FileAdapter fileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);

        RecyclerView fileRecyclerView = findViewById(R.id.fileRecyclerView);
        Button btnArchive = findViewById(R.id.archiveButton);

        fileUris = getIntent().getParcelableArrayListExtra("selectedFiles");

        // Настройка RecyclerView
        fileRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileAdapter = new FileAdapter(getFileItems());
        fileRecyclerView.setAdapter(fileAdapter);

        // Обработчик кнопки архивировации
        btnArchive.setOnClickListener(v -> {
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

    //Метод для вызова архивации
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
            // Убираем расширение файла
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
            Toast.makeText(this, "Файл успешно заархивирован: " + outputFilePath, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Ошибка при архивации файла", Toast.LENGTH_SHORT).show();
        }
    }
}

