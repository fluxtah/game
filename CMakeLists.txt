cmake_minimum_required(VERSION 3.10)
project(Game C)

set(CMAKE_BUILD_TYPE Debug)
set(KOTLIN_BUILD_TYPE "releaseShared")
set(KOTLIN_LIB_DIR "${PROJECT_SOURCE_DIR}/kotlin-sdk/build/bin/native/${KOTLIN_BUILD_TYPE}")
set(KOTLIN_LIB_NAME "libkotlin_sdk.dylib") # Default to macOS
if(WIN32)
    set(KOTLIN_LIB_NAME "kotlin_sdk.dll")
elseif(UNIX AND NOT APPLE)
    set(KOTLIN_LIB_NAME "libkotlin_sdk.so")
endif()

list(APPEND CMAKE_MODULE_PATH "${PROJECT_SOURCE_DIR}/cmake")

find_package(Vulkan REQUIRED)
find_package(glfw3 REQUIRED)
find_package(OpenAL REQUIRED)

add_subdirectory(modules/networking)
add_subdirectory(modules/physics)

file(GLOB_RECURSE PROJECT_SOURCES "${PROJECT_SOURCE_DIR}/src/*.c")
file(GLOB_RECURSE PROJECT_HEADERS "${PROJECT_SOURCE_DIR}/include/*.h")
file(GLOB_RECURSE SINGLE_FILE_LIBS "${PROJECT_SOURCE_DIR}/libs/include/*.h")
file(GLOB_RECURSE SINGLE_FILE_LIBS_SRC "${PROJECT_SOURCE_DIR}/libs/src/*.c")
add_executable(Game ${SINGLE_FILE_LIBS} ${SINGLE_FILE_LIBS_SRC} ${PROJECT_SOURCES} ${PROJECT_HEADERS})

target_include_directories(Game PRIVATE
        ${PROJECT_SOURCE_DIR}/include
        ${PROJECT_SOURCE_DIR}/libs/include
        ${PROJECT_SOURCE_DIR}/src
        kotlin-sdk/build/bin/native/releaseShared
        kotlin-sdk/cinterop
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${Vulkan_INCLUDE_DIRS}
        ${OPENAL_INCLUDE_DIRS}
)

add_library(kotlin_library SHARED IMPORTED)
set_target_properties(kotlin_library PROPERTIES
        IMPORTED_LOCATION "${KOTLIN_LIB_DIR}/${KOTLIN_LIB_NAME}"
)

target_link_libraries(
        Game
        ${Vulkan_LIBRARIES}
        glfw
        ${CGLM_LIBRARY}
        kotlin_library
        OpenAL::OpenAL
        networking
        physics
)
