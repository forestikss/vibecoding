package ru.etc1337.api.irc.client;

import lombok.SneakyThrows;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.handler.EventListener;
import ru.etc1337.api.events.impl.game.EventSendMessage;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.other.AsyncManager;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IRCClient implements EventListener, QuickImports {
    private final String SERVER = "89.35.130.212"; // или "localhost" для тестов // 89.35.130.212
    private final int PORT = 6667;
    // ВАЖНО: Этот ключ должен быть ТОЧНО ТАКИМ ЖЕ, как на сервере!
    private static final String SECRET_KEY_BASE64 = "5v4jKpP3+QqX7W2yLb9f8g==";
    private final int TIMEOUT = 30000;

    private volatile boolean running = false;
    private Socket socket;
    private BufferedWriter out;
    private BufferedReader in;
    private CryptoManager cryptoManager;
    private IrcCommandManager ircCommandManager;
    private ScheduledExecutorService pingService;

    public IRCClient() {
        Client.getEventManager().register(this);
        try {
            this.cryptoManager = new CryptoManager(SECRET_KEY_BASE64);
            this.ircCommandManager = new IrcCommandManager(this);
        } catch (Exception e) {
            System.err.println("[IRC] КРИТИЧЕСКАЯ ОШИБКА: Не удалось инициализировать криптографию. IRC отключен.");
            e.printStackTrace();
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventSendMessage eventSendMessage && cryptoManager != null) {
            String message = eventSendMessage.getMessage();

            if (message.startsWith("#") && !event.isCancelled()) {
                event.setCancelled(true);
                if (!running) {
                    start(Client.getInstance().getUserInfo().getUsername());
                    printToChat("[IRC] Вы не подключены. Сначала подключитесь к серверу.");
                    return;
                }
                AsyncManager.run(() -> ircCommandManager.handleCommand(message));
            }
        }
    }

    public void start(String username) {
        if (running) {
            printToChat("[IRC] Вы уже подключены.");
            return;
        }
        AsyncManager.run(() -> connect(username));
    }

    @SneakyThrows
    private void connect(String username) {
        try {
            socket = new Socket(SERVER, PORT);
            socket.setSoTimeout(TIMEOUT * 2);
            running = true;

            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

    //        printToChat("[IRC] Подключено к " + SERVER + ". Отправка ника...");

            // Отправка ника при подключении
            sendCommand("#nick " + username);

            // Поток для чтения сообщений
            Thread readerThread = new Thread(this::listen);
            readerThread.setDaemon(true);
            readerThread.start();

            // Сервис для отправки PING
            pingService = Executors.newSingleThreadScheduledExecutor();
            pingService.scheduleAtFixedRate(() -> sendSystemCommand("PING"), TIMEOUT / 2, TIMEOUT / 2, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            printToChat("[IRC] Ошибка подключения: " + e.getMessage());
            disconnect();
        }
    }

    private void listen() {
        try {
            String encryptedLine;
            while (running && (encryptedLine = in.readLine()) != null) {
                String decrypted = cryptoManager.decrypt(encryptedLine);
                if (isSystemCommand(decrypted, "PONG")) {
                    // PONG получен, все в порядке
                } else if (!isSystemCommand(decrypted)) {
                    printToChat(decrypted);
                }
            }
        } catch (SocketTimeoutException e) {
            printToChat("[IRC] Соединение прервано по таймауту.");
        } catch (Exception e) {
            if (running) {
                printToChat("[IRC] Соединение потеряно: " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        if (!running) return;
        running = false;

        if (pingService != null) {
            pingService.shutdownNow();
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // ignore
        }
        printToChat("[IRC] Вы были отключены от сервера.");
    }

    public void sendCommand(String command) {
        if (!running || out == null) return;
        try {
            String encrypted = cryptoManager.encrypt(command);
            out.write(encrypted + "\n");
            out.flush();
        } catch (Exception e) {
            printToChat("[IRC] Ошибка отправки команды: " + e.getMessage());
            disconnect();
        }
    }

    private void sendSystemCommand(String command) {
        if (!running) return;
        sendCommand("\u0001" + command + "\u0001");
    }

    public void printToChat(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(new StringTextComponent(message), Util.DUMMY_UUID);
        } else {
            System.out.println(message);
        }
    }

    private boolean isSystemCommand(String message) {
        return message.startsWith("\u0001") && message.endsWith("\u0001");
    }

    private boolean isSystemCommand(String message, String expectedCommand) {
        return isSystemCommand(message) && message.substring(1, message.length() - 1).equals(expectedCommand);
    }
}