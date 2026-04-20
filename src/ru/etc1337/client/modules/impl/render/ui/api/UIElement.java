package ru.etc1337.client.modules.impl.render.ui.api;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.vector.Vector2f;
import ru.etc1337.api.draggable.Draggable;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.render.ui.dropui.elements.extended.*;
import ru.etc1337.api.settings.Setting;
import ru.etc1337.api.settings.api.Parent;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class UIElement extends Parent implements QuickImports {
    private final ElementInfo elementInfo = this.getClass().getAnnotation(ElementInfo.class);
    private final List<Setting> settings = new ArrayList<>();
    private final List<BooleanRenderer> checkboxes = new ArrayList<>();
    private final List<SliderRenderer> sliders = new ArrayList<>();
    private final List<BindRenderer> binds = new ArrayList<>();
    private final List<ColorRenderer> colors = new ArrayList<>();
    private final List<ModeRenderer> modes = new ArrayList<>();
    private final List<MultiModeRenderer> multiModes = new ArrayList<>();
    private boolean settingsWindow;
    private Vector2f clickedMousePosition = Vector2f.ZERO;

    private String name;
    private String icon;
    private float initHeight;

    private final Draggable draggable;

    public UIElement() {
        this.name = elementInfo.name();
        this.icon = elementInfo.icon();
        this.initHeight = elementInfo.initHeight();
        this.draggable = new Draggable(this.name, elementInfo.initX(), elementInfo.initY(), 0.0F, this.initHeight);
    }

    public void onEvent(Event event) { } // Listener
}