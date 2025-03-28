package com.example.filearchiver;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;


public class FileListActivity extends AppCompatActivity {

    static {
        System.loadLibrary("filearchiver");
    }
    private ArrayList<Uri> fileUris;
    private FileAdapter fileAdapter;
    private LinearProgressIndicator progressBar;
    private TextView progressText;
    private Button btnArchive;

    private int totalFiles; // Общее количество файлов
    private int filesArchived = 0; // Количество заархивированных файлов

    private boolean isArchiving = false; // Флаг режима загрузки
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

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

    private final ActivityResultLauncher<Intent> folderPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri folderUri = result.getData().getData();
                    if (folderUri != null) {
                        getContentResolver().takePersistableUriPermission(
                                folderUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        );

                        // Обновляем UI
                        setArchiving(true);
                        // Архивируем файлы
                        for (Uri fileUri : fileUris) {
                            compressSelectedFile(fileUri, folderUri);
                        }
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
            if (fileUris != null && !fileUris.isEmpty()) {
                openFolderPicker();
            } else {
                Toast.makeText(this, "Файлы не выбраны", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file_list, menu);
        MenuItem addFileItem = menu.findItem(R.id.action_add_file);
        addFileItem.setVisible(!isArchiving);
        return true;
    }


    // Метод для включения/выключения режима загрузки
    private void setArchiving(boolean archiving) {
        isArchiving = archiving;
        if(archiving){
            filesArchived = 0;
            totalFiles = fileUris != null ? fileUris.size() : 0;
            progressBar.setProgress(0);
            progressText.setText("File " +  "0/" + totalFiles);
            btnArchive.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
        }
        else {
            progressBar.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            btnArchive.setVisibility(View.VISIBLE);
            fileUris.clear();
        }
        invalidateOptionsMenu();
    }


    // Метод для обновления прогресса
    public void updateFileProgress() {
        uiHandler.post(() -> {
            filesArchived++;
            int progress = (int) ((filesArchived * 100.0) / totalFiles);
            progressBar.setProgressCompat(progress, true);
            progressText.setText("File " + filesArchived + "/" + totalFiles);
            if (filesArchived == totalFiles) {
                progressBar.postDelayed(() -> {
                    setArchiving(false);
                }, 1000);
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
                fileItems.add(new FileItem(uri, fileName, fileSize, R.drawable.ic_file));
            }
        }
        return fileItems;
    }


    // Нативный метод для архивации файла
    public native boolean compressFile(
            String inputFilePath,
            String outputFilePath
    );


    private void compressSelectedFile(Uri fileUri, Uri folderUri) {
        new Thread(() -> {
            String inputFilePath = FileUtils.getPath(FileListActivity.this, fileUri);
            if (inputFilePath == null) {
                uiHandler.post(() -> Toast.makeText(FileListActivity.this, "Не удалось получить путь к файлу", Toast.LENGTH_SHORT).show());
                return;
            }

            String fileName = FileUtils.getFileName(FileListActivity.this, fileUri);
            if (fileName == null) {
                fileName = "archive";
            } else {
                int lastDotIndex = fileName.lastIndexOf('.');
                if (lastDotIndex != -1) {
                    fileName = fileName.substring(0, lastDotIndex);
                }
            }

            String tempOutputFilePath = new File(getCacheDir(), fileName + ".zip").getAbsolutePath();
            boolean success = compressFile(inputFilePath, tempOutputFilePath);

            uiHandler.post(() -> {
                int position = fileAdapter.findItemPosition(fileUri);
                if (position != -1) {
                    fileAdapter.removeItem(position);
                }
            });

            if (success) {
                moveArchiveToSelectedFolder(tempOutputFilePath, folderUri, fileName + ".zip");
            } else {
                uiHandler.post(() -> Toast.makeText(FileListActivity.this, "Ошибка при архивации файла", Toast.LENGTH_SHORT).show());
            }

            File file = new File(inputFilePath);
            if (file.exists()) {
                file.delete();
            }

        }).start();
    }


    // Метод для перемещения архива в выбранную папку
    private void moveArchiveToSelectedFolder(String tempFilePath, Uri folderUri, String archiveName) {
        File tempFile = new File(tempFilePath);
        if (!tempFile.exists()) {
            uiHandler.post(() -> Toast.makeText(FileListActivity.this, "Временный файл архива не найден", Toast.LENGTH_SHORT).show());
            return;
        }

        DocumentFile folder = DocumentFile.fromTreeUri(this, folderUri);
        if (folder != null && folder.isDirectory()) {
            DocumentFile archiveFile = folder.createFile("application/zip", archiveName);
            if (archiveFile != null) {
                try (InputStream inputStream = new FileInputStream(tempFile);
                     OutputStream outputStream = getContentResolver().openOutputStream(archiveFile.getUri())) {

                    if (outputStream != null) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                        uiHandler.post(() -> Toast.makeText(FileListActivity.this, "Файл успешно заархивирован: " + archiveName, Toast.LENGTH_SHORT).show());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    uiHandler.post(() -> Toast.makeText(FileListActivity.this, "Ошибка при перемещении архива", Toast.LENGTH_SHORT).show());
                }
            }
        }

        if (tempFile.delete()) {
            Log.i("FileArchiver", "Временный файл удален из кэша");
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

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        folderPickerLauncher.launch(intent);
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

        if (!fileUris.contains(fileUri)){
            fileUris.add(fileUri);
            fileAdapter.setFileItems(getFileItems());
            fileAdapter.notifyDataSetChanged();
            totalFiles++;
        }
    }
}