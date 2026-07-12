package cross.liqui_mineral.datagen;

import java.util.Map;

/**
 * English/Chinese display names for this mod's known molten fluids — kept here, not in
 * {@code fluids.json}, the same way any other mod's translations live in its lang files rather
 * than in a gameplay config a player edits. A fluid id with no entry here (i.e. one a player
 * added themselves via {@code fluids.json}) falls back to a title-cased version of its id.
 */
public final class MoltenFluidNames {

    private record Name(String en, String zh) {
    }

    private static final Map<String, Name> KNOWN = Map.ofEntries(
            Map.entry("molten_iron", new Name("Molten Iron", "熔融铁")),
            Map.entry("molten_gold", new Name("Molten Gold", "熔融金")),
            Map.entry("molten_copper", new Name("Molten Copper", "熔融铜")),
            Map.entry("molten_diamond", new Name("Molten Diamond", "熔融钻石")),
            Map.entry("molten_netherite", new Name("Molten Netherite", "熔融下界合金")),
            Map.entry("molten_zinc", new Name("Molten Zinc", "熔融锌")),
            Map.entry("molten_brass", new Name("Molten Brass", "熔融黄铜")),
            Map.entry("molten_amber_gold", new Name("Molten Amber Gold", "熔融琥珀金")));

    public static String en(String id) {
        Name known = KNOWN.get(id);
        return known != null ? known.en() : titleCase(id);
    }

    public static String zh(String id) {
        Name known = KNOWN.get(id);
        return known != null ? known.zh() : en(id);
    }

    private static String titleCase(String id) {
        StringBuilder titleCased = new StringBuilder();
        for (String word : id.split("_")) {
            if (word.isEmpty()) {
                continue;
            }
            if (titleCased.length() > 0) {
                titleCased.append(' ');
            }
            titleCased.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return titleCased.toString();
    }

    private MoltenFluidNames() {
    }
}
