package top.ribs.scguns.attributes;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.world.entity.EntityType;
import top.ribs.scguns.ScorchedGuns;

@EventBusSubscriber(modid = ScorchedGuns.MODID, bus = EventBusSubscriber.Bus.MOD)
public class SCEntityAttributes {
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, SCAttributes.PROJECTILE_SPEED);
        event.add(EntityType.PLAYER, SCAttributes.RELOAD_SPEED);
        event.add(EntityType.PLAYER, SCAttributes.ADDITIONAL_BULLET_DAMAGE);
        event.add(EntityType.PLAYER, SCAttributes.BULLET_DAMAGE_MULTIPLIER);
        event.add(EntityType.PLAYER, SCAttributes.SPREAD_MULTIPLIER);
        //Disabled
        //event.add(EntityType.PLAYER, SCAttributes.BULLET_RESISTANCE.get());
        //event.add(EntityType.PLAYER, SCAttributes.FIRE_RATE_MULTIPLIER.get());
    }
}