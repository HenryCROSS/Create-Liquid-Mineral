package cross.liqui_mineral.registry;

import java.util.HashMap;
import java.util.Map;

import cross.liqui_mineral.CreateLiquidMineral;
import cross.liqui_mineral.fluid.MoltenFluidSpec;
import cross.liqui_mineral.fluid.MoltenFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the bucket item for every {@link MoltenFluidSpec}. */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateLiquidMineral.MODID);

    private static final Map<String, DeferredItem<BucketItem>> BUCKETS = new HashMap<>();

    static {
        for (MoltenFluidSpec spec : MoltenFluids.ALL) {
            BUCKETS.put(spec.id(), ITEMS.register(spec.bucketId(), () -> new BucketItem(
                    ModFluids.getSource(spec.id()).get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
            )));
        }
    }

    public static BucketItem getBucket(String id) {
        return BUCKETS.get(id).get();
    }

    public static DeferredItem<BucketItem> getBucketHolder(String id) {
        return BUCKETS.get(id);
    }

    private ModItems() {
    }
}
