package net.optifine.util;

import java.util.Random; import ru.etc1337.api.mods.fastrandom.FastRandom;

public class RandomUtils
{
    private static final Random random = new FastRandom();

    public static Random getRandom()
    {
        return random;
    }

    public static int getRandomInt(int bound)
    {
        return random.nextInt(bound);
    }
}
