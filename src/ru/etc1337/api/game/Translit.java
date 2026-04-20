package ru.etc1337.api.game;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Translit {
    private static final Map<Character, String> RU_TO_EN = new HashMap<>();
    private static final Map<String, Character> EN_TO_RU = new HashMap<>();

    static {
        RU_TO_EN.put('а', "a");
        RU_TO_EN.put('б', "b");
        RU_TO_EN.put('в', "v");
        RU_TO_EN.put('г', "g");
        RU_TO_EN.put('д', "d");
        RU_TO_EN.put('е', "e");
        RU_TO_EN.put('ё', "yo");
        RU_TO_EN.put('ж', "zh");
        RU_TO_EN.put('з', "z");
        RU_TO_EN.put('и', "i");
        RU_TO_EN.put('й', "y");
        RU_TO_EN.put('к', "k");
        RU_TO_EN.put('л', "l");
        RU_TO_EN.put('м', "m");
        RU_TO_EN.put('н', "n");
        RU_TO_EN.put('о', "o");
        RU_TO_EN.put('п', "p");
        RU_TO_EN.put('р', "r");
        RU_TO_EN.put('с', "s");
        RU_TO_EN.put('т', "t");
        RU_TO_EN.put('у', "u");
        RU_TO_EN.put('ф', "f");
        RU_TO_EN.put('х', "kh");
        RU_TO_EN.put('ц', "ts");
        RU_TO_EN.put('ч', "ch");
        RU_TO_EN.put('ш', "sh");
        RU_TO_EN.put('щ', "shch");
        RU_TO_EN.put('ъ', "");
        RU_TO_EN.put('ы', "y");
        RU_TO_EN.put('ь', "");
        RU_TO_EN.put('э', "e");
        RU_TO_EN.put('ю', "yu");
        RU_TO_EN.put('я', "ya");

        for (Map.Entry<Character, String> entry : RU_TO_EN.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                EN_TO_RU.put(entry.getValue(), entry.getKey());
            }
        }
    }

    public  String translitRuToEn(String text) {
        StringBuilder result = new StringBuilder();
        text = text.toLowerCase();

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (RU_TO_EN.containsKey(ch)) {
                result.append(RU_TO_EN.get(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    public String translitEnToRu(String text) {
        StringBuilder result = new StringBuilder();
        text = text.toLowerCase();

        for (int i = 0; i < text.length(); i++) {
            boolean found = false;
            for (int len = 4; len >= 1; len--) {
                if (i + len <= text.length()) {
                    String substr = text.substring(i, i + len);
                    if (EN_TO_RU.containsKey(substr)) {
                        result.append(EN_TO_RU.get(substr));
                        i += len - 1;
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                result.append(text.charAt(i));
            }
        }
        return result.toString();
    }
}
