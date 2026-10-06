package ru.hachclient.mixin;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
import net.minecraft.client.render.VertexConsumerProvider;
// created by ЗНАХАРКА АФТОДИЯ
import net.minecraft.client.render.item.HeldItemRenderer;
// created by ЗНАХАРКА АФТОДИЯ
import org.spongepowered.asm.mixin.Mixin;
// created by ЗНАХАРКА АФТОДИЯ
import org.spongepowered.asm.mixin.injection.At;
// created by ЗНАХАРКА АФТОДИЯ
import org.spongepowered.asm.mixin.injection.ModifyArg;
// created by ЗНАХАРКА АФТОДИЯ
import ru.hachclient.modules.impl.render.Hands;
// created by ЗНАХАРКА АФТОДИЯ
import ru.hachclient.utils.render.HandsTintProvider;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
@Mixin(HeldItemRenderer.class)
// created by ЗНАХАРКА АФТОДИЯ
public class HandsItemRendererMixin {
// created by ЗНАХАРКА АФТОДИЯ
    private static long hachclient$lastLog;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    @ModifyArg(
// created by ЗНАХАРКА АФТОДИЯ
            method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
// created by ЗНАХАРКА АФТОДИЯ
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"),
// created by ЗНАХАРКА АФТОДИЯ
            index = 8
// created by ЗНАХАРКА АФТОДИЯ
    )
// created by ЗНАХАРКА АФТОДИЯ
    private VertexConsumerProvider hachclient$tintHands(VertexConsumerProvider provider) {
// created by ЗНАХАРКА АФТОДИЯ
        Hands hands = Hands.INSTANCE;
// created by ЗНАХАРКА АФТОДИЯ
        if (System.currentTimeMillis() - hachclient$lastLog > 2000) {
// created by ЗНАХАРКА АФТОДИЯ
            hachclient$lastLog = System.currentTimeMillis();
// created by ЗНАХАРКА АФТОДИЯ
            System.out.println("[Hands] mixin ok, instance=" + (hands != null) + " state=" + (hands != null && hands.state()) + " fill=" + (hands != null && hands.isFill()) + " mode=" + (hands != null ? hands.mode.get() : "-"));
// created by ЗНАХАРКА АФТОДИЯ
        }
// created by ЗНАХАРКА АФТОДИЯ
        if (hands == null || !hands.state() || !hands.isFill()) return provider;
// created by ЗНАХАРКА АФТОДИЯ
        return new HandsTintProvider(provider, hands.getTintColor());
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ
}
// created by ЗНАХАРКА АФТОДИЯ
