package ru.etc1337;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import ru.etc1337.api.UserInfo;
import ru.etc1337.api.capes.WaveyCapesBase;
import ru.etc1337.api.config.ConfigManager;
import ru.etc1337.api.config.Directory;
import ru.etc1337.api.discord.rpc.DiscordManager;
import ru.etc1337.api.draggable.DraggableManager;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.EventManager;
import ru.etc1337.api.events.handler.EventListener;
import ru.etc1337.api.events.impl.game.EventShutdown;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.game.EventWorldChanged;
import ru.etc1337.api.events.impl.input.EventInputKey;
import ru.etc1337.api.friend.FriendManager;
import ru.etc1337.api.game.Inventory;
import ru.etc1337.api.game.JoinHelper;
import ru.etc1337.api.game.PlayerInfoLogger;
import ru.etc1337.api.game.TPSHandler;
import ru.etc1337.api.game.maths.chunkAnimator.ChunkAnimations;
import ru.etc1337.api.irc.client.IRCClient;
import ru.etc1337.api.macro.MacroManager;
import ru.etc1337.api.notifications.NotificationManager;
import ru.etc1337.api.proxy.ProxyConfig;
import ru.etc1337.api.render.ui.dropui.DropUi;
import ru.etc1337.api.render.ui.mainmenu.account.AccountGuiScreen;
import ru.etc1337.api.render.ui.mainmenu.account.AccountManager;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.api.staff.StaffManager;
import ru.etc1337.api.targets.TargetManager;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.api.viamcp.ViaMCP;
import ru.etc1337.client.commands.CommandManager;
import ru.etc1337.client.modules.ModuleManager;
import ru.etc1337.client.modules.impl.combat.killaura.rotation.ReverseRotation;
import ru.etc1337.client.modules.impl.combat.killaura.rotation.api.Hit;
import ru.etc1337.protection.checks.Checks;
import ru.etc1337.protection.interfaces.Include;
import ru.etc1337.protection.userapi.UserAPI;
import ru.kotopushka.compiler.sdk.annotations.Compile;

import java.util.List;

@Getter
public class Client implements EventListener {
    public static final String clientName = "Stradix";
    public static final String releaseType = "Development";
    public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    private static Client instance = new Client();
    private static EventManager eventManager = new EventManager();

    public static Client getInstance() { return instance; }
    public static EventManager getEventManager() { return eventManager; }

    private UserInfo userInfo;

    private DraggableManager draggableManager;

    private ModuleManager moduleManager;
    private CommandManager commandManager;
    private ConfigManager configManager;
    private FriendManager friendManager;
    private TargetManager targetManager;
    private DiscordManager discordManager;
    private MacroManager macroManager;
    private StaffManager staffManager;

    // Other helpers
    private NotificationManager notificationManager;
    private AccountGuiScreen accountGui;
    private AccountManager accountManager;
    private Directory directory;
    private TPSHandler tpsHandler;
    private PlayerInfoLogger playerInfoLogger;
    private Hit hit;
    private JoinHelper joinHelper;
    private final Timer autoSaveTimer = Timer.create();
    private ChunkAnimations chunkAnimations;
    private WaveyCapesBase waveyCapesBase;
    private ReverseRotation reverseRotation;
   // private IRCClient ircClient;

