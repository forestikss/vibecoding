package ru.etc1337.client.commands.impl;


import net.minecraft.util.text.TextFormatting;
import ru.etc1337.Client;
import ru.etc1337.api.config.Config;
import ru.etc1337.api.game.Chat;
import ru.etc1337.client.commands.Command;
import ru.etc1337.client.commands.api.CommandInfo;
import ru.etc1337.client.commands.api.CommandParameter;
import ru.etc1337.client.modules.impl.misc.NameProtect;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@CommandInfo(name = "Friend", description = "Позволяет добавить друзей, чтобы их видел чит", aliases = {"f", "friend", "friends"})
public class FriendCommand extends Command {

    CommandParameter add = new CommandParameter(this, "add", "save", "фвв", "ыфму");
    CommandParameter remove = new CommandParameter(this, "remove", "куьщму", "delete", "del", "вудуеу");
    CommandParameter list = new CommandParameter(this, "list", "дшые", "список");
    CommandParameter clear = new CommandParameter(this, "clear", "сдуфк");

    @Compile
    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            sendErrorMessage();
            return;
        }
        if (this.contains(args[1], add)) {
            if (args.length == 3) {
                
               
               


                if (!Client.getInstance().getFriendManager().isFriend(args[2])) {
                    addFriend(args[2]);
                } else {
                    Chat.send("Player " + args[2] + " is already in the friends list.");
                }
            } else {
                sendErrorMessage();
            }
        } else if (this.contains(args[1], remove)) {
            if (args.length == 3) {


                if (Client.getInstance().getFriendManager().isFriend(args[2])) {
                    removeFriend(args[2]);
                } else {
                    Chat.send("Player " + args[2] + " is not in the friends list.");
                }
            } else {
                sendErrorMessage();
            }
        } else if (this.contains(args[1], list)) {
            displayFriends();
        } else if (this.contains(args[1], clear)) {
            clearFriends();
        } else {
            sendErrorMessage();
        }
    }

    @Compile
    public void addFriend(String name) {
        Client.getInstance().getFriendManager().addFriend(name);
        
       
       

        Chat.send("Success! " + name + " was added as a friends!");
    }

    @Compile
    public void removeFriend(String name) {
        Client.getInstance().getFriendManager().removeFriend(name);
        
       

        Chat.send("Success! " + name + " was removed from friends!");
    }

    @Compile
    public void displayFriends() {
        if (Client.getInstance().getFriendManager().getFriends().isEmpty()) {
            Chat.send("Friends list is empty.");
        } else {
            Chat.send("Friends: ");
            for (String friend : Client.getInstance().getFriendManager().getFriends()) {
                
               

                Chat.send(TextFormatting.GRAY + String.valueOf(TextFormatting.BOLD) + "> " + TextFormatting.WHITE + friend);
            }
        }
    }

    @Compile
    public void clearFriends() {
        Client.getInstance().getFriendManager().getFriends().clear();
        Chat.send("Success! The friend list has been cleared!");

    }

    @Compile
    public void sendErrorMessage() {
        error();
        Chat.send(TextFormatting.GRAY + ".friend " + TextFormatting.WHITE + "<name>");
        Chat.send(TextFormatting.GRAY + ".friend add " + TextFormatting.WHITE + "<name>");
        Chat.send(TextFormatting.GRAY + ".friend remove/del/delete " + TextFormatting.WHITE + "<name>");
        Chat.send(TextFormatting.GRAY + ".friend list");
        Chat.send(TextFormatting.GRAY + ".friend clear");
    }
}