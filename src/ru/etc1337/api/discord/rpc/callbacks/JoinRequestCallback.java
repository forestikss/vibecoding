package ru.etc1337.api.discord.rpc.callbacks;

import com.sun.jna.Callback;
import ru.etc1337.api.discord.rpc.utils.DiscordUser;

public interface JoinRequestCallback extends Callback {
    void apply(DiscordUser var1);
}