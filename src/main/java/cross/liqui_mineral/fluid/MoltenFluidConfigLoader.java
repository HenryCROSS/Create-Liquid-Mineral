package cross.liqui_mineral.fluid;

import static cross.liqui_mineral.fluid.MoltenFluidSpec.BURNS_LIKE_LAVA;
import static cross.liqui_mineral.fluid.MoltenFluidSpec.GENERATED_DEFAULT_TEXTURE;
import static cross.liqui_mineral.fluid.MoltenFluidSpec.GENERATED_TEXTURE;
import static cross.liqui_mineral.fluid.MoltenFluidSpec.LAVA_PHYSICS;
import static cross.liqui_mineral.fluid.MoltenFluidSpec.LAVA_TEXTURE;
import static cross.liqui_mineral.fluid.MoltenFluidSpec.PROTECTS_FAMILY;
import static cross.liqui_mineral.fluid.MoltenFluidSpec.SAFE;
import static cross.liqui_mineral.fluid.MoltenFluidSpec.SHARED_FAMILY;
import static cross.liqui_mineral.fluid.MoltenFluidSpec.WATER_PHYSICS;
import static cross.liqui_mineral.fluid.MoltenFluidSpec.WATER_TEXTURE;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import cross.liqui_mineral.CreateLiquidMineral;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

/**
 * Loads the list of molten fluids to register from {@code config/createliquidmineral/fluids.json}
 * instead of hardcoding them in {@link MoltenFluids}. Adding a new molten fluid is then just
 * adding one JSON object to that file and restarting the game — no Java code change or rebuild
 * needed for the fluid itself. (Its bucket item model and lang entries still need
 * {@code gradlew runData}; see {@code cross.liqui_mineral.datagen}, which reads {@link #SPECS}
 * the same way this class does, so an entry that's disabled ({@code "enabled": false}) or whose
 * {@code requiredMod} isn't present is skipped consistently everywhere.)
 * <p>
 * Display names are intentionally not configured here — see
 * {@code cross.liqui_mineral.datagen.MoltenFluidNames} — this file is only physics/behavior, the
 * same way a datapack recipe never carries its own translated name.
 * <p>
 * On first run, or whenever the file is missing/unreadable, this writes out the mod's built-in
 * fluids as the file's contents, so there's always a valid, example-filled file to edit rather
 * than an empty one.
 */
public final class MoltenFluidConfigLoader {

