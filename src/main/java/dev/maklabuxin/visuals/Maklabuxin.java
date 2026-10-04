package dev.maklabuxin.visuals;

import dev.maklabuxin.visuals.event.EventHandler;
import dev.maklabuxin.visuals.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class Maklabuxin implements ClientModInitializer {
    public static final String MOD_ID = "maklabuxin";
    public static final MinecraftClient mc = MinecraftClient.getInstance();

    private static Maklabuxin instance;
    private final EventHandler eventHandler = new EventHandler();
    private ModuleManager moduleManager;

    public static Maklabuxin getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        moduleManager = new ModuleManager();

        // Временный бинд для теста: K включает/выключает SkyShader.
        KeyBinding skyShaderKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.maklabuxin.skyshader", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.maklabuxin"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (skyShaderKey.wasPressed()) ModuleManager.skyShader.toggle();
        });
    }

    public EventHandler getEventHandler() {
        return eventHandler;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}