    public UserInfo getUserInfo() { return userInfo; }
    public DraggableManager getDraggableManager() { return draggableManager; }
    public ModuleManager getModuleManager() { return moduleManager; }
    public CommandManager getCommandManager() { return commandManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public FriendManager getFriendManager() { return friendManager; }
    public TargetManager getTargetManager() { return targetManager; }
    public DiscordManager getDiscordManager() { return discordManager; }
    public MacroManager getMacroManager() { return macroManager; }
    public StaffManager getStaffManager() { return staffManager; }
    public NotificationManager getNotificationManager() { return notificationManager; }
    public AccountGuiScreen getAccountGui() { return accountGui; }
    public AccountManager getAccountManager() { return accountManager; }
    public Directory getDirectory() { return directory; }
    public TPSHandler getTpsHandler() { return tpsHandler; }
    public PlayerInfoLogger getPlayerInfoLogger() { return playerInfoLogger; }
    public Hit getHit() { return hit; }
    public JoinHelper getJoinHelper() { return joinHelper; }
    public Timer getAutoSaveTimer() { return autoSaveTimer; }
    public ChunkAnimations getChunkAnimations() { return chunkAnimations; }
    public WaveyCapesBase getWaveyCapesBase() { return waveyCapesBase; }
    public ReverseRotation getReverseRotation() { return reverseRotation; }


    public void start() {
        initGuard();
        initManagers();
        initOthers();
    }

    @Include
    public void initGuard() {
        userInfo = new UserInfo(UserAPI.username, UserAPI.role, UserAPI.uid, UserAPI.beta);
    }

    @Include
    @Compile
    public void initManagers() {
        draggableManager = new DraggableManager();
        friendManager = new FriendManager();
        targetManager = new TargetManager();
        moduleManager = new ModuleManager().start();
        commandManager = new CommandManager().start();
        discordManager = new DiscordManager().start();
        macroManager = new MacroManager();
        staffManager = new StaffManager();
        configManager = new ConfigManager().start();
    }

    @Include
    @Compile
    public void initOthers() {
        notificationManager = new NotificationManager();
        accountGui = new AccountGuiScreen();
        accountManager = new AccountManager();
        tpsHandler = new TPSHandler();
        playerInfoLogger = new PlayerInfoLogger();
        hit = new Hit();
        joinHelper = new JoinHelper();
        directory = new Directory();
        configManager.getAutoSaveConfig().start();
        waveyCapesBase = new WaveyCapesBase().start();
        reverseRotation = new ReverseRotation();
        chunkAnimations = new ChunkAnimations();
        //  ircClient = new IRCClient();
        eventManager.register(getInstance());
        eventManager.register(playerInfoLogger);
        eventManager.register(joinHelper);
        eventManager.register(new Inventory.Use());
        eventManager.register(new Hit());
      //  eventManager.register(ircClient);
       // ircClient.start(userInfo.getUsername());
        ProxyConfig.loadConfig();
        ViaMCP.create();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventInputKey eventInputKey) {
            if (eventInputKey.getKey() == -1 || eventInputKey.isReleased()) return;

            if (eventInputKey.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                Minecraft.getInstance().displayGuiScreen(new DropUi());
            }

            moduleManager.getModules().forEach((module) -> {
                if (module.getKey() == eventInputKey.getKey()) {
                    module.toggle();
                }
                module.getSettings().forEach((setting) -> {
                    if (setting instanceof BooleanSetting booleanSetting && booleanSetting.getKey() == eventInputKey.getKey()) {
                        booleanSetting.toggle();
                    }
                    if (setting instanceof MultiModeSetting multiModeSetting) {
                        multiModeSetting.getBoolSettings().forEach(boolSetting -> {
                            if (boolSetting.getKey() == eventInputKey.getKey()) {
                                boolSetting.toggle();
                            }
                        });
                    }
                    if (setting instanceof ModeSetting modeSetting) {
                        for (String mode : modeSetting.getModes()) {
                            if (modeSetting.getModeKey(mode) == eventInputKey.getKey()) {
                                modeSetting.setCurrentMode(mode);
                                break;
                            }
                        }
                    }
                });
            });
            macroManager.getMacros().forEach((macro) -> {
                if (macro.getKey() == eventInputKey.getKey()) {
                    Minecraft.getInstance().player.sendChatMessage(macro.getMessage());
                }
            });
        }

        if (event instanceof EventWorldChanged /*|| event instanceof EventLoadWorld*/) {
            configManager.getAutoSaveConfig().save();
        }
        if (event instanceof EventShutdown) {
            getDiscordManager().stopRPC();
            configManager.getAutoSaveConfig().save();
        }
        if (event instanceof EventUpdate eventUpdate && autoSaveTimer.finished(10 * 60 * 1000)) {
            configManager.getAutoSaveConfig().save();
        }
    }

}
