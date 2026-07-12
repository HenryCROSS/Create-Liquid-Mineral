package cross.liqui_mineral.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/** A source fluid that refuses to be overwritten by a different molten fluid — see {@link MoltenFluidFamily}. */
public class MoltenFluidSource extends BaseFlowingFluid.Source {
    private final boolean protectsFamily;

    public MoltenFluidSource(Properties properties, boolean protectsFamily) {
        super(properties);
        this.protectsFamily = protectsFamily;
    }

    @Override
    public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        if (protectsFamily && MoltenFluidFamily.isDifferentMoltenFluid(this, fluid)) {
            return false;
        }
        return super.canBeReplacedWith(state, level, pos, fluid, direction);
    }
}
