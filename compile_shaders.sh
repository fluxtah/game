#!/bin/bash

# Directory containing shader files
SHADER_DIR="./assets/shaders"

# Compile each shader file found in the directory
for SHADER in $(find $SHADER_DIR -type f \( -iname \*.frag -o -iname \*.comp -o -iname \*.vert \)); do
    # Output file name by appending .spv to the original file name
    OUTPUT="${SHADER}.spv"

    # Command to compile the shader to SPIR-V
    glslangValidator -V "$SHADER" -o "$OUTPUT"

    # Optional: Echo progress
    echo "Compiled $SHADER to $OUTPUT"
done

echo "Shader compilation complete."
