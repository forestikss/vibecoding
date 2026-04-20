package ru.etc1337.api.interfaces;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;

public interface QuickImports {
    Minecraft mc = Minecraft.getInstance();
    MainWindow window = mc.getMainWindow();
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder builder = tessellator.getBuffer();
}
