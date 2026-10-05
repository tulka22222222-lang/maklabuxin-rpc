package ru.hachclient.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import ru.hachclient.events.Event;
import ru.hachclient.events.EventListener;
import ru.hachclient.events.impl.EventRender;
import ru.hachclient.modules.Module;
import ru.hachclient.modules.ModuleType;
import ru.hachclient.modules.settings.impl.NumberSetting;

import static ru.hachclient.Hachclient.mc;

// by ls5sq
/**
 * Ambience: настоящая насыщенность цвета мира через пост-шейдер.
 * После рендера мира кадр копируется в текстуру и перерисовывается
 * с усиленной насыщенностью. Рука и HUD не затрагиваются, гамма не меняется.
 */
public class Ambience extends Module {

    public static NumberSetting saturation = new NumberSetting("Saturation", 1.4f, 0f, 3f, 0.1f);

    private int program = 0;
    private int vao = 0;
    private int texture = 0;
    private int texWidth = -1;
    private int texHeight = -1;
    private boolean failed = false;

    EventListener<Event> onEvent = event -> {
        if (!(event instanceof EventRender e)) return;
        if (!e.is3d()) return;
        if (mc.player == null || mc.world == null) return;

        applySaturation();
    };

    public Ambience() {
        super("Ambience", "Усиливает насыщенность цветов мира", ModuleType.VISUAL);
        addSettings(saturation);
    }

    public float getSaturation() {
        return saturation.get();
    }

    private void applySaturation() {
        float sat = saturation.get();
        if (Math.abs(sat - 1.0f) < 0.001f) return; // 1.0 = без изменений
        if (failed) return;
        if (program == 0 && !init()) return;

        Framebuffer fb = mc.getFramebuffer();
        int width = fb.textureWidth;
        int height = fb.textureHeight;
        fb.beginWrite(false);

        // Копия кадра: читать и писать одну и ту же текстуру за один проход нельзя.
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        if (texture == 0) texture = GlStateManager._genTexture();
        GlStateManager._bindTexture(texture);
        if (width != texWidth || height != texHeight) {
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_EDGE);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_EDGE);
            GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0,
                    GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null);
            texWidth = width;
            texHeight = height;
        }
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);

        // Отвязываем VAO майна, иначе его кэш думает, что буфер ещё забинжен.
        BufferRenderer.reset();

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        GL20.glUseProgram(program);
        GL20.glUniform1i(GL20.glGetUniformLocation(program, "Scene"), 0);
        GL20.glUniform1f(GL20.glGetUniformLocation(program, "Saturation"), sat);

        GL30.glBindVertexArray(vao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);

        GlStateManager._bindTexture(0);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
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
            System.err.println("[Ambience] link error: " + GL20.glGetProgramInfoLog(prog));
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
            System.err.println("[Ambience] compile error: " + GL20.glGetShaderInfoLog(shader));
            GL20.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    private static final String VERTEX = """
            #version 150

            out vec2 uv;

            void main() {
                // Один треугольник на весь экран, вершины берутся из gl_VertexID.
                vec2 p = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
                uv = p;
                gl_Position = vec4(p * 2.0 - 1.0, 0.0, 1.0);
            }
            """;

    private static final String FRAGMENT = """
            #version 150

            uniform sampler2D Scene;
            uniform float Saturation;

            in vec2 uv;

            out vec4 fragColor;

            void main() {
                vec3 col = texture(Scene, uv).rgb;
                float luma = dot(col, vec3(0.2126, 0.7152, 0.0722));
                // Насыщенность: отдаляем цвет от серого той же яркости.
                vec3 saturated = mix(vec3(luma), col, Saturation);
                // Мягко сжимаем пересветы, чтобы яркие цвета не выгорали в плоские пятна.
                vec3 over = max(saturated - 1.0, 0.0);
                saturated = saturated - over + over / (1.0 + over * 4.0);
                fragColor = vec4(clamp(saturated, 0.0, 1.0), 1.0);
            }
            """;
}
