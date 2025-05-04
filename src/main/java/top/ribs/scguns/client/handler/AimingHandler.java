package top.ribs.scguns.client.handler;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiOverlayEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import top.ribs.scguns.Config;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.client.util.PropertyHelper;
import top.ribs.scguns.common.GripType;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.compat.PlayerReviveHelper;
import top.ribs.scguns.debug.Debug;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageAim;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Author: MrCrayfish
 */
public class AimingHandler
{
    private static AimingHandler instance;

    public static AimingHandler get()
    {
        if(instance == null)
        {
            instance = new AimingHandler();
        }
        return instance;
    }

    private static final double MAX_AIM_PROGRESS = 5;
    private final AimTracker localTracker = new AimTracker();
    private final Map<Player, AimTracker> aimingMap = new WeakHashMap<>();
    private double normalisedAdsProgress;
    private boolean aiming = false;
    private boolean wasKeyPressed = false;

    private AimingHandler() {}

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;

        Player player = event.player;
        AimTracker tracker = getAimTracker(player);
        if(tracker != null)
        {
            tracker.handleAiming(player, player.getItemInHand(InteractionHand.MAIN_HAND));
            if(!tracker.isAiming())
            {
                this.aimingMap.remove(player);
            }
        }
    }

    @Nullable
    private AimTracker getAimTracker(Player player)
    {
        if(ModSyncedDataKeys.AIMING.getValue(player) && !this.aimingMap.containsKey(player))
        {
            this.aimingMap.put(player, new AimTracker());
        }
        return this.aimingMap.get(player);
    }

    public float getAimProgress(Player player, float partialTicks)
    {
        if(player.isLocalPlayer())
        {
            return (float) this.localTracker.getNormalProgress(partialTicks);
        }

        AimTracker tracker = this.getAimTracker(player);
        if(tracker != null)
        {
            return (float) tracker.getNormalProgress(partialTicks);
        }
        return 0F;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;

        Player player = Minecraft.getInstance().player;
        if(player == null)
            return;

        boolean currentKeyPressed = KeyBinds.getAimMapping().isDown();
        boolean toggleAdsEnabled = Config.COMMON.gameplay.toggleADS.get();

        if(toggleAdsEnabled)
        {
            if(currentKeyPressed && !wasKeyPressed)
            {
                this.aiming = !this.aiming;
            }
        }
        else
        {
            this.aiming = currentKeyPressed;
        }
        wasKeyPressed = currentKeyPressed;

        if(ScorchedGuns.controllableLoaded)
        {
            boolean controllerAiming = ControllerHandler.isAiming();
            if(toggleAdsEnabled)
            {
                if(controllerAiming && !wasKeyPressed)
                {
                    this.aiming = !this.aiming;
                }
            }
            else
            {
                this.aiming |= controllerAiming;
            }
        }
        boolean shouldBeAiming = this.isAiming();
        if(shouldBeAiming)
        {
            if(!ModSyncedDataKeys.AIMING.getValue(player))
            {
                ModSyncedDataKeys.AIMING.setValue(player, true);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageAim(true));
            }
        }
        else if(ModSyncedDataKeys.AIMING.getValue(player))
        {
            ModSyncedDataKeys.AIMING.setValue(player, false);
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageAim(false));
        }

        this.localTracker.handleAiming(player, player.getItemInHand(InteractionHand.MAIN_HAND));
    }

    public boolean isAiming()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return false;

        if(mc.player.isSpectator())
            return false;

        if(Debug.isForceAim())
            return true;

        if(mc.screen != null)
        {
            this.aiming = false;
            return false;
        }

        if(PlayerReviveHelper.isBleeding(mc.player))
        {
            this.aiming = false;
            return false;
        }

        ItemStack heldItem = mc.player.getMainHandItem();
        if(!(heldItem.getItem() instanceof GunItem))
        {
            this.aiming = false;
            return false;
        }

        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
        if(!gun.canAimDownSight())
        {
            this.aiming = false;
            return false;
        }

        if(mc.player.getOffhandItem().getItem() == Items.SHIELD &&
                (gun.getGeneral().getGripType(heldItem) == GripType.ONE_HANDED ||
                        gun.getGeneral().getGripType(heldItem) == GripType.ONE_HANDED_2))
        {
            this.aiming = false;
            return false;
        }

        if(!this.localTracker.isAiming() && this.isLookingAtInteractableBlock())
        {
            this.aiming = false;
            return false;
        }

        if(ModSyncedDataKeys.RELOADING.getValue(mc.player))
        {
            this.aiming = false;
            return false;
        }

        return this.aiming;
    }

    @SubscribeEvent
    public void onFovUpdate(ViewportEvent.ComputeFov event)
    {
        if(!event.usedConfiguredFov())
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || mc.player.getMainHandItem().isEmpty() || mc.options.getCameraType() != CameraType.FIRST_PERSON)
            return;

        ItemStack heldItem = mc.player.getMainHandItem();
        if(!(heldItem.getItem() instanceof GunItem gunItem))
            return;

        if(AimingHandler.get().getNormalisedAdsProgress() == 0)
            return;

        if(ModSyncedDataKeys.RELOADING.getValue(mc.player))
            return;

        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        if(modifiedGun.getModules().getZoom() == null)
            return;

        double time = PropertyHelper.getSightAnimations(heldItem, modifiedGun).getFovCurve().apply(this.normalisedAdsProgress);
        float modifier = Gun.getFovModifier(heldItem, modifiedGun);
        modifier = (1.0F - modifier) * (float) time;
        event.setFOV(event.getFOV() - event.getFOV() * modifier);
    }

    @SubscribeEvent
    public void onClientTick(ClientPlayerNetworkEvent.LoggingOut event)
    {
        this.aimingMap.clear();
    }

    /**
     * Prevents the crosshair from rendering when aiming down sight
     */
    @SubscribeEvent(receiveCanceled = true)
    public void onRenderOverlay(RenderGuiOverlayEvent event)
    {
        this.normalisedAdsProgress = this.localTracker.getNormalProgress(event.getPartialTick());
    }

    public boolean isZooming()
    {
        return this.aiming;
    }



    public boolean isLookingAtInteractableBlock()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.hitResult != null && mc.level != null)
        {
            if(mc.hitResult instanceof BlockHitResult result)
            {
                BlockState state = mc.level.getBlockState(result.getBlockPos());
                Block block = state.getBlock();
                // Forge should add a tag for intractable blocks so modders can know which blocks can be interacted with :)
                return block instanceof EntityBlock || block == ModBlocks.GUN_BENCH .get()||block == ModBlocks.POWERED_MACERATOR .get()||block == ModBlocks.POWERED_MECHANICAL_PRESS .get()||block == ModBlocks.POLAR_GENERATOR .get()||block == ModBlocks.LIGHTNING_BATTERY .get()||block == ModBlocks.MECHANICAL_PRESS .get()|| block == ModBlocks.MACERATOR .get()|| block == Blocks.CRAFTING_TABLE ||  state.is(BlockTags.DOORS) || state.is(BlockTags.TRAPDOORS) || state.is(Tags.Blocks.CHESTS) || state.is(Tags.Blocks.FENCE_GATES);
            }
            else if(mc.hitResult instanceof EntityHitResult result)
            {
                return result.getEntity() instanceof ItemFrame;
            }
        }
        return false;
    }

    public double getNormalisedAdsProgress()
    {
        return this.normalisedAdsProgress;
    }

    public class AimTracker
    {
        private double currentAim;
        private double previousAim;

        private void handleAiming(Player player, ItemStack heldItem)
        {
            this.previousAim = this.currentAim;
            if(ModSyncedDataKeys.AIMING.getValue(player) || (player.isLocalPlayer() && AimingHandler.this.isAiming()))
            {
                if(this.currentAim < MAX_AIM_PROGRESS)
                {
                    double speed = GunEnchantmentHelper.getAimDownSightSpeed(heldItem);
                    speed = GunModifierHelper.getModifiedAimDownSightSpeed(heldItem, speed);
                    this.currentAim += speed;
                    if(this.currentAim > MAX_AIM_PROGRESS)
                    {
                        this.currentAim = (int) MAX_AIM_PROGRESS;
                    }
                }
            }
            else
            {
                if(this.currentAim > 0)
                {
                    double speed = GunEnchantmentHelper.getAimDownSightSpeed(heldItem);
                    speed = GunModifierHelper.getModifiedAimDownSightSpeed(heldItem, speed);
                    this.currentAim -= speed;
                    if(this.currentAim < 0)
                    {
                        this.currentAim = 0;
                    }
                }
            }
        }

        public boolean isAiming()
        {
            return this.currentAim != 0 || this.previousAim != 0;
        }

        public double getNormalProgress(float partialTicks)
        {
            return Mth.clamp((this.previousAim + (this.currentAim - this.previousAim) * partialTicks) / MAX_AIM_PROGRESS, 0.0, 1.0);
        }
    }
}