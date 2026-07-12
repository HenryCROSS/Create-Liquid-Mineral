package cross.liqui_mineral;

import java.util.Optional;

import cross.liqui_mineral.fluid.MoltenFluidSpec;
import cross.liqui_mineral.fluid.MoltenFluids;
import cross.liqui_mineral.registry.ModFluidTypes;
import cross.liqui_mineral.registry.ModFluids;
import cross.liqui_mineral.registry.ModItems;
import cross.liqui_mineral.resource.ExternalTexturePack;
import cross.liqui_mineral.resource.GeneratedFallbackPack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import net.neoforged.neoforge.event.AddPackFindersEvent;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = CreateLiquidMineral.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CreateLiquidMineral.MODID, value = Dist.CLIENT)
public class CreateLiquidMineralClient {

    @SubscribeEvent
    static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        for (MoltenFluidSpec spec : MoltenFluids.ALL) {
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return spec.stillTexture();
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return spec.flowingTexture();
                }

                @Override
                public ResourceLocation getOverlayTexture() {
                    return spec.overlayTexture();
                }

                @Override
                public int getTintColor() {
                    return spec.tintColor();
                }
            }, ModFluidTypes.get(spec.id()).get());
        }
    }

    // Fluids default to rendering fully opaque (RenderType.solid()); a fluid only gets alpha
    // blending (its texture's own alpha channel actually showing through, like vanilla water)
    // once it's explicitly opted in here via "translucent": true in fluids.json -- see
    // MoltenFluidSpec#withTranslucent's javadoc. Must run during client loading (enforced by
    // ItemBlockRenderTypes.setRenderLayer itself), so this is done from FMLClientSetupEvent,
    // deferred onto the main thread via enqueueWork per NeoForge's own guidance for that event.
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (MoltenFluidSpec spec : MoltenFluids.ALL) {
                if (!spec.translucent()) {
                    continue;
                }
                ItemBlockRenderTypes.setRenderLayer(ModFluids.getSource(spec.id()).get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ModFluids.getFlowing(spec.id()).get(), RenderType.translucent());
            }
        });
    }

    // The bucket item model (neoforge:fluid_container) only paints its fluid layer with the
    // FluidType tint if an ItemColor is registered for tint index 1 — without this, every
    // bucket renders with the fluid texture's raw (untinted) colors.
    @SubscribeEvent
    static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        DynamicFluidContainerModel.Colors bucketColors = new DynamicFluidContainerModel.Colors();
        for (MoltenFluidSpec spec : MoltenFluids.ALL) {
            event.register(bucketColors, ModItems.getBucket(spec.id()));
        }
    }

    // Fills in bucket item models / lang entries for fluids added purely via fluids.json (no
    // gradlew runData run for them) with a synthetic, in-memory resource pack. Registered at the
    // lowest priority (Pack.Position.BOTTOM) so the mod's own real, datagen'd assets always win
    // when both exist for the same fluid -- see GeneratedFallbackPack's class javadoc.
    //
    // Also registers ExternalTexturePack at the highest priority (Pack.Position.TOP) so a
    // player's own PNGs dropped into config/createliquidmineral/textures/ always win over both
    // the mod's jar-bundled textures and the fallback pack above -- see ExternalTexturePack's
    // class javadoc.
    @SubscribeEvent
    static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }
        ExternalTexturePack.ensureFolderWithReadme();

        PackLocationInfo fallbackLocation = new PackLocationInfo(
                CreateLiquidMineral.MODID + "_generated_fallback",
                Component.literal("Create: Liquid Mineral (generated fallback)"),
                PackSource.DEFAULT,
                Optional.empty());
        Pack fallbackPack = Pack.readMetaAndCreate(
                fallbackLocation,
                new GeneratedFallbackPack.ResourcesSupplier(),
                PackType.CLIENT_RESOURCES,
                new PackSelectionConfig(true, Pack.Position.BOTTOM, false));
        if (fallbackPack != null) {
            event.addRepositorySource(packAcceptor -> packAcceptor.accept(fallbackPack));
        }

        PackLocationInfo customTexturesLocation = new PackLocationInfo(
                CreateLiquidMineral.MODID + "_custom_textures",
                Component.literal("Create: Liquid Mineral (custom textures)"),
                PackSource.DEFAULT,
                Optional.empty());
        Pack customTexturesPack = Pack.readMetaAndCreate(
                customTexturesLocation,
                new ExternalTexturePack.ResourcesSupplier(),
                PackType.CLIENT_RESOURCES,
                new PackSelectionConfig(true, Pack.Position.TOP, false));
        if (customTexturesPack != null) {
            event.addRepositorySource(packAcceptor -> packAcceptor.accept(customTexturesPack));
        }
    }
}
