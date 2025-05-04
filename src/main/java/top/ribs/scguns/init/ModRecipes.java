package top.ribs.scguns.init;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.Holder;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.screen.*;


public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.RECIPE_SERIALIZERS, Reference.MOD_ID);

    public static final Holder<RecipeSerializer<MechanicalPressRecipe>> MECHANICAL_PRESS_SERIALIZER =
            SERIALIZERS.register("mechanical_pressing", () -> MechanicalPressRecipe.Serializer.INSTANCE);
    public static final Holder<RecipeSerializer<PoweredMechanicalPressRecipe>> POWERED_MECHANICAL_PRESS_SERIALIZER =
            SERIALIZERS.register("powered_mechanical_pressing", () -> PoweredMechanicalPressRecipe.Serializer.INSTANCE);
    public static final Holder<RecipeSerializer<MaceratorRecipe>> MACERATOR_SERIALIZER =
            SERIALIZERS.register("macerating", () -> MaceratorRecipe.Serializer.INSTANCE);
    public static final Holder<RecipeSerializer<PoweredMaceratorRecipe>> POWERED_MACERATOR_SERIALIZER =
            SERIALIZERS.register("powered_macerating", () -> PoweredMaceratorRecipe.Serializer.INSTANCE);
    public static final Holder<RecipeSerializer<GunBenchRecipe>> GUN_BENCH_SERIALIZER =
            SERIALIZERS.register("gun_bench", () -> GunBenchRecipe.Serializer.INSTANCE);

    public static final Holder<RecipeSerializer<LightningBatteryRecipe>> LIGHTNING_BATTERY_SERIALIZER =
            SERIALIZERS.register("lightning_battery", () -> LightningBatteryRecipe.Serializer.INSTANCE);
    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}



