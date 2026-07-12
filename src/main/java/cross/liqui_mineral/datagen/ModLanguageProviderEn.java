package cross.liqui_mineral.datagen;

import cross.liqui_mineral.CreateLiquidMineral;
import cross.liqui_mineral.fluid.MoltenFluidSpec;
import cross.liqui_mineral.fluid.MoltenFluids;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/** Generates {@code lang/en_us.json} entries for every registered molten fluid. */
public final class ModLanguageProviderEn extends LanguageProvider {

    public ModLanguageProviderEn(PackOutput output) {
        super(output, CreateLiquidMineral.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + CreateLiquidMineral.MODID, "Molten Minerals");
        for (MoltenFluidSpec spec : MoltenFluids.ALL) {
            String name = MoltenFluidNames.en(spec.id());
            add("fluid_type." + CreateLiquidMineral.MODID + "." + spec.id(), name);
            add("block." + CreateLiquidMineral.MODID + "." + spec.id(), name);
            add("item." + CreateLiquidMineral.MODID + "." + spec.id() + "_bucket", name + " Bucket");
        }
    }
}
