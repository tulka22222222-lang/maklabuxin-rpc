package dev.maklabuxin.visuals.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.maklabuxin.visuals.Maklabuxin;
import dev.maklabuxin.visuals.event.EventRender;
import dev.maklabuxin.visuals.util.Render;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void maklabuxin$render3d(RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Camera camera = mc.gameRenderer.getCamera();
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));

        Render.lastProjMat.set(RenderSystem.getProjectionMatrix());
        Render.lastModMat.set(RenderSystem.getModelViewMatrix());
        Render.lastWorldSpaceMatrix.set(matrixStack.peek().getPositionMatrix());
        Render.worldStack = matrixStack;

        // В 3D-ивенте modelView — единичная, вся камера лежит в matrixStack.
        RenderSystem.getModelViewStack().pushMatrix().identity();
        float tickDelta = tickCounter.getTickDelta(true);
        Maklabuxin.getInstance().getEventHandler().call(
                EventRender.build(EventRender.RenderType.WORLD, matrixStack, tickDelta)
        );
        RenderSystem.getModelViewStack().popMatrix();
    }
}
