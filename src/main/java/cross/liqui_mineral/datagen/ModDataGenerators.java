package cross.liqui_mineral.datagen;

import cross.liqui_mineral.CreateLiquidMineral;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/** Wires up datagen for {@code gradlew runData}: bucket item models and lang, both driven by {@code fluids.json}. */
@EventBusSubscriber(modid = CreateLiquidMineral.MODID)
public final class ModDataGenerators {

    @SubscribeEvent
    static void gatherData(GatherDataEvent event) {
        if (event.includeClient()) {
            event.addProvider(new ModItemModelProvider(event.getGenerator().getPackOutput(), event.getExistingFileHelper()));
            event.addProvider(new ModLanguageProviderEn(event.getGenerator().getPackOutput()));
            event.addProvider(new ModLanguageProviderZh(event.getGenerator().getPackOutput()));
        }
    }

    private ModDataGenerators() {
    }
}
