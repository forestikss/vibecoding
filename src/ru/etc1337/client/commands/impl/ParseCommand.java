package ru.etc1337.client.commands.impl;

import net.minecraft.util.text.TextFormatting;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.other.AsyncManager;
import ru.etc1337.client.commands.Command;
import ru.etc1337.client.commands.api.CommandInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@CommandInfo(name = "ParseAnydesk", description = "Анализирует соединения Anydesk и показывает информацию о них", aliases = {"parse", "parseanydesk"})
public class ParseCommand extends Command {
    @Compile
    @Override
    public void execute(String[] args) {
        AsyncManager.run(() -> {
            try {
                List<ConnectionInfo> connections = getAnydeskConnections();

                if (connections.isEmpty()) {
                    Chat.send(TextFormatting.YELLOW + "Активных соединений Anydesk не найдено.");
                    return;
                }

                boolean foundValidConnections = false;
                for (ConnectionInfo conn : connections) {
                    if (!conn.isp.contains("Limestone")
                            && !conn.isp.contains("DataCamp")) {
                        displayConnectionInfo(conn);
                        foundValidConnections = true;
                    }
                }

                if (!foundValidConnections) {
                    Chat.send(TextFormatting.YELLOW + "Найдены только нежелательные соединения, информация не отображается.");
                }
            } catch (Exception e) {
                Chat.send(TextFormatting.RED + "Ошибка при анализе соединений Anydesk: " + e.getMessage());
            }
        });
    }

    @Compile
    private List<ConnectionInfo> getAnydeskConnections() throws IOException {
        List<ConnectionInfo> connections = new ArrayList<>();
        Process process = Runtime.getRuntime().exec("netstat -ano");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("ESTABLISHED")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 5) {
                        String foreignAddress = parts[2];
                        String pid = parts[4];
                        if (isAnydeskProcess(pid)) {
                            String[] foreignParts = foreignAddress.split(":");
                            if (foreignParts.length == 2) {
                                String ip = foreignParts[0];
                                int port = Integer.parseInt(foreignParts[1]);

                                if (!ip.startsWith("192.168.") && !ip.equals("127.0.0.1")) {
                                    ConnectionInfo info = getIpInfo(ip, port);
                                    connections.add(info);
                                }
                            }
                        }
                    }
                }
            }
        }

        return connections;
    }

    @Compile
    private boolean isAnydeskProcess(String pid) throws IOException {
        Process process = Runtime.getRuntime().exec("tasklist /FI \"PID eq " + pid + "\"");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("anydesk")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Compile
    private ConnectionInfo getIpInfo(String ip, int port) throws IOException {
        URL url = new URL("http://ip-api.com/json/" + ip);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            String json = response.toString();
            String country = extractJsonValue(json, "country");
            String region = extractJsonValue(json, "regionName");
            String city = extractJsonValue(json, "city");
            String isp = extractJsonValue(json, "isp");

            return new ConnectionInfo(ip, port, country, region, city, isp);
        }
    }

    @Compile
    private String extractJsonValue(String json, String key) {
        int start = json.indexOf("\"" + key + "\":") + key.length() + 3;
        if (start < key.length() + 3) return "Неизвестно";
        int end = json.indexOf("\"", start + 1);
        if (end == -1) return "Неизвестно";
        return json.substring(start, end);
    }

    @Compile
    private void displayConnectionInfo(ConnectionInfo info) {
        String ipPort = info.ip + ":" + info.port;

        Chat.send(TextFormatting.GRAY + "IP: " + TextFormatting.WHITE + info.ip);
        Chat.send(TextFormatting.GRAY + "Port: " + TextFormatting.WHITE + info.port);
        Chat.send(TextFormatting.GRAY + "Сountry: " + TextFormatting.WHITE + info.country.replace("\"", ""));
        Chat.send(TextFormatting.GRAY + "Region: " + TextFormatting.WHITE + info.region.replace("\"", ""));
        Chat.send(TextFormatting.GRAY + "Сity: " + TextFormatting.WHITE + info.city.replace("\"", ""));
        Chat.send(TextFormatting.GRAY + "ISP: " + TextFormatting.WHITE + info.isp.replace("\"", ""));

        copyToClipboard(ipPort);
    }

    @Compile
    private void copyToClipboard(String text) {
        try {
            mc.keyboardListener.getClipboardHelper().setClipboardString(mc.getMainWindow().getHandle(), text);
        } catch (Exception e) {
            Chat.send(TextFormatting.RED + "Ошибка копирования в буфер обмена: " + e.getMessage());
        }
    }

    private static class ConnectionInfo {
        String ip;
        int port;
        String country;
        String region;
        String city;
        String isp;

        ConnectionInfo(String ip, int port, String country, String region, String city, String isp) {
            this.ip = ip;
            this.port = port;
            this.country = country;
            this.region = region;
            this.city = city;
            this.isp = isp;
        }
    }
}