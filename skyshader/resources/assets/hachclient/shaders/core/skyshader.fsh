#version 150

uniform float Time;
uniform float StarsAmount;
uniform float AuroraStrength;
uniform float Opacity;
uniform vec3 TopColor;
uniform vec3 HorizonColor;
uniform vec3 AuroraColor;

in vec3 dir;

out vec4 fragColor;

float hash(vec3 p) {
    p = fract(p * 0.3183099 + 0.1);
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

float noise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(hash(i + vec3(0, 0, 0)), hash(i + vec3(1, 0, 0)), f.x),
                   mix(hash(i + vec3(0, 1, 0)), hash(i + vec3(1, 1, 0)), f.x), f.y),
               mix(mix(hash(i + vec3(0, 0, 1)), hash(i + vec3(1, 0, 1)), f.x),
                   mix(hash(i + vec3(0, 1, 1)), hash(i + vec3(1, 1, 1)), f.x), f.y), f.z);
}

float fbm(vec3 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += a * noise(p);
        p *= 2.03;
        a *= 0.5;
    }
    return v;
}

float stars(vec3 d) {
    vec3 p = d * 220.0;
    vec3 cell = floor(p);
    float h = hash(cell);
    if (h < 1.0 - 0.02 * StarsAmount) return 0.0;
    float dist = length(fract(p) - 0.5);
    float twinkle = 0.6 + 0.4 * sin(Time * 3.0 + h * 100.0);
    return smoothstep(0.35, 0.0, dist) * twinkle;
}

float aurora(vec3 d) {
    if (d.y <= 0.02) return 0.0;
    // Проецируем на "потолок", чтобы полосы тянулись по небу.
    vec2 uv = d.xz / (d.y + 0.25);
    float band = 0.0;
    for (int i = 0; i < 3; i++) {
        float fi = float(i);
        float wave = sin(uv.x * (1.2 + fi * 0.4) + Time * 0.3 + fi * 2.1) * 0.6
                   + fbm(vec3(uv * 0.8, Time * 0.08 + fi)) * 1.4;
        band += smoothstep(0.25, 0.0, abs(uv.y - wave + fi * 0.7 - 0.7)) * (1.0 - fi * 0.25);
    }
    float shimmer = 0.6 + 0.4 * fbm(vec3(uv * 4.0, Time * 0.5));
    return band * shimmer * smoothstep(0.02, 0.35, d.y);
}

void main() {
    vec3 d = normalize(dir);
    float h = clamp(d.y, -1.0, 1.0);

    vec3 col = mix(HorizonColor, TopColor, smoothstep(0.0, 0.6, h));
    col = mix(col, HorizonColor * 0.4, smoothstep(0.0, -0.4, h));
    col += HorizonColor * 0.35 * exp(-abs(h) * 12.0);

    float upper = smoothstep(-0.05, 0.15, h);
    col += vec3(stars(d)) * upper;
    col += AuroraColor * aurora(d) * AuroraStrength;

    fragColor = vec4(col, Opacity);
}
