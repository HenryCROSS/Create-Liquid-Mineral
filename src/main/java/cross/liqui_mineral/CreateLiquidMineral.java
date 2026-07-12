package cross.liqui_mineral;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import cross.liqui_mineral.registry.ModBlocks;
import cross.liqui_mineral.registry.ModCreativeTabs;
import cross.liqui_mineral.registry.ModFluidTypes;
import cross.liqui_mineral.registry.ModFluids;
import cross.liqui_mineral.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CreateLiquidMineral.MODID)
public class CreateLiquidMineral {
    public static final String MODID = "createliquidmineral";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateLiquidMineral(IEventBus modEventBus, ModContainer modContainer) {
        ModFluidTypes.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
    }
}
