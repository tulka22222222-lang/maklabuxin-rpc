package ru.hachclient.mixins;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
import net.minecraft.client.render.GameRenderer;
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
@Mixin(GameRenderer.class)
// created by ЗНАХАРКА АФТОДИЯ
public class MixinGameRenderer {
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    @Inject(method = "renderHand", at = @At("HEAD"))
// created by ЗНАХАРКА АФТОДИЯ
    private void hachclient$beginHands(CallbackInfo ci) {
// created by ЗНАХАРКА АФТОДИЯ
        if (Hands.INSTANCE != null) Hands.INSTANCE.beginCapture();
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    @Inject(method = "renderHand", at = @At("RETURN"))
// created by ЗНАХАРКА АФТОДИЯ
    private void hachclient$endHands(CallbackInfo ci) {
// created by ЗНАХАРКА АФТОДИЯ
        if (Hands.INSTANCE != null) Hands.INSTANCE.endCapture();
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ
}
// created by ЗНАХАРКА АФТОДИЯ
