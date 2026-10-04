package dev.maklabuxin.visuals.event;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class EventRender extends Event {
    public final RenderType type;
    public final MatrixStack position;
    public final DrawContext draw;
    public final float tickDelta;

    private EventRender(RenderType type, MatrixStack position, DrawContext draw, float tickDelta) {
        this.type = type;
        this.position = position;
        this.draw = draw;
        this.tickDelta = tickDelta;
    }

    public static EventRender build(RenderType type, MatrixStack position, float tickDelta) {
        return new EventRender(type, position, null, tickDelta);
    }

    public static EventRender build(RenderType type, DrawContext dc, float tickDelta) {
        return new EventRender(type, null, dc, tickDelta);
    }

    public boolean is2d() {
        return type == RenderType.HUD;
    }

    public boolean is3d() {
        return type == RenderType.WORLD;
    }

    public enum RenderType {
        HUD,
        WORLD
    }
}
