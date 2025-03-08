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
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedFiles.clear();
                        selectedFiles.add(uri);

                        Toast.makeText(this, "Выбран файл: " + uri.getLastPathSegment(), Toast.LENGTH_SHORT).show();

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
        filePickerLauncher.launch(intent);
    }


}