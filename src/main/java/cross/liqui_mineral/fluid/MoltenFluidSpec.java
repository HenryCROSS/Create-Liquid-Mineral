package cross.liqui_mineral.fluid;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.UnaryOperator;

import cross.liqui_mineral.CreateLiquidMineral;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.fml.loading.FMLPaths;

/**
 * An immutable, functional description of a fluid to register.
 * <p>
 * There is no "water fluid" or "lava fluid" preset that bundles a bunch of unrelated concerns
 * together. Instead, every concern is its own independent {@code UnaryOperator<MoltenFluidSpec>}
 * <b>trait</b> — a pure function that takes a spec and returns a new one with just that one
 * concern set — and {@link #of(String, UnaryOperator[])} folds however many traits you pick onto
 * a neutral {@link #blank(String)} spec to produce the final, correct object. Traits compose
 * freely because they don't know about each other: {@code LAVA_PHYSICS} never touches texture
 * fields, {@code WATER_TEXTURE} never touches physics fields, etc.
 * <p>
 * {@code LAVA_PHYSICS}/{@code WATER_PHYSICS} are just starting points, not locked-in bundles —
 * every field they set also has its own {@code with*} wither, so {@code fluids.json} can override
 * any single one of them (or, via {@code "physics": "custom"}, skip the preset entirely and
 * specify every physics field directly) without dragging the rest of the preset along.
 * <p>
 * Example: {@code MoltenFluidSpec.of("molten_iron", LAVA_PHYSICS, WATER_TEXTURE, BURNS_LIKE_LAVA, PROTECTS_FAMILY)
 *     .withApparentColor(0xFFFF5A1F).withLightLevel(10)}
 * — lava's weight/glow/danger, water's cleanly-tintable texture, lava's burn behavior, immunity to
 * being overwritten by other molten fluids, and then a couple of final per-material tweaks layered
 * on top.
 */
