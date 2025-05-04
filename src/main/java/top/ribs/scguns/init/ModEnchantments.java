package top.ribs.scguns.init;

import top.ribs.scguns.Reference;
import top.ribs.scguns.enchantment.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

/**
 * Author: MrCrayfish
 */
public class ModEnchantments
{
    public static final DeferredRegister<Enchantment> REGISTER = DeferredRegister.create(NeoForgeRegistries.ENCHANTMENTS, Reference.MOD_ID);

    public static final RegistryObject<Enchantment> QUICK_HANDS = REGISTER.register("quick_hands", QuickHandsEnchantment::new);
    public static final RegistryObject<Enchantment> TRIGGER_FINGER = REGISTER.register("trigger_finger", TriggerFingerEnchantment::new);
    public static final RegistryObject<Enchantment> LIGHTWEIGHT = REGISTER.register("lightweight", LightweightEnchantment::new);
    public static final RegistryObject<Enchantment> COLLATERAL = REGISTER.register("collateral", CollateralEnchantment::new);
    public static final RegistryObject<Enchantment> RECLAIMED = REGISTER.register("reclaimed", ReclaimedEnchantment::new);
    public static final RegistryObject<Enchantment> ACCELERATOR = REGISTER.register("accelerator", AcceleratorEnchantment::new);
    public static final RegistryObject<Enchantment> PUNCTURING = REGISTER.register("puncturing", PuncturingEnchantment::new);
    public static final RegistryObject<Enchantment> SHELL_CATCHER = REGISTER.register("shell_catcher", ShellCatcherEnchantment::new);
    public static final RegistryObject<Enchantment> BANZAI = REGISTER.register("banzai", BanzaiEnchantment::new);
    public static final RegistryObject<Enchantment> HEAVY_SHOT = REGISTER.register("heavy_shot", HeavyShotEnchantment::new);
    public static final RegistryObject<Enchantment> ELEMENTAL_POP = REGISTER.register("elemental_pop", ElementalPopEnchantment::new);
    public static final RegistryObject<Enchantment> WATER_PROOF = REGISTER.register("waterproof", WaterProofEnchantment::new);
    public static final RegistryObject<Enchantment> HOT_BARREL = REGISTER.register("hot_barrel", HotBarrelEnchantment::new);
}
