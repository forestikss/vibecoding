package ru.etc1337.api.discord.rpc.callbacks;

import com.sun.jna.Callback;
import ru.etc1337.api.discord.rpc.utils.DiscordUser;

public interface ReadyCallback extends Callback {
    void apply(DiscordUser var1);
}