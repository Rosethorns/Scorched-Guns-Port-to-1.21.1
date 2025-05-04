package top.ribs.scguns.client;

import com.mrcrayfish.framework.api.client.FrameworkClientAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import org.lwjgl.glfw.GLFW;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.handler.*;
import top.ribs.scguns.client.render.block.*;
import top.ribs.scguns.client.render.curios.AmmoBoxRenderer;
import top.ribs.scguns.client.render.entity.TurretProjectileRenderer;
import top.ribs.scguns.client.screen.*;
import top.ribs.scguns.client.screen.VentCollectorScreen;
import top.ribs.scguns.client.screen.widget.ThermolithScreen;
import top.ribs.scguns.client.util.PropertyHelper;
import top.ribs.scguns.debug.IEditorMenu;
import top.ribs.scguns.debug.client.screen.EditorScreen;
import top.ribs.scguns.entity.client.*;
import top.ribs.scguns.init.*;
import top.ribs.scguns.item.AmmoBoxItem;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageAttachments;
import top.ribs.scguns.network.message.C2SMessageMeleeAttack;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import java.lang.reflect.Field;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class ClientHandler {
    private static Field mouseOptionsField;

    public static void registerClientHandlers(IEventBus bus) {
        FrameworkClientAPI.registerDataLoader(MetaLoader.getInstance());
       // onRegisterCreativeTab(bus);
        bus.addListener(KeyBinds::registerKeyMappings);
        bus.addListener(CrosshairHandler::onConfigReload);
        bus.addListener(ClientHandler::onRegisterReloadListener);
        bus.addListener(ClientHandler::registerAdditional);
        bus.addListener(ClientHandler::onClientSetup);
        NeoForge.EVENT_BUS.register(HUDRenderHandler.class);
    }
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            //BeamHandler.clientTick();
        }
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Minecraft.getInstance().getTextureManager().register(
                    new ResourceLocation("textures/entity/beacon_beam.png"),
                    new SimpleTexture(new ResourceLocation("textures/entity/beacon_beam.png"))
            );
        });
        EntityRenderers.register(ModEntities.PRIMED_POWDER_KEG.get(), PowderKegRenderer::new);
        EntityRenderers.register(ModEntities.PRIMED_NITRO_KEG.get(), NitroKegRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.MACERATOR.get(), MaceratorRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.GUN_SHELF_BLOCK_ENTITY.get(), GunShelfRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.POWERED_MACERATOR.get(), PoweredMaceratorRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.MECHANICAL_PRESS.get(), MechanicalPressRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.BASIC_TURRET.get(), BasicTurretRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ENEMY_TURRET.get(), EnemyTurretRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.AUTO_TURRET.get(), AutoTurretRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.SHOTGUN_TURRET.get(), ShotgunTurretRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.POWERED_MECHANICAL_PRESS.get(), PoweredMechanicalPressRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.POLAR_GENERATOR.get(), PolarGeneratorRenderer::new);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PLASMA_LANTERN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.WHITE_NITER_GLASS.get(), RenderType.translucent());;
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RED_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.GREEN_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLUE_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.YELLOW_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.ORANGE_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PURPLE_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLACK_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PINK_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.BROWN_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.CYAN_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIGHT_BLUE_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIME_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.MAGENTA_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.GRAY_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIGHT_GRAY_NITER_GLASS.get(), RenderType.translucent());

        registerAmmoCountProperty(ModItems.PISTOL_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.RIFLE_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.SHOTGUN_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.MAGNUM_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.ENERGY_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.ROCKET_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.SPECIAL_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.EMPTY_CASING_POUCH.get());
        registerAmmoCountProperty(ModItems.DISHES_POUCH.get());
        registerAmmoCountProperty(ModItems.ROCK_POUCH.get());
        registerAmmoCountProperty(ModItems.CREATIVE_AMMO_BOX.get());
        NeoForge.EVENT_BUS.register(new PlayerModelHandler());
        MenuScreens.register(ModMenuTypes.MACERATOR_MENU.get(), MaceratorScreen::new);
        MenuScreens.register(ModMenuTypes.POWERED_MACERATOR_MENU.get(), PoweredMaceratorScreen::new);
        MenuScreens.register(ModMenuTypes.LIGHTING_BATTERY_MENU.get(), LightningBatteryScreen::new);
        MenuScreens.register(ModMenuTypes.SUPPLY_SCAMP_MENU.get(), SupplyScampScreen::new);
        MenuScreens.register(ModMenuTypes.SHELL_CATCHER_MODULE.get(), ShellCatcherModuleScreen::new);
        MenuScreens.register(ModMenuTypes.AMMO_MODULE.get(), AmmoModuleScreen::new);
        MenuScreens.register(ModMenuTypes.BASIC_TURRET_MENU.get(), BasicTurretScreen::new);
        MenuScreens.register(ModMenuTypes.AUTO_TURRET_MENU.get(), AutoTurretScreen::new);
        MenuScreens.register(ModMenuTypes.SHOTGUN_TURRET_MENU.get(), ShotgunTurretScreen::new);
        MenuScreens.register(ModMenuTypes.VENT_COLLECTOR_MENU.get(), VentCollectorScreen::new);

        MenuScreens.register(ModMenuTypes.MECHANICAL_PRESS_MENU.get(), MechanicalPressScreen::new);
        MenuScreens.register(ModMenuTypes.POWERED_MECHANICAL_PRESS_MENU.get(), PoweredMechanicalPressScreen::new);
        MenuScreens.register(ModMenuTypes.POLAR_GENERATOR_MENU.get(), PolarGeneratorScreen::new);
        MenuScreens.register(ModMenuTypes.CRYONITER_MENU.get(), CryoniterScreen::new);
        MenuScreens.register(ModMenuTypes.THERMOLITH_MENU.get(), ThermolithScreen::new);
        MenuScreens.register(ModMenuTypes.GUN_BENCH.get(), GunBenchScreen::new);
        EntityRenderers.register(ModEntities.COG_MINION.get(), CogMinionRenderer::new);
        EntityRenderers.register(ModEntities.COG_KNIGHT.get(), CogKnightRenderer::new);
        EntityRenderers.register(ModEntities.SKY_CARRIER.get(), SkyCarrierRenderer::new);
        EntityRenderers.register(ModEntities.SUPPLY_SCAMP.get(), SupplyScampRenderer::new);
        EntityRenderers.register(ModEntities.REDCOAT.get(), RedcoatRenderer::new);
        EntityRenderers.register(ModEntities.HIVE.get(), HiveRenderer::new);
        EntityRenderers.register(ModEntities.SWARM.get(), SwarmRenderer::new);
        EntityRenderers.register(ModEntities.DISSIDENT.get(), DissidentRenderer::new);
        EntityRenderers.register(ModEntities.HORNLIN.get(), HornlinRenderer::new);
        EntityRenderers.register(ModEntities.ZOMBIFIED_HORNLIN.get(), ZombifiedHornlinRenderer::new);
        EntityRenderers.register(ModEntities.BLUNDERER.get(), BlundererRenderer::new);
        EntityRenderers.register(ModEntities.BRASS_BOLT.get(), BrassBoltRenderer::new);
        EntityRenderers.register(ModEntities.TURRET_PROJECTILE.get(), TurretProjectileRenderer::new);
        // Register the AmmoBoxRenderer for each ammo box item
        CuriosRendererRegistry.register(ModItems.PISTOL_AMMO_BOX.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.RIFLE_AMMO_BOX.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.SHOTGUN_AMMO_BOX.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.MAGNUM_AMMO_BOX.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.ENERGY_AMMO_BOX.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.ROCKET_AMMO_BOX.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.SPECIAL_AMMO_BOX.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.EMPTY_CASING_POUCH.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.DISHES_POUCH.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.ROCK_POUCH.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.CREATIVE_AMMO_BOX.get(), AmmoBoxRenderer::new);

        event.enqueueWork(ModMuzzleFlashes::init);
        event.enqueueWork(ClientHandler::setup);
    }
    private ResourceLocation getFlashTexture(String flashType) {
        return ModMuzzleFlashes.getMuzzleFlashTexture(flashType);
    }
    public static void setup() {
        NeoForge.EVENT_BUS.register(AimingHandler.get());
        NeoForge.EVENT_BUS.register(BulletTrailRenderingHandler.get());
        NeoForge.EVENT_BUS.register(CrosshairHandler.get());
        NeoForge.EVENT_BUS.register(GunRenderingHandler.get());
        NeoForge.EVENT_BUS.register(RecoilHandler.get());
        NeoForge.EVENT_BUS.register(ReloadHandler.get());
        NeoForge.EVENT_BUS.register(ShootingHandler.get());
        NeoForge.EVENT_BUS.register(SoundHandler.get());
        NeoForge.EVENT_BUS.register(new PlayerModelHandler());

        if (ScorchedGuns.controllableLoaded) {
            ControllerHandler.init();
            GunButtonBindings.register();
        }
        setupRenderLayers();
        registerModelOverrides();
        registerScreenFactories();
    }

    private static void setupRenderLayers() {
        // Implement render layer setup here
    }
    private static void registerModelOverrides() {

        //ModelOverrides.register(ModItems.EARTHS_CORPSE.get(), new EarthsCorpseModel());
        //ModelOverrides.register(ModItems.FLAYED_GOD.get(), new FlayedGodModel());
      //  ModelOverrides.register(ModItems.NERVEPINCH.get(), new NervepinchModel());

        //ModelOverrides.register(ModItems.RAT_KING_AND_QUEEN.get(), new RatKingAndQueenModel());
        //ModelOverrides.register(ModItems.LOCUST.get(), new LocustModel());
        // ModelOverrides.register(ModItems.NEWBORN_CYST.get(), new NewbornCystModel());
        //ModelOverrides.register(ModItems.LONE_WONDER.get(), new LoneWonderModel());
        // ModelOverrides.register(ModItems.CARAPICE.get(), new CarapiceModel());
        //ModelOverrides.register(ModItems.SHELLURKER.get(), new ShellurkerModel());
        // ModelOverrides.register(ModItems.ECHOES_2.get(), new Echoes2Model());
        //ModelOverrides.register(ModItems.RAYGUN.get(), new RaygunModel());
        //ModelOverrides.register(ModItems.SCULK_RESONATOR.get(), new SculkResonatorModel());
        //ModelOverrides.register(ModItems.BLASPHEMY.get(), new BlasphemyModel());
        //ModelOverrides.register(ModItems.WHISPERS.get(), new WhispersModel());
        //ModelOverrides.register(ModItems.PYROCLASTIC_FLOW.get(), new PyroclasticFlowModel());
        //ModelOverrides.register(ModItems.FREYR.get(), new FreyrModel());
        //ModelOverrides.register(ModItems.VULCANIC_REPEATER.get(), new VulcanicRepeaterModel());
        //ModelOverrides.register(ModItems.SCRATCHES.get(), new ScratchesModel());
        //ModelOverrides.register(ModItems.OSGOOD_50.get(), new Osgood50Model());
        //ModelOverrides.register(ModItems.GALE.get(), new GaleModel());
        //ModelOverrides.register(ModItems.WALTZ_CONVERSION.get(), new WaltzConversionModel());
        //ModelOverrides.register(ModItems.UMAX_PISTOL.get(), new UmaxPistolModel());
        //ModelOverrides.register(ModItems.SPITFIRE.get(), new SpitfireModel());
        //ModelOverrides.register(ModItems.SHARD_CULLER.get(), new ShardCullerModel());
        //ModelOverrides.register(ModItems.GATTALER.get(), new GattalerModel());
        //ModelOverrides.register(ModItems.CR4K_MINING_LASER.get(), new Cr4kMiningLaserModel());
        //ModelOverrides.register(ModItems.THUNDERHEAD.get(), new ThunderheadModel());
        //ModelOverrides.register(ModItems.GYROJET_PISTOL.get(), new GyrojetPistolModel());
        //ModelOverrides.register(ModItems.DARK_MATTER.get(), new DarkMatterModel());
        //ModelOverrides.register(ModItems.DOZIER_RL.get(), new DozierRLModel());
        // ModelOverrides.register(ModItems.SUPER_SHOTGUN.get(), new SuperShotgunModel());
        //ModelOverrides.register(ModItems.BOMB_LANCE.get(), new BombLanceModel());
        //ModelOverrides.register(ModItems.VENTURI.get(), new VenturiModel());
        //ModelOverrides.register(ModItems.MK43_RIFLE.get(), new Mk43RifleModel());
        //ModelOverrides.register(ModItems.PLASGUN.get(), new PlasgunModel());
        //ModelOverrides.register(ModItems.REPEATING_MUSKET.get(), new RepeatingMusketModel());
        //ModelOverrides.register(ModItems.ULTRA_KNIGHT_HAWK.get(), new UltraKnightHawkModel());
        //ModelOverrides.register(ModItems.SEQUOIA.get(), new SequoiaModel());
        //ModelOverrides.register(ModItems.LASER_MUSKET.get(), new LaserMusketModel());
        //ModelOverrides.register(ModItems.PLASMABUSS.get(), new PlasmabussModel());
        //ModelOverrides.register(ModItems.JACKHAMMER.get(), new JackhammerModel());
        //ModelOverrides.register(ModItems.PAX.get(), new PaxModel());
        // ModelOverrides.register(ModItems.PULSAR.get(), new PulsarModel());
        //ModelOverrides.register(ModItems.HOWLER.get(), new HowlerModel());
        //ModelOverrides.register(ModItems.HOWLER_CONVERSION.get(), new HowlerConversionModel());
        //ModelOverrides.register(ModItems.BIG_BORE.get(), new BigBoreModel());
        //ModelOverrides.register(ModItems.ARC_WORKER.get(), new ArcWorkerModel());
        //ModelOverrides.register(ModItems.FLINTLOCK_PISTOL.get(), new FlintlockPistolModel());
        //ModelOverrides.register(ModItems.HANDCANNON.get(), new HandcannonPistolModel());
        //ModelOverrides.register(ModItems.MUSKET.get(), new MusketModel());
        //ModelOverrides.register(ModItems.BLUNDERBUSS.get(), new BlunderbussModel());
        //ModelOverrides.register(ModItems.ASTELLA.get(), new AstellaModel());
        //ModelOverrides.register(ModItems.BRAWLER.get(), new BrawlerModel());
        //ModelOverrides.register(ModItems.FLOUNDERGAT.get(), new FloundergatModel());
        //ModelOverrides.register(ModItems.SAKETINI.get(), new SaketiniModel());
        //ModelOverrides.register(ModItems.CALLWELL.get(), new CallwellModel());
        //ModelOverrides.register(ModItems.WINNIE.get(), new WinnieModel());
        //ModelOverrides.register(ModItems.SCRAPPER.get(), new ScrapperModel());
       // ModelOverrides.register(ModItems.MAKESHIFT_RIFLE.get(), new MakeshiftRifleModel());
        //ModelOverrides.register(ModItems.BOOMSTICK.get(), new BoomstickModel());
        //ModelOverrides.register(ModItems.RUSTY_GNAT.get(), new RustyGnatModel());
        //ModelOverrides.register(ModItems.BRUISER.get(), new BruiserModel());
        //ModelOverrides.register(ModItems.LLR_DIRECTOR.get(), new LlrDirectorModel());
        //ModelOverrides.register(ModItems.MARLIN.get(), new MarlinModel());
        //ModelOverrides.register(ModItems.IRON_SPEAR.get(), new IronSpearModel());
        //ModelOverrides.register(ModItems.IRON_JAVELIN.get(), new IronJavelinModel());
        //ModelOverrides.register(ModItems.M3_CARABINE.get(), new M3CarabineModel());
        //ModelOverrides.register(ModItems.LOCKEWOOD.get(), new LockewoodModel());
        //ModelOverrides.register(ModItems.GREASER_SMG.get(), new GreaserSmgModel());
       // ModelOverrides.register(ModItems.DEFENDER_PISTOL.get(), new DefenderPistolModel());
        //ModelOverrides.register(ModItems.COMBAT_SHOTGUN.get(), new CombatShotgunModel());
        //ModelOverrides.register(ModItems.AUVTOMAG.get(), new AuvtomagModel());
        // ModelOverrides.register(ModItems.GAUSS_RIFLE.get(), new GaussRifleModel());
        //ModelOverrides.register(ModItems.ROCKET_RIFLE.get(), new RocketRifleModel());
        //ModelOverrides.register(ModItems.PRUSH_GUN.get(), new PrushGunModel());
       // ModelOverrides.register(ModItems.INERTIAL.get(), new InertialModel());
       // ModelOverrides.register(ModItems.COGLOADER.get(), new CogloaderModel());
        //ModelOverrides.register(ModItems.GRANDLE.get(), new GrandleModel());
        //ModelOverrides.register(ModItems.UPPERCUT.get(), new UppercutModel());
        //ModelOverrides.register(ModItems.MAS_55.get(), new Mas55Model());
        // ModelOverrides.register(ModItems.CYCLONE.get(), new CycloneModel());
        // ModelOverrides.register(ModItems.SOUL_DRUMMER.get(), new SoulDrummerModel());
        //ModelOverrides.register(ModItems.VALORA.get(), new ValoraModel());
       // ModelOverrides.register(ModItems.KRAUSER.get(), new KrauserModel());
        //ModelOverrides.register(ModItems.M22_WALTZ.get(), new M22WaltzModel());


    }


    private static void registerScreenFactories() {
        MenuScreens.register(ModContainers.ATTACHMENTS.get(), AttachmentScreen::new);
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof MouseSettingsScreen) {
            MouseSettingsScreen screen = (MouseSettingsScreen) event.getScreen();
            if (mouseOptionsField == null) {
                mouseOptionsField = ObfuscationReflectionHelper.findField(MouseSettingsScreen.class, "f_96218_");
                mouseOptionsField.setAccessible(true);
            }
            try {
                OptionsList list = (OptionsList) mouseOptionsField.get(screen);
                //list.addSmall(GunOptions.ADS_SENSITIVITY, GunOptions.CROSSHAIR);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public static void onKeyPressed(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.screen == null && event.getAction() == GLFW.GLFW_PRESS) {
            if (KeyBinds.KEY_ATTACHMENTS.isDown()) {
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageAttachments());
            }
        }
        if (KeyBinds.KEY_MELEE.consumeClick() && event.getAction() == GLFW.GLFW_PRESS) {
            if (mc.player.getMainHandItem().getItem() instanceof GunItem gunItem) {
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageMeleeAttack());

            }
        }

    }

    public static void onRegisterReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            PropertyHelper.resetCache();
        });
    }

    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        //event.register(new ResourceLocation(Reference.MOD_ID, "special/test"));
    }


    public static Screen createEditorScreen(IEditorMenu menu) {
        return new EditorScreen(Minecraft.getInstance().screen, menu);
    }


    private static void registerAmmoCountProperty(Item item) {
        ItemProperties.register(item, new ResourceLocation("ammo_count"),
                (stack, world, entity, seed) -> {
                    int totalItemCount = AmmoBoxItem.getTotalItemCount(stack);
                    int maxItemCount = AmmoBoxItem.getMaxItemCount(stack);
                    if (totalItemCount == 0) {
                        return 0.0f;
                    } else if (totalItemCount <= maxItemCount / 3) {
                        return 0.33f;
                    } else if (totalItemCount <= 2 * maxItemCount / 3) {
                        return 0.66f;
                    } else {
                        return 1.0f;
                    }
                });
    }
}
