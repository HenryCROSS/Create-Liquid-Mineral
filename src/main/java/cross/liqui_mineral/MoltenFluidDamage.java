package cross.liqui_mineral;

import java.util.List;

import cross.liqui_mineral.fluid.MoltenFluidSpec;
import cross.liqui_mineral.fluid.MoltenFluids;
import cross.liqui_mineral.registry.ModFluidTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Makes every molten fluid burn entities (and burn up dropped items) exactly like vanilla lava.
 * <p>
 * NeoForge patches {@code Entity#isInLava()} to compare {@code FluidType} identity against
 * {@code NeoForgeMod.LAVA_TYPE}, so a custom, distinctly-tinted {@code FluidType} never triggers
 * vanilla's lava damage on its own — this replicates {@code Entity#lavaHurt()} manually for our
 * fluid types instead.
 */
@EventBusSubscriber(modid = CreateLiquidMineral.MODID)
public final class MoltenFluidDamage {
    private static List<FluidType> moltenTypesCache;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide() || entity.fireImmune()) {
            return;
        }
        for (FluidType fluidType : moltenTypes()) {
            if (entity.getFluidTypeHeight(fluidType) > 0.0D) {
                entity.igniteForSeconds(15.0F);
                if (entity.hurt(entity.damageSources().lava(), 4.0F)) {
                    entity.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + entity.getRandom().nextFloat() * 0.4F);
                }
                return;
            }
        }
    }

    // Resolved lazily (not in a static field initializer) because DeferredHolder#get() throws
    // until the FluidType registry event has fired; by the time any entity is ticking, it has.
    // Only fluids composed with the BURNS_LIKE_LAVA trait end up here — this stays in sync with
    // MoltenFluids automatically, no separate list to maintain.
    private static List<FluidType> moltenTypes() {
        if (moltenTypesCache == null) {
            moltenTypesCache = MoltenFluids.ALL.stream()
                    .filter(MoltenFluidSpec::burnsEntities)
                    .map(spec -> ModFluidTypes.get(spec.id()).get())
                    .toList();
        }
        return moltenTypesCache;
    }

    private MoltenFluidDamage() {
    }
}
