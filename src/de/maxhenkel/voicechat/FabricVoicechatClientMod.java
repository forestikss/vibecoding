package de.maxhenkel.voicechat;

import com.focamacho.keeptheresourcepack.client.KeepTheResourcePackClient;

public class FabricVoicechatClientMod extends VoicechatClient {


    public void onInitializeClient() {
        initializeClient();
        new KeepTheResourcePackClient().onInitializeClient();
    }

}
