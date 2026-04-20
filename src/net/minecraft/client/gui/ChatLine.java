package net.minecraft.client.gui;

import lombok.Getter;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.Easing;

public class ChatLine<T>
{
    private final int updateCounterCreated;
    private final T lineString;
    private final int chatLineID;
    @Getter
    private Animation slideAnimation;

    public ChatLine(int updatedCounterCreated, T lineString, int chatLineID)
    {
        this.slideAnimation = new Animation(Easing.SINE_IN_OUT, 200);
        this.lineString = lineString;
        this.updateCounterCreated = updatedCounterCreated;
        this.chatLineID = chatLineID;
    }

    public T getLineString()
    {
        return this.lineString;
    }

    public int getUpdatedCounter()
    {
        return this.updateCounterCreated;
    }

    public int getChatLineID()
    {
        return this.chatLineID;
    }
}
