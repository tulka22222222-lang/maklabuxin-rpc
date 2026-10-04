#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec3 dir;

void main() {
    dir = Position;
    vec4 pos = ProjMat * ModelViewMat * vec4(Position, 1.0);
    // Прижимаем купол к дальней плоскости: он виден только там, где в буфере глубины небо.
    gl_Position = vec4(pos.xy, pos.w * 0.99999, pos.w);
}
