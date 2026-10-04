package dev.maklabuxin.visuals.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.maklabuxin.visuals.event.Event;
import dev.maklabuxin.visuals.event.EventListener;
import dev.maklabuxin.visuals.event.EventRender;
import dev.maklabuxin.visuals.module.Category;
import dev.maklabuxin.visuals.module.Module;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * Заглушка SkyShader: пока рисует градиентный купол вокруг камеры,
 * чтобы проверить, что ивент, матрицы и depth test работают.
 * Сюда переносится логика из исходников чужого скайшейдера.
 */
public class SkyShader extends Module {
    private static final int SEGMENTS = 32;
    private static final int RINGS = 16;

    private final EventListener<Event> onEvent = event -> {
        if (!(event instanceof EventRender e)) return;
        if (!e.is3d()) return;
        if (mc.player == null || mc.world == null) return;

        renderSkyShader(e.position);
    };

    public SkyShader() {
        super("SkyShader", Category.RENDER);
    }

    private void renderSkyShader(MatrixStack stack) {
        // Купол чуть ближе дальней плоскости, чтобы не отсекался.
        float radius = mc.gameRenderer.getFarPlaneDistance() * 0.9f;
        Matrix4f matrix = stack.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        // Рисуем только туда, где глубина ещё "небо" (1.0): блоки и энтити перекрывают купол.
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (int r = 0; r < RINGS; r++) {
            double phi0 = Math.PI * r / RINGS - Math.PI / 2;
            double phi1 = Math.PI * (r + 1) / RINGS - Math.PI / 2;
            for (int s = 0; s < SEGMENTS; s++) {
                double th0 = 2 * Math.PI * s / SEGMENTS;
                double th1 = 2 * Math.PI * (s + 1) / SEGMENTS;
                vertex(buffer, matrix, radius, phi0, th0);
                vertex(buffer, matrix, radius, phi0, th1);
                vertex(buffer, matrix, radius, phi1, th1);
                vertex(buffer, matrix, radius, phi1, th0);
            }
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, float radius, double phi, double theta) {
        float x = (float) (Math.cos(phi) * Math.cos(theta)) * radius;
        float y = (float) Math.sin(phi) * radius;
        float z = (float) (Math.cos(phi) * Math.sin(theta)) * radius;
        // 0 у горизонта/внизу, 1 в зените.
        float t = (float) Math.max(0.0, Math.sin(phi));
        int red = (int) (255 * (0.95f - 0.75f * t));
        int green = (int) (255 * (0.45f - 0.25f * t));
        int blue = (int) (255 * (0.55f + 0.35f * t));
        buffer.vertex(matrix, x, y, z).color(red, green, blue, 170);
    }
}
