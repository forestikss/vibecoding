package net.minecraft.client.main;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.Empty3i;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.GameConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenSize;
import net.minecraft.client.util.UndeclaredException;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.DefaultUncaughtExceptionHandler;
import net.minecraft.util.Session;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Bootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.etc1337.protection.checks.Checks;
import ru.etc1337.protection.interfaces.Include;
import ru.etc1337.protection.storage.Constants;

import java.io.File;
import java.net.Proxy;
import java.util.OptionalInt;

public class Starter {
    private static Logger LOGGER = LogManager.getLogger();

    static {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(Runtime.getRuntime().availableProcessors()));
    }

    @Include
    public static void start() {
        if (!Checks.check()) return;
        CrashReport.crash();
        Bootstrap.register();
        Bootstrap.checkTranslations();
        Util.func_240994_l_();

        // UserData
        String username = "SelekMC";
        String uuid = "b04df7e9a5154a6c91e153f3c9b23dc1";
        String accessToken = "eyJraWQiOiIwNDkxODEiLCJhbGciOiJSUzI1NiJ9.eyJ4dWlkIjoiMjUzNTQxNTM4ODY3OTUyOCIsImFnZyI6IkFkdWx0Iiwic3ViIjoiYjNiMTdmNGMtZjc4Ni00MzU0LTk4NWMtYWU2MTE0NGExNzZhIiwiYXV0aCI6IlhCT1giLCJucyI6ImRlZmF1bHQiLCJwc25pZCI6IjI0MDk3MjcxNjM0MDgzODI4NDQiLCJyb2xlcyI6W10sImlzcyI6ImF1dGhlbnRpY2F0aW9uIiwiZmxhZ3MiOlsibXNhbWlncmF0aW9uX3N0YWdlNCIsInR3b2ZhY3RvcmF1dGgiLCJtdWx0aXBsYXllciIsIm9yZGVyc18yMDIyIl0sInByb2ZpbGVzIjp7Im1jIjoiYjA0ZGY3ZTktYTUxNS00YTZjLTkxZTEtNTNmM2M5YjIzZGMxIn0sInBsYXRmb3JtIjoiVU5LTk9XTiIsInl1aWQiOiJjOTA2NWIzNDI5ZDg2NDI3M2Q0NTAzMzVkNzNjZDgzMiIsInBmZCI6W3sidHlwZSI6Im1jIiwiaWQiOiJiMDRkZjdlOS1hNTE1LTRhNmMtOTFlMS01M2YzYzliMjNkYzEiLCJuYW1lIjoiU2VsZWtNQyJ9XSwibmJmIjoxNzQ5MzAxMTg0LCJleHAiOjE3NDkzODc1ODQsImlhdCI6MTc0OTMwMTE4NH0.vg4tg8vv2Pr6cmSUIEeVZb69zl4Lt4NwU7k6Gw3aXiJ8YXIJV0N6vevtZKtPnIeq27crBLRk9QoMzpOgjF1P9tVQYk55-6zfYr_JtIm2CVY13lReS1hHiuugNyoDex_4U15FtKrIQopDy6_HH29W7dyellpRcpuUSpluTvn_mO9DlRUXqm4fHDXPMDCPASCvu9gS6njy05xPB7VOUhK1qVzHX1ML7u2EwwGgn4ZsGJI6UKQtoWH_BFrs43Oo8-O00vYLkU9sOZthFwcBLgcuLScMtshOOYmcB9Gg00YtevWxQzIpFVQN3CLxnFcKZb1T_F7dt4e4f7N1eZyhLdpAbQ";
        String type = "mojang";
        Session session = new Session(username, uuid, accessToken, type);

        // DisplayData
        int width = 854;
        int height = 480;
        OptionalInt fullscreenWidth = OptionalInt.empty();
        OptionalInt fullscreenHeight = OptionalInt.empty();
        boolean isFullscreen = false;

        // FolderData
        File gameDir = new File(Constants.MAIN_DIRECTORY.getAbsolutePath());
        File assetsDir = new File(gameDir, "assets/");
        File resourcepacksDir = new File(gameDir, "resourcepacks/");
        String assetIndex = "1.16";

        // GameData
        boolean demo = false;
        String launchVersion = "1.16.5";
        String versionType = "release";
        boolean disableMultiplayer = false;
        boolean disableChat = false;

        // ServerData
        String server = null;
        int port = 25565;

        GameConfiguration gameConfiguration = new GameConfiguration(
                new GameConfiguration.UserInformation(session, new PropertyMap(), new PropertyMap(), Proxy.NO_PROXY),
                new ScreenSize(width, height, fullscreenWidth, fullscreenHeight, isFullscreen),
                new GameConfiguration.FolderInformation(gameDir, resourcepacksDir, assetsDir, assetIndex),
                new GameConfiguration.GameInformation(launchVersion, versionType),
                new GameConfiguration.ServerInformation(server, port));
        start(gameConfiguration);
    }

    @Include
    private static void start(GameConfiguration gameConfiguration) {
        Thread thread = new Thread("Client Shutdown Thread") {
            public void run() {
                Minecraft minecraft1 = Minecraft.getInstance();

                IntegratedServer integratedserver = minecraft1.getIntegratedServer();

                if (integratedserver != null) {
                    integratedserver.initiateShutdown(true);
                }
            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        Runtime.getRuntime().addShutdownHook(thread);
        new Empty3i();
        final Minecraft minecraft;

        try {
            Thread.currentThread().setName("Main");
            RenderSystem.initRenderThread();
            RenderSystem.beginInitialization();
            minecraft = new Minecraft(gameConfiguration);
            RenderSystem.finishInitialization();
        } catch (UndeclaredException undeclaredexception) {
            LOGGER.warn("Failed to create window: ", undeclaredexception);
            return;
        } catch (Throwable throwable1) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Initializing game");
            crashreport.makeCategory("Initialization");
            Minecraft.fillCrashReport(null, gameConfiguration.gameInfo.version, null, crashreport);
            Minecraft.displayCrashReport(crashreport);
            return;
        }

        Thread mainThread;

        if (minecraft.isRenderOnThread()) {
            mainThread = new Thread("Game thread") {
                public void run() {
                    try {
                        RenderSystem.initGameThread(true);
                        minecraft.run();
                    } catch (Throwable throwable2) {
                        LOGGER.error("Exception in client thread", throwable2);
                    }
                }
            };
            mainThread.start();
        } else {
            mainThread = null;

            try {
                RenderSystem.initGameThread(false);
                minecraft.run();
            } catch (Throwable throwable) {
                LOGGER.error("Unhandled game exception", throwable);
            }
        }

        try {
            minecraft.shutdown();

            if (mainThread != null) {
                mainThread.join();
            }
        } catch (InterruptedException interruptedexception) {
            LOGGER.error("Exception during client thread shutdown", interruptedexception);
        } finally {
            minecraft.shutdownMinecraftApplet();
        }
    }
}
