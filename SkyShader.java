package ru.hachclient.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import ru.hachclient.modules.Module;
import ru.hachclient.modules.ModuleType;

/**
 * SkyShader в стиле космоса: звёзды, туманности, полоса галактики, солнце
 * и планета с кольцами. Всё в одном классе: GLSL лежит строками ниже и
 * компилится прямо через OpenGL, файлы в assets не нужны.
 * Рисуется полноэкранным треугольником на дальней плоскости с GL_LEQUAL,
 * поэтому блоки, энтити и облака остаются спереди.
 */
public class SkyShader extends Module {

    // Настройки.
    public float speed = 1.0f;
    public float starDensity = 0.7f;
    public float nebulaStrength = 0.8f;
    public float planetSize = 0.12f;
    public float opacity = 1.0f;
    public float[] nebulaColor1 = {0.45f, 0.10f, 0.75f};
    public float[] nebulaColor2 = {0.05f, 0.45f, 0.85f};
    public float[] planetColor = {0.95f, 0.55f, 0.30f};

    private final long startTime = System.currentTimeMillis();

    private int program = 0;
    private int vao = 0;
    private boolean failed = false;

    EventListener<Event> onEvent = event -> {
        if (!(event instanceof EventRender e)) return;
        if (!e.is3d()) return;
        if (mc.player == null || mc.world == null) return;

        renderSkyShader(e.position);
    };

    public SkyShader() {
        super("SkyShader", "Космическое небо: звёзды, туманности, планета", ModuleType.RENDER);
    }

    private void renderSkyShader(MatrixStack stack) {
        if (failed) return;
        if (program == 0 && !init()) return;

        // e.position содержит только поворот камеры, поэтому inverse(proj * rot)
        // переводит пиксель экрана в направление взгляда в мире.
        Matrix4f invViewProj = new Matrix4f(RenderSystem.getProjectionMatrix())
                .mul(stack.peek().getPositionMatrix())
                .invert();
        float time = (System.currentTimeMillis() - startTime) / 1000.0f * speed;

        // Отвязываем VAO майна, иначе его кэш думает, что буфер ещё забинжен.
        BufferRenderer.reset();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL); // 515, не 519 (519 = GL_ALWAYS, зальёт весь экран)
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        GL20.glUseProgram(program);
        try (MemoryStack mem = MemoryStack.stackPush()) {
            GL20.glUniformMatrix4fv(loc("InvViewProj"), false, invViewProj.get(mem.mallocFloat(16)));
        }
        GL20.glUniform1f(loc("Time"), time);
        GL20.glUniform1f(loc("StarDensity"), starDensity);
        GL20.glUniform1f(loc("NebulaStrength"), nebulaStrength);
        GL20.glUniform1f(loc("PlanetSize"), planetSize);
        GL20.glUniform1f(loc("Opacity"), opacity);
        GL20.glUniform3f(loc("NebulaColor1"), nebulaColor1[0], nebulaColor1[1], nebulaColor1[2]);
        GL20.glUniform3f(loc("NebulaColor2"), nebulaColor2[0], nebulaColor2[1], nebulaColor2[2]);
        GL20.glUniform3f(loc("PlanetColor"), planetColor[0], planetColor[1], planetColor[2]);

        GL30.glBindVertexArray(vao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private int loc(String name) {
        return GL20.glGetUniformLocation(program, name);
    }

    private boolean init() {
        int vs = compile(GL20.GL_VERTEX_SHADER, VERTEX);
        int fs = compile(GL20.GL_FRAGMENT_SHADER, FRAGMENT);
        if (vs == 0 || fs == 0) {
            failed = true;
            return false;
        }
        int prog = GL20.glCreateProgram();
        GL20.glAttachShader(prog, vs);
        GL20.glAttachShader(prog, fs);
        GL20.glLinkProgram(prog);
        GL20.glDeleteShader(vs);
        GL20.glDeleteShader(fs);
        if (GL20.glGetProgrami(prog, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            System.err.println("[SkyShader] link error: " + GL20.glGetProgramInfoLog(prog));
            GL20.glDeleteProgram(prog);
            failed = true;
            return false;
        }
        program = prog;
        // Пустой VAO: в core-профиле без него glDrawArrays не рисует.
        vao = GL30.glGenVertexArrays();
        return true;
    }

    private static int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println("[SkyShader] compile error: " + GL20.glGetShaderInfoLog(shader));
            GL20.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    private static final String VERTEX = """
            #version 150

            out vec2 ndc;

            void main() {
                // Один треугольник на весь экран, вершины берутся из gl_VertexID.
                vec2 p = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
                ndc = p * 2.0 - 1.0;
                // Глубина почти на дальней плоскости: рисуется только там, где небо.
                gl_Position = vec4(ndc, 0.99999, 1.0);
            }
            """;

    private static final String FRAGMENT = """
            #version 150

            uniform mat4 InvViewProj;
            uniform float Time;
            uniform float StarDensity;
            uniform float NebulaStrength;
            uniform float PlanetSize;
            uniform float Opacity;
            uniform vec3 NebulaColor1;
            uniform vec3 NebulaColor2;
            uniform vec3 PlanetColor;

            in vec2 ndc;

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
                // Направление взгляда для пикселя: из экрана обратно в мир.
                vec4 world = InvViewProj * vec4(ndc, 1.0, 1.0);
                vec3 d = normalize(world.xyz / world.w);

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
            """;
}
