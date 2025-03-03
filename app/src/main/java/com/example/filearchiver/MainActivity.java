package com.example.filearchiver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.filearchiver.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'filearchiver' library on application startup.
    static {
        System.loadLibrary("filearchiver");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    /**
     * A native method that is implemented by the 'filearchiver' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}