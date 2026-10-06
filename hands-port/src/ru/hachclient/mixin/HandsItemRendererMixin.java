package ru.hachclient.mixin;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
import com.mojang.blaze3d.systems.RenderSystem;
// created by ЗНАХАРКА АФТОДИЯ
import net.minecraft.client.render.item.HeldItemRenderer;
// created by ЗНАХАРКА АФТОДИЯ
import org.spongepowered.asm.mixin.Mixin;
// created by ЗНАХАРКА АФТОДИЯ
import org.spongepowered.asm.mixin.injection.At;
// created by ЗНАХАРКА АФТОДИЯ
import org.spongepowered.asm.mixin.injection.Inject;
// created by ЗНАХАРКА АФТОДИЯ
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
// created by ЗНАХАРКА АФТОДИЯ
import ru.hachclient.modules.impl.render.Hands;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
@Mixin(HeldItemRenderer.class)
// created by ЗНАХАРКА АФТОДИЯ
public class HandsItemRendererMixin {
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At("HEAD"))
// created by ЗНАХАРКА АФТОДИЯ
    private void hachclient$handsStart(CallbackInfo ci) {
// created by ЗНАХАРКА АФТОДИЯ
        Hands hands = Hands.INSTANCE;
// created by ЗНАХАРКА АФТОДИЯ
        if (hands == null || !hands.state() || !hands.isFill()) return;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        int color = hands.getTintColor();
// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.setShaderColor(
// created by ЗНАХАРКА АФТОДИЯ
                (color >> 16 & 0xFF) / 255.0F,
// created by ЗНАХАРКА АФТОДИЯ
                (color >> 8 & 0xFF) / 255.0F,
// created by ЗНАХАРКА АФТОДИЯ
                (color & 0xFF) / 255.0F,
// created by ЗНАХАРКА АФТОДИЯ
                1.0F
// created by ЗНАХАРКА АФТОДИЯ
        );
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At("RETURN"))
// created by ЗНАХАРКА АФТОДИЯ
    private void hachclient$handsEnd(CallbackInfo ci) {
// created by ЗНАХАРКА АФТОДИЯ
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ
}
// created by ЗНАХАРКА АФТОДИЯ
