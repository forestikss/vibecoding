package ru.etc1337.api.irc.server;

import lombok.Data;
import java.util.Set;

@Data
public class ChannelData {
    private String name;
    private String password; // null, если пароля нет
    private Set<String> persistentUsers; // Ники пользователей, которые были в канале
}