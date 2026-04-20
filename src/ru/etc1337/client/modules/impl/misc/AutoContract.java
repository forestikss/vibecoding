package ru.etc1337.client.modules.impl.misc;

import de.maxhenkel.voicechat.voice.client.ChatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventSendMessage;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.game.EventWorldChanged;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.events.impl.packet.EventSendPacket;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.game.Inventory;
import ru.etc1337.api.game.JoinHelper;
import ru.etc1337.api.game.Server;
import ru.etc1337.api.notifications.Notification;
import ru.etc1337.api.other.ScriptConstructor;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ModuleInfo(name = "Auto Contract", description = "asd", category = ModuleCategory.MISC)
public class AutoContract extends Module {
    public final SliderSetting grief = new SliderSetting("Grief", this, 5, 1, 54, 1);
    public final SliderSetting delay = new SliderSetting("Delay", this, 6, 1, 10, 1);
    private final BooleanSetting autoGps = new BooleanSetting("Auto Gps", this);

    private static final ScriptConstructor script = new ScriptConstructor();
    private final Timer lastSendMessageTimer = new Timer();
    private String lastSendMessage = "empty";
    private int lastSendMessageTicks;
    private boolean waitingForTarget = false;

    @Compile
    @Override
    public void onEvent(Event event) {
        if (mc.player == null || mc.world == null) return;

        if (event instanceof EventUpdate) {
            script.update();

            if (!lastSendMessage.equals("/hub")) {
                lastSendMessageTicks++;
            } else {
                lastSendMessageTicks = 0;
            }

            if (waitingForTarget && lastSendMessageTicks >= (delay.getValue() * 10)) {
                waitingForTarget = false;
                Chat.send(TextFormatting.RED + "Цель не найдена, перезаход...");
                mc.player.sendChatMessage("/hub");
                lastSendMessage = "/hub";
                lastSendMessageTimer.reset();
            }
        }

        if (event instanceof EventSendPacket eventSendPacket) {
            if (eventSendPacket.getPacket() instanceof CChatMessagePacket msg) {
                lastSendMessage = msg.getMessage();
                lastSendMessageTimer.reset();
            }
        }

        if (event instanceof EventReceivePacket eventReceivePacket) {
            if (eventReceivePacket.getPacket() instanceof SChatPacket sChatPacket) {
                String message = sChatPacket.getChatComponent().getString();
                if (!message.contains("Контракты")) return;

                eventReceivePacket.setCancelled(true);

                String targetMessage = message
                        .trim()
                        .replace("Контракты » Ваша цель - ", "");

                Pattern pattern = Pattern.compile("([^§]+)\\s*\\((-?\\d+),\\s*(-?\\d+),\\s*(-?\\d+)\\)");
                Matcher matcher = pattern.matcher(targetMessage);

                if (matcher.find()) {
                    String username = matcher.group(1).trim();
                    int x = Integer.parseInt(matcher.group(2));
                    int y = Integer.parseInt(matcher.group(3));
                    int z = Integer.parseInt(matcher.group(4));

                    if (Client.getInstance().getTargetManager().isTarget(username)) {
                        Chat.send(TextFormatting.GREEN + "Цель найдена!");
                        Chat.send(TextFormatting.GRAY + "> Никнейм: " + TextFormatting.WHITE + username);
                        Chat.send(TextFormatting.GRAY + "> Координаты: " + TextFormatting.WHITE + x + ", " + y + ", " + z);
                        if (autoGps.isEnabled()) {
                            mc.player.sendChatMessage(String.format(".gps %s %s", x, z));
                        }
                        waitingForTarget = false;
                    } else {
                        waitingForTarget = true;
                        lastSendMessageTicks = 0;
                    }
                }
            }

            if (eventReceivePacket.getPacket() instanceof SJoinGamePacket) {
                if (lastSendMessage.contains("/hub") && lastSendMessageTimer.finished(1000)) {
              //      Chat.send(TextFormatting.YELLOW + "Перезаход выполнен, запрашиваем цель...");
                    mc.player.sendChatMessage("/contract get");
                } else {
                    JoinHelper joinHelper = Client.getInstance().getJoinHelper();
                    if (script.isFinished() && !joinHelper.isEnabled()) {
                        script.cleanup()
                                .addTickStep(1, () -> {
                                    joinHelper.join(true, (int) grief.getValue());
                                });
                    }
                }
            }
        }
    }

    @Compile
    @Override
    public void onEnable() {
        super.onEnable();
        resetState();
        mc.player.sendChatMessage("/contract get");
    }

    @Compile
    @Override
    public void onDisable() {
        super.onDisable();
        resetState();
    }
    @Compile
    private void resetState() {
        lastSendMessageTimer.reset();
        lastSendMessageTicks = 0;
        lastSendMessage = "empty";
        waitingForTarget = false;
        script.cleanup();
    }
}
