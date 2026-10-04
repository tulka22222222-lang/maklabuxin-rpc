package dev.maklabuxin.visuals.module;

import dev.maklabuxin.visuals.Maklabuxin;
import net.minecraft.client.MinecraftClient;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final Category category;
    private boolean enabled;

    protected Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public boolean state() {
        return enabled;
    }

    public void setState(boolean state) {
        if (enabled == state) return;
        enabled = state;
        if (state) {
            Maklabuxin.getInstance().getEventHandler().register(this);
            onEnable();
        } else {
            Maklabuxin.getInstance().getEventHandler().unregister(this);
            onDisable();
        }
    }

    public void toggle() {
        setState(!enabled);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }
}
