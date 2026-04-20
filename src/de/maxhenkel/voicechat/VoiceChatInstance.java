package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.FabricVoicechatClientMod;
import de.maxhenkel.voicechat.FabricVoicechatMod;
import de.maxhenkel.voicechat.intercompatibility.FabricClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.FabricCommonCompatibilityManager;
import de.maxhenkel.voicechat.voice.client.KeyEvents;

public class VoiceChatInstance {
    public static final boolean ALLOW = true;

    public static FabricVoicechatClientMod fabricVoicechatClientMod;

    public static void initOptions() {
        new FabricCommonCompatibilityManager();
        new FabricClientCompatibilityManager();

        if (ALLOW) {
            KeyEvents.registerKeyBinds();
            fabricVoicechatClientMod = new FabricVoicechatClientMod();
        }
    }

    public static void initialize() {
        if (ALLOW) {
            (new FabricVoicechatMod()).onInitialize();
            fabricVoicechatClientMod.onInitializeClient();
        }
    }

}
