#version 410 core
layout(location=0) in vec3 aPos;
layout(location=1) in vec3 aCol;

// Uniforms de transformación
uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProj;

out vec3 vCol;

void main() {
    vCol = aCol;
    gl_Position = uProj * uView * uModel * vec4(aPos, 1.0);
}
