package net.minecraft.loot;

import java.util.Random; import ru.etc1337.api.mods.fastrandom.FastRandom;
import net.minecraft.util.ResourceLocation;

public interface IRandomRange
{
    ResourceLocation CONSTANT = new ResourceLocation("constant");
    ResourceLocation UNIFORM = new ResourceLocation("uniform");
    ResourceLocation BINOMIAL = new ResourceLocation("binomial");

    int generateInt(Random rand);

    ResourceLocation getType();
}
