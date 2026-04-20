package ru.etc1337.api.render.fonts.v2;

import net.minecraft.util.ResourceLocation;

public class Resource extends ResourceLocation
{

    private static final String NAMESPACE = "dreamcore/";

    public Resource(String pathIn)
    {
        super("minecraft", NAMESPACE + pathIn);
    }
}
