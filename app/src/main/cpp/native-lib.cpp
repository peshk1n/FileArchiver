#include <jni.h>
#include <string>
#include <android/log.h>
#include "zlib.h"
#include <fstream>
#include <vector>
#include "minizip/zip.h"

#define LOG_TAG "FileArchiver"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Функция для обновления прогресса в Java
void updateFileProgress(JNIEnv* env, jobject thiz) {
    jclass clazz = env->GetObjectClass(thiz);
    jmethodID methodId = env->GetMethodID(clazz, "updateFileProgress", "()V");
    if (methodId == nullptr) {
        LOGE("Метод updateFileProgress не найден в Java-классе");
        return;
    }
    env->CallVoidMethod(thiz, methodId);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_filearchiver_FileListActivity_compressFile(
        JNIEnv* env,
        jobject thiz,
        jstring inputFilePath,
        jstring outputFilePath) {
    const char* inputPath = env->GetStringUTFChars(inputFilePath, nullptr);
    const char* outputPath = env->GetStringUTFChars(outputFilePath, nullptr);

    // Открываем ZIP-архив для записи
    zipFile zip = zipOpen(outputPath, APPEND_STATUS_CREATE);
    if (!zip) {
        LOGE("Не удалось создать ZIP-архив: %s", outputPath);
        env->ReleaseStringUTFChars(inputFilePath, inputPath);
        env->ReleaseStringUTFChars(outputFilePath, outputPath);
        return false;
    }

    // Открываем входной файл
    FILE* inputFile = fopen(inputPath, "rb");
    if (!inputFile) {
        LOGE("Не удалось открыть входной файл: %s", inputPath);
        zipClose(zip, nullptr);
        env->ReleaseStringUTFChars(inputFilePath, inputPath);
        env->ReleaseStringUTFChars(outputFilePath, outputPath);
        return false;
    }

    // Получаем имя файла для архива
    const char* fileName = strrchr(inputPath, '/');
    if (fileName) {
        fileName++; // Пропускаем '/'
    } else {
        fileName = inputPath; // Если путь не содержит '/', используем весь путь
    }

    // Добавляем файл в архив
    zip_fileinfo fileInfo = {};
    if (ZIP_OK != zipOpenNewFileInZip(zip, fileName, &fileInfo, nullptr, 0, nullptr, 0, nullptr, Z_DEFLATED, Z_DEFAULT_COMPRESSION)) {
        LOGE("Не удалось добавить файл в архив: %s", fileName);
        fclose(inputFile);
        zipClose(zip, nullptr);
        env->ReleaseStringUTFChars(inputFilePath, inputPath);
        env->ReleaseStringUTFChars(outputFilePath, outputPath);
        return false;
    }

    // Читаем и записываем данные
    char buffer[1024];
    size_t bytesRead;
    while ((bytesRead = fread(buffer, 1, sizeof(buffer), inputFile)) > 0) {
        if (ZIP_OK != zipWriteInFileInZip(zip, buffer, bytesRead)) {
            LOGE("Ошибка при записи данных в архив");
            fclose(inputFile);
            zipCloseFileInZip(zip);
            zipClose(zip, nullptr);
            env->ReleaseStringUTFChars(inputFilePath, inputPath);
            env->ReleaseStringUTFChars(outputFilePath, outputPath);
            return false;
        }
    }

    // Проверка на ошибки при чтении
    if (ferror(inputFile)) {
        LOGE("Ошибка при чтении файла: %s", inputPath);
        fclose(inputFile);
        zipCloseFileInZip(zip);
        zipClose(zip, nullptr);
        env->ReleaseStringUTFChars(inputFilePath, inputPath);
        env->ReleaseStringUTFChars(outputFilePath, outputPath);
        return false;
    }

    // Закрываем файл и архив
    fclose(inputFile);
    zipCloseFileInZip(zip);
    zipClose(zip, nullptr);

    LOGI("Файл успешно заархивирован: %s -> %s", inputPath, outputPath);

    // Освобождаем ресурсы
    env->ReleaseStringUTFChars(inputFilePath, inputPath);
    env->ReleaseStringUTFChars(outputFilePath, outputPath);

    // Уведомляем Java о завершении архивации файла
    updateFileProgress(env, thiz);

    return true;
}