package top.ribs.scguns.init;

import top.ribs.scguns.Reference;
import top.ribs.scguns.effect.IncurableEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

/**
 * Author: MrCrayfish
 */
public class    ModEffects
{
    public static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create(NeoForgeRegistries.MOB_EFFECTS, Reference.MOD_ID);

    public static final RegistryObject<IncurableEffect> BLINDED = REGISTER.register("blinded", () -> new IncurableEffect(MobEffectCategory.HARMFUL, 0));
    public static final RegistryObject<IncurableEffect> DEAFENED = REGISTER.register("deafened", () -> new IncurableEffect(MobEffectCategory.HARMFUL, 0));
}
