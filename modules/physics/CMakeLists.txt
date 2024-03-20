cmake_minimum_required(VERSION 3.10)
project(PhysicsModule)

set(CMAKE_CXX_STANDARD 11)

# Include directories
file(GLOB_RECURSE PROJECT_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/src/*.cpp")
file(GLOB_RECURSE PROJECT_HEADERS "${CMAKE_CURRENT_SOURCE_DIR}/include/*.h")

# Find Bullet Physics package
find_package(Bullet REQUIRED)

add_library(physics SHARED ${PROJECT_SOURCES} ${PROJECT_HEADERS})

target_include_directories(physics PRIVATE ${BULLET_INCLUDE_DIRS} ${CMAKE_CURRENT_SOURCE_DIR}/include)

target_link_libraries(physics ${BULLET_LIBRARIES})