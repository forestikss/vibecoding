package ru.etc1337.api.capes;


import ru.etc1337.api.capes.config.Config;

public class WaveyCapesBase {

    public static WaveyCapesBase INSTANCE;
    public static Config config;

    public WaveyCapesBase start() {
        INSTANCE = this;
        config = new Config();
        return this;
    }
}
