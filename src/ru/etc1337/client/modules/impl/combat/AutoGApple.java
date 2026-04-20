package ru.etc1337.client.modules.impl.combat;

import net.minecraft.item.Items;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@ModuleInfo(name = "Auto GApple", description = "Автоматически ест золотые яблоки", category = ModuleCategory.COMBAT)
public class AutoGApple extends Module {
    private SliderSetting health = new SliderSetting("Здоровье", this, 15, 0, 20, 0.5F);
    private BooleanSetting absorption = new BooleanSetting("Золотые сердца", this);
    boolean isEating;

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            eating();
        }
    }

    private void eating() {
        if (canEat()) {
            startEating();
        } else if (isEating) {
            stopEating();
        }
    }

    @Compile
    public boolean canEat() {
        float health = mc.player.getRealHealth();
        if (absorption.isEnabled()) {
            health += mc.player.getAbsorptionAmount();
        }
        return !mc.player.getShouldBeDead() && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && health <= this.health.getValue() && !mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE);
    }

    private void startEating() {
        if (!mc.gameSettings.keyBindUseItem.isKeyDown()) {
            mc.gameSettings.keyBindUseItem.setPressed(true);
            isEating = true;
        }
    }

    private void stopEating() {
        mc.gameSettings.keyBindUseItem.setPressed(false);
        isEating = false;
    }
}
