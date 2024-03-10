# Define the assets directory path
set(ASSETS_DIR "${CMAKE_SOURCE_DIR}/assets")
# Define the libraries directory path
set(LIBS_DIR "${CMAKE_SOURCE_DIR}/libs")

# Define the destination path for the assets and libs in the build directory
set(ASSETS_DEST_DIR "${CMAKE_BINARY_DIR}/assets")
set(LIBS_DEST_DIR "${CMAKE_BINARY_DIR}")

# Copy the assets directory to the build directory after building the project
add_custom_command(
        TARGET Game POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy_directory ${ASSETS_DIR} ${ASSETS_DEST_DIR}
        COMMENT "Copying assets to build directory"
)

# Copy the libs to the build directory after building the project
add_custom_command(
        TARGET Game POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy_directory ${LIBS_DIR} ${LIBS_DEST_DIR}
        COMMENT "Copying dynamic libraries to build directory"
)
