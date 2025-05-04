package top.ribs.scguns.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.client.*;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEventsBus {
    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.COG_MINION_LAYER, CogMinionModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.COG_KNIGHT_LAYER, CogKnightModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.SKY_CARRIER_LAYER, SkyCarrierModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.SUPPLY_SCAMP_LAYER, SupplyScampModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.REDCOAT_LAYER, RedcoatModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.BLUNDERER_LAYER, BlundererModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.DISSIDENT_LAYER, DissidentModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.HIVE_LAYER, HiveModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.SWARM_LAYER, SwarmModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.HORNLIN_LAYER, HornlinModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.ZOMBIFIED_HORNLIN_LAYER, ZombifiedHornlinModel::createBodyLayer);
    }
}