public record MoltenFluidSpec(
        String id,
        int tintColor,
        int density,
        int viscosity,
        int temperature,
        int lightLevel,
        int tickRate,
        int slopeFindDistance,
        int levelDecreasePerBlock,
        boolean canSwim,
        boolean canDrown,
        boolean canConvertToSource,
        boolean canHydrate,
        boolean canExtinguish,
        boolean burnsEntities,
        boolean protectsFamily,
        ResourceLocation stillTexture,
        ResourceLocation flowingTexture,
        ResourceLocation overlayTexture,
        SoundEvent fillSound,
        SoundEvent emptySound,
        boolean translucent) {

    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");
    private static final ResourceLocation WATER_OVERLAY = ResourceLocation.withDefaultNamespace("block/water_overlay");
    private static final ResourceLocation LAVA_STILL = ResourceLocation.withDefaultNamespace("block/lava_still");
    private static final ResourceLocation LAVA_FLOW = ResourceLocation.withDefaultNamespace("block/lava_flow");

    /**
     * The dominant pixel value of vanilla's {@code water_still.png} (sampled directly from the
     * texture: (165,165,165) is by far the most common pixel across every animation frame). The
     * texture is neutral grey, not white, so a raw tint multiplies down to ~65% brightness unless
     * compensated for — see {@link #withApparentColor(int)}.
     */
    private static final int WATER_BASE_GRAY = 165;

    // ---- traits: independent, composable, pure spec -> spec functions ----

    /**
     * Physics only: light, thin, hydrating, extinguishes fire, spreads on water's schedule
     * (tickRate 5, slopeFindDistance 4, levelDecreasePerBlock 1 — vanilla water's own values),
     * water's bucket sounds. No texture opinion.
     */
    public static final UnaryOperator<MoltenFluidSpec> WATER_PHYSICS = spec -> spec
            .withPhysics(1000, 1000, 300, 0, 5, 4, 1, true, true, true, true, true)
            .withSounds(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY);

    /**
     * Physics only: heavy, thick, glowing, non-swimmable, doesn't hydrate/extinguish, spreads on
     * lava's overworld schedule (tickRate 30, slopeFindDistance 2, levelDecreasePerBlock 2 —
     * matches vanilla {@code LavaFluid}), lava's bucket sounds.
     */
    public static final UnaryOperator<MoltenFluidSpec> LAVA_PHYSICS = spec -> spec
            .withPhysics(3000, 6000, 1300, 15, 30, 2, 2, false, false, false, false, false)
            .withSounds(SoundEvents.BUCKET_FILL_LAVA, SoundEvents.BUCKET_EMPTY_LAVA);

    /** Texture only: water's still/flow/overlay — the only vanilla fluid texture that tints cleanly. */
    public static final UnaryOperator<MoltenFluidSpec> WATER_TEXTURE =
            spec -> spec.withTextures(WATER_STILL, WATER_FLOW, WATER_OVERLAY);

    /** Texture only: lava's still/flow, already-orange with no overlay; tints will skew warm. */
    public static final UnaryOperator<MoltenFluidSpec> LAVA_TEXTURE =
            spec -> spec.withTextures(LAVA_STILL, LAVA_FLOW, null);

    /**
     * Texture only: this mod's own procedurally-generated {@code block/<id>_still}/{@code _flow}
     * (see {@code fluid_texture_gen/} next to the project). Color is already baked into the
     * texture itself, so unlike {@link #WATER_TEXTURE}/{@link #LAVA_TEXTURE} this is meant to be
     * used untinted (leave {@code tintColor} at {@link #blank}'s default 0xFFFFFFFF — applying a
     * tint on top would just re-darken/re-color pixels that were already painted correctly).
     * <p>
     * Deliberately under {@code textures/block/}, not {@code textures/fluid/}: vanilla's block
     * atlas (see {@code assets/minecraft/atlases/blocks.json}) only auto-stitches sprites found
     * under a {@code block/} prefix (that's the whole reason {@link #WATER_STILL}/{@link
     * #LAVA_STILL} resolve without any extra registration) — a {@code fluid/} prefix isn't swept
     * by anything and silently renders as the missing-texture checkerboard unless you also ship a
     * custom sprite source (an {@code atlases/blocks.json} entry, generated via NeoForge's
     * {@code SpriteSourceProvider}) to opt that prefix in.
     * <p>
     * Falls back to {@link #GENERATED_DEFAULT_TEXTURE} if this fluid's own {@code block/<id>_still}
     * isn't actually bundled in the jar and hasn't been supplied by a player either — e.g. a fluid
     * a player added via {@code fluids.json} without running {@code fluid_texture_gen} for it and
     * without dropping their own PNGs into {@code config/createliquidmineral/textures/}. Without
     * this, that fluid would silently render as the missing-texture checkerboard instead of a
     * plain (but correct-looking) grey.
     * <p>
     * A player-supplied texture at {@code config/createliquidmineral/textures/block/<id>_still.png}
     * (and matching {@code _flow.png}) is picked up the same way as a jar-bundled one — see
     * {@code cross.liqui_mineral.resource.ExternalTexturePack}, which serves that config folder as
     * a top-priority resource pack so it also overrides any of the mod's own bundled textures.
     */
    public static final UnaryOperator<MoltenFluidSpec> GENERATED_TEXTURE = spec -> {
        String stillPath = "block/" + spec.id() + "_still";
        if (!hasBundledTexture(stillPath) && !hasExternalTexture(stillPath)) {
            return MoltenFluidSpec.GENERATED_DEFAULT_TEXTURE.apply(spec);
        }
        return spec.withTextures(
                ResourceLocation.fromNamespaceAndPath("createliquidmineral", stillPath),
                ResourceLocation.fromNamespaceAndPath("createliquidmineral", "block/" + spec.id() + "_flow"),
                null);
    };

    /** Whether {@code assets/createliquidmineral/textures/<path>.png} is on the classpath (i.e. shipped in this jar). */
    private static boolean hasBundledTexture(String path) {
        return MoltenFluidSpec.class.getResource("/assets/createliquidmineral/textures/" + path + ".png") != null;
    }

    /** Whether a player has dropped {@code config/createliquidmineral/textures/<path>.png} on disk. */
    private static boolean hasExternalTexture(String path) {
        Path file = FMLPaths.CONFIGDIR.get().resolve(CreateLiquidMineral.MODID).resolve("textures").resolve(path + ".png");
        return Files.isRegularFile(file);
    }

    /**
     * Texture only: one shared, neutral grey/white procedurally-generated texture (same generator,
     * same noise, just fed a colorless tint) used by any fluid that opts out of its own per-metal
     * color via {@code "texture": "default"} in {@code fluids.json} — a plain fallback look, same
     * as {@link #GENERATED_TEXTURE} otherwise (untinted, {@code block/} prefix for atlas stitching).
     */
    public static final UnaryOperator<MoltenFluidSpec> GENERATED_DEFAULT_TEXTURE = spec -> spec.withTextures(
            ResourceLocation.fromNamespaceAndPath("createliquidmineral", "block/molten_default_still"),
            ResourceLocation.fromNamespaceAndPath("createliquidmineral", "block/molten_default_flow"),
            null);

    /** Behavior only: ignites and deals fire damage to entities standing in it, like real lava. */
    public static final UnaryOperator<MoltenFluidSpec> BURNS_LIKE_LAVA = spec -> spec.withBurnsEntities(true);

    /** Behavior only: explicitly harmless to stand in (this is also the default). */
    public static final UnaryOperator<MoltenFluidSpec> SAFE = spec -> spec.withBurnsEntities(false);

    /**
     * Behavior only: a different molten fluid flowing into this one will never silently overwrite
     * it (they just stop at each other instead). Without this, vanilla's default fluid-spread rule
     * lets one flowing fluid overwrite a different one wherever they meet.
     */
    public static final UnaryOperator<MoltenFluidSpec> PROTECTS_FAMILY = spec -> spec.withProtectsFamily(true);

    /** Behavior only: explicitly allows other molten fluids to overwrite this one on contact (this is also the default). */
    public static final UnaryOperator<MoltenFluidSpec> SHARED_FAMILY = spec -> spec.withProtectsFamily(false);

    /**
     * A neutral starting point with no opinions baked in yet: apply traits with {@link #of}. Used
     * directly (no {@code LAVA_PHYSICS}/{@code WATER_PHYSICS} trait applied) when a
     * {@code fluids.json} entry sets {@code "physics": "custom"}, so every physics field comes
     * from that entry's own values, falling back to this inert baseline for whichever ones it
     * leaves unset.
     */
    public static MoltenFluidSpec blank(String id) {
        return new MoltenFluidSpec(
                id,
                0xFFFFFFFF,
                1000, 1000, 300,
                0,
                5, 4, 1,
                false, false,
                false, false, false,
                false,
                false,
                WATER_STILL, WATER_FLOW, WATER_OVERLAY,
                SoundEvents.BUCKET_FILL,
                SoundEvents.BUCKET_EMPTY,
                false);
    }

    /** Folds every given trait onto {@link #blank(String)}, in order, to build the final spec. */
    @SafeVarargs
    public static MoltenFluidSpec of(String id, UnaryOperator<MoltenFluidSpec>... traits) {
        MoltenFluidSpec spec = blank(id);
        for (UnaryOperator<MoltenFluidSpec> trait : traits) {
            spec = trait.apply(spec);
        }
        return spec;
    }

    /** Applies one more trait on top of this spec; lets you chain {@code .apply(TRAIT)} after {@link #of}. */
    public MoltenFluidSpec apply(UnaryOperator<MoltenFluidSpec> trait) {
        return trait.apply(this);
    }

    // ---- per-field withers, used both directly and to implement the traits above ----

    public MoltenFluidSpec withId(String newId) {
        return new MoltenFluidSpec(newId, tintColor, density, viscosity, temperature, lightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, canHydrate, canExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    /**
     * Sets the raw {@code FluidType} tint that gets multiplied against the texture's own pixels.
     * If the texture is water's (the default), its dominant pixel is grey ~165/255, not white, so
     * the color you see in-game will look noticeably darker/muddier than this hex value. Prefer
     * {@link #withApparentColor(int)} unless you specifically need the raw multiplier.
     */
    public MoltenFluidSpec withTint(int newTintColor) {
        return new MoltenFluidSpec(id, newTintColor, density, viscosity, temperature, lightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, canHydrate, canExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    /**
     * Sets the tint so the fluid actually LOOKS like {@code desiredRgb} in-game, by dividing out
     * the base texture's own dominant pixel color ({@link #WATER_BASE_GRAY}) before it gets
     * re-multiplied in at render time. Channels that would need to go above 255 are clamped, so
     * very bright/saturated targets lose a little precision — that's an unavoidable limit of
     * multiply-blend tinting, not a bug. Only accurate while the texture is water's; if you've
     * applied {@link #LAVA_TEXTURE}, call {@link #withTint(int)} directly instead.
     */
    public MoltenFluidSpec withApparentColor(int desiredRgb) {
        int r = compensate((desiredRgb >> 16) & 0xFF);
        int g = compensate((desiredRgb >> 8) & 0xFF);
        int b = compensate(desiredRgb & 0xFF);
        return withTint(0xFF000000 | (r << 16) | (g << 8) | b);
    }

    private static int compensate(int channel) {
        return Math.min(255, Math.round(channel * 255f / WATER_BASE_GRAY));
    }

    public MoltenFluidSpec withDensity(int newDensity) {
        return new MoltenFluidSpec(id, tintColor, newDensity, viscosity, temperature, lightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, canHydrate, canExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    public MoltenFluidSpec withViscosity(int newViscosity) {
        return new MoltenFluidSpec(id, tintColor, density, newViscosity, temperature, lightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, canHydrate, canExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    public MoltenFluidSpec withTemperature(int newTemperature) {
        return new MoltenFluidSpec(id, tintColor, density, viscosity, newTemperature, lightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, canHydrate, canExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    public MoltenFluidSpec withLightLevel(int newLightLevel) {
        return new MoltenFluidSpec(id, tintColor, density, viscosity, temperature, newLightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, canHydrate, canExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    /** How often (in ticks) this fluid spreads — lower is faster. Vanilla water is 5, lava 30 (overworld). */
    public MoltenFluidSpec withTickRate(int newTickRate) {
        return new MoltenFluidSpec(id, tintColor, density, viscosity, temperature, lightLevel,
                newTickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, canHydrate, canExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    /** How far this fluid looks for a downward slope before spreading sideways. Vanilla water is 4, lava 2. */
    public MoltenFluidSpec withSlopeFindDistance(int newSlopeFindDistance) {
        return new MoltenFluidSpec(id, tintColor, density, viscosity, temperature, lightLevel,
                tickRate, newSlopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, canHydrate, canExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    /** How much the fluid level drops per block spread sideways. Vanilla water is 1, lava 2. */
    public MoltenFluidSpec withLevelDecreasePerBlock(int newLevelDecreasePerBlock) {
        return new MoltenFluidSpec(id, tintColor, density, viscosity, temperature, lightLevel,
                tickRate, slopeFindDistance, newLevelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, canHydrate, canExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    public MoltenFluidSpec withCanSwim(boolean newCanSwim) {
        return new MoltenFluidSpec(id, tintColor, density, viscosity, temperature, lightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                newCanSwim, canDrown, canConvertToSource, canHydrate, canExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    public MoltenFluidSpec withCanDrown(boolean newCanDrown) {
        return new MoltenFluidSpec(id, tintColor, density, viscosity, temperature, lightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, newCanDrown, canConvertToSource, canHydrate, canExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    public MoltenFluidSpec withCanConvertToSource(boolean newCanConvertToSource) {
        return new MoltenFluidSpec(id, tintColor, density, viscosity, temperature, lightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, newCanConvertToSource, canHydrate, canExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    public MoltenFluidSpec withCanHydrate(boolean newCanHydrate) {
        return new MoltenFluidSpec(id, tintColor, density, viscosity, temperature, lightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, newCanHydrate, canExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    public MoltenFluidSpec withCanExtinguish(boolean newCanExtinguish) {
        return new MoltenFluidSpec(id, tintColor, density, viscosity, temperature, lightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, canHydrate, newCanExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    public MoltenFluidSpec withBurnsEntities(boolean newBurnsEntities) {
        return new MoltenFluidSpec(id, tintColor, density, viscosity, temperature, lightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, canHydrate, canExtinguish, newBurnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    public MoltenFluidSpec withProtectsFamily(boolean newProtectsFamily) {
        return new MoltenFluidSpec(id, tintColor, density, viscosity, temperature, lightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, canHydrate, canExtinguish, burnsEntities, newProtectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    public MoltenFluidSpec withSounds(SoundEvent newFillSound, SoundEvent newEmptySound) {
        return new MoltenFluidSpec(id, tintColor, density, viscosity, temperature, lightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, canHydrate, canExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, newFillSound, newEmptySound, translucent);
    }

    /**
     * Whether this fluid should be rendered with alpha blending (like vanilla water) instead of
     * fully opaque (like vanilla lava, and every fluid in this mod before this existed). Doesn't
     * do anything by itself — the texture also needs an actual alpha channel baked in (see
     * {@code fluid_texture_gen}'s {@code --alpha}) or there's nothing to blend against. Read by
     * {@code CreateLiquidMineralClient}'s {@code FMLClientSetupEvent} handler, which registers
     * {@code RenderType.translucent()} for this fluid's source/flowing blocks via
     * {@code ItemBlockRenderTypes.setRenderLayer(Fluid, RenderType)}.
     */
    public MoltenFluidSpec withTranslucent(boolean newTranslucent) {
        return new MoltenFluidSpec(id, tintColor, density, viscosity, temperature, lightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, canHydrate, canExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, newTranslucent);
    }

    /** Bundles every physics field at once — used by {@link #LAVA_PHYSICS}/{@link #WATER_PHYSICS}; prefer the individual withers for one-off tweaks. */
    public MoltenFluidSpec withPhysics(int newDensity, int newViscosity, int newTemperature, int newLightLevel,
            int newTickRate, int newSlopeFindDistance, int newLevelDecreasePerBlock,
            boolean newCanSwim, boolean newCanDrown, boolean newCanConvertToSource, boolean newCanHydrate,
            boolean newCanExtinguish) {
        return new MoltenFluidSpec(id, tintColor, newDensity, newViscosity, newTemperature, newLightLevel,
                newTickRate, newSlopeFindDistance, newLevelDecreasePerBlock,
                newCanSwim, newCanDrown, newCanConvertToSource, newCanHydrate, newCanExtinguish, burnsEntities, protectsFamily,
                stillTexture, flowingTexture, overlayTexture, fillSound, emptySound, translucent);
    }

    public MoltenFluidSpec withTextures(ResourceLocation newStillTexture, ResourceLocation newFlowingTexture) {
        return withTextures(newStillTexture, newFlowingTexture, overlayTexture);
    }

    public MoltenFluidSpec withTextures(ResourceLocation newStillTexture, ResourceLocation newFlowingTexture,
            ResourceLocation newOverlayTexture) {
        return new MoltenFluidSpec(id, tintColor, density, viscosity, temperature, lightLevel,
                tickRate, slopeFindDistance, levelDecreasePerBlock,
                canSwim, canDrown, canConvertToSource, canHydrate, canExtinguish, burnsEntities, protectsFamily,
                newStillTexture, newFlowingTexture, newOverlayTexture, fillSound, emptySound, translucent);
    }

    public String bucketId() {
        return id + "_bucket";
    }

    public String flowingId() {
        return id + "_flowing";
    }
}
