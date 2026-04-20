package ru.etc1337.client.modules.impl.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.SkullItem;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.TextFormatting;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Aiming Items", description = "Притягивает к выбранным предметам", category = ModuleCategory.PLAYER)
public class AimingItems extends Module {
    private final ModeSetting aimMode = new ModeSetting("Наводка",this, "Незаметная", "Обычная");

    private final MultiModeSetting aimAt = new MultiModeSetting("Наводиться на", this, "Голову игрока", "Элитру");

    private Vector2f rotationVector = new Vector2f(0, 0);
    private ItemEntity targetItem;
    private boolean hasDisplayedMessage = false;


    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            var foundItem = false;
            for (Entity entity : mc.world.getAllEntities()) {
                if (entity instanceof ItemEntity item) {
                    this.rotationVector = Player.get(item.getPositionVec().add(item.getMotion().scale(1.1F)));
                    if (isItemOfInterest(item)) {
                        if (!this.hasDisplayedMessage) {
                            Chat.send(TextFormatting.GREEN + "Предмет найден! " + TextFormatting.WHITE + "Иду к предмету " + TextFormatting.GOLD + item.getDisplayName().getString() + TextFormatting.RESET);
                            this.hasDisplayedMessage = true;
                        }
                        this.targetItem = item;
                        foundItem = true;
                        break;
                    }
                }
            }
            if (!foundItem) {
                this.rotationVector = new Vector2f(mc.player != null ? mc.player.rotationYaw : 0, mc.player != null ? mc.player.rotationPitch : 0);
                this.targetItem = null;
                this.hasDisplayedMessage = false;
            }
        }

        if (this.targetItem == null || mc.player == null) return;

        Player.look(event, rotationVector, aimMode.is("Обычная") ? Player.Correction.CLIENT : Player.Correction.STRICT, null);
    }

    private boolean isItemOfInterest(ItemEntity item) {
        var itemStack = item.getItem().getItem();
        return (itemStack instanceof SkullItem && aimAt.get("Голову игрока").isEnabled()) ||
                (itemStack instanceof ElytraItem && aimAt.get("Элитру").isEnabled());
    }

    @Override
    public void onDisable() {
        this.rotationVector = new Vector2f(mc.player != null ? mc.player.rotationYaw : 0, mc.player != null ? mc.player.rotationPitch : 0);
        this.targetItem = null;
        this.hasDisplayedMessage = false;
        super.onDisable();
    }
}
