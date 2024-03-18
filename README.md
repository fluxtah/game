# Ship Game Documentation

Ship Game is currently a small prototype that provides an engine layer written in C and a game logic layer written in Kotlin. The engine layer is responsible for handling graphics, audio, physics, and user input, while the game logic layer is responsible for defining the game world, player interactions, and game mechanics.

## Prerequisites

Ensure you have the following tools and libraries installed on your system:

- **CMake**: For building the C engine components.
- **Gradle**: For compiling the Kotlin game logic.
- **GLFW**: Required for window management and handling user input.
- **Vulkan SDK**: For graphics rendering.
- **CGLM**: A mathematics library for C, useful for graphics programming.
- **OpenAL-Soft**: For audio playback.
- **Bullet**: A physics engine for realistic game physics.
- **glslangValidator**: For compiling shader programs.

Installation commands for macOS (using Homebrew):

```bash
brew install glfw cmake cglm openal-soft bullet
```

For Windows and Linux, you can use the package manager of your choice to install the required libraries. ???

For Vulkan SDK, please visit the official website [Vulkan SDK](https://vulkan.lunarg.com/)  to download and install the SDK for your platform.

## Building Instructions
The kotlin sdk must be built before building the engine as it builds a dynamic library that the engine depends on.

### Building the Game (Kotlin)
Navigate to the Kotlin SDK directory and build the game using Gradle:
```bash
cd kotlin-sdk
./gradlew build
```

### Building the Engine (C)
1. Create a build directory and navigate into it:
    ```bash
    mkdir build
    cd build
    ```
2. Configure the build with CMake and compile:
    ```bash
    cmake ..
    make
    ```

### Compiling Shaders
Compile your Vulkan shader programs using `glslangValidator`, e.g.:
```bash
glslangvalidator -V shaders/myshader.frag -o shaders/myshader.frag.spv
```

Compile all shaders using the provided `compile_shaders.sh` script:
```bash
sh compile_shaders.sh
```

## Development Environment Setup

- **CLion**: For engine development in C. Set the build directory to `./` in your run/build configurations.
- **IntelliJ IDEA**: For game development in Kotlin. Open the `kotlin-sdk` project directory.

## Game Assets

### Textures and Models
- **Models**: Import your 3D models as GLTF files.
- **Textures**: Use PNG files for textures. Assign basic textures in the GLTF file and override them using a `skins` directory alongside your GLTF files, with subdirectories for different skins (`0`, `1`, etc.), containing:
    - `base.png`
    - `collisionNormal.png`
    - `metalrough.png`

### Axis-Aligned Bounding Boxes (AABBs)
Create AABBs directly in your GLTF files by adding a mesh named `AABB.x`, where `x` is a unique identifier. The AABB is constructed from the mesh vertices, allowing for collision detection and spatial queries.
