package ru.etc1337.client.commands.impl;

import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;
import ru.etc1337.Client;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.client.commands.Command;
import ru.etc1337.client.commands.api.CommandInfo;
import ru.etc1337.client.commands.api.CommandParameter;
import ru.etc1337.client.modules.Module;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@CommandInfo(name = "Bind", description = "Управление биндами", aliases = {"b", "bind"})
public class BindCommand extends Command implements QuickImports {

    CommandParameter list = new CommandParameter(this, "list");
    CommandParameter clear = new CommandParameter(this, "clear");
    CommandParameter remove = new CommandParameter(this, "remove", "del", "delete");

    @Compile
    @Override
    public void execute(String[] args) {
        try {
            if (args.length >= 2) {
                if (this.contains(args[1], list)) {
                    listBoundKeys();
                } else if (this.contains(args[1], clear)) {
                    clearAllBindings();
                } else if (this.contains(args[1], remove)) {
                    if (args.length >= 4) {
                        removeKeyBinding(args[2], args[3]);
                    } else {
                        error();
                    }
                } else {
                    error();
                }
            } else {
                error();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Compile
    private void listBoundKeys() {
        Chat.send(TextFormatting.GRAY + "Список всех модулей с привязанными клавишами:");
        for (Module f : Client.getInstance().getModuleManager().getModules()) {
            if (f.getKey() == -1) continue;
            Chat.send(f.getName() + " [" + TextFormatting.AQUA + (GLFW.glfwGetKeyName(f.getKey(), -1) == null ? "" : GLFW.glfwGetKeyName(f.getKey(), -1)) + TextFormatting.RESET + "]");
        }
    }

    @Compile
    private void clearAllBindings() {
        for (Module f : Client.getInstance().getModuleManager().getModules()) {
            f.setKey(-1);
        }
        Chat.send(TextFormatting.GREEN + "Все клавиши были отвязаны от модулей");
    }

    @Compile
    private void removeKeyBinding(String moduleName, String keyName) {
        for (Module f : Client.getInstance().getModuleManager().getModules()) {
            if (f.getName().equalsIgnoreCase(moduleName)) {
                f.setKey(-1);
                Chat.send("Клавиша " + TextFormatting.AQUA + keyName + TextFormatting.RESET + " была отвязана от модуля " + TextFormatting.AQUA + f.getName());
            }
        }
    }

    @Compile
    public void sendErrorMessage() {
        error();
        Chat.send(TextFormatting.GRAY + ".bind add " + TextFormatting.WHITE + "<name> <message>");
        Chat.send(TextFormatting.GRAY + ".bind remove/del/delete " + TextFormatting.WHITE + "<name>");
        Chat.send(TextFormatting.GRAY + ".bind list");
        Chat.send(TextFormatting.GRAY + ".bind clear");
    }
}