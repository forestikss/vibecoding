package ru.etc1337.api.bot;

import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;

public class BotPlayHandler implements IClientPlayNetHandler {

    private final BotConnection bot;
    private final NetworkManager networkManager;

    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround = true;
    private boolean forward, back, left, right, jump, sneak, sprint;

    public BotPlayHandler(BotConnection bot, NetworkManager networkManager) {
        this.bot = bot;
        this.networkManager = networkManager;
    }

    public void sendChat(String message) {
        networkManager.sendPacket(new CChatMessagePacket(message));
    }

    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw; this.pitch = pitch;
    }

    public void setMovement(boolean forward, boolean back, boolean left, boolean right,
                            boolean jump, boolean sneak, boolean sprint) {
        this.forward = forward; this.back = back;
        this.left = left; this.right = right;
        this.jump = jump; this.sneak = sneak;
        this.sprint = sprint;
    }

    public void attack() {
        networkManager.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
    }

    public void useItem() {
        networkManager.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
    }

    public void sendPosition() {
        networkManager.sendPacket(new CPlayerPacket.PositionRotationPacket(x, y, z, yaw, pitch, onGround));
    }

    public void tickMovement() {
        double speed = sprint ? 0.13 : 0.1;
        double rad = Math.toRadians(yaw);
        if (forward) { x -= Math.sin(rad) * speed; z += Math.cos(rad) * speed; }
        if (back)    { x += Math.sin(rad) * speed; z -= Math.cos(rad) * speed; }
        if (left)    { x -= Math.cos(rad) * speed; z -= Math.sin(rad) * speed; }
        if (right)   { x += Math.cos(rad) * speed; z += Math.sin(rad) * speed; }
        if (jump && onGround) { y += 0.42; onGround = false; }
        if (!onGround) {
            y -= 0.08;
            if (y <= Math.floor(y) + 0.001) { y = Math.floor(y); onGround = true; }
        }
        sendPosition();
    }

    // ---- Обработка пакетов ----

    @Override
    public void handleJoinGame(SJoinGamePacket p) {
        // Отправляем настройки клиента (127 = все части модели, 0 = chat, RIGHT hand)
        networkManager.sendPacket(new CClientSettingsPacket(
                "ru_ru", 8,
                net.minecraft.entity.player.ChatVisibility.FULL,
                true, 127,
                net.minecraft.util.HandSide.RIGHT));
        networkManager.sendPacket(new CConfirmTeleportPacket(0));
    }

    @Override
    public void handlePlayerPosLook(SPlayerPositionLookPacket p) {
        x = p.getX(); y = p.getY(); z = p.getZ();
        yaw = p.getYaw(); pitch = p.getPitch();
        networkManager.sendPacket(new CConfirmTeleportPacket(p.getTeleportId()));
        sendPosition();
    }

    @Override
    public void handleChat(SChatPacket p) {
        bot.addChatMessage(p.getChatComponent().getString());
    }

    @Override
    public void handleResourcePack(SSendResourcePackPacket p) {
        if (bot.isSpoofResourcePack()) {
            networkManager.sendPacket(new CResourcePackStatusPacket(
                    CResourcePackStatusPacket.Action.SUCCESSFULLY_LOADED));
        } else {
            networkManager.sendPacket(new CResourcePackStatusPacket(
                    CResourcePackStatusPacket.Action.DECLINED));
        }
    }

    @Override
    public void handleKeepAlive(SKeepAlivePacket p) {
        networkManager.sendPacket(new CKeepAlivePacket(p.getId()));
    }

    @Override
    public void handleDisconnect(SDisconnectPacket p) {
        bot.setConnected(false);
        bot.addSystemMessage("§cОтключён: " + p.getReason().getString());
    }

    @Override public NetworkManager getNetworkManager() { return networkManager; }

    @Override
    public void onDisconnect(ITextComponent reason) {
        bot.setConnected(false);
        bot.addSystemMessage("§cОтключён: " + reason.getString());
    }

    // Заглушки для всех остальных методов интерфейса
    @Override public void handleSpawnObject(SSpawnObjectPacket p) {}
    @Override public void handleSpawnExperienceOrb(SSpawnExperienceOrbPacket p) {}
    @Override public void handleSpawnMob(SSpawnMobPacket p) {}
    @Override public void handleScoreboardObjective(SScoreboardObjectivePacket p) {}
    @Override public void handleSpawnPainting(SSpawnPaintingPacket p) {}
    @Override public void handleSpawnPlayer(SSpawnPlayerPacket p) {}
    @Override public void handleAnimation(SAnimateHandPacket p) {}
    @Override public void handleStatistics(SStatisticsPacket p) {}
    @Override public void handleRecipeBook(SRecipeBookPacket p) {}
    @Override public void handleBlockBreakAnim(SAnimateBlockBreakPacket p) {}
    @Override public void handleSignEditorOpen(SOpenSignMenuPacket p) {}
    @Override public void handleUpdateTileEntity(SUpdateTileEntityPacket p) {}
    @Override public void handleBlockAction(SBlockActionPacket p) {}
    @Override public void handleBlockChange(SChangeBlockPacket p) {}
    @Override public void handleMultiBlockChange(SMultiBlockChangePacket p) {}
    @Override public void handleMaps(SMapDataPacket p) {}
    @Override public void handleConfirmTransaction(SConfirmTransactionPacket p) {}
    @Override public void handleCloseWindow(SCloseWindowPacket p) {}
    @Override public void handleWindowItems(SWindowItemsPacket p) {}
    @Override public void handleOpenHorseWindow(SOpenHorseWindowPacket p) {}
    @Override public void handleWindowProperty(SWindowPropertyPacket p) {}
    @Override public void handleSetSlot(SSetSlotPacket p) {}
    @Override public void handleCustomPayload(SCustomPayloadPlayPacket p) {}
    @Override public void handleEntityStatus(SEntityStatusPacket p) {}
    @Override public void handleEntityAttach(SMountEntityPacket p) {}
    @Override public void handleSetPassengers(SSetPassengersPacket p) {}
    @Override public void handleExplosion(SExplosionPacket p) {}
    @Override public void handleChangeGameState(SChangeGameStatePacket p) {}
    @Override public void handleChunkData(SChunkDataPacket p) {}
    @Override public void processChunkUnload(SUnloadChunkPacket p) {}
    @Override public void handleEffect(SPlaySoundEventPacket p) {}
    @Override public void handleEntityMovement(SEntityPacket p) {}
    @Override public void handleParticles(SSpawnParticlePacket p) {}
    @Override public void handlePlayerAbilities(SPlayerAbilitiesPacket p) {}
    @Override public void handlePlayerListItem(SPlayerListItemPacket p) {}
    @Override public void handleDestroyEntities(SDestroyEntitiesPacket p) {}
    @Override public void handleRemoveEntityEffect(SRemoveEntityEffectPacket p) {}
    @Override public void handleRespawn(SRespawnPacket p) {}
    @Override public void handleEntityHeadLook(SEntityHeadLookPacket p) {}
    @Override public void handleHeldItemChange(SHeldItemChangePacket p) {}
    @Override public void handleDisplayObjective(SDisplayObjectivePacket p) {}
    @Override public void handleEntityMetadata(SEntityMetadataPacket p) {}
    @Override public void handleEntityVelocity(SEntityVelocityPacket p) {}
    @Override public void handleEntityEquipment(SEntityEquipmentPacket p) {}
    @Override public void handleSetExperience(SSetExperiencePacket p) {}
    @Override public void handleUpdateHealth(SUpdateHealthPacket p) {}
    @Override public void handleTeams(STeamsPacket p) {}
    @Override public void handleUpdateScore(SUpdateScorePacket p) {}
    @Override public void func_230488_a_(SWorldSpawnChangedPacket p) {}
    @Override public void handleTimeUpdate(SUpdateTimePacket p) {}
    @Override public void handleSoundEffect(SPlaySoundEffectPacket p) {}
    @Override public void handleSpawnMovingSoundEffect(SSpawnMovingSoundEffectPacket p) {}
    @Override public void handleCustomSound(SPlaySoundPacket p) {}
    @Override public void handleCollectItem(SCollectItemPacket p) {}
    @Override public void handleEntityTeleport(SEntityTeleportPacket p) {}
    @Override public void handleEntityProperties(SEntityPropertiesPacket p) {}
    @Override public void handleEntityEffect(SPlayEntityEffectPacket p) {}
    @Override public void handleTags(STagsListPacket p) {}
    @Override public void handleCombatEvent(SCombatPacket p) {}
    @Override public void handleServerDifficulty(SServerDifficultyPacket p) {}
    @Override public void handleCamera(SCameraPacket p) {}
    @Override public void handleWorldBorder(SWorldBorderPacket p) {}
    @Override public void handleTitle(STitlePacket p) {}
    @Override public void handlePlayerListHeaderFooter(SPlayerListHeaderFooterPacket p) {}
    @Override public void handleUpdateBossInfo(SUpdateBossInfoPacket p) {}
    @Override public void handleCooldown(SCooldownPacket p) {}
    @Override public void handleMoveVehicle(SMoveVehiclePacket p) {}
    @Override public void handleAdvancementInfo(SAdvancementInfoPacket p) {}
    @Override public void handleSelectAdvancementsTab(SSelectAdvancementsTabPacket p) {}
    @Override public void handlePlaceGhostRecipe(SPlaceGhostRecipePacket p) {}
    @Override public void handleCommandList(SCommandListPacket p) {}
    @Override public void handleStopSound(SStopSoundPacket p) {}
    @Override public void handleTabComplete(STabCompletePacket p) {}
    @Override public void handleUpdateRecipes(SUpdateRecipesPacket p) {}
    @Override public void handlePlayerLook(SPlayerLookPacket p) {}
    @Override public void handleNBTQueryResponse(SQueryNBTResponsePacket p) {}
    @Override public void handleUpdateLight(SUpdateLightPacket p) {}
    @Override public void handleOpenBookPacket(SOpenBookWindowPacket p) {}
    @Override public void handleOpenWindowPacket(SOpenWindowPacket p) {}
    @Override public void handleMerchantOffers(SMerchantOffersPacket p) {}
    @Override public void handleUpdateViewDistancePacket(SUpdateViewDistancePacket p) {}
    @Override public void handleChunkPositionPacket(SUpdateChunkPositionPacket p) {}
    @Override public void handleAcknowledgePlayerDigging(SPlayerDiggingPacket p) {}
}
