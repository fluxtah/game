cmake_minimum_required(VERSION 3.10)
project(PhysicsModule)

set(CMAKE_CXX_STANDARD 11)

# Include directories
include_directories(include)

# Find Bullet Physics package
find_package(Bullet REQUIRED)

add_library(physics SHARED src/physics.cpp)

target_include_directories(physics PRIVATE ${BULLET_INCLUDE_DIRS})

target_link_libraries(physics ${BULLET_LIBRARIES})