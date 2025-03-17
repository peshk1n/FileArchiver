package com.example.filearchiver;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import androidx.activity.result.ActivityResultLauncher;

import androidx.activity.result.contract.ActivityResultContracts;
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

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        // Пользователь выбрал несколько файлов
                        ClipData clipData = data.getClipData();
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri fileUri = clipData.getItemAt(i).getUri();
                            addFileToList(fileUri);
                        }
                    } else if (data.getData() != null) {
                        // Пользователь выбрал один файл
                        Uri fileUri = data.getData();
                        addFileToList(fileUri);
                    }
                }
            });


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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file_list, menu);
        return true;
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
                fileItems.add(new FileItem(fileName, fileSize, R.drawable.ic_file)); // Иконка для всех файлов
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


    // Обработка нажатия на кнопку в меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_file) {
            openFilePicker();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filePickerLauncher.launch(intent);
    }

    private void addFileToList(Uri fileUri) {
        if (fileUris == null) {
            fileUris = new ArrayList<>();
        }
        fileUris.add(fileUri);
        fileAdapter.setFileItems(getFileItems());
        fileAdapter.notifyDataSetChanged();
    }
}