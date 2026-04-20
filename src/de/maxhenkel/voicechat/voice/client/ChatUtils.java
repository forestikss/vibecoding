package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;

public class ChatUtils {

    public static void sendPlayerError(String translationKey, @Nullable Exception e) {
        IFormattableTextComponent error = createModMessage(new TranslationTextComponent(translationKey).mergeStyle(TextFormatting.RED)).modifyStyle(style -> {
            if (e != null) {
                return style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(e.getMessage()).mergeStyle(TextFormatting.RED)));
            }
            return style;
        });
        sendPlayerMessage(error);
    }

    public static void sendModMessage(ITextComponent message) {
        sendPlayerMessage(createModMessage(message));
    }

    public static IFormattableTextComponent createModMessage(ITextComponent message) {
        return new StringTextComponent("")
                .append(TextComponentUtils.wrapWithSquareBrackets(new StringTextComponent(CommonCompatibilityManager.INSTANCE.getModName())).mergeStyle(TextFormatting.GREEN))
                .append(new StringTextComponent(" "))
                .append(message);
    }

    public static void sendPlayerMessage(ITextComponent component) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.sendMessage(component, Util.DUMMY_UUID);
    }
}
