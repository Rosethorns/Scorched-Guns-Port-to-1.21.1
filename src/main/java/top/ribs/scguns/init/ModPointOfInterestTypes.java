package top.ribs.scguns.init;

import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.Holder;
import top.ribs.scguns.Reference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModPointOfInterestTypes
{
    public static final DeferredRegister<PoiType> REGISTER = DeferredRegister.create(NeoForgeRegistries.POI_TYPES, Reference.MOD_ID);


    private static Holder<PoiType> register(String name, Holder<Block> block, int maxFreeTickets) {
        List<Holder<Block>> blocks = new ArrayList<>();
        blocks.add(block);
        return register(name, blocks, maxFreeTickets);
    }

    private static Holder<PoiType> register(String name, Supplier<List<Holder<Block>>> supplier, int maxFreeTickets) {
        return register(name, supplier.get(), maxFreeTickets);
    }

    private static Holder<PoiType> register(String name, List<Holder<Block>> blocks, int maxFreeTickets) {
        return REGISTER.register(name, () -> {
            Set<BlockState> blockStates = new HashSet<>();
            for (Holder<Block> block : blocks) {
                blockStates.addAll(block.get().getStateDefinition().getPossibleStates());
            }
            return new PoiType(blockStates, maxFreeTickets, 1);
        });
    }
}
