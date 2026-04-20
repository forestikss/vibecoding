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

public class Start {

    private static final Logger LOGGER = LogManager.getLogger();

    static {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
                String.valueOf(Runtime.getRuntime().availableProcessors()));
    }

    public static void main(String[] args) {
        // Данные пользователя — замени на свои
        UserAPI.username = "DevPlayer";
        UserAPI.uid = 0;
        UserAPI.role = "dev";
        UserAPI.beta = true;

        CrashReport.crash();
        Bootstrap.register();
        Bootstrap.checkTranslations();
        Util.func_240994_l_();

        Session session = new Session(UserAPI.username, "00000000000000000000000000000000", "0", "mojang");

        File gameDir          = new File(Constants.MAIN_DIRECTORY.getAbsolutePath());
        File assetsDir        = new File(gameDir, "assets/");
        File resourcepacksDir = new File(gameDir, "resourcepacks/");

        GameConfiguration config = new GameConfiguration(
                new GameConfiguration.UserInformation(session, new PropertyMap(), new PropertyMap(), Proxy.NO_PROXY),
                new ScreenSize(854, 480, OptionalInt.empty(), OptionalInt.empty(), false),
                new GameConfiguration.FolderInformation(gameDir, resourcepacksDir, assetsDir, "1.16"),
                new GameConfiguration.GameInformation("1.16.5", "release"),
                new GameConfiguration.ServerInformation(null, 25565));

        launch(config);
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
            CrashReport crash = CrashReport.makeCrashReport(t, "Initializing game");
            crash.makeCategory("Initialization");
            Minecraft.fillCrashReport(null, config.gameInfo.version, null, crash);
            Minecraft.displayCrashReport(crash);
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
