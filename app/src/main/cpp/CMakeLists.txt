cmake_minimum_required(VERSION 3.10)

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

# Путь к исходникам minizip
set(MINIZIP_SRC_DIR ${CMAKE_SOURCE_DIR}/minizip)

# Сборка minizip как статической библиотеки
add_library(minizip STATIC
        ${MINIZIP_SRC_DIR}/zip.c
        ${MINIZIP_SRC_DIR}/unzip.c
        ${MINIZIP_SRC_DIR}/ioapi.c
)

# Указываем путь к заголовочным файлам minizip
target_include_directories(minizip PUBLIC ${MINIZIP_SRC_DIR})

# Основная библиотека проекта
add_library(filearchiver SHARED
        native-lib.cpp
)

# Связываем библиотеки
target_link_libraries(filearchiver
        android
        log
        zlib
        minizip
)