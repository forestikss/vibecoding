package ru.etc1337.api.game;

import lombok.experimental.UtilityClass;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.interfaces.QuickImports;

@UtilityClass
public class Chat implements QuickImports {
    private ITextComponent get(String message, boolean error) {
        return FixColor.gradient(Client.clientName.toLowerCase(), TempColor.getClientColor().getRGB(), TempColor.getClientColor().darker().getRGB()).
                append(new StringTextComponent(TextFormatting.DARK_GRAY + " » " + TextFormatting.RESET + (error ? TextFormatting.RED : TextFormatting.WHITE) + message));
    }

    public void send(String message) {
        if (mc.player == null) return;
        mc.player.sendMessage(get(message, false), Util.DUMMY_UUID);
    }

    public void sendError(String message) {
        if (mc.player == null) return;
        mc.player.sendMessage(get(message, true), Util.DUMMY_UUID);
    }
}
