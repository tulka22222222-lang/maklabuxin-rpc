package ru.hachclient.modules.impl.render;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
import ru.hachclient.modules.Module;
// created by ЗНАХАРКА АФТОДИЯ
import ru.hachclient.modules.ModuleType;
// created by ЗНАХАРКА АФТОДИЯ
import ru.hachclient.modules.settings.impl.BooleanSetting;
// created by ЗНАХАРКА АФТОДИЯ
import ru.hachclient.modules.settings.impl.ColorSetting;
// created by ЗНАХАРКА АФТОДИЯ
import ru.hachclient.modules.settings.impl.ModeSetting;
// created by ЗНАХАРКА АФТОДИЯ
import ru.hachclient.modules.settings.impl.SliderSetting;
// created by ЗНАХАРКА АФТОДИЯ
import ru.hachclient.themes.Themes;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
import java.awt.Color;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
public class Hands extends Module {
// created by ЗНАХАРКА АФТОДИЯ
    public static Hands INSTANCE;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    private long startTime = System.currentTimeMillis();
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    // ===== Режим =====
// created by ЗНАХАРКА АФТОДИЯ
    public final ModeSetting mode = new ModeSetting("Режим", "Заливка", "Nothing", "Заливка", "Шейдер", "Зеркало", "Шейдер + Зеркало");
// created by ЗНАХАРКА АФТОДИЯ
    public final BooleanSetting autoThemeColor = new BooleanSetting("Авто цвет под тему", true);
// created by ЗНАХАРКА АФТОДИЯ
    public final BooleanSetting autoItemColor = new BooleanSetting("Авто цвет под предмет", false);
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    // ===== Зеркало =====
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting mirrorBlend = new SliderSetting("Смешивание", 0.5F, 0.0F, 1.0F, 0.01F).setVisible(this::isMirror);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting mirrorDistortion = new SliderSetting("Искажение зеркала", 0.0F, 0.0F, 1.0F, 0.05F).setVisible(this::isMirror);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting blurStrength = new SliderSetting("Сила размытия", 1.0F, 0.1F, 3.0F, 0.05F).setVisible(this::isMirror);
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    // ===== Заливка =====
// created by ЗНАХАРКА АФТОДИЯ
    public final BooleanSetting rainbow = new BooleanSetting("Радужная", false).setVisible(this::isFill);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting rainbowSpeed = new SliderSetting("Скорость радуги", 0.4F, 0.0F, 2.0F, 0.05F).setVisible(() -> isFill() && rainbow.get());
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting rainbowScale = new SliderSetting("Масштаб радуги", 1.0F, 0.2F, 3.0F, 0.1F).setVisible(() -> isFill() && rainbow.get());
// created by ЗНАХАРКА АФТОДИЯ
    public final ColorSetting fillColor = new ColorSetting("Цвет заливки", new Color(-48060, true)).setVisible(() -> isFill() && !rainbow.get() && !autoThemeColor.get());
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting fillAlpha = new SliderSetting("Прозрачность заливки", 0.8F, 0.0F, 1.0F, 0.05F).setVisible(this::isFill);
// created by ЗНАХАРКА АФТОДИЯ
    public final BooleanSetting keepShadows = new BooleanSetting("Сохранить тени", true).setVisible(this::isFill);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting shadowStrength = new SliderSetting("Сила теней", 0.3F, 0.0F, 1.0F, 0.05F).setVisible(() -> isFill() && keepShadows.get());
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    // ===== Шейдер =====
// created by ЗНАХАРКА АФТОДИЯ
    public final ModeSetting shaderStyle = new ModeSetting("Стиль шейдера", "Шейдер", "Шейдер", "HandShader", "Туманность", "Космос", "Градиент", "Homie").setVisible(this::isShader);
// created by ЗНАХАРКА АФТОДИЯ
    public final ColorSetting shaderColor1 = new ColorSetting("Цвет шейдера 1", new Color(-3632385, true)).setVisible(() -> isShader() && !autoThemeColor.get());
// created by ЗНАХАРКА АФТОДИЯ
    public final BooleanSetting secondColor = new BooleanSetting("Второй цвет", false).setVisible(this::isShader);
// created by ЗНАХАРКА АФТОДИЯ
    public final ColorSetting shaderColor2 = new ColorSetting("Цвет шейдера 2", new Color(-11534136, true)).setVisible(() -> isShader() && secondColor.get() && !autoThemeColor.get());
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting shaderSpeed = new SliderSetting("Скорость шейдера", 1.5F, 0.05F, 2.5F, 0.05F).setVisible(this::isShader);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting shaderScale = new SliderSetting("Масштаб шейдера", 1.5F, 0.2F, 5.0F, 0.1F).setVisible(this::isShader);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting shaderGlow = new SliderSetting("Свечение шейдера", 1.2F, 0.0F, 3.0F, 0.05F).setVisible(this::isShader);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting shaderQuality = new SliderSetting("Качество шейдера", 2.0F, 1.0F, 3.0F, 1.0F).setVisible(() -> isShader() && !shaderStyle.is("Градиент"));
// created by ЗНАХАРКА АФТОДИЯ
    public final BooleanSetting stars = new BooleanSetting("Звёзды", true).setVisible(this::isShader);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting shaderAlpha = new SliderSetting("Прозрачность шейдера", 1.0F, 0.0F, 1.0F, 0.05F).setVisible(this::isShader);
// created by ЗНАХАРКА АФТОДИЯ
    public final BooleanSetting shaderKeepShadows = new BooleanSetting("Сохранить тени шейдера", false).setVisible(this::isShader);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting shaderShadowStrength = new SliderSetting("Сила теней шейдера", 0.5F, 0.0F, 1.0F, 0.05F).setVisible(() -> isShader() && shaderKeepShadows.get());
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    // ===== Глов =====
// created by ЗНАХАРКА АФТОДИЯ
    public final BooleanSetting glow = new BooleanSetting("Глов", true);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting glowBlur = new SliderSetting("Размытие", 3.0F, 1.0F, 5.0F, 1.0F).setVisible(glow::get);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting glowBrightness = new SliderSetting("Яркость", 1.0F, 0.5F, 5.0F, 0.1F).setVisible(glow::get);
// created by ЗНАХАРКА АФТОДИЯ
    public final ColorSetting glowColor1 = new ColorSetting("Цвет глова 1", new Color(-11713, true)).setVisible(() -> glow.get() && !autoThemeColor.get() && !autoItemColor.get());
// created by ЗНАХАРКА АФТОДИЯ
    public final ColorSetting glowColor2 = new ColorSetting("Цвет глова 2", new Color(-54784, true)).setVisible(() -> glow.get() && !autoThemeColor.get() && !autoItemColor.get());
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting innerGlow = new SliderSetting("Внутреннее свечение", 0.65F, 0.0F, 1.5F, 0.05F).setVisible(glow::get);
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    // ===== Обводка =====
// created by ЗНАХАРКА АФТОДИЯ
    public final BooleanSetting outline = new BooleanSetting("Обводка", true);
// created by ЗНАХАРКА АФТОДИЯ
    public final ColorSetting outlineColor = new ColorSetting("Цвет обводки", new Color(-1, true)).setVisible(() -> outline.get() && !autoThemeColor.get() && !autoItemColor.get());
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting outlineWidth = new SliderSetting("Толщина обводки", 1.0F, 0.5F, 2.0F, 0.5F).setVisible(outline::get);
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    // ===== Шлейф =====
// created by ЗНАХАРКА АФТОДИЯ
    public final BooleanSetting trail = new BooleanSetting("Шлейф", true).setVisible(glow::get);
// created by ЗНАХАРКА АФТОДИЯ
    public final ModeSetting trailMode = new ModeSetting("Режим шлейфа", "Обычный", "Обычный", "Энергия", "Ленты").setVisible(this::isTrail);
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting energyLength = new SliderSetting("Длина энергии", 1.0F, 0.4F, 2.0F, 0.05F).setVisible(this::isEnergyTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting energyBrightness = new SliderSetting("Яркость энергии", 1.35F, 0.5F, 2.5F, 0.05F).setVisible(this::isEnergyTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting energyMotion = new SliderSetting("Движение энергии", 1.3F, 0.0F, 2.0F, 0.05F).setVisible(this::isEnergyTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting energyPulse = new SliderSetting("Пульсация энергии", 1.0F, 0.0F, 2.0F, 0.05F).setVisible(this::isEnergyTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting energyCamera = new SliderSetting("Шлейф за камерой", 0.05F, 0.0F, 2.0F, 0.05F).setVisible(this::isEnergyTrail);
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting ribbonLength = new SliderSetting("Длина лент", 0.3F, 0.15F, 3.0F, 0.05F).setVisible(this::isRibbonTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting ribbonWidth = new SliderSetting("Ширина лент", 0.55F, 0.25F, 1.5F, 0.05F).setVisible(this::isRibbonTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting ribbonBrightness = new SliderSetting("Яркость лент", 1.0F, 0.0F, 2.5F, 0.05F).setVisible(this::isRibbonTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting ribbonCamera = new SliderSetting("Сила лент за камерой", 0.1F, 0.0F, 2.0F, 0.05F).setVisible(this::isRibbonTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting ribbonLift = new SliderSetting("Подъём лент", 0.32F, 0.0F, 1.0F, 0.01F).setVisible(this::isRibbonTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting ribbonBend = new SliderSetting("Изгиб лент", 2.0F, 0.0F, 2.0F, 0.05F).setVisible(this::isRibbonTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting waveSpeed = new SliderSetting("Скорость волн", 1.5F, 0.0F, 3.0F, 0.05F).setVisible(this::isRibbonTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final BooleanSetting ribbonsOnHit = new BooleanSetting("Ленты при ударе", true).setVisible(this::isRibbonTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting hitLength = new SliderSetting("Длина при ударе", 0.15F, 0.15F, 3.0F, 0.05F).setVisible(() -> isRibbonTrail() && ribbonsOnHit.get());
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting hitStrength = new SliderSetting("Сила при ударе", 1.15F, 0.0F, 2.5F, 0.05F).setVisible(() -> isRibbonTrail() && ribbonsOnHit.get());
// created by ЗНАХАРКА АФТОДИЯ
    public final BooleanSetting ribbonsOnEat = new BooleanSetting("Ленты при еде", true).setVisible(this::isRibbonTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting eatLength = new SliderSetting("Длина при еде", 0.65F, 0.15F, 3.0F, 0.05F).setVisible(() -> isRibbonTrail() && ribbonsOnEat.get());
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting eatStrength = new SliderSetting("Сила при еде", 0.7F, 0.0F, 2.5F, 0.05F).setVisible(() -> isRibbonTrail() && ribbonsOnEat.get());
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting fadeSpeed = new SliderSetting("Скорость затухания", 0.04F, 0.002F, 0.2F, 0.002F).setVisible(this::isNormalTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting lift = new SliderSetting("Подъём", 0.2F, 0.0F, 1.5F, 0.05F).setVisible(this::isNormalTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting sway = new SliderSetting("Качание", 0.0F, 0.0F, 0.2F, 0.005F).setVisible(this::isNormalTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting cameraTrail = new SliderSetting("Шлейф от камеры", 0.0F, 0.0F, 2.0F, 0.05F).setVisible(this::isNormalTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting turbulence = new SliderSetting("Турбулентность", 0.02F, 0.0F, 0.6F, 0.01F).setVisible(this::isNormalTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting flicker = new SliderSetting("Мерцание", 0.2F, 0.0F, 0.2F, 0.01F).setVisible(this::isNormalTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final BooleanSetting blowOnHit = new BooleanSetting("Сдув при ударе", false).setVisible(this::isNormalTrail);
// created by ЗНАХАРКА АФТОДИЯ
    public final SliderSetting blowStrength = new SliderSetting("Сила сдува", 4.0F, 1.0F, 10.0F, 0.5F).setVisible(() -> isNormalTrail() && blowOnHit.get());
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public Hands() {
// created by ЗНАХАРКА АФТОДИЯ
        super("Hands", "Визуальные эффекты рук от первого лица", ModuleType.VISUAL);
// created by ЗНАХАРКА АФТОДИЯ
        INSTANCE = this;
// created by ЗНАХАРКА АФТОДИЯ
        addSettings(
// created by ЗНАХАРКА АФТОДИЯ
                mode, autoThemeColor, autoItemColor,
// created by ЗНАХАРКА АФТОДИЯ
                mirrorBlend, mirrorDistortion, blurStrength,
// created by ЗНАХАРКА АФТОДИЯ
                rainbow, rainbowSpeed, rainbowScale, fillColor, fillAlpha, keepShadows, shadowStrength,
// created by ЗНАХАРКА АФТОДИЯ
                shaderStyle, shaderColor1, secondColor, shaderColor2, shaderSpeed, shaderScale, shaderGlow,
// created by ЗНАХАРКА АФТОДИЯ
                shaderQuality, stars, shaderAlpha, shaderKeepShadows, shaderShadowStrength,
// created by ЗНАХАРКА АФТОДИЯ
                glow, glowBlur, glowBrightness, glowColor1, glowColor2, innerGlow,
// created by ЗНАХАРКА АФТОДИЯ
                outline, outlineColor, outlineWidth,
// created by ЗНАХАРКА АФТОДИЯ
                trail, trailMode,
// created by ЗНАХАРКА АФТОДИЯ
                energyLength, energyBrightness, energyMotion, energyPulse, energyCamera,
// created by ЗНАХАРКА АФТОДИЯ
                ribbonLength, ribbonWidth, ribbonBrightness, ribbonCamera, ribbonLift, ribbonBend, waveSpeed,
// created by ЗНАХАРКА АФТОДИЯ
                ribbonsOnHit, hitLength, hitStrength, ribbonsOnEat, eatLength, eatStrength,
// created by ЗНАХАРКА АФТОДИЯ
                fadeSpeed, lift, sway, cameraTrail, turbulence, flicker, blowOnHit, blowStrength
// created by ЗНАХАРКА АФТОДИЯ
        );
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    @Override
// created by ЗНАХАРКА АФТОДИЯ
    public void enabled() {
// created by ЗНАХАРКА АФТОДИЯ
        startTime = System.currentTimeMillis();
// created by ЗНАХАРКА АФТОДИЯ
        super.enabled();
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    // старые вызовы из HeldItemRendererMixin, если они там есть - теперь ничего не делают
// created by ЗНАХАРКА АФТОДИЯ
    public void beginCapture() {
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public void endCapture() {
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    // ===== Цвет рук для заливки =====
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public int getTintColor() {
// created by ЗНАХАРКА АФТОДИЯ
        int color = rainbow.get() ? getRainbowColor() : getFillColor();
// created by ЗНАХАРКА АФТОДИЯ
        float strength = fillAlpha.get();
// created by ЗНАХАРКА АФТОДИЯ
        if (keepShadows.get()) {
// created by ЗНАХАРКА АФТОДИЯ
            strength *= 1.0F - shadowStrength.get() * 0.5F;
// created by ЗНАХАРКА АФТОДИЯ
        }
// created by ЗНАХАРКА АФТОДИЯ
        return mix(0xFFFFFFFF, color, strength);
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    private int getRainbowColor() {
// created by ЗНАХАРКА АФТОДИЯ
        float hue = (getTime() * rainbowSpeed.get() * 0.25F * rainbowScale.get()) % 1.0F;
// created by ЗНАХАРКА АФТОДИЯ
        return Color.HSBtoRGB(hue, 0.7F, 1.0F) | 0xFF000000;
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    private static int mix(int from, int to, float t) {
// created by ЗНАХАРКА АФТОДИЯ
        t = Math.max(0.0F, Math.min(1.0F, t));
// created by ЗНАХАРКА АФТОДИЯ
        int r = Math.round((from >> 16 & 0xFF) + ((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * t);
// created by ЗНАХАРКА АФТОДИЯ
        int g = Math.round((from >> 8 & 0xFF) + ((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * t);
// created by ЗНАХАРКА АФТОДИЯ
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
// created by ЗНАХАРКА АФТОДИЯ
        return 0xFF000000 | r << 16 | g << 8 | b;
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public float getTime() {
// created by ЗНАХАРКА АФТОДИЯ
        return (System.currentTimeMillis() - startTime) / 1000.0F;
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    // ===== Режимы =====
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public boolean isNothing() {
// created by ЗНАХАРКА АФТОДИЯ
        return mode.is("Nothing");
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public boolean isFill() {
// created by ЗНАХАРКА АФТОДИЯ
        return mode.is("Заливка");
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public boolean isShader() {
// created by ЗНАХАРКА АФТОДИЯ
        return mode.is("Шейдер") || isShaderMirror();
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public boolean isMirror() {
// created by ЗНАХАРКА АФТОДИЯ
        return mode.is("Зеркало") || isShaderMirror();
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public boolean isShaderMirror() {
// created by ЗНАХАРКА АФТОДИЯ
        return mode.is("Шейдер + Зеркало");
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public boolean isTrail() {
// created by ЗНАХАРКА АФТОДИЯ
        return glow.get() && trail.get();
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public boolean isNormalTrail() {
// created by ЗНАХАРКА АФТОДИЯ
        return isTrail() && trailMode.is("Обычный");
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public boolean isEnergyTrail() {
// created by ЗНАХАРКА АФТОДИЯ
        return isTrail() && trailMode.is("Энергия");
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public boolean isRibbonTrail() {
// created by ЗНАХАРКА АФТОДИЯ
        return isTrail() && trailMode.is("Ленты");
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    // ===== Цвета =====
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public int getThemeColor(boolean second) {
// created by ЗНАХАРКА АФТОДИЯ
        Color[] colors = Themes.DEFAULT.getColors();
// created by ЗНАХАРКА АФТОДИЯ
        return colors[second && colors.length > 1 ? 1 : 0].getRGB();
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public int getFillColor() {
// created by ЗНАХАРКА АФТОДИЯ
        return autoThemeColor.get() ? getThemeColor(false) : fillColor.get();
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ
}
// created by ЗНАХАРКА АФТОДИЯ
