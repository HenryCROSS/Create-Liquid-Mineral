package cross.liqui_mineral.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;

import cross.liqui_mineral.CreateLiquidMineral;
import cross.liqui_mineral.datagen.MoltenFluidNames;
import cross.liqui_mineral.fluid.MoltenFluidSpec;
import cross.liqui_mineral.fluid.MoltenFluids;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;

/**
 * A synthetic, in-memory resource pack — no files on disk, generated fresh every game start — that
 * supplies the bucket item model and lang entries {@code gradlew runData} would otherwise bake into
 * the jar. Covers fluids a player added purely by editing {@code fluids.json}, with no dev
 * environment to run datagen for them.
 * <p>
 * Registered at {@link net.minecraft.server.packs.repository.Pack.Position#BOTTOM} (see
 * {@code CreateLiquidMineralClient#onAddPackFinders}), so the mod's own real, datagen'd assets
 * always win when both exist for the same fluid — this only fills genuine gaps, it never
 * overrides an intentionally-authored model or translation.
 */
public final class GeneratedFallbackPack extends AbstractPackResources {

    private final Map<ResourceLocation, byte[]> content;

    public GeneratedFallbackPack(PackLocationInfo id) {
        super(id);
        this.content = build();
    }

    private static Map<ResourceLocation, byte[]> build() {
        Map<ResourceLocation, byte[]> map = new HashMap<>();
        for (MoltenFluidSpec spec : MoltenFluids.ALL) {
            put(map, "models/item/" + spec.id() + "_bucket.json", bucketModelJson(spec));
        }
        put(map, "lang/en_us.json", langJson(false));
        put(map, "lang/zh_cn.json", langJson(true));
        return Map.copyOf(map);
    }

    private static void put(Map<ResourceLocation, byte[]> map, String path, JsonObject json) {
        map.put(ResourceLocation.fromNamespaceAndPath(CreateLiquidMineral.MODID, path),
                json.toString().getBytes(StandardCharsets.UTF_8));
    }

    /** Same shape {@code DynamicFluidContainerModelBuilder} datagens: parent + loader + fluid id. */
    private static JsonObject bucketModelJson(MoltenFluidSpec spec) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "neoforge:item/bucket");
        json.addProperty("loader", "neoforge:fluid_container");
        json.addProperty("fluid", CreateLiquidMineral.MODID + ":" + spec.id());
        return json;
    }

    /** Mirrors ModLanguageProviderEn/Zh's exact keys so a real datagen'd file overrides cleanly. */
    private static JsonObject langJson(boolean zh) {
        JsonObject json = new JsonObject();
        json.addProperty("itemGroup." + CreateLiquidMineral.MODID, zh ? "熔融矿物" : "Molten Minerals");
        for (MoltenFluidSpec spec : MoltenFluids.ALL) {
            String id = spec.id();
            String name = zh ? MoltenFluidNames.zh(id) : MoltenFluidNames.en(id);
            json.addProperty("fluid_type." + CreateLiquidMineral.MODID + "." + id, name);
            json.addProperty("block." + CreateLiquidMineral.MODID + "." + id, name);
            json.addProperty("item." + CreateLiquidMineral.MODID + "." + id + "_bucket", name + (zh ? "桶" : " Bucket"));
        }
        return json;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) {
        if (!deserializer.getMetadataSectionName().equals("pack")) {
            return null;
        }
        int format = SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES);
        return (T) new PackMetadataSection(Component.literal("Create: Liquid Mineral (generated fallback)"), format);
    }

    @Override
    public void close() {
    }

    @Override
    public void listResources(PackType type, String resourceNamespace, String path, ResourceOutput resourceOutput) {
        if (type != PackType.CLIENT_RESOURCES || !resourceNamespace.equals(CreateLiquidMineral.MODID)) {
            return;
        }
        for (Map.Entry<ResourceLocation, byte[]> entry : content.entrySet()) {
            if (entry.getKey().getPath().startsWith(path)) {
                resourceOutput.accept(entry.getKey(), toSupplier(entry.getValue()));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? Set.of(CreateLiquidMineral.MODID) : Set.of();
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... paths) {
        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        byte[] bytes = type == PackType.CLIENT_RESOURCES ? content.get(location) : null;
        return bytes == null ? null : toSupplier(bytes);
    }

    private static IoSupplier<InputStream> toSupplier(byte[] bytes) {
        return () -> new ByteArrayInputStream(bytes);
    }

    public static final class ResourcesSupplier implements Pack.ResourcesSupplier {
        @Override
        public PackResources openPrimary(PackLocationInfo id) {
            return new GeneratedFallbackPack(id);
        }

        @Override
        public PackResources openFull(PackLocationInfo id, Pack.Metadata info) {
            return openPrimary(id);
        }
    }
}
