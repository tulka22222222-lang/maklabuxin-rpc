package hachclient.module.render; // поменяй на пакет, где лежат модули

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

// + импорты из клиента: Module, Event, EventListener, EventRender

/**
 * SkyShader в стиле космоса: поверх ванильного неба рисуется купол со своим
 * шейдером (звёзды, туманности, полоса галактики, планета с кольцами). Блоки, энтити и облака остаются
 * спереди, потому что купол прижат к дальней плоскости и рисуется с GL_LEQUAL.
 */
public class SkyShader extends Module {

    // Неймспейс ресурсов клиента: assets/<NAMESPACE>/shaders/core/skyshader.json
    private static final String NAMESPACE = "hachclient";
    private static final ShaderProgramKey SHADER = new ShaderProgramKey(
            Identifier.of(NAMESPACE, "core/skyshader"), VertexFormats.POSITION, Defines.EMPTY);

    private static final int SEGMENTS = 48;
    private static final int RINGS = 24;
    private static final float RADIUS = 10.0f;

    // Настройки. Если в клиенте есть Slider/ColorSetting — перенеси на них.
    public float speed = 1.0f;
    public float starDensity = 0.7f;
    public float nebulaStrength = 0.8f;
    public float planetSize = 0.12f;
    public float opacity = 1.0f;
    public float[] nebulaColor1 = {0.45f, 0.10f, 0.75f};
    public float[] nebulaColor2 = {0.05f, 0.45f, 0.85f};
    public float[] planetColor = {0.95f, 0.55f, 0.30f};

    private final long startTime = System.currentTimeMillis();

    EventListener<Event> onEvent = event -> {
        if (!(event instanceof EventRender e)) return;
        if (!e.is3d()) return;
        if (mc.player == null || mc.world == null) return;

        renderSkyShader(e.position);
    };

    public SkyShader() {
        super("SkyShader"); // подгони под конструктор Module в клиенте
    }

    private void renderSkyShader(MatrixStack stack) {
        // e.position содержит только поворот камеры, без смещения, поэтому
        // позиции вершин купола равны направлениям взгляда в мире.
        RenderSystem.getModelViewStack().pushMatrix().set(stack.peek().getPositionMatrix());

        ShaderProgram program = RenderSystem.setShader(SHADER);
        if (program == null) {
            RenderSystem.getModelViewStack().popMatrix();
            return;
        }
        float time = (System.currentTimeMillis() - startTime) / 1000.0f * speed;
        setUniform(program, "Time", time);
        setUniform(program, "StarDensity", starDensity);
        setUniform(program, "NebulaStrength", nebulaStrength);
        setUniform(program, "PlanetSize", planetSize);
        setUniform(program, "Opacity", opacity);
        setUniform(program, "NebulaColor1", nebulaColor1);
        setUniform(program, "NebulaColor2", nebulaColor2);
        setUniform(program, "PlanetColor", planetColor);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL); // 515, не 519 (519 = GL_ALWAYS, зальёт весь экран)
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        for (int r = 0; r < RINGS; r++) {
            double phi0 = Math.PI * r / RINGS - Math.PI / 2;
            double phi1 = Math.PI * (r + 1) / RINGS - Math.PI / 2;
            for (int s = 0; s < SEGMENTS; s++) {
                double th0 = 2 * Math.PI * s / SEGMENTS;
                double th1 = 2 * Math.PI * (s + 1) / SEGMENTS;
                vertex(buffer, phi0, th0);
                vertex(buffer, phi0, th1);
                vertex(buffer, phi1, th1);
                vertex(buffer, phi1, th0);
            }
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.getModelViewStack().popMatrix();
    }

    private static void vertex(BufferBuilder buffer, double phi, double theta) {
        float x = (float) (Math.cos(phi) * Math.cos(theta)) * RADIUS;
        float y = (float) Math.sin(phi) * RADIUS;
        float z = (float) (Math.cos(phi) * Math.sin(theta)) * RADIUS;
        buffer.vertex(x, y, z);
    }

    private static void setUniform(ShaderProgram program, String name, float value) {
        GlUniform uniform = program.getUniform(name);
        if (uniform != null) uniform.set(value);
    }

    private static void setUniform(ShaderProgram program, String name, float[] rgb) {
        GlUniform uniform = program.getUniform(name);
        if (uniform != null) uniform.set(rgb[0], rgb[1], rgb[2]);
    }
}
