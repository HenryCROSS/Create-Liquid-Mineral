package cross.liqui_mineral.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

import cross.liqui_mineral.CreateLiquidMineral;
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
import net.neoforged.fml.loading.FMLPaths;

/**
 * Serves {@code config/createliquidmineral/textures/} on disk as a resource pack, so a player can
 * override or add a fluid's texture just by dropping PNGs there — no resource pack to install or
 * enable, no rebuild, not even a restart's worth of Java code to touch.
 * <p>
 * The folder mirrors the real asset layout under {@code textures/}: a file at
 * {@code config/createliquidmineral/textures/block/molten_iron_still.png} is served as
 * {@code assets/createliquidmineral/textures/block/molten_iron_still.png}, same as
 * {@code molten_iron_flow.png} and, for animated textures, a same-named {@code .png.mcmeta} next
 * to each PNG (same format vanilla/this mod's own bundled textures use).
 * <p>
 * Registered at {@link net.minecraft.server.packs.repository.Pack.Position#TOP} (see
 * {@code CreateLiquidMineralClient#onAddPackFinders}), so a player's own texture always wins over
 * both the mod's jar-bundled ones and {@link GeneratedFallbackPack}'s in-memory fallbacks.
 * {@link cross.liqui_mineral.fluid.MoltenFluidSpec#GENERATED_TEXTURE} also checks this folder
 * directly so a brand-new fluid (added purely via {@code fluids.json}, never bundled in the jar)
 * still gets a real texture instead of falling back to plain grey once its PNGs are dropped here.
 */
public final class ExternalTexturePack extends AbstractPackResources {

    private static final String TEXTURES_PREFIX = "textures/";

    private final Path root;

    public ExternalTexturePack(PackLocationInfo id, Path root) {
        super(id);
        this.root = root;
    }

    /** {@code config/createliquidmineral/textures/} — where players drop their own texture files. */
    public static Path root() {
        return FMLPaths.CONFIGDIR.get().resolve(CreateLiquidMineral.MODID).resolve("textures");
    }

    /** Creates the folder (if missing) with a short usage note, so it's discoverable on first launch. */
    public static void ensureFolderWithReadme() {
        Path root = root();
        if (Files.exists(root)) {
            return;
        }
        try {
            Files.createDirectories(root.resolve("block"));
            Files.writeString(root.resolve("README.txt"), README_TEXT);
        } catch (IOException e) {
            CreateLiquidMineral.LOGGER.error("Failed to create {}", root, e);
        }
    }

    private static final String README_TEXT = """
            Create: Liquid Mineral - custom textures
            =========================================

            Drop your own fluid textures here and they'll be used automatically the next time you
            start the game -- no resource pack to install/enable, no rebuild.

            Folder layout mirrors the mod's real assets, under textures/:

              config/createliquidmineral/textures/block/<fluid_id>_still.png
              config/createliquidmineral/textures/block/<fluid_id>_flow.png

            <fluid_id> is the "id" field of the fluid in config/createliquidmineral/fluids.json
            (e.g. molten_iron). Provide BOTH _still and _flow for a given fluid.

            Animated textures: drop a same-named "<file>.png.mcmeta" next to the PNG, same format
            Minecraft resource packs use, e.g.:

              {
                "animation": { "frametime": 3 }
              }

            A texture placed here overrides the mod's own bundled texture for that fluid too, and
            also works for brand-new fluids you only added via fluids.json (which otherwise fall
            back to a plain grey texture).

            ---

            Create: Liquid Mineral（熔融矿物）- 自定义材质
            ===============================================

            把你自己的流体材质放在这里，下次启动游戏时会自动生效 -- 不需要安装/启用资源包，也不需要重新构建。

            目录结构与本模组真实的资源路径一致，都在 textures/ 下：

              config/createliquidmineral/textures/block/<流体id>_still.png
              config/createliquidmineral/textures/block/<流体id>_flow.png

            <流体id> 就是 config/createliquidmineral/fluids.json 中该流体的 "id" 字段（例如
            molten_iron）。_still 和 _flow 两个文件都要提供。

            动画材质：在 PNG 旁边放一个同名的 "<文件名>.png.mcmeta"，格式和 Minecraft 资源包一致，例如：

              {
                "animation": { "frametime": 3 }
              }

            放在这里的材质会覆盖本模组自带的对应材质，也可以给只在 fluids.json 里新增、从未打包进
            jar 的全新流体提供材质（否则它们会退回成一个纯灰色的材质）。
            """;

    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) {
        if (!deserializer.getMetadataSectionName().equals("pack")) {
            return null;
        }
        int format = SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES);
        @SuppressWarnings("unchecked")
        T section = (T) new PackMetadataSection(Component.literal("Create: Liquid Mineral (custom textures)"), format);
        return section;
    }

    @Override
    public void close() {
    }

    @Override
    public void listResources(PackType type, String resourceNamespace, String path, ResourceOutput resourceOutput) {
        if (type != PackType.CLIENT_RESOURCES || !resourceNamespace.equals(CreateLiquidMineral.MODID) || !Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(Files::isRegularFile).forEach(file -> {
                String resourcePath = TEXTURES_PREFIX + root.relativize(file).toString().replace('\\', '/');
                if (resourcePath.startsWith(path)) {
                    resourceOutput.accept(ResourceLocation.fromNamespaceAndPath(CreateLiquidMineral.MODID, resourcePath),
                            toSupplier(file));
                }
            });
        } catch (IOException e) {
            CreateLiquidMineral.LOGGER.error("Failed to list external textures in {}", root, e);
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES && Files.isDirectory(root) ? Set.of(CreateLiquidMineral.MODID) : Set.of();
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... paths) {
        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.CLIENT_RESOURCES || !location.getNamespace().equals(CreateLiquidMineral.MODID)) {
            return null;
        }
        String path = location.getPath();
        if (!path.startsWith(TEXTURES_PREFIX)) {
            return null;
        }
        // Resolve-then-verify-still-under-root guards against a resource location that tries to
        // escape the textures/ folder via ".." segments.
        Path file = root.resolve(path.substring(TEXTURES_PREFIX.length())).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            return null;
        }
        return toSupplier(file);
    }

    private static IoSupplier<InputStream> toSupplier(Path file) {
        return () -> Files.newInputStream(file);
    }

    public static final class ResourcesSupplier implements Pack.ResourcesSupplier {
        @Override
        public PackResources openPrimary(PackLocationInfo id) {
            return new ExternalTexturePack(id, root());
        }

        @Override
        public PackResources openFull(PackLocationInfo id, Pack.Metadata info) {
            return openPrimary(id);
        }
    }
}
