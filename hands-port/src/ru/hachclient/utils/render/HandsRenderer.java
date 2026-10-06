package ru.hachclient.utils.render;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
import com.mojang.blaze3d.systems.ProjectionType;
// created by ЗНАХАРКА АФТОДИЯ
import com.mojang.blaze3d.systems.RenderSystem;
// created by ЗНАХАРКА АФТОДИЯ
import net.minecraft.client.gl.Framebuffer;
// created by ЗНАХАРКА АФТОДИЯ
import net.minecraft.client.gl.ShaderProgramKeys;
// created by ЗНАХАРКА АФТОДИЯ
import net.minecraft.client.render.BufferBuilder;
// created by ЗНАХАРКА АФТОДИЯ
import net.minecraft.client.render.BufferRenderer;
// created by ЗНАХАРКА АФТОДИЯ
import net.minecraft.client.render.Tessellator;
// created by ЗНАХАРКА АФТОДИЯ
import net.minecraft.client.render.VertexFormat;
// created by ЗНАХАРКА АФТОДИЯ
import net.minecraft.client.render.VertexFormats;
// created by ЗНАХАРКА АФТОДИЯ
import org.joml.Matrix4f;
// created by ЗНАХАРКА АФТОДИЯ
import org.joml.Matrix4fStack;
// created by ЗНАХАРКА АФТОДИЯ
import ru.hachclient.modules.impl.render.Hands;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
import java.awt.Color;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
public class HandsRenderer {
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public static void composite(Framebuffer capture, Framebuffer target, Hands hands) {
// created by ЗНАХАРКА АФТОДИЯ
        target.beginWrite(false);
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.backupProjectionMatrix();
// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, 1.0F, 0.0F, 1.0F, -1.0F, 1.0F), ProjectionType.ORTHOGRAPHIC);
// created by ЗНАХАРКА АФТОДИЯ
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
// created by ЗНАХАРКА АФТОДИЯ
        modelView.pushMatrix();
// created by ЗНАХАРКА АФТОДИЯ
        modelView.identity();
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.disableDepthTest();
// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.depthMask(false);
// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.disableCull();
// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.enableBlend();
// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.defaultBlendFunc();
// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.setShaderTexture(0, capture.getColorAttachment());
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        switch (hands.getModeIndex()) {
// created by ЗНАХАРКА АФТОДИЯ
            case 0 -> drawFill(hands);
// created by ЗНАХАРКА АФТОДИЯ
            // TODO: Шейдер / Зеркало / Глов / Обводка / Шлейф - нужны GLSL шейдеры
// created by ЗНАХАРКА АФТОДИЯ
            default -> drawQuad(0xFFFFFFFF);
// created by ЗНАХАРКА АФТОДИЯ
        }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.disableBlend();
// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.enableCull();
// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.depthMask(true);
// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.enableDepthTest();
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        modelView.popMatrix();
// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.restoreProjectionMatrix();
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    private static void drawFill(Hands hands) {
// created by ЗНАХАРКА АФТОДИЯ
        int color = hands.rainbow.get() ? rainbow(hands) : hands.getFillColor();
// created by ЗНАХАРКА АФТОДИЯ
        float alpha = hands.fillAlpha.get();
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        // оригинальные руки снизу, если надо сохранить тени
// created by ЗНАХАРКА АФТОДИЯ
        if (hands.keepShadows.get()) {
// created by ЗНАХАРКА АФТОДИЯ
            drawQuad(withAlpha(0xFFFFFFFF, hands.shadowStrength.get()));
// created by ЗНАХАРКА АФТОДИЯ
        }
// created by ЗНАХАРКА АФТОДИЯ
        drawQuad(withAlpha(color, alpha));
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    private static void drawQuad(int color) {
// created by ЗНАХАРКА АФТОДИЯ
        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
// created by ЗНАХАРКА АФТОДИЯ
        builder.vertex(0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).color(color);
// created by ЗНАХАРКА АФТОДИЯ
        builder.vertex(1.0F, 0.0F, 0.0F).texture(1.0F, 0.0F).color(color);
// created by ЗНАХАРКА АФТОДИЯ
        builder.vertex(1.0F, 1.0F, 0.0F).texture(1.0F, 1.0F).color(color);
// created by ЗНАХАРКА АФТОДИЯ
        builder.vertex(0.0F, 1.0F, 0.0F).texture(0.0F, 1.0F).color(color);
// created by ЗНАХАРКА АФТОДИЯ
        BufferRenderer.drawWithGlobalProgram(builder.end());
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    private static int rainbow(Hands hands) {
// created by ЗНАХАРКА АФТОДИЯ
        float hue = (hands.getTime() * hands.rainbowSpeed.get() * 0.25F * hands.rainbowScale.get()) % 1.0F;
// created by ЗНАХАРКА АФТОДИЯ
        return Color.HSBtoRGB(hue, 0.7F, 1.0F) | 0xFF000000;
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    private static int withAlpha(int color, float alpha) {
// created by ЗНАХАРКА АФТОДИЯ
        int a = Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F);
// created by ЗНАХАРКА АФТОДИЯ
        return (a << 24) | (color & 0x00FFFFFF);
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public static void reset() {
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ
}
// created by ЗНАХАРКА АФТОДИЯ
