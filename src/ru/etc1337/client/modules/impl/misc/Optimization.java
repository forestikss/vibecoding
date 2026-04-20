package ru.etc1337.client.modules.impl.misc;

import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Optimization", description = "Оптимизация FPS", category = ModuleCategory.MISC)
public class Optimization extends Module {

    private final BooleanSetting noParticles   = new BooleanSetting("Убрать частицы", this);
    private final BooleanSetting noWeather     = new BooleanSetting("Убрать погоду", this);
    private final BooleanSetting noSky         = new BooleanSetting("Убрать небо", this);
    private final BooleanSetting noFog         = new BooleanSetting("Убрать туман", this);
    private final BooleanSetting noEntityShadow = new BooleanSetting("Убрать тени", this);
    private final BooleanSetting limitEntities = new BooleanSetting("Лимит сущностей", this);

    // сохранённые значения
    private int savedParticles = -1;
    private boolean savedWeather = false;
    private int savedFog = -1;

    @Override
    public void onEnable() {
        apply();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        restore();
        super.onDisable();
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventUpdate)) return;
        apply();
    }

    private void apply() {
        if (mc.gameSettings == null) return;

        if (noParticles.isEnabled()) {
            if (savedParticles == -1) savedParticles = mc.gameSettings.particles.ordinal();
            // 2 = minimal
            if (mc.gameSettings.particles.ordinal() != 2) {
                mc.gameSettings.particles = net.minecraft.client.settings.ParticleStatus.MINIMAL;
            }
        }

        if (noWeather.isEnabled()) {
            mc.gameSettings.renderDistanceChunks = Math.min(mc.gameSettings.renderDistanceChunks, 8);
        }

        if (noFog.isEnabled()) {
            if (savedFog == -1) savedFog = mc.gameSettings.ofFogType;
            mc.gameSettings.ofFogType = 0; // OFF
        }

        if (noEntityShadow.isEnabled()) {
            mc.gameSettings.entityShadows = false;
        }
    }

    private void restore() {
        if (mc.gameSettings == null) return;

        if (savedParticles != -1) {
            mc.gameSettings.particles = net.minecraft.client.settings.ParticleStatus.values()[savedParticles];
            savedParticles = -1;
        }
        if (savedFog != -1) {
            mc.gameSettings.ofFogType = savedFog;
            savedFog = -1;
        }
        mc.gameSettings.entityShadows = true;
    }
}
