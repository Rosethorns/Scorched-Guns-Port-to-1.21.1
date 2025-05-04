package top.ribs.scguns.init;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.Holder;
import top.ribs.scguns.Reference;
import top.ribs.scguns.world.GeothermalVentFeature;
import top.ribs.scguns.world.SulfurVentFeature;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(NeoForgeRegistries.FEATURES, Reference.MOD_ID);

    public static final Holder<Feature<NoneFeatureConfiguration>> GEOTHERMAL_VENT_FEATURE = FEATURES.register("geothermal_vent",
            () -> new GeothermalVentFeature(NoneFeatureConfiguration.CODEC));

    public static final Holder<Feature<NoneFeatureConfiguration>> SULFUR_VENT_FEATURE = FEATURES.register("sulfur_vent",
            () -> new SulfurVentFeature(NoneFeatureConfiguration.CODEC));


    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }
}