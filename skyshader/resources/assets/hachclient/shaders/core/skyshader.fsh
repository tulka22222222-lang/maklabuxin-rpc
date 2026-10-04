#version 150

uniform float Time;
uniform float StarDensity;
uniform float NebulaStrength;
uniform float PlanetSize;
uniform float Opacity;
uniform vec3 NebulaColor1;
uniform vec3 NebulaColor2;
uniform vec3 PlanetColor;

in vec3 dir;

out vec4 fragColor;

// Направления объектов на небе (в мировых координатах).
const vec3 GALAXY_NORMAL = normalize(vec3(0.35, 0.25, 1.0));
const vec3 PLANET_DIR = normalize(vec3(0.6, 0.35, -0.7));
const vec3 SUN_DIR = normalize(vec3(-0.6, 0.45, 0.65));

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
    for (int i = 0; i < 5; i++) {
        v += a * noise(p);
        p = p * 2.03 + vec3(1.7, 9.2, 3.1);
        a *= 0.5;
    }
    return v;
}

// Цвет звезды по "температуре": от красноватых до голубых.
vec3 starColor(float t) {
    return mix(vec3(1.0, 0.65, 0.45), mix(vec3(1.0), vec3(0.6, 0.75, 1.0), smoothstep(0.5, 1.0, t)), smoothstep(0.0, 0.5, t));
}

vec3 starLayer(vec3 d, float scale, float chance, float size) {
    vec3 p = d * scale;
    vec3 cell = floor(p);
    float h = hash(cell);
    if (h < 1.0 - chance * StarDensity) return vec3(0.0);
    vec3 center = vec3(hash(cell + 3.1), hash(cell + 7.7), hash(cell + 1.3)) * 0.6 + 0.2;
    float dist = length(fract(p) - center);
    float twinkle = 0.65 + 0.35 * sin(Time * (2.0 + h * 4.0) + h * 120.0);
    float core = smoothstep(size, 0.0, dist);
    return starColor(hash(cell + 5.5)) * core * core * twinkle;
}

vec3 stars(vec3 d) {
    vec3 c = starLayer(d, 320.0, 0.035, 0.30) * 0.7;
    c += starLayer(d, 150.0, 0.025, 0.25);
    c += starLayer(d, 60.0, 0.012, 0.18) * 1.6;
    return c;
}

vec3 nebula(vec3 d) {
    float t = Time * 0.01;
    vec3 q = d * 2.2;
    // Искажение координат даёт "клубящиеся" облака газа.
    vec3 warp = vec3(fbm(q + t), fbm(q + vec3(5.2, 1.3, 2.8) - t), fbm(q + vec3(2.1, 7.4, 4.6)));
    float n = fbm(q + warp * 1.8);
    float mask = smoothstep(0.45, 0.85, n);
    vec3 col = mix(NebulaColor1, NebulaColor2, smoothstep(0.3, 0.7, warp.x));
    col += vec3(1.0, 0.4, 0.6) * smoothstep(0.75, 0.95, n) * 0.4;
    return col * mask * NebulaStrength;
}

vec3 galaxy(vec3 d) {
    float plane = dot(d, GALAXY_NORMAL);
    float band = exp(-plane * plane * 18.0);
    float dust = fbm(d * 7.0 + vec3(0.0, 0.0, Time * 0.003));
    float lanes = smoothstep(0.35, 0.65, dust);
    float core = exp(-plane * plane * 120.0) * (0.6 + 0.4 * fbm(d * 15.0));
    vec3 col = vec3(0.75, 0.7, 0.95) * band * 0.35 * lanes
             + vec3(1.0, 0.85, 0.7) * core * 0.35 * lanes;
    // Россыпь мелких звёзд вдоль полосы.
    col += starLayer(d, 500.0, 0.12 * band, 0.35) * band;
    return col;
}

vec4 planet(vec3 d) {
    float facing = dot(d, PLANET_DIR);
    if (facing <= 0.0) return vec4(0.0);

    vec3 t1 = normalize(cross(PLANET_DIR, vec3(0.0, 1.0, 0.0)));
    vec3 t2 = cross(t1, PLANET_DIR);
    vec2 p = vec2(dot(d, t1), dot(d, t2)) / PlanetSize;
    float l2 = dot(p, p);

    vec3 col = vec3(0.0);
    float alpha = 0.0;

    // Тело планеты: освещённая сфера с полосами, как у газового гиганта.
    if (l2 < 1.0) {
        vec3 n = vec3(p, sqrt(1.0 - l2));
        vec3 sunLocal = normalize(vec3(dot(SUN_DIR, t1), dot(SUN_DIR, t2), dot(SUN_DIR, PLANET_DIR)));
        float light = max(dot(n, sunLocal), 0.0);
        float bands = fbm(vec3(p.y * 6.0 + fbm(vec3(p * 3.0, Time * 0.02)) * 1.5, p.x * 0.5, 1.0));
        vec3 surface = mix(PlanetColor * 0.45, PlanetColor * 1.15, bands);
        col = surface * (0.04 + light * 0.96);
        // Подсветка атмосферы по краю на освещённой стороне.
        col += PlanetColor * pow(1.0 - n.z, 3.0) * light * 0.8;
        alpha = 1.0;
    } else {
        float glow = exp(-(sqrt(l2) - 1.0) * 14.0);
        col = PlanetColor * glow * 0.45;
        alpha = glow * 0.5;
    }

    // Кольца: наклонённый эллипс, задняя половина скрыта за планетой.
    float e = length(vec2(p.x, p.y / 0.28));
    if (e > 1.35 && e < 2.3 && !(l2 < 1.0 && p.y > 0.0)) {
        float rings = 0.5 + 0.5 * sin(e * 38.0) * noise(vec3(e * 20.0, 0.0, 0.0));
        float fade = smoothstep(1.35, 1.5, e) * smoothstep(2.3, 2.1, e);
        // Тень планеты на кольцах.
        float shadow = (p.x > 0.0 && abs(p.y) < 0.9) ? 0.35 : 1.0;
        vec3 ringCol = mix(PlanetColor, vec3(0.9, 0.85, 0.8), 0.5) * rings * shadow;
        float ringA = fade * (0.35 + 0.5 * rings);
        col = mix(col, ringCol, ringA);
        alpha = max(alpha, ringA);
    }
    return vec4(col, alpha);
}

void main() {
    vec3 d = normalize(dir);

    vec3 col = vec3(0.005, 0.006, 0.02);
    col += nebula(d);
    col += galaxy(d);
    col += stars(d);

    // Мягкое свечение далёкого солнца.
    float sun = max(dot(d, SUN_DIR), 0.0);
    col += vec3(1.0, 0.9, 0.75) * (pow(sun, 900.0) * 4.0 + pow(sun, 40.0) * 0.08);

    vec4 pl = planet(d);
    col = mix(col, pl.rgb, pl.a);

    // Лёгкое тонирование, чтобы яркие места не выгорали.
    col = col / (1.0 + col * 0.6);
    fragColor = vec4(col, Opacity);
}