    /**
     * One fluid, exactly as written in {@code fluids.json}. Null fields fall back to sensible
     * defaults. {@code physics} picks a preset ({@code "lava"}/{@code "water"}) or opts out of
     * presets entirely ({@code "custom"}); either way, every physics field below can still be set
     * explicitly to override (or, under {@code "custom"}, to fully define) that one fluid's
     * behavior without affecting any other.
     */
    public record FluidEntry(
            String id,
            Boolean enabled,
            String texture,
            String physics,
            String tint,
            Integer density,
            Integer viscosity,
            Integer temperature,
            Integer lightLevel,
            Integer tickRate,
            Integer slopeFindDistance,
            Integer levelDecreasePerBlock,
            Boolean canSwim,
            Boolean canDrown,
            Boolean canConvertToSource,
            Boolean canHydrate,
            Boolean canExtinguish,
            Boolean burnsEntities,
            Boolean protectsFamily,
            @JsonAdapter(RequiredModAdapter.class) List<String> requiredMod,
            Boolean translucent) {

        public MoltenFluidSpec toSpec() {
            UnaryOperator<MoltenFluidSpec> physicsTrait = switch (physics == null ? "" : physics.toLowerCase()) {
                case "lava" -> LAVA_PHYSICS;
                // No preset applied: every physics field below is read straight from this entry,
                // falling back to MoltenFluidSpec.blank()'s inert baseline for whatever it omits.
                case "custom" -> UnaryOperator.identity();
                default -> WATER_PHYSICS;
            };
            UnaryOperator<MoltenFluidSpec> textureTrait = switch (texture == null ? "" : texture.toLowerCase()) {
                case "lava" -> LAVA_TEXTURE;
                case "generated" -> GENERATED_TEXTURE;
                case "default" -> GENERATED_DEFAULT_TEXTURE;
                case "water" -> WATER_TEXTURE;
                default -> GENERATED_DEFAULT_TEXTURE;
            };
            MoltenFluidSpec spec = MoltenFluidSpec.of(id, physicsTrait, textureTrait)
                    .apply(Boolean.TRUE.equals(burnsEntities) ? BURNS_LIKE_LAVA : SAFE)
                    .apply(Boolean.TRUE.equals(protectsFamily) ? PROTECTS_FAMILY : SHARED_FAMILY);
            if (tint != null && !tint.isBlank()) {
                spec = spec.withApparentColor(0xFF000000 | (Integer.parseInt(tint.replace("#", ""), 16) & 0xFFFFFF));
            }
            if (density != null) {
                spec = spec.withDensity(density);
            }
            if (viscosity != null) {
                spec = spec.withViscosity(viscosity);
            }
            if (temperature != null) {
                spec = spec.withTemperature(temperature);
            }
            if (lightLevel != null) {
                spec = spec.withLightLevel(lightLevel);
            }
            if (tickRate != null) {
                spec = spec.withTickRate(tickRate);
            }
            if (slopeFindDistance != null) {
                spec = spec.withSlopeFindDistance(slopeFindDistance);
            }
            if (levelDecreasePerBlock != null) {
                spec = spec.withLevelDecreasePerBlock(levelDecreasePerBlock);
            }
            if (canSwim != null) {
                spec = spec.withCanSwim(canSwim);
            }
            if (canDrown != null) {
                spec = spec.withCanDrown(canDrown);
            }
            if (canConvertToSource != null) {
                spec = spec.withCanConvertToSource(canConvertToSource);
            }
            if (canHydrate != null) {
                spec = spec.withCanHydrate(canHydrate);
            }
            if (canExtinguish != null) {
                spec = spec.withCanExtinguish(canExtinguish);
            }
            if (translucent != null) {
                spec = spec.withTranslucent(translucent);
            }
            return spec;
        }

        /**
         * False if explicitly disabled ({@code "enabled": false}) — checked first, before
         * {@code requiredMod} is even looked at, so a disabled entry never registers regardless of
         * what mods are installed. Otherwise, true if {@code requiredMod} is empty/unset, or if
         * <em>any one</em> of the mod IDs it lists is loaded (OR, not AND — e.g.
         * {@code ["create", "createaddition"]} registers as soon as either one is present).
         */
        public boolean isAvailable() {
            if (Boolean.FALSE.equals(enabled)) {
                return false;
            }
            return requiredMod == null || requiredMod.isEmpty() || requiredMod.stream().anyMatch(ModList.get()::isLoaded);
        }
    }

    /**
     * Lets {@code requiredMod} in {@code fluids.json} be written either as a single string
     * ({@code "requiredMod": "createaddition"}, the original/still-supported shape) or a JSON
     * array of strings ({@code "requiredMod": ["create", "createaddition"]}) when a fluid should
     * register as long as any one of several mods is present. Always serializes back out as a
     * plain string for a single entry, or an array for more than one, whichever reads cleaner.
     */
    private static final class RequiredModAdapter extends TypeAdapter<List<String>> {
        @Override
        public void write(JsonWriter out, List<String> value) throws IOException {
            if (value == null || value.isEmpty()) {
                out.nullValue();
            } else if (value.size() == 1) {
                out.value(value.get(0));
            } else {
                out.beginArray();
                for (String modId : value) {
                    out.value(modId);
                }
                out.endArray();
            }
        }

        @Override
        public List<String> read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            if (in.peek() == JsonToken.STRING) {
                String modId = in.nextString();
                return modId.isBlank() ? null : List.of(modId);
            }
            List<String> modIds = new ArrayList<>();
            in.beginArray();
            while (in.hasNext()) {
                modIds.add(in.nextString());
            }
            in.endArray();
            return modIds.isEmpty() ? null : List.copyOf(modIds);
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type ENTRY_LIST_TYPE = new TypeToken<List<FluidEntry>>() {
    }.getType();

