package cross.liqui_mineral.registry;

import java.util.HashMap;
import java.util.Map;

import cross.liqui_mineral.CreateLiquidMineral;
import cross.liqui_mineral.fluid.MoltenFluidSpec;
import cross.liqui_mineral.fluid.MoltenFluids;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, CreateLiquidMineral.MODID);

    private static final Map<String, DeferredHolder<FluidType, FluidType>> TYPES = new HashMap<>();

    static {
        for (MoltenFluidSpec spec : MoltenFluids.ALL) {
            TYPES.put(spec.id(), FLUID_TYPES.register(spec.id(), () -> new FluidType(
                    FluidType.Properties.create()
                            .density(spec.density())
                            .viscosity(spec.viscosity())
                            .temperature(spec.temperature())
                            .lightLevel(spec.lightLevel())
                            .canConvertToSource(spec.canConvertToSource())
                            .canHydrate(spec.canHydrate())
                            .canSwim(spec.canSwim())
                            .canDrown(spec.canDrown())
                            .canPushEntity(true)
                            .canExtinguish(spec.canExtinguish())
                            .sound(SoundActions.BUCKET_FILL, spec.fillSound())
                            .sound(SoundActions.BUCKET_EMPTY, spec.emptySound())
            )));
        }
    }

    public static DeferredHolder<FluidType, FluidType> get(String id) {
        return TYPES.get(id);
    }

    private ModFluidTypes() {
    }
}
