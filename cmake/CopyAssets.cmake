# Define the directories
set(ASSETS_DIR "${CMAKE_SOURCE_DIR}/assets")
set(LIBS_DIR "${ASSETS_DIR}/libs")
set(SOUNDS_DIR "${ASSETS_DIR}/sounds")
set(MODELS_DIR "${ASSETS_DIR}/models")
set(SHEETS_DIR "${ASSETS_DIR}/sheets")
set(SHADERS_DIR "${ASSETS_DIR}/shaders")
set(TEXTURES_DIR "${ASSETS_DIR}/textures")

# Define the destination directories in the build directory
set(ASSETS_DEST_DIR "${CMAKE_BINARY_DIR}/assets")
set(LIBS_DEST_DIR "${CMAKE_BINARY_DIR}")
set(SOUNDS_DEST_DIR "${ASSETS_DEST_DIR}/sounds")
set(MODELS_DEST_DIR "${ASSETS_DEST_DIR}/models")
set(SHEETS_DEST_DIR "${ASSETS_DEST_DIR}/sheets")
set(SHADERS_DEST_DIR "${ASSETS_DEST_DIR}/shaders")
set(TEXTURES_DEST_DIR "${ASSETS_DEST_DIR}/textures")

# Custom target for compiling shaders
add_custom_target(compile_shaders
        COMMAND sh ${CMAKE_SOURCE_DIR}/compile_shaders.sh
        WORKING_DIRECTORY ${SHADERS_DIR}
        COMMENT "Compiling shaders..."
)

# Ensure Game depends on compile_shaders to make sure it compiles before building the Game
add_dependencies(Game compile_shaders)

# Copy specific folders within assets
add_custom_command(
        TARGET Game POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy_directory ${SOUNDS_DIR} ${SOUNDS_DEST_DIR}
        COMMAND ${CMAKE_COMMAND} -E copy_directory ${MODELS_DIR} ${MODELS_DEST_DIR}
        COMMAND ${CMAKE_COMMAND} -E copy_directory ${SHEETS_DIR} ${SHEETS_DEST_DIR}
        COMMAND ${CMAKE_COMMAND} -E copy_directory ${TEXTURES_DIR} ${TEXTURES_DEST_DIR}
        COMMENT "Copying sounds, models, and sheets to build directory"
)

# Copy the libs to the build directory after building the project
add_custom_command(
        TARGET Game POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy_directory ${LIBS_DIR} ${LIBS_DEST_DIR}
        COMMENT "Copying dynamic libraries to build directory"
)

# Copy only compiled .spv files from shaders directory
file(GLOB SHADER_SPVS "${SHADERS_DIR}/*.spv")
foreach(SPV ${SHADER_SPVS})
    get_filename_component(SPV_NAME ${SPV} NAME)
    add_custom_command(
            TARGET Game POST_BUILD
            COMMAND ${CMAKE_COMMAND} -E copy ${SPV} ${SHADERS_DEST_DIR}/${SPV_NAME}
            COMMENT "Copying shader SPV files to build directory"
    )
endforeach()
