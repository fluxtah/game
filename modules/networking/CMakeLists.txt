# Assuming this is the CMakeLists.txt in the networking module
cmake_minimum_required(VERSION 3.10)
project(NetworkingModule)

set(CMAKE_CXX_STANDARD 11)

# Include directories
include_directories(include)
include_directories(${CMAKE_SOURCE_DIR}/libs/steamworks/public/steam)

# Source files
add_library(networking SHARED src/hello.cpp
        include/LobbyManager.h
        src/LobbyManager.cpp)

target_include_directories(networking PRIVATE
        include
        ${CMAKE_SOURCE_DIR}/libs/steamworks/public/steam
        "${CMAKE_SOURCE_DIR}/kotlin-sdk/cinterop")

if(APPLE)
    set(STEAMWORKS_LIBRARY_PATH "${CMAKE_SOURCE_DIR}/libs/steamworks/redistributable_bin/osx")
elseif(WIN32)
    set(STEAMWORKS_LIBRARY_PATH "${CMAKE_SOURCE_DIR}/libs/steamworks/redistributable_bin/win64") # Adjust if you're targeting 32-bit
endif()

find_library(STEAMWORKS_LIBRARY NAMES steam_api steam_api64 PATHS ${STEAMWORKS_LIBRARY_PATH} NO_DEFAULT_PATH)
if(NOT STEAMWORKS_LIBRARY)
    message(FATAL_ERROR "Steamworks library not found")
endif()

target_link_libraries(networking ${STEAMWORKS_LIBRARY})

