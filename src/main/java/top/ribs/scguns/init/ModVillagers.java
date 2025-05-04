package top.ribs.scguns.init;

import com.google.common.collect.ImmutableSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.Holder;
import top.ribs.scguns.Reference;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(NeoForgeRegistries.POI_TYPES, Reference.MOD_ID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
            DeferredRegister.create(NeoForgeRegistries.VILLAGER_PROFESSIONS, Reference.MOD_ID);

    public static final Holder<PoiType> GUNSMITH_POI = POI_TYPES.register("gunsmith_poi",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.GUN_BENCH.get().getStateDefinition().getPossibleStates()),
                    1, 1));

    public static final Holder<VillagerProfession> GUNSMITH =
            VILLAGER_PROFESSIONS.register("gunsmith", () -> new VillagerProfession("gunsmith",
                    holder -> holder.get() == GUNSMITH_POI.get(), holder -> holder.get() == GUNSMITH_POI.get(),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_ARMORER));



    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        VILLAGER_PROFESSIONS.register(eventBus);
    }
}