package ru.hachclient.modules.settings.impl;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
import com.google.gson.JsonObject;
// created by ЗНАХАРКА АФТОДИЯ
import ru.aloweeed.perfect.Ignored;
// created by ЗНАХАРКА АФТОДИЯ
import ru.hachclient.modules.settings.Setting;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
@Ignored
// created by ЗНАХАРКА АФТОДИЯ
public class SliderSetting extends Setting<SliderSetting> {
// created by ЗНАХАРКА АФТОДИЯ
    private float value;
// created by ЗНАХАРКА АФТОДИЯ
    private final float min;
// created by ЗНАХАРКА АФТОДИЯ
    private final float max;
// created by ЗНАХАРКА АФТОДИЯ
    private final float step;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public SliderSetting(String name, float defaultValue, float min, float max, float step) {
// created by ЗНАХАРКА АФТОДИЯ
        this.name = name;
// created by ЗНАХАРКА АФТОДИЯ
        this.min = min;
// created by ЗНАХАРКА АФТОДИЯ
        this.max = max;
// created by ЗНАХАРКА АФТОДИЯ
        this.step = step;
// created by ЗНАХАРКА АФТОДИЯ
        setValue(defaultValue);
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public float get() {
// created by ЗНАХАРКА АФТОДИЯ
        return value;
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public void setValue(float value) {
// created by ЗНАХАРКА АФТОДИЯ
        float clamped = Math.max(min, Math.min(max, value));
// created by ЗНАХАРКА АФТОДИЯ
        if (step > 0.0F) {
// created by ЗНАХАРКА АФТОДИЯ
            clamped = min + Math.round((clamped - min) / step) * step;
// created by ЗНАХАРКА АФТОДИЯ
            clamped = Math.max(min, Math.min(max, clamped));
// created by ЗНАХАРКА АФТОДИЯ
        }
// created by ЗНАХАРКА АФТОДИЯ
        this.value = clamped;
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public float getMin() {
// created by ЗНАХАРКА АФТОДИЯ
        return min;
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public float getMax() {
// created by ЗНАХАРКА АФТОДИЯ
        return max;
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public float getStep() {
// created by ЗНАХАРКА АФТОДИЯ
        return step;
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    @Override
// created by ЗНАХАРКА АФТОДИЯ
    public void save(JsonObject json) {
// created by ЗНАХАРКА АФТОДИЯ
        super.save(json);
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        json.addProperty(this.name, value);
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    @Override
// created by ЗНАХАРКА АФТОДИЯ
    public void load(JsonObject json) {
// created by ЗНАХАРКА АФТОДИЯ
        super.load(json);
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        if (json.has(this.name)) {
// created by ЗНАХАРКА АФТОДИЯ
            setValue(json.get(this.name).getAsFloat());
// created by ЗНАХАРКА АФТОДИЯ
        }
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ
}
// created by ЗНАХАРКА АФТОДИЯ
