package cross.liqui_mineral.registry;

import cross.liqui_mineral.CreateLiquidMineral;
import cross.liqui_mineral.fluid.MoltenFluidSpec;
import cross.liqui_mineral.fluid.MoltenFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The creative tab that lists every molten fluid bucket added by this mod. */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateLiquidMineral.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MOLTEN_MINERALS_TAB =
            CREATIVE_MODE_TABS.register("molten_minerals", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.createliquidmineral"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModItems.getBucket("molten_gold").getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        for (MoltenFluidSpec spec : MoltenFluids.ALL) {
                            output.accept(ModItems.getBucket(spec.id()));
                        }
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
