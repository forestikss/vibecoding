package ru.etc1337.api.game;

import lombok.experimental.UtilityClass;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Translator {
    private static final Map<Character, String> translitMap = new HashMap<>();

    static {

        translitMap.put('ᴀ', "A");
        translitMap.put('ʙ', "B");
        translitMap.put('ᴄ', "C");
        translitMap.put('ᴅ', "D");
        translitMap.put('ᴇ', "E");
        translitMap.put('ғ', "F");
        translitMap.put('ɢ', "G");
        translitMap.put('ʜ', "H");
        translitMap.put('ɪ', "I");
        translitMap.put('ᴊ', "J");
        translitMap.put('ᴋ', "K");
        translitMap.put('ʟ', "L");
        translitMap.put('ᴍ', "M");
        translitMap.put('ɴ', "N");
        translitMap.put('ᴏ', "O");
        translitMap.put('ᴘ', "P");
        translitMap.put('ǫ', "Q");
        translitMap.put('ʀ', "R");
        translitMap.put('s', "S");
        translitMap.put('ᴛ', "T");
        translitMap.put('ᴜ', "U");
        translitMap.put('ᴠ', "V");
        translitMap.put('ᴡ', "W");
        translitMap.put('z', "Z");
        translitMap.put('x', "X");
        translitMap.put('ʏ', "Y");
    }

    public ITextComponent translate(ITextComponent input) {
        String translatedText = TextFormatting.getTextWithoutFormattingCodes(
                translate(input.getUnformattedComponentText().replace("●", "").strip())
        );

        IFormattableTextComponent translatedComponent = new StringTextComponent(translatedText).setStyle(input.getStyle());
        for (ITextComponent sibling : input.getSiblings()) {
            translatedComponent.append(translate(sibling));
        }
        return translatedComponent;
    }

    public String translate(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            result.append(translitMap.getOrDefault(c, String.valueOf(c)));
        }
        return result.toString();
    }
}
