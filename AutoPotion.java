package ru.hachclient.modules.impl.player;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import ru.hachclient.events.Event;
import ru.hachclient.events.EventListener;
import ru.hachclient.events.impl.EventUpdate;
import ru.hachclient.modules.Module;
import ru.hachclient.modules.ModuleType;
import ru.hachclient.modules.settings.impl.BooleanSetting;
import ru.hachclient.modules.settings.impl.MultiBooleanSetting;
import ru.hachclient.modules.settings.impl.NumberSetting;
import ru.hachclient.utils.math.TimerUtil;

import static ru.hachclient.Hachclient.mc;

// by ls5sq
/**
 * AutoPotion: кидает взрывные зелья силы, скорости и огнестойкости себе под ноги,
 * когда соответствующего эффекта нет. Поворот вниз видит только сервер,
 * камера игрока не двигается. Слот возвращается в том же тике.
 */
public class AutoPotion extends Module {

    // SETTINGS
    BooleanSetting onlyOnGround = new BooleanSetting("Only On Ground", true);
    BooleanSetting notWhileFlying = new BooleanSetting("Not While Flying", true);
    MultiBooleanSetting potions = new MultiBooleanSetting("Potions",
            new BooleanSetting("Strength", true),
            new BooleanSetting("Speed", true),
            new BooleanSetting("Fire Resistance", true)
    );
    NumberSetting delay = new NumberSetting("Delay (ms)", 500, 100, 5000, 50);

    // UTILS
    final TimerUtil timer = new TimerUtil();

    public AutoPotion() {
        super("AutoPotion", "Кидает зелья силы, скорости и огнестойкости под себя", ModuleType.PLAYER);
        addSettings(onlyOnGround, notWhileFlying, potions, delay);
    }

    EventListener<Event> onEvent = event -> {
        if (!(event instanceof EventUpdate)) return;
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null || mc.player.isUsingItem()) return;

        if (onlyOnGround.get() && !mc.player.isOnGround()) return;
        if (notWhileFlying.get() && isFlying()) return;

        int slot = findPotionSlot();
        if (slot == -1) return;

        if (!timer.hasTimeElapsed((long) delay.get(), true)) return;

        throwUnderSelf(slot);
    };

    // /fly, элитры или левитация: зелье под ноги всё равно не долетит.
    private boolean isFlying() {
        return mc.player.getAbilities().flying
                || mc.player.isGliding()
                || mc.player.hasStatusEffect(StatusEffects.LEVITATION);
    }

    private void throwUnderSelf(int slot) {
        PlayerInventory inventory = mc.player.getInventory();
        int prevSlot = inventory.selectedSlot;
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        boolean onGround = mc.player.isOnGround();
        boolean collision = mc.player.horizontalCollision;

        // Поворот вниз только для сервера.
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, 90f, onGround, collision));

        // interactItem сам синхронизирует слот и шлёт пакет с текущим поворотом,
        // поэтому на время вызова ставим pitch 90 и сразу возвращаем: кадр
        // ещё не отрисован, камера не дёрнется.
        inventory.selectedSlot = slot;
        mc.player.setPitch(90f);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.setPitch(pitch);
        inventory.selectedSlot = prevSlot;

        // Возвращаем серверу настоящий поворот, иначе он так и будет считать, что мы смотрим вниз.
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, onGround, collision));
        // Прежний слот уйдёт на сервер в interactionManager.tick() этого же тика.
    }

    private int findPotionSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (shouldThrow(stack)) return i;
        }
        return -1;
    }

    private boolean shouldThrow(ItemStack stack) {
        if (stack.isEmpty() || !stack.isOf(Items.SPLASH_POTION)) return false;

        PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (contents == null) return false;

        for (StatusEffectInstance instance : contents.getEffects()) {
            StatusEffect effect = instance.getEffectType().value();
            if (potions.get(0) && effect == StatusEffects.STRENGTH.value()
                    && !mc.player.hasStatusEffect(StatusEffects.STRENGTH)) return true;
            if (potions.get(1) && effect == StatusEffects.SPEED.value()
                    && !mc.player.hasStatusEffect(StatusEffects.SPEED)) return true;
            if (potions.get(2) && effect == StatusEffects.FIRE_RESISTANCE.value()
                    && !mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) return true;
        }
        return false;
    }
}
