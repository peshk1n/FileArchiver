# Минимальная версия CMake
cmake_minimum_required(VERSION 3.22.1)

# Имя проекта
project("filearchiver")

# Путь к исходникам zlib
set(ZLIB_SRC_DIR ${CMAKE_SOURCE_DIR}/zlib)

# Сборка zlib как статической библиотеки
add_library(zlib STATIC
        ${ZLIB_SRC_DIR}/adler32.c
        ${ZLIB_SRC_DIR}/compress.c
        ${ZLIB_SRC_DIR}/crc32.c
        ${ZLIB_SRC_DIR}/deflate.c
        ${ZLIB_SRC_DIR}/infback.c
        ${ZLIB_SRC_DIR}/inffast.c
        ${ZLIB_SRC_DIR}/inflate.c
        ${ZLIB_SRC_DIR}/inftrees.c
        ${ZLIB_SRC_DIR}/trees.c
        ${ZLIB_SRC_DIR}/uncompr.c
        ${ZLIB_SRC_DIR}/zutil.c
)

# Указываем путь к заголовочным файлам zlib
target_include_directories(zlib PUBLIC ${ZLIB_SRC_DIR})

# Основная библиотека проекта
add_library(${CMAKE_PROJECT_NAME} SHARED
        native-lib.cpp
)

# Связываем библиотеки
target_link_libraries(${CMAKE_PROJECT_NAME}
        android
        log
        zlib  # Добавляем zlib
)