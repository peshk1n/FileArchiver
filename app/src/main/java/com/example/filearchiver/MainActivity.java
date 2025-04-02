package com.example.filearchiver;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import com.example.filearchiver.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("filearchiver");
    }

    private ActivityMainBinding binding;
    private final List<Uri> selectedFiles = new ArrayList<>();

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    // Очищаем список перед добавлением новых файлов
                    selectedFiles.clear();

                    // Проверяем, выбраны ли несколько файлов
                    if (data.getClipData() != null) {
                        // Если выбрано несколько файлов
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            selectedFiles.add(uri);
                        }
                    } else if (data.getData() != null) {
                        // Если выбран один файл
                        Uri uri = data.getData();
                        selectedFiles.add(uri);
                    }

                    // Переходим на экран со списком файлов
                    if (!selectedFiles.isEmpty()) {
                        Intent intent = new Intent(MainActivity.this, FileListActivity.class);
                        intent.putParcelableArrayListExtra("selectedFiles", new ArrayList<>(selectedFiles));
                        startActivity(intent);
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestNotificationPermission();
        createNotificationChannel();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.selectFilesButton.setOnClickListener(v -> openFilePicker());
    }


    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filePickerLauncher.launch(intent);
    }
    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "archive_progress";

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Архивация файлов",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}