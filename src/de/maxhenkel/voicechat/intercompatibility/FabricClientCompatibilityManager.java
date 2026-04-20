package de.maxhenkel.voicechat.intercompatibility;

import de.maxhenkel.voicechat.KeyBindingRegistryImpl;
import net.minecraft.client.util.InputMappings;
import de.maxhenkel.voicechat.events.*;
import de.maxhenkel.voicechat.resourcepacks.IPackRepository;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.resources.IPackFinder;

import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class FabricClientCompatibilityManager extends ClientCompatibilityManager {

    private static final Minecraft mc = Minecraft.getInstance();

    public FabricClientCompatibilityManager() {
        init();
    }

    @Override
    public void onRenderNamePlate(RenderNameplateEvent onRenderNamePlate) {
        RenderEvents.RENDER_NAMEPLATE.register(onRenderNamePlate);
    }

    @Override
    public void onRenderHUD(RenderHUDEvent onRenderHUD) {
        RenderEvents.RENDER_HUD.register(poseStack -> onRenderHUD.render(poseStack, Minecraft.getDebugFPS()));
    }

    @Override
    public void onKeyboardEvent(KeyboardEvent onKeyboardEvent) {
        InputEvents.KEYBOARD_KEY.register(onKeyboardEvent);
    }

    @Override
    public void onMouseEvent(MouseEvent onMouseEvent) {
        InputEvents.MOUSE_KEY.register(onMouseEvent);
    }

    @Override
    public InputMappings.Input getBoundKeyOf(KeyBinding keyBinding) {
        return /*KeyBindingHelper.getBoundKeyOf(keyBinding)*/keyBinding.keyCode;
    }

    @Override
    public void onHandleKeyBinds(Runnable onHandleKeyBinds) {
        InputEvents.HANDLE_KEYBINDS.register(onHandleKeyBinds);
    }

    @Override
    public KeyBinding registerKeyBinding(KeyBinding keyBinding) {
        return /*KeyBindingHelper.registerKeyBinding(keyBinding)*/KeyBindingRegistryImpl.registerKeyBinding(keyBinding);
    }

    @Override
    public void emitVoiceChatConnectedEvent(ClientVoicechatConnection client) {
        ClientVoiceChatEvents.VOICECHAT_CONNECTED.invoker().accept(client);
    }

    @Override
    public void emitVoiceChatDisconnectedEvent() {
        ClientVoiceChatEvents.VOICECHAT_DISCONNECTED.invoker().run();
    }

    @Override
    public void onVoiceChatConnected(Consumer<ClientVoicechatConnection> onVoiceChatConnected) {
        ClientVoiceChatEvents.VOICECHAT_CONNECTED.register(onVoiceChatConnected);
    }

    @Override
    public void onVoiceChatDisconnected(Runnable onVoiceChatDisconnected) {
        ClientVoiceChatEvents.VOICECHAT_DISCONNECTED.register(onVoiceChatDisconnected);
    }

    @Override
    public void onDisconnect(Runnable onDisconnect) {
        ClientWorldEvents.DISCONNECT.register(onDisconnect);
    }



    @Override
    public void onPublishServer(Consumer<Integer> onPublishServer) {
        PublishServerEvents.SERVER_PUBLISHED.register(onPublishServer);
    }

    @Override
    public SocketAddress getSocketAddress(NetworkManager connection) {
        return connection.getRemoteAddress();
    }

    @Override
    public void addResourcePackSource(ResourcePackList packRepository, IPackFinder repositorySource) {
        final Set<IPackFinder> sources = mc.getResourcePackList().packFinders;
        Set<IPackFinder> set = new HashSet<>(sources);
        set.add(repositorySource);
        mc.getResourcePackList().packFinders = set;
    }
}
