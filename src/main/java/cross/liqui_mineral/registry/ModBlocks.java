package cross.liqui_mineral.registry;

import java.util.HashMap;
import java.util.Map;

import cross.liqui_mineral.CreateLiquidMineral;
import cross.liqui_mineral.fluid.MoltenFluidSpec;
import cross.liqui_mineral.fluid.MoltenFluids;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the world liquid block for every {@link MoltenFluidSpec}. */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateLiquidMineral.MODID);

    private static final Map<String, DeferredBlock<LiquidBlock>> LIQUID_BLOCKS = new HashMap<>();

    static {
        for (MoltenFluidSpec spec : MoltenFluids.ALL) {
            LIQUID_BLOCKS.put(spec.id(), BLOCKS.register(spec.id(), () -> new LiquidBlock(
                    ModFluids.getSource(spec.id()).get(),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .replaceable()
                            .noCollission()
                            .strength(100.0F)
                            .noLootTable()
                            .liquid()
                            .pushReaction(PushReaction.DESTROY)
                            .lightLevel(state -> spec.lightLevel())
            )));
        }
    }

    public static LiquidBlock getLiquidBlock(String id) {
        return LIQUID_BLOCKS.get(id).get();
    }

    private ModBlocks() {
    }
}
