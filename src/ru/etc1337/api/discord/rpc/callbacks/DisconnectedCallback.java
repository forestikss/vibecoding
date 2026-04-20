package ru.etc1337.api.discord.rpc.callbacks;

import com.sun.jna.Callback;

public interface DisconnectedCallback extends Callback {
    void apply(int var1, String var2);
}