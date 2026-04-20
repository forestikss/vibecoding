package ru.etc1337.api.irc.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class IRCServer {
    private static final int PORT = 6667;
    // ВАЖНО: Сгенерируйте свой собственный ключ и храните его безопасно!
    // Генератор ключа: `openssl rand -base64 16` (для AES-128)
    private static final String SECRET_KEY_BASE64 = "5v4jKpP3+QqX7W2yLb9f8g==";
    private static final int TIMEOUT = 30000;
    private static final String DB_FILE = "irc_groups.json";

    private static final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static final ConcurrentHashMap<String, Set<ClientHandler>> channels = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> groupPasswords = new ConcurrentHashMap<>();
    private static final Map<String, String> userGroups = new ConcurrentHashMap<>();
    private static final Set<String> nicknames = ConcurrentHashMap.newKeySet();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static CryptoManager cryptoManager;

    public static void main(String[] args) {
        try {
            cryptoManager = new CryptoManager(SECRET_KEY_BASE64);
            System.out.println("[СЕРВЕР] Рабочая директория: " + System.getProperty("user.dir"));
            loadGroupsFromFile();

            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("IRC Сервер запущен на порту " + PORT);

            channels.computeIfAbsent("#общий", k -> ConcurrentHashMap.newKeySet());

            ScheduledExecutorService timeoutService = Executors.newSingleThreadScheduledExecutor();
            timeoutService.scheduleAtFixedRate(IRCServer::checkTimeouts, TIMEOUT / 2, TIMEOUT / 2, TimeUnit.MILLISECONDS);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[СЕРВЕР] Новое подключение от " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Не удалось запустить сервер на порту " + PORT + ": " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Критическая ошибка инициализации: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void checkTimeouts() {
        long currentTime = System.currentTimeMillis();
        for (ClientHandler client : clients) {
            if (currentTime - client.getLastActivity().get() > TIMEOUT) {
                System.out.println("[СЕРВЕР] " + client.getNickname() + " отключён по таймауту");
                client.disconnect();
            }
        }
    }

    @SneakyThrows
    private static synchronized void loadGroupsFromFile() {
        File file = new File(DB_FILE);
        if (!file.exists() || file.length() == 0) return;

        byte[] fileBytes = Files.readAllBytes(Paths.get(DB_FILE));
        String encryptedJson = new String(fileBytes, StandardCharsets.UTF_8);
        String json = cryptoManager.decrypt(encryptedJson);

        Type channelListType = new TypeToken<List<ChannelData>>() {}.getType();
        List<ChannelData> loadedChannels = gson.fromJson(json, channelListType);

        if (loadedChannels == null) return;

        for (ChannelData data : loadedChannels) {
            channels.put(data.getName(), ConcurrentHashMap.newKeySet());
            if (data.getPassword() != null && !data.getPassword().isEmpty()) {
                groupPasswords.put(data.getName(), data.getPassword());
            }
            if (data.getPersistentUsers() != null) {
                for (String nick : data.getPersistentUsers()) {
                    userGroups.put(nick.trim(), data.getName());
                }
            }
        }
        System.out.println("[СЕРВЕР] Группы и пользователи успешно загружены из " + DB_FILE);
    }

    @SneakyThrows
    private static synchronized void saveGroupsToFile() {
        List<ChannelData> channelsToSave = new ArrayList<>();
        for (String groupName : channels.keySet()) {
            if (groupName.equals("#общий")) continue;

            ChannelData data = new ChannelData();
            data.setName(groupName);
            data.setPassword(groupPasswords.get(groupName));

            Set<String> persistentUsers = new HashSet<>();
            userGroups.forEach((user, group) -> {
                if (group.equals(groupName)) {
                    persistentUsers.add(user);
                }
            });
            data.setPersistentUsers(persistentUsers);
            channelsToSave.add(data);
        }

        String json = gson.toJson(channelsToSave);
        String encryptedJson = cryptoManager.encrypt(json);

        Files.write(Paths.get(DB_FILE), encryptedJson.getBytes(StandardCharsets.UTF_8));
    }


    @Data
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final BufferedReader in;
        private final BufferedWriter out;
        private String nickname;
        private final AtomicLong lastActivity = new AtomicLong(System.currentTimeMillis());
        private volatile boolean running = true;
        private String currentGroup = "#общий";

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.socket.setSoTimeout(TIMEOUT * 2); // Увеличим, т.к. есть свой тайм-аут
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        }

        @Override
        public void run() {
            try {
               // sendMessage("[СЕРВЕР] Добро пожаловать! Пожалуйста, представьтесь командой: #nick <ваше_имя>");
                String encryptedMessage;

                while (running && (encryptedMessage = in.readLine()) != null) {
                    lastActivity.set(System.currentTimeMillis());
                    String message;
                    try {
                        message = cryptoManager.decrypt(encryptedMessage);
                    } catch (Exception e) {
                        System.out.println("[СЕРВЕР] Ошибка дешифровки от клиента. Разрыв соединения.");
                        break;
                    }

                    if (nickname == null) {
                        if (message.toLowerCase().startsWith("#nick ")) {
                            handleNickCommand(message.substring(6).trim());
                        }// else {
            //                sendMessage("[СЕРВЕР] Сначала необходимо указать ник: #nick <ваше_имя>");
                      //  }
                        continue;
                    }

                    if (message.startsWith("#")) {
                        processCommand(message);
                    } else if (isSystemCommand(message, "PING")) {
                        sendSystemCommand("PONG");
                    } /*else if (!isSystemCommand(message)) {
                        broadcastToChannel("[" + currentGroup + "] " + nickname + ": " + message, currentGroup);
                    }*/
                }
            } catch (IOException e) {
                // Игнорируем SocketTimeoutException, т.к. у нас свой механизм
                if (!(e instanceof java.net.SocketTimeoutException)) {
                    System.out.println("[СЕРВЕР] " + (nickname != null ? nickname : "Клиент") + " отключён: " + e.getMessage());
                }
            } finally {
                disconnect();
            }
        }

        private void processCommand(String message) throws IOException {
            String[] parts = message.split("\\s+", 3);
            String command = parts[0].toLowerCase();
            String args1 = parts.length > 1 ? parts[1].trim() : "";
            String args2 = parts.length > 2 ? parts[2].trim() : "";

            switch (command) {
                case "#channel" -> handleChannelCommand(args1, args2);
                case "#msg" -> handlePrivateMessage(args1, args2);
                case "#help" -> handleHelpCommand();
                case "#quit" -> disconnect();
                case "#message" -> {
                    String text = message.substring(command.length()).trim();
                    if (!text.isEmpty()) {
                        String stringToSend = String.format(
                                "[%s%s%s] %s%s: %s",
                                TextFormatting.GRAY,
                                currentGroup,
                                TextFormatting.RESET,
                                nickname,
                                TextFormatting.GRAY,
                                text
                        );
                        broadcastToChannel(stringToSend, currentGroup);
                    }
                }
                default -> sendMessage("[СЕРВЕР] Неизвестная команда. Введите #help для помощи.");
            }
        }

        private void handleNickCommand(String newNick) throws IOException {
            if (newNick.isEmpty() || !newNick.matches("[a-zA-Z0-9_]{1,20}")) {
                sendMessage("[СЕРВЕР] Имя должно содержать 1-20 символов (латинские буквы, цифры, подчёркивание).");
                return;
            }

            if (!nicknames.add(newNick)) {
                sendMessage("[СЕРВЕР] Это имя уже используется. Попробуйте другое.");
                return;
            }

            if (this.nickname != null) {
                nicknames.remove(this.nickname);
                broadcastToChannel("[СЕРВЕР] " + this.nickname + " сменил имя на " + newNick, currentGroup);
            }

            this.nickname = newNick;

            String savedGroup = userGroups.getOrDefault(nickname, "#общий");
            joinChannel(savedGroup);

            sendMessage("[СЕРВЕР] Ваш ник успешно установлен: " + nickname);
            sendMessage("[СЕРВЕР] Вы автоматически подключены к каналу: " + currentGroup);
            broadcastToChannel("[СЕРВЕР] " + nickname + " вошёл в канал.", currentGroup);
        }

        private void handleChannelCommand(String subCommand, String args) throws IOException {
            String[] parts = args.split("\\s+", 2);
            String channelName = parts.length > 0 ? parts[0].trim() : "";
            String password = parts.length > 1 ? parts[1].trim() : "";

            switch (subCommand.toLowerCase()) {
                case "create" -> {
                    if (!channelName.startsWith("#") || channelName.length() < 2) {
                        sendMessage("[СЕРВЕР] Название канала должно начинаться с # и содержать символы.");
                        return;
                    }
                    if (channels.containsKey(channelName)) {
                        sendMessage("[СЕРВЕР] Канал с таким именем уже существует.");
                        return;
                    }

                    channels.put(channelName, ConcurrentHashMap.newKeySet());
                    if (!password.isEmpty()) {
                        groupPasswords.put(channelName, password);
                    }
                    saveGroupsToFile();
                    sendMessage("[СЕРВЕР] Канал " + channelName + " успешно создан.");
                }
                case "join" -> {
                    if (!channels.containsKey(channelName)) {
                        sendMessage("[СЕРВЕР] Такого канала не существует.");
                        return;
                    }
                    String requiredPassword = groupPasswords.get(channelName);
                    if (requiredPassword != null && !requiredPassword.equals(password)) {
                        sendMessage("[СЕРВЕР] Неверный пароль для канала " + channelName);
                        return;
                    }

                    leaveCurrentChannel();
                    joinChannel(channelName);
                    sendMessage("[СЕРВЕР] Вы успешно вошли в канал " + channelName);
                    broadcastToChannel("[СЕРВЕР] " + nickname + " присоединился к каналу.", channelName);
                }
                case "leave" -> {
                    if (currentGroup.equals("#общий")) {
                        sendMessage("[СЕРВЕР] Вы не можете покинуть #общий канал.");
                        return;
                    }
                    sendMessage("[СЕРВЕР] Вы покинули канал " + currentGroup + " и вернулись в #общий.");
                    leaveCurrentChannel();
                    joinChannel("#общий");
                }
                default -> sendMessage("[СЕРВЕР] Неизвестная подкоманда для #channel. Доступно: create, join, leave.");
            }
        }

        private void handlePrivateMessage(String targetNick, String message) throws IOException {
            if (targetNick.isEmpty() || message.isEmpty()) {
                sendMessage("[СЕРВЕР] Использование: #msg <имя> <сообщение>");
                return;
            }
            Optional<ClientHandler> target = clients.stream()
                    .filter(c -> targetNick.equalsIgnoreCase(c.getNickname()))
                    .findFirst();

            if (target.isPresent()) {
                target.get().sendMessage("[ЛС от " + nickname + "] " + message);
                sendMessage("[ЛС для " + targetNick + "] " + message);
            } else {
                sendMessage("[СЕРВЕР] Пользователь с ником '" + targetNick + "' не найден в сети.");
            }
        }

        private void handleHelpCommand() throws IOException {
            sendMessage("""
                [СЕРВЕР] Список доступных команд:
                #nick <имя> - Установить или сменить ваш ник.
                #channel create <#название> [пароль] - Создать новый канал.
                #channel join <#название> [пароль] - Присоединиться к каналу.
                #channel leave - Покинуть текущий канал и вернуться в #общий.
                #msg <ник> <сообщение> - Отправить личное сообщение.
                #message <сообщение> - Отправить сообщение в текущий канал (или просто начните печатать).
                #quit - Отключиться от сервера.
                #help - Показать это сообщение.
                """);
        }

        private void leaveCurrentChannel() {
            Set<ClientHandler> channel = channels.get(currentGroup);
            if (channel != null) {
                channel.remove(this);
                broadcastToChannel("[СЕРВЕР] " + nickname + " покинул канал.", currentGroup);
            }
        }

        private void joinChannel(String channelName) {
            this.currentGroup = channelName;
            channels.computeIfAbsent(channelName, k -> ConcurrentHashMap.newKeySet()).add(this);
            userGroups.put(this.nickname, channelName);
            saveGroupsToFile();
        }

        public void disconnect() {
            if (!running) return;
            running = false;

            if (nickname != null) {
                nicknames.remove(nickname);
                System.out.println("[СЕРВЕР] " + nickname + " отключился.");
                leaveCurrentChannel();
            }
            clients.remove(this);
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
        }

        private void broadcastToChannel(String message, String channelName) {
            Set<ClientHandler> channelClients = channels.get(channelName);
            if (channelClients == null) return;

            for (ClientHandler client : channelClients) {
                client.sendMessage(message);
            }
        }

        @SneakyThrows
        private void sendMessage(String message) {
            if (!running || out == null) return;
            String encrypted = cryptoManager.encrypt(message);
            out.write(encrypted + "\n");
            out.flush();
        }

        @SneakyThrows
        private void sendSystemCommand(String command) {
            sendMessage("\u0001" + command + "\u0001");
        }

        private boolean isSystemCommand(String message) {
            return message.startsWith("\u0001") && message.endsWith("\u0001");
        }

        private boolean isSystemCommand(String message, String expectedCommand) {
            return isSystemCommand(message) &&
                    message.substring(1, message.length() - 1).equals(expectedCommand);
        }
    }
}