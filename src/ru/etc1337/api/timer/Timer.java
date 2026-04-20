package ru.etc1337.api.timer;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

@Getter
@Setter
public class Timer {
    private long startTime = System.currentTimeMillis();

    private long millis;

    public Timer() {
        reset();
    }

    public static Timer create() {
        return new Timer();
    }

    public boolean finished(long delay) {
        return System.currentTimeMillis() - delay >= millis;
    }

    public void reset() {
        this.millis = System.currentTimeMillis();
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - this.millis;
    }

    public double deltaTime() {
        return Minecraft.getDebugFPS() > 0 ? (1.0000 / Minecraft.getDebugFPS()) : 1;
    }
    public boolean every(long ms) {
        boolean passed = getMillis(System.nanoTime() - millis) >= ms;
        if (passed)
            reset();
        return passed;
    }
    public boolean passed(long time) {
        return System.currentTimeMillis() - startTime > time;
    }
    public long getMillis(long time) {
        return time / 1000000L;
    }
}