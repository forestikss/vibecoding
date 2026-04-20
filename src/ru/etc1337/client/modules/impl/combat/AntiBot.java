package ru.etc1337.client.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.game.EventWorldChanged;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ModuleInfo(name = "Anti Bot", description = "Убирает ботов сервера", category = ModuleCategory.COMBAT)
public class AntiBot extends Module {
    private final ModeSetting mode = new ModeSetting("Обход", this, "ReallyWorld", "Matrix");
    public static final List<Entity> bot = new ArrayList<>();
    private final Timer timer = new Timer();

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            for (Entity entity : mc.world.getAllEntities()) {
                if (!(entity instanceof PlayerEntity player) || player.equals(mc.player)) continue;

                boolean isBot = false;

                if (mode.is("ReallyWorld")) {
                    boolean hasValidArmor = player.inventory.armorInventory.stream().allMatch(armorItem ->
                            armorItem.getItem() != Items.AIR && armorItem.isEnchantable() && !armorItem.isDamaged());
                    boolean hasValidEquipment = player.getHeldItemOffhand().getItem() == Items.AIR &&
                            (player.inventory.armorInventory.stream().anyMatch(armorItem ->
                                    armorItem.getItem() == Items.LEATHER_BOOTS ||
                                            armorItem.getItem() == Items.LEATHER_LEGGINGS ||
                                            armorItem.getItem() == Items.LEATHER_CHESTPLATE ||
                                            armorItem.getItem() == Items.LEATHER_HELMET ||
                                            armorItem.getItem() == Items.IRON_BOOTS ||
                                            armorItem.getItem() == Items.IRON_LEGGINGS ||
                                            armorItem.getItem() == Items.IRON_CHESTPLATE ||
                                            armorItem.getItem() == Items.IRON_HELMET));
                    boolean hasFullFood = player.getFoodStats().getFoodLevel() == 20;
                    isBot = hasValidArmor && hasValidEquipment && hasFullFood;
                } else if (mode.is("Matrix")) {
                    Iterator list = mc.world.getPlayers().iterator();
                    while(list.hasNext()) {
                        PlayerEntity playerEntity = (PlayerEntity)list.next();
                        if (!playerEntity.getUniqueID().equals(PlayerEntity.getOfflineUUID(playerEntity.getName().getString())) && !bot.contains(playerEntity)) {
                            bot.add(playerEntity);
                        }
                    }
                }

                if (isBot) {
                    if (!bot.contains(player)) {
                        bot.add(player);
                    }
                } else {
                    bot.remove(player);
                }
            }
        }
        if (event instanceof EventWorldChanged) {
            bot.clear();
        }
    }

    @Override
    public void onDisable() {
        bot.clear();
        super.onDisable();
    }
    public static boolean isBot(Entity player) {
        return Client.getInstance().getModuleManager().get(AntiBot.class).isEnabled() && bot.contains(player);
    }
}
