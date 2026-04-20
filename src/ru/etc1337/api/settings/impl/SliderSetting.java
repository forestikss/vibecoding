package ru.etc1337.api.settings.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.Setter;
import ru.etc1337.api.settings.Setting;
import ru.etc1337.api.settings.api.Parent;

import java.util.function.BooleanSupplier;

@Getter @Setter
public class SliderSetting extends Setting {

    private float min, max, step, value;

    public SliderSetting(String name, Parent parent, float defaultValue, float min, float max, float step) {
        super(name, parent);
        this.step = step;
        this.min = min;
        this.max = max;
        this.value = defaultValue;
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(value);
    }

    @Override
    public void load(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            this.setCurrentValue(element.getAsFloat());
        }
    }

    public void setCurrentValue(float currentValue) {
        this.value = Math.max(min, Math.min(max, currentValue));
    }

    public float getValue() { return value; }
    public float get() { return value; }
}
