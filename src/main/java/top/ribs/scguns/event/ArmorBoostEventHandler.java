package top.ribs.scguns.event;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import top.ribs.scguns.init.ModItems;

@Mod.EventBusSubscriber(modid = "scguns")
public class ArmorBoostEventHandler {

    private static final int RESISTANCE_LEVEL = 1;
    private static final int EFFECT_DURATION = 40;

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            applyResistanceBoost(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            applyResistanceBoost(player);
        }
    }

    private static void applyResistanceBoost(Player player) {
        boolean holdingSpecialItem = isSpecialItem(player.getMainHandItem()) || isSpecialItem(player.getOffhandItem());

        if (holdingSpecialItem) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, EFFECT_DURATION, RESISTANCE_LEVEL, false, false));
        }
    }

    private static boolean isSpecialItem(ItemStack itemStack) {
        return itemStack.getItem() == ModItems.SHELLURKER.get();
    }
}