package ru.etc1337.api.animations.simple;

import lombok.Getter;
import lombok.Setter;
import ru.etc1337.api.color.FixColor;

import java.awt.*;

@Getter @Setter
public class ColorAnimation {

    private final long duration;
    private final Animation r, g, b, a;

    public ColorAnimation(long duration) {
        this.duration = duration;
        r = new Animation(Easing.SINE_IN_OUT, duration);
        g = new Animation(Easing.SINE_IN_OUT, duration);
        b = new Animation(Easing.SINE_IN_OUT, duration);
        a = new Animation(Easing.SINE_IN_OUT, duration);
    }

    public void update(Color color) {
        r.update(color.getRed());
        g.update(color.getGreen());
        b.update(color.getBlue());
        a.update(color.getAlpha());
    }

    public FixColor getColor() {
        return new FixColor((int) r.getValue(), (int) g.getValue(), (int) b.getValue(), (int) a.getValue());
    }

    public void setEasing(Easing easing) {
        r.setEasing(easing);
        g.setEasing(easing);
        b.setEasing(easing);
        a.setEasing(easing);
    }

    public void setColor(Color color) {
        r.setValue(color.getRed());
        g.setValue(color.getGreen());
        b.setValue(color.getBlue());
        a.setValue(color.getAlpha());
    }

    public void setDuration(long duration) {
        r.setDuration(duration);
        g.setDuration(duration);
        b.setDuration(duration);
        a.setDuration(duration);
    }
}