    /** The mod's built-in molten fluids — seeds {@code fluids.json} on first run and is the fallback if it can't be read. */
    // texture="generated" points at fluid_texture_gen/'s procedurally-painted animated textures
    // (see MoltenFluidSpec.GENERATED_TEXTURE); tint is null because the color is already baked
    // into those textures — see fluid_texture_gen/generate.py for the source hex per fluid.
    // lightLevel is uniformly 15 (max) across every fluid here -- originally varied per material
    // (8-15) by rough real-world temperature, but the request was to match every fluid's glow to
    // molten_diamond's (already 15), so there's no meaningful spread left to preserve.
    // Physics fields past lightLevel (tickRate/slopeFindDistance/levelDecreasePerBlock and the
    // five can*/swim/drown/etc. booleans) are all null here — every built-in fluid is happy with
    // whatever LAVA_PHYSICS already sets for them, so there's nothing to override.
    private static final List<FluidEntry> DEFAULTS = List.of(
            new FluidEntry("molten_iron", null, "generated", "lava", null, 7000, 6000, 1800, 15,
                    null, null, null, null, null, null, null, null, true, true, null, null),
            new FluidEntry("molten_gold", null, "generated", "lava", null, 6000, 4000, 1300, 15,
                    null, null, null, null, null, null, null, null, true, true, null, null),
            new FluidEntry("molten_copper", null, "generated", "lava", null, 5500, 3500, 1200, 15,
                    null, null, null, null, null, null, null, null, true, true, null, null),
            new FluidEntry("molten_diamond", null, "generated", "lava", null, 8000, 8000, 4000, 15,
                    null, null, null, null, null, null, null, null, true, true, null, null),
            new FluidEntry("molten_netherite", null, "generated", "lava", null, 9000, 9000, 2200, 15,
                    null, null, null, null, null, null, null, null, true, true, null, null),
            new FluidEntry("molten_zinc", null, "generated", "lava", null, 4500, 3000, 900, 15,
                    null, null, null, null, null, null, null, null, true, true, null, null),
            new FluidEntry("molten_brass", null, "generated", "lava", null, 5200, 3800, 1150, 15,
                    null, null, null, null, null, null, null, null, true, true, null, null),
            // Electrum — only registers if Create: Additions & Synthetics (modid "createaddition"),
            // which adds the electrum ingot this mod's fluid is themed after, is installed.
            new FluidEntry("molten_amber_gold", null, "generated", "lava", null, 5800, 4200, 1250, 15,
                    null, null, null, null, null, null, null, null, true, true, List.of("createaddition"), null));

    public static final List<FluidEntry> ENTRIES = load();
    public static final List<MoltenFluidSpec> SPECS = ENTRIES.stream()
            .filter(FluidEntry::isAvailable)
            .map(FluidEntry::toSpec)
            .toList();

    private static List<FluidEntry> load() {
        Path path = configPath();
        if (Files.notExists(path)) {
            writeDefaults(path);
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            List<FluidEntry> entries = GSON.fromJson(reader, ENTRY_LIST_TYPE);
            if (entries != null && !entries.isEmpty()) {
                return List.copyOf(entries);
            }
            CreateLiquidMineral.LOGGER.warn("{} has no fluid entries, falling back to the built-in molten fluids", path);
        } catch (IOException | JsonSyntaxException e) {
            CreateLiquidMineral.LOGGER.error("Failed to read {}, falling back to the built-in molten fluids", path, e);
        }
        return DEFAULTS;
    }

    private static void writeDefaults(Path path) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(DEFAULTS, ENTRY_LIST_TYPE, writer);
            }
        } catch (IOException e) {
            CreateLiquidMineral.LOGGER.error("Failed to write default {}", path, e);
        }
    }

    private static Path configPath() {
        return FMLPaths.CONFIGDIR.get().resolve(CreateLiquidMineral.MODID).resolve("fluids.json");
    }

    private MoltenFluidConfigLoader() {
    }
}
