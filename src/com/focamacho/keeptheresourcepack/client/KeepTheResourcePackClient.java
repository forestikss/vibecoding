package com.focamacho.keeptheresourcepack.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IPackNameDecorator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class KeepTheResourcePackClient {

    private static final File latestServerResourcePack = new File(Minecraft.getInstance().gameDir, "latestServerResourcePack.json");
    public static File cacheResourcePackFile = null;

    public void onInitializeClient() {
      /*  if(latestServerResourcePack.exists()) {
            try {
                JsonObject jsonObject = new JsonParser().parse(FileUtils.readFileToString(latestServerResourcePack, StandardCharsets.UTF_8)).getAsJsonObject();
                File resourcePack = new File(jsonObject.get("file").getAsString());

                if(resourcePack.exists()) {
                    cacheResourcePackFile = resourcePack;
                    Minecraft.getInstance().getPackFinder().setServerPack(resourcePack, IPackNameDecorator.SERVER);
                }
                else setLatestServerResourcePack(null);
            } catch (IOException e) { e.printStackTrace(); }
        }*/
    }

    public static void setLatestServerResourcePack(File file) {
        if(file == null) latestServerResourcePack.delete();
        else {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("file", file.getPath());

            cacheResourcePackFile = file;
            try { FileUtils.writeStringToFile(latestServerResourcePack, jsonObject.toString(), StandardCharsets.UTF_8); } catch(IOException e) { e.printStackTrace(); }
        }
    }
}