package ru.etc1337.api.settings;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.etc1337.api.settings.api.Parent;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.impl.render.Interface;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

import java.util.function.Supplier;


@Getter @Setter @RequiredArgsConstructor
public abstract class Setting {

    protected String name;
    protected String description;

    protected Supplier<Boolean> hideCondition;

    protected int key = -1;
    protected Parent parent;

    public abstract JsonElement save();

    public abstract void load(JsonElement element);

    public Setting(String name, Parent parent) {
        this(name, null, parent);
    }

    public Setting(String name, String description, Parent parent) {
        this.name = name;
        this.description = description;
        this.parent = parent;

        if (parent instanceof Module module) {
            module.getSettings().add(this);
            return;
        }

        if (parent instanceof UIElement module) {
            module.getSettings().add(this);
            return;
        }
    }

    public <T extends Setting> T setVisible(Supplier<Boolean> hide) {
        this.hideCondition = hide;
        return (T) this;
    }
}