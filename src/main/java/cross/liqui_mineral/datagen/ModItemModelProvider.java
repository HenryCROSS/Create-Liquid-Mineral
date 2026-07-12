package cross.liqui_mineral.datagen;

import cross.liqui_mineral.CreateLiquidMineral;
import cross.liqui_mineral.fluid.MoltenFluidSpec;
import cross.liqui_mineral.fluid.MoltenFluids;
import cross.liqui_mineral.registry.ModFluids;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Generates the {@code neoforge:fluid_container} bucket item model for every registered molten
 * fluid — the same model every bucket in this mod already used by hand, just no longer requiring
 * a hand-written JSON file per fluid. Iterates {@link MoltenFluids#ALL} (not the raw config
 * entries), so a fluid whose {@code requiredMod} isn't present here either is skipped, matching
 * what actually got registered.
 */
public final class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CreateLiquidMineral.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (MoltenFluidSpec spec : MoltenFluids.ALL) {
            getBuilder(spec.id() + "_bucket")
                    .parent(new ModelFile.UncheckedModelFile("neoforge:item/bucket"))
                    .customLoader(DynamicFluidContainerModelBuilder::begin)
                    .fluid(ModFluids.getSource(spec.id()).get());
        }
    }
}
