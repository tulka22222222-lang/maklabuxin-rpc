package dev.maklabuxin.visuals.util;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

/** Матрицы последнего кадра, их заполняет GameRendererMixin. */
public final class Render {
    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
    public static MatrixStack worldStack = new MatrixStack();

    private Render() {
    }
}
