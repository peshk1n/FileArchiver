package com.example.filearchiver;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

                    for (Uri uri : selectedFiles) {
                        Toast.makeText(this, "Выбран файл: " + uri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
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
}