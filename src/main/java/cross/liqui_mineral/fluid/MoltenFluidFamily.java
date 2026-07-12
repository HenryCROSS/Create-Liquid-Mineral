package cross.liqui_mineral.fluid;

import cross.liqui_mineral.CreateLiquidMineral;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

/**
 * The shared rule used by {@link MoltenFluidSource} and {@link MoltenFluidFlowing}: none of our
 * molten fluids ever overwrite another one of our molten fluids when they flow into each other.
 * <p>
 * Without this, vanilla's default {@code FlowingFluid#canBeReplacedWith} lets a fluid flowing
 * downward silently overwrite whatever different fluid was there before (the same rule that would
 * let plain lava overwrite plain water if lava didn't specifically override it for the
 * obsidian/cobblestone reaction) — with 7 unrelated molten fluids and no such reaction between
 * them, that looked like one fluid randomly vanishing wherever two of them met.
 */
final class MoltenFluidFamily {
    static boolean isDifferentMoltenFluid(Fluid self, Fluid other) {
        if (other == self) {
            return false;
        }
        ResourceLocation key = BuiltInRegistries.FLUID.getKey(other);
        return key != null && key.getNamespace().equals(CreateLiquidMineral.MODID);
    }

    private MoltenFluidFamily() {
    }
}
