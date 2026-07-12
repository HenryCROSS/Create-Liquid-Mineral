package cross.liqui_mineral.registry;

import java.util.HashMap;
import java.util.Map;

import cross.liqui_mineral.CreateLiquidMineral;
import cross.liqui_mineral.fluid.MoltenFluidFlowing;
import cross.liqui_mineral.fluid.MoltenFluidSource;
import cross.liqui_mineral.fluid.MoltenFluidSpec;
import cross.liqui_mineral.fluid.MoltenFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers the source/flowing fluid pair for every {@link MoltenFluidSpec}.
 * The matching {@link ModBlocks} liquid block and {@link ModItems} bucket are wired in lazily
 * (via suppliers) to break the fluid/block/item registration cycle.
 */
public final class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, CreateLiquidMineral.MODID);

    private static final Map<String, DeferredHolder<Fluid, MoltenFluidSource>> SOURCES = new HashMap<>();
    private static final Map<String, DeferredHolder<Fluid, MoltenFluidFlowing>> FLOWING = new HashMap<>();

    static {
        for (MoltenFluidSpec spec : MoltenFluids.ALL) {
            SOURCES.put(spec.id(), FLUIDS.register(spec.id(),
                    () -> new MoltenFluidSource(properties(spec), spec.protectsFamily())));
            FLOWING.put(spec.id(), FLUIDS.register(spec.flowingId(),
                    () -> new MoltenFluidFlowing(properties(spec), spec.protectsFamily())));
        }
    }

    private static BaseFlowingFluid.Properties properties(MoltenFluidSpec spec) {
        return new BaseFlowingFluid.Properties(
                ModFluidTypes.get(spec.id()),
                SOURCES.get(spec.id()),
                FLOWING.get(spec.id()))
                .bucket(() -> ModItems.getBucket(spec.id()))
                .block(() -> ModBlocks.getLiquidBlock(spec.id()))
                // Read from the spec (set by its physics preset, or overridden directly in
                // fluids.json) instead of hardcoded — a "water" physics fluid now actually
                // spreads on water's faster schedule instead of lava's, and "custom" fluids get
                // whatever tickRate/slopeFindDistance/levelDecreasePerBlock their entry specifies.
                .slopeFindDistance(spec.slopeFindDistance())
                .levelDecreasePerBlock(spec.levelDecreasePerBlock())
                .tickRate(spec.tickRate());
    }

    public static DeferredHolder<Fluid, MoltenFluidSource> getSource(String id) {
        return SOURCES.get(id);
    }

    public static DeferredHolder<Fluid, MoltenFluidFlowing> getFlowing(String id) {
        return FLOWING.get(id);
    }

    private ModFluids() {
    }
}
