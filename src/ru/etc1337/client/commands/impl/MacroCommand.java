package ru.etc1337.client.commands.impl;

import net.minecraft.util.text.TextFormatting;
import ru.etc1337.Client;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.Easing;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.handler.EventListener;
import ru.etc1337.api.events.impl.input.EventInputKey;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.game.StringHelper;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.macro.Macro;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.client.commands.Command;
import ru.etc1337.client.commands.api.CommandInfo;
import ru.etc1337.client.commands.api.CommandParameter;
import ru.kotopushka.compiler.sdk.annotations.Compile;

import java.util.Arrays;

@CommandInfo(name = "Macro", description = "Позволяет биндить сообщение в чате на любую кнопку", aliases = {"macro", "macros"})
public class MacroCommand extends Command implements QuickImports, EventListener {

    private String name;
    private String message;
    private boolean binding;
    private int pressedKey;

    CommandParameter add = new CommandParameter(this, "add");
    CommandParameter remove = new CommandParameter(this, "remove", "del", "delete");
    CommandParameter list = new CommandParameter(this, "list");
    CommandParameter clear = new CommandParameter(this, "clear");

    final Animation alphaAnimation = new Animation(Easing.CUBIC_IN_OUT, 150);

    public MacroCommand() {
        Client.getEventManager().register(this);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D eventRender2D) {
            alphaAnimation.update(this.binding ? 1.0F : 0.0F);
            if (alphaAnimation.getValue() > 0.05F) {
                float x = window.getScaledWidth() / 2.0F;
                float y = (window.getScaledHeight() / 2.0F);
                FixColor rectColor = FixColor.BLACK.alpha(205.0F * alphaAnimation.getValue());
                FixColor textColor = FixColor.WHITE.alpha(255.0F * alphaAnimation.getValue());
                Render.drawRect(eventRender2D.getMatrixStack(), 0.0F, 0.0F, (float) window.getScaledWidth(), (float) window.getScaledHeight(), rectColor);
                Fonts.SEMIBOLD_16.drawCenter(eventRender2D.getMatrixStack(), "Press any button", x, y - (float) Fonts.SEMIBOLD_16.height() / 2.0F, textColor.getRGB());
            }
        }
        if (event instanceof EventInputKey eventInputKey) {
            if (this.binding) {
                if (eventInputKey.isReleased() || eventInputKey.getKey() == 0) return;
                this.pressedKey = eventInputKey.getKey();
                this.binding = false;

                if (this.pressedKey != -1) {
                    Macro macros = new Macro(this.name, this.pressedKey, this.message);
                    Client.getInstance().getMacroManager().getMacros().add(macros);
                    Chat.send("Макрос %s был добавлен!".formatted(this.name));
                } else {
                    Chat.send("Необходимо нажать кнопку для привязки макроса.");
                }
            }
        }
    }

    @Compile
    @Override
    public void execute(String[] args) {
        if (args.length == 1) {
            sendErrorMessage();
        } else {
            if (this.contains(args[1], add)) {
                addMacros(args);
            } else if (this.contains(args[1], remove)) {
                removeMacros(args);
            } else if (this.contains(args[1], list)) {
                displayMacrosList();
            } else if (this.contains(args[1], clear)) {
                clearMacrosList();
            }
        }
    }

    @Compile
    public void clearMacrosList() {
        Client.getInstance().getMacroManager().getMacros().clear();
        Chat.send("The list of macros has been cleared!");

    }

    @Compile
    public void displayMacrosList() {

        if (Client.getInstance().getMacroManager().getMacros().isEmpty()) {
            Chat.send("Macros list is empty :(");
        } else {
            for (Macro macros : Client.getInstance().getMacroManager().getMacros()) {
                Chat.send(TextFormatting.GRAY + String.valueOf(TextFormatting.BOLD) + "> " + TextFormatting.WHITE + "%s [%s]: %s".formatted(
                        TextFormatting.GRAY + macros.getName() + TextFormatting.RESET,
                        StringHelper.getKeyString(macros.getKey()),
                        TextFormatting.GRAY + macros.getMessage() + TextFormatting.RESET));
            }
        }
    }

    @Compile
    public void removeMacros(String[] args) {


        if (args.length != 3) {
            sendErrorMessage();
        } else {
            String name = args[2];
            Macro macros = Client.getInstance().getMacroManager().get(name);

            if (macros == null) {
                Chat.send("A macro with the name %s was not found!".formatted(name));
            } else {
                Client.getInstance().getMacroManager().getMacros().remove(macros);
                Chat.send("Macro %s was deleted!".formatted(name));

            }
        }
    }

    @Compile
    public void addMacros(String[] args) {

        if (args.length < 4) {
            sendErrorMessage();
        } else {
            this.name = args[2];
            this.message = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            this.binding = true;
            this.pressedKey = -1;

            Chat.send("Press a key to bind the macro...");
        }
    }

    @Compile
    public void sendErrorMessage() {
        error();
        Chat.send(TextFormatting.GRAY + ".macros add " + TextFormatting.WHITE + "<name> <message>");
        Chat.send(TextFormatting.GRAY + ".macros remove/del/delete " + TextFormatting.WHITE + "<name>");
        Chat.send(TextFormatting.GRAY + ".macros list");
        Chat.send(TextFormatting.GRAY + ".macros clear");
    }
}