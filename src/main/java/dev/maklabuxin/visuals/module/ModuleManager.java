package dev.maklabuxin.visuals.module;

import dev.maklabuxin.visuals.module.render.SkyShader;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static SkyShader skyShader;

    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        skyShader = add(new SkyShader());
    }

    private <T extends Module> T add(T module) {
        modules.add(module);
        return module;
    }

    public List<Module> getModules() {
        return modules;
    }
}
