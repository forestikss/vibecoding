package ru.etc1337.api.game;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourcePack;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import ru.etc1337.api.interfaces.QuickImports;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class PrefixHelper implements QuickImports {
    private static final Map<String, String> translitMap = new HashMap<>();

    static {
        updateTranslitMap();
    }

    public void updateTranslitMap() {
        translitMap.put("ꔀ", TextFormatting.GRAY + "PLAYER");
        translitMap.put("ꔄ", TextFormatting.BLUE + "HERO");
        translitMap.put("ꔈ", TextFormatting.YELLOW + "TITAN");
        translitMap.put("ꔒ", TextFormatting.GREEN + "AVENGER");
        translitMap.put("ꔖ", TextFormatting.AQUA + "OVERLORD");
        translitMap.put("ꔠ", TextFormatting.GOLD + "MAGISTER");
        translitMap.put("ꔤ", TextFormatting.RED + "IMPERATOR");
        translitMap.put("ꔨ", TextFormatting.LIGHT_PURPLE + "DRAGON");
        translitMap.put("ꔲ", TextFormatting.DARK_PURPLE + "BULL");
        translitMap.put("ꔶ", TextFormatting.GOLD + "TIGER");
        translitMap.put("ꕀ", TextFormatting.DARK_GREEN + "HYDRA");
        translitMap.put("ꕁ", TextFormatting.GOLD + "LEGENDA");
        translitMap.put("ꕄ", TextFormatting.DARK_RED + "DRACULA");
        translitMap.put("ꕅ", TextFormatting.BLUE + "RAIN");
        translitMap.put("ꕈ", TextFormatting.GREEN + "COBRA");
        translitMap.put("ꕉ", TextFormatting.GREEN + "LIME");
        translitMap.put("ꕒ", TextFormatting.WHITE + "RABBIT");
        translitMap.put("ꕓ", TextFormatting.LIGHT_PURPLE + "SAKURA");
        translitMap.put("ꕖ", TextFormatting.BLACK + "BUNNY");
        translitMap.put("ꕠ", TextFormatting.YELLOW + "D.HELPER");
        translitMap.put("ꔉ", TextFormatting.YELLOW + "HELPER");
        translitMap.put("ꔓ", TextFormatting.BLUE + "ML.MODER");
        translitMap.put("ꔗ", TextFormatting.BLUE + "MODER");
        translitMap.put("ꔡ", TextFormatting.DARK_PURPLE + "MODER+");
        translitMap.put("ꔩ", TextFormatting.BLUE + "GL.MODER");
        translitMap.put("ꔳ", TextFormatting.AQUA + "ML.ADMIN");
        translitMap.put("ꔷꔸ", TextFormatting.RED + "ADMIN");
        translitMap.put("ꔅ", TextFormatting.RED + "YT");
        translitMap.put("ꔁ", TextFormatting.LIGHT_PURPLE + "MEDIA");
    }

    public boolean isServerForFix() {
        return mc.getResourcePackList().getEnabledPacks().stream()
                //.filter(ResourcePackInfo::isAlwaysEnabled)
                .map(pack -> TextFormatting.getTextWithoutFormattingCodes(pack.getDescription().getString().trim()))
                .filter(Objects::nonNull)
                .anyMatch(description -> description.contains("ReallyWorld") || description.contains("LegendsGrief") || description.contains("CakeWorld"));
    }

    public IFormattableTextComponent translate(ITextComponent input) {
        String translatedText = translate(input.getString());

        IFormattableTextComponent translatedComponent = new StringTextComponent(translatedText).setStyle(input.getStyle());

        for (ITextComponent sibling : input.getSiblings()) {
            translatedComponent.append(translate(sibling));
        }
        return translatedComponent;
    }

    public String translate(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            String character = input.substring(i, i + 1);
            result.append(translitMap.getOrDefault(character, character));
        }
        return result.toString();
    }
}
