package ru.hachclient.modules.impl.player;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import ru.hachclient.events.impl.EventInput;
import ru.hachclient.events.impl.EventPacket;
import ru.hachclient.events.impl.EventUpdate;
import ru.hachclient.gui.widgets.impl.ColorWidget;
import ru.hachclient.modules.Module;
import ru.hachclient.modules.ModuleManager;
import ru.hachclient.modules.ModuleType;
import ru.hachclient.modules.impl.player.invmove.IModeInvMove;
import ru.hachclient.modules.impl.player.invmove.impl.ModeFast;
import ru.hachclient.modules.impl.player.invmove.impl.ModeLegit;
import ru.hachclient.modules.impl.player.invmove.impl.ModeSilent;
import ru.hachclient.modules.settings.impl.ModeSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static ru.hachclient.Hachclient.mc;

// created by aloweeed -> 17.04.2025
public class InvMove extends Module {
    public InvMove() {
        super("InvMove", "hachclient.descriptions.invmove", ModuleType.PLAYER);
        addSettings(mode);
    }

    public static boolean texthovered = false;

    ModeSetting mode = new ModeSetting("Mode", "Legit", "Legit", "Fast", "Silent");

    public static ArrayList<Packet<?>> packets = new ArrayList<>();

    public boolean could_sent = false;
    public int ticks = 0;
    public int interval = 0;

    ArrayList<Integer> ignoreSlots = new ArrayList<>();

    Map<String, IModeInvMove> modes = Map.of(
            "Legit", new ModeLegit(),
            "Fast", new ModeFast(),
            "Silent", new ModeSilent()
    );

    public void addIgnoreSlots(Integer... slots) {
        ignoreSlots.addAll(Arrays.asList(slots));
    }

    public void onInput(EventInput e) {
        if (ticks > 0) {
            e.setForward(0);
            e.setStrafe(0);
            e.cancel();
        }
    }

    public static void addPacket(Packet<?> packet) {
        if (ModuleManager.invMove.state())
            packets.add(packet);
        else
            mc.getNetworkHandler().sendPacket(packet);
    }

    public void onUpdate(EventUpdate e) {
        modes.get(mode.get()).process(packets, this);
        if (packets.isEmpty() && interval > 0) {
            interval--;
        }
    }

    public void onPacket(EventPacket e) {
        if (e.isReceive() && (e.getPacket() instanceof InventoryS2CPacket
                || e.getPacket() instanceof PlayerRespawnS2CPacket
                || e.getPacket() instanceof GameJoinS2CPacket)) {
            // Сервер выдал инвентарь заново (кит, респавн): старые клики относятся
            // к прошлому инвентарю и переложат не тот предмет, поэтому выкидываем их.
            packets.clear();
            ignoreSlots.clear();
            ticks = 0;
            interval = 0;
            could_sent = false;
            return;
        }

        if (mc.player == null)
            return;

        if (e.isSend() && !mc.player.isGliding()) {
            if (e.getPacket() instanceof ClickSlotC2SPacket clickSlotC2SPacket) {
                packets.add(clickSlotC2SPacket);
                e.cancel();
            }
            if (e.getPacket() instanceof CloseHandledScreenC2SPacket) {
                e.cancel();
                could_sent = true;
                ticks = 3;
            }
        }
        if (e.isReceive()) {
            if (e.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket p && !packets.isEmpty() && interval <= 0) {
                if (ignoreSlots.contains(p.getSlot())) {
                    ignoreSlots.remove((Integer) p.getSlot());
                    e.cancel();
                }
            }
        }
    }

    public void handleKeybinds() {
        KeyBinding[] keyBindings = new KeyBinding[] {
                mc.options.jumpKey, mc.options.forwardKey, mc.options.leftKey, mc.options.rightKey, mc.options.backKey
        };
        for (KeyBinding keyBinding : keyBindings) {
            boolean is = InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyBinding.getDefaultKey().getCode()) && !texthovered && !ColorWidget.typinghex && !ColorWidget.typingalpha;

            keyBinding.setPressed(is);
        }
    }
}
