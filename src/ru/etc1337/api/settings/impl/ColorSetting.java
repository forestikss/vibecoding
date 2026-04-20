package ru.etc1337.api.settings.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.Setter;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.settings.Setting;
import ru.etc1337.api.settings.api.Parent;

import java.awt.*;
import java.util.function.BooleanSupplier;

@Getter @Setter
public class ColorSetting extends Setting {
    public int color = 0;
    public boolean rainbow;

    public ColorSetting(String name, Parent parent, int color) {
        super(name, parent);
        this.color = color;
    }


    public int get() {
        return color;
    }

    public String getHex() {
        return String.format("#%06X", (0xFFFFFF & get()));
    }

    public FixColor getColor() {
        return new FixColor(get());
    }


    @Override
    public JsonElement save() {
        return new JsonPrimitive(color);
    }

    @Override
    public void load(JsonElement element) {
        this.color = element.getAsInt();
    }
}
