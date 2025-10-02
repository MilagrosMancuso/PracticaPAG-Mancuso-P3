#version 410
layout (location = 0) in vec3 posicion;
layout (location = 1) in vec3 color;

out vec3 vColor; // paso al fragment

void main() {
    gl_Position = vec4(posicion, 1.0);
    vColor = color;
}