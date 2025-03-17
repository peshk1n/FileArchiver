package com.example.filearchiver;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import android.database.Cursor;
import android.provider.OpenableColumns;

public class FileUtils {

    // Метод для получения имени файла из Uri
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (displayNameIndex != -1) {
                        result = cursor.getString(displayNameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    // Метод для получения пути к файлу из Uri
    public static String getPath(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        String fileName = getFileName(context, uri);
        if (fileName == null) {
            fileName = "temp_file";
        }

        File file = new File(context.getCacheDir(), fileName);
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(file)) {
            if (inputStream == null) {
                return null;
            }

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    // Метод для получения размера файла
    public static String getFileSize(Context context, Uri uri) {
        long fileSizeInBytes = 0;

        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex != -1) {
                        fileSizeInBytes = cursor.getLong(sizeIndex);
                    }
                }
            }
        } else {
            File file = new File(uri.getPath());
            if (file.exists()) {
                fileSizeInBytes = file.length();
            }
        }

        return formatFileSize(fileSizeInBytes);
    }

    // Метод для форматирования размера файла
    private static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes <= 0) {
            return "0 B";
        }

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(sizeInBytes) / Math.log10(1024));

        digitGroups = Math.min(digitGroups, units.length - 1);
        
        double size = sizeInBytes / Math.pow(1024, digitGroups);
        return String.format("%.1f %s", size, units[digitGroups]);
    }
}