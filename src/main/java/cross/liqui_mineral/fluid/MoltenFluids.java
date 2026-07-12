package cross.liqui_mineral.fluid;

import java.util.List;

/**
 * The list of every molten fluid this mod registers, loaded from
 * {@code config/createliquidmineral/fluids.json} by {@link MoltenFluidConfigLoader} — see that
 * class to add a new molten fluid without touching Java code.
 */
public final class MoltenFluids {
    public static final List<MoltenFluidSpec> ALL = MoltenFluidConfigLoader.SPECS;

    private MoltenFluids() {
    }
}
