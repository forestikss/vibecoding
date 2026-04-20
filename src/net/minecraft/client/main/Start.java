package net.minecraft.client.main;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.Empty3i;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.GameConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenSize;
import net.minecraft.client.util.UndeclaredException;
import net.minecraft.crash.CrashReport;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.DefaultUncaughtExceptionHandler;
import net.minecraft.util.Session;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Bootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.etc1337.protection.storage.Constants;
import ru.etc1337.protection.userapi.UserAPI;

import java.io.File;
import java.net.Proxy;
import java.util.OptionalInt;

/**
 * Start class для запуска Dreamcore из IntelliJ IDEA.
 * Дублирует Starter.start() без вызова Checks.check() (авторизация через SDK не нужна).
 *
 * Run Configuration в IntelliJ IDEA:
 *   Main class:   net.minecraft.client.main.Start
 *   Working dir:  C:/Dreamcore/client-1.16.5
 *   VM options:   -Xmx2G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -Djava.library.path=<путь к natives>
 *   JDK:          Java 8 или 11
 */
public class Start {

    private static final Logger LOGGER = LogManager.getLogger();

    static {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
                String.valueOf(Runtime.getRuntime().availableProcessors()));
    }

    public static void main(String[] args) {
        // --- Данные пользователя (замени на свои) ---
        UserAPI.username = "DevPlayer";
        UserAPI.uid = 0;
        UserAPI.role = "dev";
        UserAPI.beta = true;

        CrashReport.crash();
        Bootstrap.register();
        Bootstrap.checkTranslations();
        Util.func_240994_l_();

        // Данные сессии
        String username    = UserAPI.username;
        String uuid        = "00000000000000000000000000000000";
        String accessToken = "0";
        String type        = "mojang";
        Session session = new Session(username, uuid, accessToken, type);

        // Размер окна
        int width  = 854;
        int height = 480;
        OptionalInt fullscreenWidth  = OptionalInt.empty();
        OptionalInt fullscreenHeight = OptionalInt.empty();
        boolean isFullscreen = false;

        // Директории
        File gameDir         = new File(Constants.MAIN_DIRECTORY.getAbsolutePath());
        File assetsDir       = new File(gameDir, "assets/");
        File resourcepacksDir = new File(gameDir, "resourcepacks/");
        String assetIndex    = "1.16";

        // Версия
        String launchVersion      = "1.16.5";
        String versionType        = "release";
        boolean demo              = false;
        boolean disableMultiplayer = false;
        boolean disableChat       = false;

        // Сервер при старте (null = не подключаться)
        String server = null;
        int port = 25565;

        GameConfiguration gameConfiguration = new GameConfiguration(
                new GameConfiguration.UserInformation(session, new PropertyMap(), new PropertyMap(), Proxy.NO_PROXY),
                new ScreenSize(width, height, fullscreenWidth, fullscreenHeight, isFullscreen),
                new GameConfiguration.FolderInformation(gameDir, resourcepacksDir, assetsDir, assetIndex),
                new GameConfiguration.GameInformation(launchVersion, versionType),
                new GameConfiguration.ServerInformation(server, port));

        launch(gameConfiguration);
    }

    private static void launch(GameConfiguration config) {
        Thread shutdownThread = new Thread("Client Shutdown Thread") {
            public void run() {
                Minecraft mc = Minecraft.getInstance();
                IntegratedServer server = mc.getIntegratedServer();
                if (server != null) server.initiateShutdown(true);
            }
        };
        shutdownThread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        Runtime.getRuntime().addShutdownHook(shutdownThread);

        new Empty3i();
        final Minecraft minecraft;

        try {
            Thread.currentThread().setName("Main");
            RenderSystem.initRenderThread();
            RenderSystem.beginInitialization();
            minecraft = new Minecraft(config);
            RenderSystem.finishInitialization();
        } catch (UndeclaredException e) {
            LOGGER.warn("Failed to create window: ", e);
            return;
        } catch (Throwable t) {
            CrashReport crashReport = CrashReport.makeCrashReport(t, "Initializing game");
            crashReport.makeCategory("Initialization");
            Minecraft.fillCrashReport(null, config.gameInfo.version, null, crashReport);
            Minecraft.displayCrashReport(crashReport);
            return;
        }

        Thread gameThread = null;

        if (minecraft.isRenderOnThread()) {
            gameThread = new Thread("Game thread") {
                public void run() {
                    try {
                        RenderSystem.initGameThread(true);
                        minecraft.run();
                    } catch (Throwable t) {
                        LOGGER.error("Exception in client thread", t);
                    }
                }
            };
            gameThread.start();
        } else {
            try {
                RenderSystem.initGameThread(false);
                minecraft.run();
            } catch (Throwable t) {
                LOGGER.error("Unhandled game exception", t);
            }
        }

        try {
            minecraft.shutdown();
            if (gameThread != null) gameThread.join();
        } catch (InterruptedException e) {
            LOGGER.error("Exception during client thread shutdown", e);
        } finally {
            minecraft.shutdownMinecraftApplet();
        }
    }
}
