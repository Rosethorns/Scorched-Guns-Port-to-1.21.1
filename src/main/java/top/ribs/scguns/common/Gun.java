package top.ribs.scguns.common;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.fml.DistExecutor;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.Reference;
import top.ribs.scguns.annotation.Ignored;
import top.ribs.scguns.annotation.Optional;
import top.ribs.scguns.client.ClientHandler;
import top.ribs.scguns.debug.Debug;
import top.ribs.scguns.debug.IDebugWidget;
import top.ribs.scguns.debug.IEditorMenu;
import top.ribs.scguns.debug.client.screen.widget.DebugButton;
import top.ribs.scguns.debug.client.screen.widget.DebugSlider;
import top.ribs.scguns.debug.client.screen.widget.DebugToggle;
import top.ribs.scguns.item.*;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.item.attachment.impl.Scope;
import top.ribs.scguns.util.GunJsonUtil;
import top.ribs.scguns.util.SuperBuilder;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class Gun implements INBTSerializable<CompoundTag>, IEditorMenu {
    protected General general = new General();
    protected Reloads reloads = new Reloads();
    protected Projectile projectile = new Projectile();
    protected Sounds sounds = new Sounds();
    protected Display display = new Display();
    protected Modules modules = new Modules();
    private final GripType baseGripType = GripType.ONE_HANDED;


    public static int getMaxAmmo(ItemStack stack) {
        return ((GunItem) stack.getItem()).getModifiedGun(stack).getReloads().getMaxAmmo();
    }

    public static int getAmmoCount(ItemStack stack) {
        return stack.getOrCreateTag().getInt("AmmoCount");
    }

    public static boolean hasAmmo(ItemStack heldItem) {
        return getAmmoCount(heldItem) > 0;
    }

    public static boolean hasUnlimitedReloads(ItemStack heldItem) {
        return ((GunItem) heldItem.getItem()).getModifiedGun(heldItem).getReloads().getInfiniteAmmo();
    }

    public static int getBurstCooldown(ItemStack stack) {
        return ((GunItem) stack.getItem()).getModifiedGun(stack).getGeneral().getBurstCooldown();

    }

    public GripType determineGripType(ItemStack stack) {
        GripType baseGripType = this.general.getBaseGripType();
        if (stack.getItem() instanceof GunItem gunItem &&
                gunItem.isOneHandedCarbineCandidate(stack) &&
                (Gun.hasExtendedBarrel(stack) || Gun.hasStock(stack))) {
            return GripType.TWO_HANDED;
        }
        return baseGripType;
    }

    public GripType getBaseGripType() {
        return this.baseGripType;
    }


    public static boolean hasExtendedBarrel(ItemStack stack) {
        for (IAttachment.Type type : IAttachment.Type.values()) {
            ItemStack attachmentStack = Gun.getAttachment(type, stack);
            if (attachmentStack.getItem() instanceof ExtendedBarrelItem) {
                return true;
            }
        }
        return false;
    }
    public static boolean hasStock(ItemStack stack) {
        for (IAttachment.Type type : IAttachment.Type.values()) {
            ItemStack attachmentStack = Gun.getAttachment(type, stack);
            if (attachmentStack.getItem() instanceof StockItem) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasBurstFire(ItemStack stack) {
        return ((GunItem) stack.getItem()).getModifiedGun(stack).getGeneral().getFireMode() == FireMode.BURST;
    }

    public static boolean canShoot(ItemStack heldItem) {
        return !hasBurstFire(heldItem) || getFireTimer(heldItem) == 0;

    }

    public static int getBurstCount(ItemStack heldItem) {
        return ((GunItem) heldItem.getItem()).getModifiedGun(heldItem).getGeneral().getBurstAmount();
    }


    public General getGeneral() {
        return this.general;
    }

    public Reloads getReloads() {
        return this.reloads;
    }

    public Projectile getProjectile() {
        return this.projectile;
    }

    public Sounds getSounds() {
        return this.sounds;
    }

    public Display getDisplay() {
        return this.display;
    }

    public Modules getModules() {
        return this.modules;
    }

    @Override
    public Component getEditorLabel() {
        return Component.translatable("Gun");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ItemStack heldItem = Objects.requireNonNull(Minecraft.getInstance().player).getMainHandItem();
            ItemStack scope = Gun.getScopeStack(heldItem);
            if (scope.getItem() instanceof ScopeItem scopeItem) {
                widgets.add(Pair.of(scope.getItem().getName(scope), () -> new DebugButton(Component.translatable("Edit"), btn -> {
                    Minecraft.getInstance().setScreen(ClientHandler.createEditorScreen(Debug.getScope(scopeItem)));
                })));
            }

            widgets.add(Pair.of(this.modules.getEditorLabel(), () -> new DebugButton(Component.translatable(">"), btn -> {
                Minecraft.getInstance().setScreen(ClientHandler.createEditorScreen(this.modules));
            })));
        });
    }

    public static class General implements INBTSerializable<CompoundTag> {

        @Ignored
        private FireMode fireMode = FireMode.SEMI_AUTO;
        @Optional
        private int burstAmount;
        @Optional
        private int burstCooldown;
        private int rate;
        private int hotBarrelRate;
        @Optional
        private int fireTimer;
        @Ignored
        private GripType gripType = GripType.ONE_HANDED;
        @Ignored
        private GripType baseGripType = GripType.ONE_HANDED;
        //@Optional
        private float recoilAngle;
        @Optional
        private float recoilKick;
        @Optional
        private float recoilDurationOffset;
        @Optional
        private float recoilAdsReduction = 0.2F;
        @Optional
        private int projectileAmount = 1;
        @Optional
        private boolean alwaysSpread;
        @Optional
        private float spread;
        @Optional
        private float restingSpread = 0F;
        @Optional
        private float spreadAdsReduction = 0.5F;
        @Optional
        private boolean infiniteAmmo;
        @Optional
        private float meleeDamage = 0.0F;
        @Optional
        public int meleeCooldownTicks = 15;
        @Optional
        private float meleeReach = 3.0F;
        @Optional
        private int energyUse = 0;
        @Optional
        private String beamColor;
        @Optional
        private String secondaryBeamColor;
        @Optional
        private String enchantedBeamColor;
        @Optional
        private String enchantedSecondaryBeamColor;
        @Optional
        private int beamAmmoConsumptionDelay = 1000;
        @Optional
        private int beamDamageDelay = 300;
        @Optional
        private double beamMaxDistance = 50.0;
        @Optional
        private boolean enableMining = false;
        @Optional
        private float miningSpeed = 1.0F;
        @Optional
        private boolean hasCameraShake = true;
        @Optional
        private float criticalChance = 0.0F;
        @Optional
        private boolean playerKnockBack = false;
        @Optional
        private float playerKnockBackStrength = 0.0F;
        @Optional
        private boolean isRevolver = false;
        @Optional
        private float speedModifier = 1.0F;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putString("FireMode", this.fireMode.id().toString());
            tag.putInt("BurstAmount", this.burstAmount);
            tag.putInt("BurstCooldown", this.burstCooldown);
            tag.putInt("Rate", this.rate);
            tag.putInt("HotBarrelRate", this.hotBarrelRate);
            tag.putInt("FireTimer", this.fireTimer);
            tag.putString("GripType", this.gripType.id().toString());
            tag.putString("BaseGripType", this.baseGripType.id().toString());
            tag.putFloat("RecoilAngle", this.recoilAngle);
            tag.putFloat("RecoilKick", this.recoilKick);
            tag.putFloat("RecoilDurationOffset", this.recoilDurationOffset);
            tag.putFloat("RecoilAdsReduction", this.recoilAdsReduction);
            tag.putInt("ProjectileAmount", this.projectileAmount);
            tag.putBoolean("AlwaysSpread", this.alwaysSpread);
            tag.putFloat("Spread", this.spread);
            tag.putFloat("RestingSpread", this.restingSpread);
            tag.putFloat("MeleeDamage", this.meleeDamage);
            tag.putFloat("MeleeCooldownTicks", this.meleeCooldownTicks);
            tag.putFloat("MeleeReach", this.meleeReach);
            tag.putBoolean("InfiniteAmmo", this.infiniteAmmo);
            tag.putInt("EnergyUse", this.energyUse);
            if (this.beamColor != null && !this.beamColor.isEmpty()) {
                tag.putString("BeamColor", this.beamColor);
            }
            if (this.secondaryBeamColor != null && !this.secondaryBeamColor.isEmpty()) {
                tag.putString("SecondaryBeamColor", this.secondaryBeamColor);
            }
            if (this.secondaryBeamColor != null && !this.secondaryBeamColor.isEmpty()) {
                tag.putString("SecondaryBeamColor", this.secondaryBeamColor);
            }
            if (this.enchantedBeamColor != null && !this.enchantedBeamColor.isEmpty()) {
                tag.putString("EnchantedBeamColor", this.enchantedBeamColor);
            }
            if (this.enchantedSecondaryBeamColor != null && !this.enchantedSecondaryBeamColor.isEmpty()) {
                tag.putString("EnchantedSecondaryBeamColor", this.enchantedSecondaryBeamColor);
            }

            tag.putInt("BeamAmmoConsumptionDelay", this.beamAmmoConsumptionDelay);
            tag.putInt("BeamDamageDelay", this.beamDamageDelay);
            tag.putDouble("BeamMaxDistance", this.beamMaxDistance);
            tag.putBoolean("EnableMining", this.enableMining);
            tag.putFloat("MiningSpeed", this.miningSpeed);
            tag.putBoolean("HasCameraShake", this.hasCameraShake);
            tag.putFloat("CriticalChance", this.criticalChance);
            tag.putBoolean("PlayerKnockBack", this.playerKnockBack);
            tag.putFloat("PlayerKnockBackStrength", this.playerKnockBackStrength);
            tag.putBoolean("IsRevolver", this.isRevolver);
            tag.putFloat("SpeedModifier", this.speedModifier);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("FireMode", Tag.TAG_STRING)) {
                this.fireMode = FireMode.getType(ResourceLocation.tryParse(tag.getString("FireMode")));
            }
            if (tag.contains("BurstAmount", Tag.TAG_ANY_NUMERIC)) {
                this.burstAmount = tag.getInt("BurstAmount");
            }
            if (tag.contains("BurstCooldown", Tag.TAG_ANY_NUMERIC)) {
                this.burstCooldown = tag.getInt("BurstCooldown");
            }
            if (tag.contains("Rate", Tag.TAG_ANY_NUMERIC)) {
                this.rate = tag.getInt("Rate");
            }
            if (tag.contains("HotBarrelRate", Tag.TAG_ANY_NUMERIC)) {
                this.hotBarrelRate = tag.getInt("HotBarrelRate");
            }
            if (tag.contains("FireTimer", Tag.TAG_ANY_NUMERIC)) {
                this.fireTimer = tag.getInt("FireTimer");
            }
            if (tag.contains("GripType", Tag.TAG_STRING)) {
                this.gripType = GripType.getType(ResourceLocation.tryParse(tag.getString("GripType")));
            }
            if (tag.contains("BaseGripType", Tag.TAG_STRING)) {
                this.baseGripType = GripType.getType(ResourceLocation.tryParse(tag.getString("BaseGripType")));
            }
            if (tag.contains("RecoilAngle", Tag.TAG_ANY_NUMERIC)) {
                this.recoilAngle = tag.getFloat("RecoilAngle");
            }
            if (tag.contains("RecoilKick", Tag.TAG_ANY_NUMERIC)) {
                this.recoilKick = tag.getFloat("RecoilKick");
            }
            if (tag.contains("RecoilDurationOffset", Tag.TAG_ANY_NUMERIC)) {
                this.recoilDurationOffset = tag.getFloat("RecoilDurationOffset");
            }
            if (tag.contains("RecoilAdsReduction", Tag.TAG_ANY_NUMERIC)) {
                this.recoilAdsReduction = tag.getFloat("RecoilAdsReduction");
            }
            if (tag.contains("ProjectileAmount", Tag.TAG_ANY_NUMERIC)) {
                this.projectileAmount = tag.getInt("ProjectileAmount");
            }
            if (tag.contains("AlwaysSpread", Tag.TAG_ANY_NUMERIC)) {
                this.alwaysSpread = tag.getBoolean("AlwaysSpread");
            }
            if (tag.contains("Spread", Tag.TAG_ANY_NUMERIC)) {
                this.spread = tag.getFloat("Spread");
            }
            if (tag.contains("RestingSpread", Tag.TAG_ANY_NUMERIC)) {
                this.restingSpread = tag.getFloat("RestingSpread");
            }
            if (tag.contains("SpreadAdsReduction", Tag.TAG_ANY_NUMERIC)) {
                this.spreadAdsReduction = tag.getFloat("SpreadAdsReduction");
            }
            if (tag.contains("MeleeDamage", Tag.TAG_ANY_NUMERIC)) {
                this.meleeDamage = tag.getFloat("MeleeDamage");
            }
            if (tag.contains("MeleeCooldownTicks", Tag.TAG_ANY_NUMERIC)) {
                this.meleeCooldownTicks = tag.getInt("MeleeCooldownTicks");
            }
            if (tag.contains("MeleeReach", Tag.TAG_ANY_NUMERIC)) {
                this.meleeReach = tag.getFloat("MeleeReach");
            }
            if (tag.contains("InfiniteAmmo", Tag.TAG_ANY_NUMERIC)) {
                this.infiniteAmmo = tag.getBoolean("InfiniteAmmo");
            }
            if (tag.contains("EnergyUse", Tag.TAG_ANY_NUMERIC)) {
                this.energyUse = tag.getInt("EnergyUse");
            }
            if (tag.contains("BeamColor", Tag.TAG_STRING)) {
                this.beamColor = tag.getString("BeamColor");
            }
            if (tag.contains("SecondaryBeamColor", Tag.TAG_STRING)) {
                this.secondaryBeamColor = tag.getString("SecondaryBeamColor");
            }
            if (tag.contains("EnchantedBeamColor", Tag.TAG_STRING)) {
                this.enchantedBeamColor = tag.getString("EnchantedBeamColor");
            }
            if (tag.contains("EnchantedSecondaryBeamColor", Tag.TAG_STRING)) {
                this.enchantedSecondaryBeamColor = tag.getString("EnchantedSecondaryBeamColor");
            }
            if (tag.contains("BeamAmmoConsumptionDelay", Tag.TAG_ANY_NUMERIC)) {
                this.beamAmmoConsumptionDelay = tag.getInt("BeamAmmoConsumptionDelay");
            }
            if (tag.contains("BeamDamageDelay", Tag.TAG_ANY_NUMERIC)) {
                this.beamDamageDelay = tag.getInt("BeamDamageDelay");
            }
            if (tag.contains("BeamMaxDistance", Tag.TAG_ANY_NUMERIC)) {
                this.beamMaxDistance = tag.getDouble("BeamMaxDistance");
            }
            if (tag.contains("EnableMining", Tag.TAG_ANY_NUMERIC)) {
                this.enableMining = tag.getBoolean("EnableMining");
            }
            if (tag.contains("MiningSpeed", Tag.TAG_ANY_NUMERIC)) {
                this.miningSpeed = tag.getFloat("MiningSpeed");
            }
            if (tag.contains("HasCameraShake", Tag.TAG_ANY_NUMERIC)) {
                this.hasCameraShake = tag.getBoolean("HasCameraShake");
            }
            if (tag.contains("CriticalChance", Tag.TAG_ANY_NUMERIC)) {
                this.criticalChance = tag.getFloat("CriticalChance");
            }
            if (tag.contains("PlayerKnockBack", Tag.TAG_ANY_NUMERIC)) {
                this.playerKnockBack = tag.getBoolean("PlayerKnockBack");
            }
            if (tag.contains("PlayerKnockBackStrength", Tag.TAG_ANY_NUMERIC)) {
                this.playerKnockBackStrength = tag.getFloat("PlayerKnockBackStrength");
            }
            if (tag.contains("IsRevolver", Tag.TAG_ANY_NUMERIC)) {
                this.isRevolver = tag.getBoolean("IsRevolver");
            }
            if (tag.contains("SpeedModifier", Tag.TAG_ANY_NUMERIC)) {
                this.speedModifier = tag.getFloat("SpeedModifier");
            }
        }

        public JsonObject toJsonObject() {
            Preconditions.checkArgument(this.rate > 0, "Rate must be more than zero");
            Preconditions.checkArgument(this.hotBarrelRate >= 0, "Hot barrel rate must be more than or equal to zero");
            Preconditions.checkArgument(this.recoilAngle >= 0.0F, "Recoil angle must be more than or equal to zero");
            Preconditions.checkArgument(this.recoilKick >= 0.0F, "Recoil kick must be more than or equal to zero");
            Preconditions.checkArgument(this.recoilDurationOffset >= 0.0F && this.recoilDurationOffset <= 1.0F, "Recoil duration offset must be between 0.0 and 1.0");
            Preconditions.checkArgument(this.recoilAdsReduction >= 0.0F && this.recoilAdsReduction <= 1.0F, "Recoil ads reduction must be between 0.0 and 1.0");
            Preconditions.checkArgument(this.projectileAmount >= 1, "Projectile amount must be more than or equal to one");
            Preconditions.checkArgument(this.spread >= 0.0F, "Spread must be more than or equal to zero");
            Preconditions.checkArgument(this.restingSpread >= 0.0F, "Spread must be more than or equal to zero");
            Preconditions.checkArgument(this.spreadAdsReduction >= 0.0F && this.spreadAdsReduction <= 1.0F, "Spread ADS reduction must be between 0.0 and 1.0");

            JsonObject object = new JsonObject();
            if (this.infiniteAmmo) object.addProperty("infiniteAmmo", true);
            object.addProperty("fireMode", this.fireMode.id().toString());
            if (this.burstAmount != 0) object.addProperty("burstAmount", this.burstAmount);
            if (this.burstCooldown != 0) object.addProperty("burstCooldown", this.burstCooldown);
            object.addProperty("rate", this.rate);
            if (this.fireTimer != 0) object.addProperty("fireTimer", this.fireTimer);
            object.addProperty("gripType", this.gripType.id().toString());
            object.addProperty("baseGripType", this.baseGripType.id().toString());
            if (this.recoilAngle != 0.0F) object.addProperty("recoilAngle", this.recoilAngle);
            if (this.recoilKick != 0.0F) object.addProperty("recoilKick", this.recoilKick);
            if (this.recoilDurationOffset != 0.0F)
                object.addProperty("recoilDurationOffset", this.recoilDurationOffset);
            if (this.recoilAdsReduction != 0.2F) object.addProperty("recoilAdsReduction", this.recoilAdsReduction);
            if (this.projectileAmount != 1) object.addProperty("projectileAmount", this.projectileAmount);
            if (this.alwaysSpread) object.addProperty("alwaysSpread", true);
            if (this.spread != 0.0F) object.addProperty("spread", this.spread);
            if (this.restingSpread != 0.0F) object.addProperty("restingSpread", this.spread);
            if (this.spreadAdsReduction != 0.5F) object.addProperty("spreadAdsReduction", this.spread);
            if (this.meleeDamage != 0.0F) object.addProperty("meleeDamage", this.meleeDamage);
            if (this.meleeCooldownTicks != 15) object.addProperty("meleeCooldownTicks", this.meleeCooldownTicks);
            if (this.meleeReach != 3.0F) object.addProperty("meleeReach", this.meleeReach);
            if (this.energyUse != 0) object.addProperty("energyUse", this.energyUse);
            if (this.beamColor != null && !this.beamColor.isEmpty()) {
                object.addProperty("beamColor", this.beamColor);
            }
            if (this.secondaryBeamColor != null && !this.secondaryBeamColor.isEmpty()) {
                object.addProperty("secondaryBeamColor", this.secondaryBeamColor);
            }
            if (this.enchantedBeamColor != null && !this.enchantedBeamColor.isEmpty()) {
                object.addProperty("enchantedBeamColor", this.enchantedBeamColor);
            }
            if (this.enchantedSecondaryBeamColor != null && !this.enchantedSecondaryBeamColor.isEmpty()) {
                object.addProperty("enchantedSecondaryBeamColor", this.enchantedSecondaryBeamColor);
            }
            if (this.beamAmmoConsumptionDelay != 1000) {
                object.addProperty("beamAmmoConsumptionDelay", this.beamAmmoConsumptionDelay);
            }
            if (this.beamDamageDelay != 300) {
                object.addProperty("beamDamageDelay", this.beamDamageDelay);
            }
            if (this.beamMaxDistance != 50.0) {
                object.addProperty("beamMaxDistance", this.beamMaxDistance);
            }

            if (this.miningSpeed != 1.0F) {
                object.addProperty("miningSpeed", this.miningSpeed);
            }

            if (this.enableMining) {
                object.addProperty("enableMining", true);
            }
            if (!this.hasCameraShake) {
                object.addProperty("hasCameraShake", false);
            }
            if (this.criticalChance != 0.0F) {
                object.addProperty("criticalChance", this.criticalChance);
            }
            if (this.playerKnockBack) {
                object.addProperty("playerKnockBack", true);
            }
            if (this.playerKnockBackStrength != 0.0F) {
                object.addProperty("playerKnockBackStrength", this.playerKnockBackStrength);
            }
            if (this.isRevolver) {
                object.addProperty("isRevolver", true);
            }
            if (this.speedModifier != 1.0F) {
                object.addProperty("speedModifier", this.speedModifier);
            }
            return object;
        }

        /**
         * @return A copy of the general get
         */
        public General copy() {
            General general = new General();
            general.fireMode = this.fireMode;
            general.burstAmount = this.burstAmount;
            general.burstCooldown = this.burstCooldown;
            general.rate = this.rate;
            general.hotBarrelRate = this.hotBarrelRate;
            general.fireTimer = this.fireTimer;
            general.gripType = this.gripType;
            general.baseGripType = this.baseGripType;
            general.recoilAngle = this.recoilAngle;
            general.recoilKick = this.recoilKick;
            general.recoilDurationOffset = this.recoilDurationOffset;
            general.recoilAdsReduction = this.recoilAdsReduction;
            general.projectileAmount = this.projectileAmount;
            general.alwaysSpread = this.alwaysSpread;
            general.spread = this.spread;
            general.restingSpread = this.restingSpread;
            general.spreadAdsReduction = this.spreadAdsReduction;
            general.infiniteAmmo = this.infiniteAmmo;
            general.meleeDamage = this.meleeDamage;
            general.meleeCooldownTicks = this.meleeCooldownTicks;
            general.meleeReach = this.meleeReach;
            general.energyUse = this.energyUse;

            general.beamMaxDistance = this.beamMaxDistance;
            general.beamAmmoConsumptionDelay = this.beamAmmoConsumptionDelay;
            general.beamDamageDelay = this.beamDamageDelay;
            general.beamColor = this.beamColor;
            general.secondaryBeamColor = this.secondaryBeamColor;
            general.enchantedBeamColor = this.enchantedBeamColor;
            general.enchantedSecondaryBeamColor = this.enchantedSecondaryBeamColor;
            general.enableMining = this.enableMining;
            general.miningSpeed = this.miningSpeed;
            general.hasCameraShake = this.hasCameraShake;
            general.criticalChance = this.criticalChance;
            general.playerKnockBack = this.playerKnockBack;
            general.playerKnockBackStrength = this.playerKnockBackStrength;
            general.isRevolver = this.isRevolver;
            general.speedModifier = this.speedModifier;
            return general;
        }
        public float getSpeedModifier() {
            return this.speedModifier;
        }
        public boolean hasPlayerKnockBack() {
            return this.playerKnockBack;
        }
        public float getPlayerKnockBackStrength() {
            return this.playerKnockBackStrength;
        }

        public boolean isRevolver() {
            return this.isRevolver;
        }
        public float getCriticalChance() {return this.criticalChance;}
        public boolean hasCameraShake() {
            return this.hasCameraShake;
        }
        public boolean canMine() {
            return this.enableMining;
        }

        public float getMiningSpeed() {
            return this.miningSpeed;
        }

        public double getBeamMaxDistance() {
            return this.beamMaxDistance;
        }

        public int getBeamAmmoConsumptionDelay() {
            return this.beamAmmoConsumptionDelay;
        }

        public String getEnchantedBeamColor() {
            return this.enchantedBeamColor;
        }

        public String getEnchantedSecondaryBeamColor() {
            return this.enchantedSecondaryBeamColor;
        }

        public int getBeamDamageDelay() {
            return this.beamDamageDelay;
        }

        public int getEnergyUse() {
            return this.energyUse;
        }

        public void setEnergyUse(int energyUse) {
            this.energyUse = energyUse;
        }

        public float getMeleeDamage() {
            return this.meleeDamage;
        }

        public int getMeleeCooldownTicks() {
            return this.meleeCooldownTicks;
        }

        public void setMeleeDamage(float meleeDamage) {
            this.meleeDamage = meleeDamage;
        }
        public float getMeleeReach() {
            return this.meleeReach;
        }

        /**
         * @return The type of grip this weapon uses
         */
        public FireMode getFireMode() {
            return this.fireMode;
        }

        public String getBeamColor() {
            return this.beamColor;
        }


        public String getSecondaryBeamColor() {
            return this.secondaryBeamColor;
        }

        /**
         * @return The fire rate of this weapon in ticks
         */
        public int getRate() {
            return this.rate;
        }

        public int getHotBarrelRate() {
            return this.hotBarrelRate;
        }

        /**
         * @return The timer before firing
         */
        public int getFireTimer() {
            return this.fireTimer;
        }

        /**
         * @return The type of grip this weapon uses
         */
        public GripType getGripType(ItemStack stack) {
            return this.getModifiedGun(stack).determineGripType(stack);
        }

        private Gun getModifiedGun(ItemStack stack) {
            if (stack.isEmpty() || !(stack.getItem() instanceof GunItem item)) {
                return new Gun();
            }

            return item.getModifiedGun(stack);
        }


        /**
         * @return The amount of recoil this gun produces upon firing in degrees
         */
        public float getRecoilAngle() {
            return this.recoilAngle;
        }

        /**
         * @return The amount of kick this gun produces upon firing
         */
        public float getRecoilKick() {
            return this.recoilKick;
        }

        /**
         * @return The duration offset for recoil. This reduces the duration of recoil animation
         */
        public float getRecoilDurationOffset() {
            return this.recoilDurationOffset;
        }

        /**
         * @return The amount of reduction applied when aiming down this weapon's sight
         */
        public float getRecoilAdsReduction() {
            return this.recoilAdsReduction;
        }

        /**
         * @return The amount of projectiles this weapon fires
         */
        public int getProjectileAmount() {
            return this.projectileAmount;
        }

        /**
         * @return If this weapon should always spread it's projectiles according to {@link #getSpread()}
         */
        public boolean isAlwaysSpread() {
            return this.alwaysSpread;
        }

        /**
         * @return The maximum amount of degrees applied to the initial pitch and yaw direction of
         * the fired projectile.
         */
        public float getSpread() {
            return this.spread;
        }

        public boolean isAuto() {
            return this.fireMode == FireMode.AUTOMATIC;
        }

        public boolean getInfiniteAmmo() {
            return this.infiniteAmmo;
        }

        /**
         * @return The resting spread of the gun - using this overrides the alwaysSpread parameter.
         */
        public float getRestingSpread() {
            return this.restingSpread;
        }

        /**
         * @return The amount of spread reduction applied when aiming with this weapon.
         */
        public float getSpreadAdsReduction() {
            return this.spreadAdsReduction;
        }

        public int getBurstAmount() {
            return this.burstAmount;
        }

        public int getBurstCooldown() {
            return this.burstCooldown;
        }

        public GripType getBaseGripType() {
            return this.baseGripType;
        }
    }

    public static int getFireTimer(ItemStack stack) {
        return stack.getOrCreateTag().getInt("FireTimer");
    }

    public static class Reloads implements INBTSerializable<CompoundTag> {
        @Optional
        @Ignored
        private ResourceLocation reloadItem = new ResourceLocation(Reference.MOD_ID, "scrap");
        private int maxAmmo = 30;
        @Ignored
        private ReloadType reloadType = ReloadType.MANUAL;
        private int reloadTimer = 20;
        private int emptyMagTimer = 5;
        private int reloadAmount = 1;
        @Optional
        private boolean infiniteAmmo = false;
        @Optional
        @Ignored
        private ResourceLocation reloadByproduct;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putString("ReloadItem", this.reloadItem.toString());
            tag.putInt("MaxAmmo", this.maxAmmo);
            tag.putString("ReloadType", this.reloadType.id().toString());
            tag.putInt("ReloadTimer", this.reloadTimer);
            tag.putInt("EmptyMagTimer", this.emptyMagTimer);
            tag.putInt("ReloadAmount", this.reloadAmount);
            if (this.reloadByproduct != null) {
                tag.putString("ReloadByproduct", this.reloadByproduct.toString());
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("ReloadItem", Tag.TAG_STRING)) {
                this.reloadItem = new ResourceLocation(tag.getString("ReloadItem"));
            }
            if (tag.contains("MaxAmmo", Tag.TAG_ANY_NUMERIC)) {
                this.maxAmmo = tag.getInt("MaxAmmo");
            }
            if (tag.contains("ReloadType", Tag.TAG_STRING)) {
                this.reloadType = ReloadType.getType(ResourceLocation.tryParse(tag.getString("ReloadType")));
            }
            if (tag.contains("ReloadTimer", Tag.TAG_ANY_NUMERIC)) {
                this.reloadTimer = tag.getInt("ReloadTimer");
            }
            if (tag.contains("EmptyMagTimer", Tag.TAG_ANY_NUMERIC)) {
                this.emptyMagTimer = tag.getInt("EmptyMagTimer");
            }
            if (tag.contains("ReloadAmount", Tag.TAG_ANY_NUMERIC)) {
                this.reloadAmount = tag.getInt("ReloadAmount");
            }
            if (tag.contains("ReloadByproduct", Tag.TAG_STRING)) {
                this.reloadByproduct = new ResourceLocation(tag.getString("ReloadByproduct"));
            }
        }

        public JsonObject toJsonObject() {
            Preconditions.checkArgument(this.maxAmmo > 0, "Max ammo must be more than zero");
            Preconditions.checkArgument(this.reloadTimer >= 0, "Reload timer must be more than or equal to zero");
            Preconditions.checkArgument(this.emptyMagTimer >= 0, "Empty mag additional reload timer must be more than or equal to zero");
            Preconditions.checkArgument(this.reloadAmount >= 1, "Reloading amount must be more than or equal to zero");
            JsonObject object = new JsonObject();
            if (this.reloadItem != null) object.addProperty("reloadItem", this.reloadItem.toString());
            object.addProperty("maxAmmo", this.maxAmmo);
            object.addProperty("reloadType", this.reloadType.id().toString());
            object.addProperty("reloadTimer", this.reloadTimer);
            object.addProperty("emptyMagTimer", this.emptyMagTimer);
            if (this.reloadAmount != 1) object.addProperty("reloadAmount", this.reloadAmount);
            if (this.reloadByproduct != null) {
                object.addProperty("reloadByproduct", this.reloadByproduct.toString());
            }
            return object;
        }

        public Reloads copy() {
            Reloads reloads = new Reloads();
            reloads.reloadItem = this.reloadItem;
            reloads.maxAmmo = this.maxAmmo;
            reloads.reloadType = this.reloadType;
            reloads.reloadTimer = this.reloadTimer;
            reloads.emptyMagTimer = this.emptyMagTimer;
            reloads.reloadAmount = this.reloadAmount;
            reloads.reloadByproduct = this.reloadByproduct;
            return reloads;
        }

        public static boolean hasInfiniteAmmo(ItemStack gunStack) {
            CompoundTag tag = gunStack.getOrCreateTag();
            Gun modifiedGun = ((GunItem) gunStack.getItem()).getModifiedGun(gunStack);
            return tag.getBoolean("IgnoreAmmo") || modifiedGun.getGeneral().getInfiniteAmmo();
        }

        /**
         * @return The registry id of the reload byproduct item
         */
        @Nullable
        public Item getReloadByproduct() {
            return this.reloadByproduct != null ? NeoForgeRegistries.ITEMS.getValue(this.reloadByproduct) : null;
        }

        /**
         * Sets the reload byproduct item
         */
        public void setReloadByproduct(ResourceLocation item) {
            this.reloadByproduct = item;
        }

        /**
         * @return Whether the gun has infinite ammo.
         */
        public boolean getInfiniteAmmo() {
            return this.infiniteAmmo;
        }

        /**
         * @return The registry id of the reload item
         */
        public Item getReloadItem() {
            return NeoForgeRegistries.ITEMS.getValue(this.reloadItem);
        }

        public int getMaxAmmo() {
            return this.maxAmmo;
        }

        public ReloadType getReloadType() {
            return this.reloadType;
        }

        public int getReloadTimer() {
            return this.reloadTimer;
        }

        public int getEmptyMagTimer() {
            return this.emptyMagTimer;
        }

        public int getReloadAmount() {
            return this.reloadAmount;
        }
    }

    public static class Projectile implements INBTSerializable<CompoundTag> {
        public ResourceLocation item;
        public ResourceLocation casingType;
        @Optional
        private boolean ejectsCasing;
        @Optional
        private boolean visible;
        private float damage;
        @Optional
        private ResourceLocation advantage = new ResourceLocation(Reference.MOD_ID, "none");
        private float size;
        private double speed;
        private int life;
        @Optional
        private boolean gravity;
        @Optional
        private boolean damageReduceOverLife;
        @Optional
        private int trailColor = 0xFFD289;
        @Optional
        private double trailLengthMultiplier = 1.0;
        @Optional
        @Nullable
        private ResourceLocation casingParticle;
        @Optional
        private boolean ejectDuringReload;
        @Optional
        private boolean firesArrows = false;
        @Optional
        @Nullable
        private ResourceLocation impactEffect;

        @Optional
        private int impactEffectDuration = 100;

        @Optional
        private int impactEffectAmplifier = 0;
        @Optional
        private float impactEffectChance = 1.0F;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Item", this.item.toString());
            tag.putBoolean("EjectsCasing", this.ejectsCasing);
            tag.putBoolean("Visible", this.visible);
            tag.putFloat("Damage", this.damage);
            tag.putString("Advantage", this.advantage.toString());
            tag.putFloat("Size", this.size);
            tag.putDouble("Speed", this.speed);
            tag.putInt("Life", this.life);
            tag.putBoolean("Gravity", this.gravity);
            tag.putBoolean("DamageReduceOverLife", this.damageReduceOverLife);
            tag.putInt("TrailColor", this.trailColor);
            tag.putDouble("TrailLengthMultiplier", this.trailLengthMultiplier);
            if (this.casingType != null) {
                tag.putString("CasingType", this.casingType.toString());
            }
            if (this.casingParticle != null) {
                tag.putString("CasingParticle", this.casingParticle.toString());
            }
            tag.putBoolean("EjectDuringReload", this.ejectDuringReload);
            tag.putBoolean("FiresArrows", this.firesArrows);
            if (this.impactEffect != null) {
                tag.putString("ImpactEffect", this.impactEffect.toString());
                tag.putInt("ImpactEffectDuration", this.impactEffectDuration);
                tag.putInt("ImpactEffectAmplifier", this.impactEffectAmplifier);
                tag.putFloat("ImpactEffectChance", this.impactEffectChance);
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("Item", Tag.TAG_STRING)) {
                this.item = new ResourceLocation(tag.getString("Item"));
            }
            if (tag.contains("CasingType", Tag.TAG_STRING)) {
                this.casingType = new ResourceLocation(tag.getString("CasingType")); // Deserialize casingType
            }
            if (tag.contains("EjectsCasing", Tag.TAG_ANY_NUMERIC)) {
                this.ejectsCasing = tag.getBoolean("EjectsCasing");
            }
            if (tag.contains("Visible", Tag.TAG_ANY_NUMERIC)) {
                this.visible = tag.getBoolean("Visible");
            }
            if (tag.contains("Damage", Tag.TAG_ANY_NUMERIC)) {
                this.damage = tag.getFloat("Damage");
            }
            if (tag.contains("Advantage", Tag.TAG_STRING)) {
                this.advantage = new ResourceLocation(tag.getString("Advantage"));
            }
            if (tag.contains("Size", Tag.TAG_ANY_NUMERIC)) {
                this.size = tag.getFloat("Size");
            }
            if (tag.contains("Speed", Tag.TAG_ANY_NUMERIC)) {
                this.speed = tag.getDouble("Speed");
            }
            if (tag.contains("Life", Tag.TAG_ANY_NUMERIC)) {
                this.life = tag.getInt("Life");
            }
            if (tag.contains("Gravity", Tag.TAG_ANY_NUMERIC)) {
                this.gravity = tag.getBoolean("Gravity");
            }
            if (tag.contains("DamageReduceOverLife", Tag.TAG_ANY_NUMERIC)) {
                this.damageReduceOverLife = tag.getBoolean("DamageReduceOverLife");
            }
            if (tag.contains("TrailColor", Tag.TAG_ANY_NUMERIC)) {
                this.trailColor = tag.getInt("TrailColor");
            }
            if (tag.contains("TrailLengthMultiplier", Tag.TAG_ANY_NUMERIC)) {
                this.trailLengthMultiplier = tag.getDouble("TrailLengthMultiplier");
            }
            if (tag.contains("CasingParticle", Tag.TAG_STRING)) {
                this.casingParticle = new ResourceLocation(tag.getString("CasingParticle"));
            }
            if (tag.contains("EjectDuringReload", Tag.TAG_ANY_NUMERIC)) {
                this.ejectDuringReload = tag.getBoolean("EjectDuringReload");
            }
            if (tag.contains("FiresArrows", Tag.TAG_ANY_NUMERIC)) {
                this.firesArrows = tag.getBoolean("FiresArrows");
            }
            if (tag.contains("ImpactEffect", Tag.TAG_STRING)) {
                this.impactEffect = new ResourceLocation(tag.getString("ImpactEffect"));
                this.impactEffectDuration = tag.getInt("ImpactEffectDuration");
                this.impactEffectAmplifier = tag.getInt("ImpactEffectAmplifier");
                this.impactEffectChance = tag.getFloat("ImpactEffectChance");
            }
        }

        public JsonObject toJsonObject() {
            Preconditions.checkArgument(this.damage >= 0.0F, "Damage must be more than or equal to zero");
            Preconditions.checkArgument(this.size >= 0.0F, "Projectile size must be more than or equal to zero");
            Preconditions.checkArgument(this.speed >= 0.0, "Projectile speed must be more than or equal to zero");
            Preconditions.checkArgument(this.life > 0, "Projectile life must be more than zero");
            Preconditions.checkArgument(this.trailLengthMultiplier >= 0.0, "Projectile trail length multiplier must be more than or equal to zero");
            JsonObject object = new JsonObject();
            object.addProperty("item", this.item.toString());
            if (this.ejectsCasing) object.addProperty("ejectsCasing", true);
            if (this.visible) object.addProperty("visible", true);
            object.addProperty("damage", this.damage);
            if (this.advantage != null) object.addProperty("advantage", this.advantage.toString());
            object.addProperty("size", this.size);
            object.addProperty("speed", this.speed);
            object.addProperty("life", this.life);
            if (this.gravity) object.addProperty("gravity", true);
            if (this.damageReduceOverLife) object.addProperty("damageReduceOverLife", true);
            if (this.trailColor != 0xFFFF00) object.addProperty("trailColor", this.trailColor);
            if (this.trailLengthMultiplier != 1.0)
                object.addProperty("trailLengthMultiplier", this.trailLengthMultiplier);
            if (this.casingType != null) object.addProperty("casingType", this.casingType.toString());
            if (this.casingParticle != null) object.addProperty("casingParticle", this.casingParticle.toString());
            if (this.ejectDuringReload) object.addProperty("ejectDuringReload", true);
            if (this.firesArrows) object.addProperty("firesArrows", true);
            if (this.impactEffect != null) {
                object.addProperty("impactEffect", this.impactEffect.toString());
                if (this.impactEffectDuration != 100)
                    object.addProperty("impactEffectDuration", this.impactEffectDuration);
                if (this.impactEffectAmplifier != 0)
                    object.addProperty("impactEffectAmplifier", this.impactEffectAmplifier);
                if (this.impactEffectChance != 1.0F)
                    object.addProperty("impactEffectChance", this.impactEffectChance);
            }
            return object;
        }

        public Projectile copy() {
            Projectile projectile = new Projectile();
            projectile.item = this.item;
            projectile.ejectsCasing = this.ejectsCasing;
            projectile.visible = this.visible;
            projectile.damage = this.damage;
            projectile.advantage = this.advantage;
            projectile.size = this.size;
            projectile.speed = this.speed;
            projectile.life = this.life;
            projectile.gravity = this.gravity;
            projectile.damageReduceOverLife = this.damageReduceOverLife;
            projectile.trailColor = this.trailColor;
            projectile.trailLengthMultiplier = this.trailLengthMultiplier;
            projectile.casingType = this.casingType;
            projectile.casingParticle = this.casingParticle;
            projectile.ejectDuringReload = this.ejectDuringReload;
            projectile.firesArrows = this.firesArrows;
            projectile.impactEffect = this.impactEffect;
            projectile.impactEffectDuration = this.impactEffectDuration;
            projectile.impactEffectAmplifier = this.impactEffectAmplifier;
            projectile.impactEffectChance = this.impactEffectChance;
            return projectile;
        }
        @Nullable
        public ResourceLocation getImpactEffect() {
            return this.impactEffect;
        }

        public int getImpactEffectDuration() {
            return this.impactEffectDuration;
        }

        public int getImpactEffectAmplifier() {
            return this.impactEffectAmplifier;
        }
        public float getImpactEffectChance() {
            return this.impactEffectChance;
        }

        /**
         * @return The registry id of the ammo item
         */
        public @org.jetbrains.annotations.Nullable Item getItem() {
            return NeoForgeRegistries.ITEMS.getValue(this.item);
        }

        public boolean firesArrows() {
            return this.firesArrows;
        }

        /**
         * @return If this projectile ejects a casing/shell when fired
         */
        public boolean ejectsCasing() {
            return this.ejectsCasing;
        }

        public void setCasingType(ResourceLocation casingType) {
            this.casingType = casingType;
        }
        public ResourceLocation getCasingType() {
            return this.casingType;
        }
        public boolean ejectDuringReload() {
            return this.ejectDuringReload;
        }
        /**
         * @return If this projectile should be visible when rendering
         */
        public boolean isVisible() {
            return !this.visible;
        }

        /**
         * @return The damage caused by this projectile
         */
        public float getDamage() {
            return this.damage;
        }

        /**
         * @return The damage caused by this projectile
         */
        public ResourceLocation getAdvantage() {
            return this.advantage;
        }

        /**
         * @return The size of the projectile entity bounding box
         */
        public float getSize() {
            return this.size;
        }

        /**
         * @return The speed the projectile moves every tick
         */
        public double getSpeed() {
            return this.speed;
        }

        /**
         * @return The amount of ticks before this projectile is removed
         */
        public int getLife() {
            return this.life;
        }

        /**
         * @return If gravity should be applied to the projectile
         */
        public boolean isGravity() {
            return this.gravity;
        }

        /**
         * @return If the damage should reduce the further the projectile travels
         */
        public boolean isDamageReduceOverLife() {
            return this.damageReduceOverLife;
        }

        /**
         * @return The color of the projectile trail in rgba integer format
         */
        public int getTrailColor() {
            return this.trailColor;
        }

        /**
         * @return The multiplier to change the length of the projectile trail
         */
        public double getTrailLengthMultiplier() {
            return this.trailLengthMultiplier;
        }

        public void setDamage(float v) {
            this.damage = v;
        }

        public float getSpread() {
            return 0.0F;
        }

        public ResourceLocation getCasingParticle() {
            return this.casingParticle;
        }
    }

    public static class Sounds implements INBTSerializable<CompoundTag> {
        @Optional
        @Nullable
        private ResourceLocation fire;
        @Optional
        @Nullable
        private ResourceLocation reload;
        @Optional
        @Nullable
        private ResourceLocation cock;
        @Optional
        @Nullable
        private ResourceLocation silencedFire;
        @Optional
        @Nullable
        private ResourceLocation enchantedFire;
        @Optional
        @Nullable
        private ResourceLocation preFire;
        @Optional
        @Nullable
        private ResourceLocation flyby;
        @Optional
        @Nullable
        private ResourceLocation preReload;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            if (this.fire != null) {
                tag.putString("Fire", this.fire.toString());
            }
            if (this.reload != null) {
                tag.putString("Reloads", this.reload.toString());
            }
            if (this.cock != null) {
                tag.putString("Cock", this.cock.toString());
            }
            if (this.silencedFire != null) {
                tag.putString("SilencedFire", this.silencedFire.toString());
            }
            if (this.enchantedFire != null) {
                tag.putString("EnchantedFire", this.enchantedFire.toString());
            }
            if (this.preFire != null) {
                tag.putString("PreFire", this.preFire.toString());
            }
            if (this.preReload != null) {
                tag.putString("PreReload", this.preReload.toString());
            }
            if (this.flyby != null) {
                tag.putString("Flyby", this.flyby.toString());
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("Fire", Tag.TAG_STRING)) {
                this.fire = this.createSound(tag, "Fire");
            }
            if (tag.contains("Reloads", Tag.TAG_STRING)) {
                this.reload = this.createSound(tag, "Reloads");
            }
            if (tag.contains("Cock", Tag.TAG_STRING)) {
                this.cock = this.createSound(tag, "Cock");
            }
            if (tag.contains("SilencedFire", Tag.TAG_STRING)) {
                this.silencedFire = this.createSound(tag, "SilencedFire");
            }
            if (tag.contains("EnchantedFire", Tag.TAG_STRING)) {
                this.enchantedFire = this.createSound(tag, "EnchantedFire");
            }
            if (tag.contains("PreFire", Tag.TAG_STRING)) {
                this.preFire = this.createSound(tag, "PreFire");
            }
            if (tag.contains("PreReload", Tag.TAG_STRING)) {
                this.preReload = this.createSound(tag, "PreReload");
            }
            if (tag.contains("Flyby", Tag.TAG_STRING)) {
                this.flyby = this.createSound(tag, "Flyby");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.fire != null) {
                object.addProperty("fire", this.fire.toString());
            }
            if (this.reload != null) {
                object.addProperty("reload", this.reload.toString());
            }
            if (this.cock != null) {
                object.addProperty("cock", this.cock.toString());
            }
            if (this.silencedFire != null) {
                object.addProperty("silencedFire", this.silencedFire.toString());
            }
            if (this.enchantedFire != null) {
                object.addProperty("enchantedFire", this.enchantedFire.toString());
            }
            if (this.preFire != null) {
                object.addProperty("preFire", this.preFire.toString());
            }
            if (this.preReload != null) {
                object.addProperty("preReload", this.preReload.toString());
            }
            return object;
        }

        public Sounds copy() {
            Sounds sounds = new Sounds();
            sounds.fire = this.fire;
            sounds.reload = this.reload;
            sounds.cock = this.cock;
            sounds.silencedFire = this.silencedFire;
            sounds.enchantedFire = this.enchantedFire;
            sounds.preFire = this.preFire;
            sounds.preReload = this.preReload;
            sounds.flyby = this.flyby;
            return sounds;
        }

        @Nullable
        private ResourceLocation createSound(CompoundTag tag, String key) {
            String sound = tag.getString(key);
            return sound.isEmpty() ? null : new ResourceLocation(sound);
        }

        /**
         * @return The registry id of the sound event when firing this weapon
         */
        @Nullable
        public ResourceLocation getFire() {
            return this.fire;
        }

        /**
         * @return The registry iid of the sound event when reloading this weapon
         */
        @Nullable
        public ResourceLocation getReload() {
            return this.reload;
        }

        /**
         * @return The registry iid of the sound event when cocking this weapon
         */
        @Nullable
        public ResourceLocation getCock() {
            return this.cock;
        }

        /**
         * @return The registry iid of the sound event when silenced firing this weapon
         */
        @Nullable
        public ResourceLocation getSilencedFire() {
            return this.silencedFire;
        }

        /**
         * @return The registry iid of the sound event when silenced firing this weapon
         */
        @Nullable
        public ResourceLocation getEnchantedFire() {
            return this.enchantedFire;
        }

        /**
         * @return The registry iid of the sound event when preparing to fire this weapon
         */
        @Nullable
        public ResourceLocation getPreFire() {
            return this.preFire;
        }

        @Nullable
        public ResourceLocation getPreReload() {
            return this.preReload;
        }

        /**
         * @return the registry id of the sound event when the projectile flies by a player.
         */
        @Nullable
        public ResourceLocation getFlybySound() {
            return this.flyby;
        }
    }

    public static class Display implements INBTSerializable<CompoundTag> {
        @Optional
        @Nullable
        protected Flash flash;
        @Optional
        @Nullable
        protected BeamOrigin beamOrigin;

        @Nullable
        public Flash getFlash() {
            return this.flash;
        }

        @Optional
        private String muzzleFlashType;

        public String getMuzzleFlashType() {
            return muzzleFlashType;
        }

        public void setMuzzleFlashType(String muzzleFlashType) {
            this.muzzleFlashType = muzzleFlashType;
        }

        @Nullable
        public BeamOrigin getBeamOrigin() {
            return this.beamOrigin;
        }

        public static class BeamOrigin extends Positioned {
            private double horizontalOffset = 0.1;
            private double verticalOffset = -0.1;
            private double forwardOffset = 0.3;
            private double aimHorizontalOffset = 0.0;

            @Override
            public CompoundTag serializeNBT() {
                CompoundTag tag = super.serializeNBT();
                tag.putDouble("HorizontalOffset", this.horizontalOffset);
                tag.putDouble("VerticalOffset", this.verticalOffset);
                tag.putDouble("ForwardOffset", this.forwardOffset);
                tag.putDouble("AimHorizontalOffset", this.aimHorizontalOffset);
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                super.deserializeNBT(tag);
                if (tag.contains("HorizontalOffset", Tag.TAG_ANY_NUMERIC)) {
                    this.horizontalOffset = tag.getDouble("HorizontalOffset");
                }
                if (tag.contains("VerticalOffset", Tag.TAG_ANY_NUMERIC)) {
                    this.verticalOffset = tag.getDouble("VerticalOffset");
                }
                if (tag.contains("ForwardOffset", Tag.TAG_ANY_NUMERIC)) {
                    this.forwardOffset = tag.getDouble("ForwardOffset");
                }
                if (tag.contains("AimHorizontalOffset", Tag.TAG_ANY_NUMERIC)) {
                    this.aimHorizontalOffset = tag.getDouble("AimHorizontalOffset");
                }
            }

            @Override
            public JsonObject toJsonObject() {
                JsonObject object = super.toJsonObject();
                if (this.horizontalOffset != 0.1) {
                    object.addProperty("horizontalOffset", this.horizontalOffset);
                }
                if (this.verticalOffset != -0.1) {
                    object.addProperty("verticalOffset", this.verticalOffset);
                }
                if (this.forwardOffset != 0.3) {
                    object.addProperty("forwardOffset", this.forwardOffset);
                }
                if (this.aimHorizontalOffset != 0.0) {
                    object.addProperty("aimHorizontalOffset", this.aimHorizontalOffset);
                }
                return object;
            }

            public BeamOrigin copy() {
                BeamOrigin origin = new BeamOrigin();
                origin.horizontalOffset = this.horizontalOffset;
                origin.verticalOffset = this.verticalOffset;
                origin.forwardOffset = this.forwardOffset;
                origin.aimHorizontalOffset = this.aimHorizontalOffset;
                origin.xOffset = this.xOffset;
                origin.yOffset = this.yOffset;
                origin.zOffset = this.zOffset;
                return origin;
            }

            public double getHorizontalOffset() {
                return this.horizontalOffset;
            }

            public double getVerticalOffset() {
                return this.verticalOffset;
            }

            public double getForwardOffset() {
                return this.forwardOffset;
            }

            public double getAimHorizontalOffset() {
                return this.aimHorizontalOffset;
            }
        }

        public static class Flash extends Positioned {
            private double size = 0.5;
            private String textureLocation = "muzzle_flash_1";
            private boolean spawnParticles = false;
            private boolean alternateMuzzleFlash = false;
            private Vec3 alternatePosition = Vec3.ZERO;
            private int particleCount = 5;
            private String particleType = "minecraft:flame";
            private double particleSpread = 0.1;
            private double particleRingRadius = 0.0;

            @Override
            public CompoundTag serializeNBT() {
                CompoundTag tag = super.serializeNBT();
                tag.putDouble("Size", this.size);
                tag.putString("TextureLocation", this.textureLocation);
                tag.putBoolean("AlternateMuzzleFlash", this.alternateMuzzleFlash);

                CompoundTag altPos = new CompoundTag();
                altPos.putDouble("X", this.alternatePosition.x);
                altPos.putDouble("Y", this.alternatePosition.y);
                altPos.putDouble("Z", this.alternatePosition.z);
                tag.put("AlternatePosition", altPos);

                tag.putInt("ParticleCount", this.particleCount);
                tag.putString("ParticleType", this.particleType);
                tag.putDouble("ParticleSpread", this.particleSpread);
                tag.putDouble("ParticleRingRadius", this.particleRingRadius);
                tag.putBoolean("SpawnParticles", this.spawnParticles);
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                super.deserializeNBT(tag);
                if (tag.contains("Size", Tag.TAG_ANY_NUMERIC)) {
                    this.size = tag.getDouble("Size");
                }
                if (tag.contains("TextureLocation", Tag.TAG_STRING)) {
                    this.textureLocation = tag.getString("TextureLocation");
                }
                if (tag.contains("AlternateMuzzleFlash", Tag.TAG_ANY_NUMERIC)) {
                    this.alternateMuzzleFlash = tag.getBoolean("AlternateMuzzleFlash");
                }
                if (tag.contains("AlternatePosition", Tag.TAG_COMPOUND)) {
                    CompoundTag altPos = tag.getCompound("AlternatePosition");
                    this.alternatePosition = new Vec3(
                            altPos.getDouble("X"),
                            altPos.getDouble("Y"),
                            altPos.getDouble("Z")
                    );
                }
                if (tag.contains("ParticleCount", Tag.TAG_ANY_NUMERIC)) {
                    this.particleCount = tag.getInt("ParticleCount");
                }
                if (tag.contains("ParticleType", Tag.TAG_STRING)) {
                    this.particleType = tag.getString("ParticleType");
                }
                if (tag.contains("ParticleSpread", Tag.TAG_ANY_NUMERIC)) {
                    this.particleSpread = tag.getDouble("ParticleSpread");
                }
                if (tag.contains("ParticleRingRadius", Tag.TAG_ANY_NUMERIC)) {
                    this.particleRingRadius = tag.getDouble("ParticleRingRadius");
                }
                if (tag.contains("SpawnParticles", Tag.TAG_ANY_NUMERIC)) {
                    this.spawnParticles = tag.getBoolean("SpawnParticles");
                }
            }

            @Override
            public JsonObject toJsonObject() {
                JsonObject object = super.toJsonObject();
                if (this.size != 0.5) {
                    object.addProperty("size", this.size);
                }
                object.addProperty("textureLocation", this.textureLocation);
                if (this.alternateMuzzleFlash) {
                    object.addProperty("alternateMuzzleFlash", true);
                    JsonObject altPos = new JsonObject();
                    altPos.addProperty("x", this.alternatePosition.x);
                    altPos.addProperty("y", this.alternatePosition.y);
                    altPos.addProperty("z", this.alternatePosition.z);
                    object.add("alternatePosition", altPos);
                }
                if (this.particleCount != 5) {
                    object.addProperty("particleCount", this.particleCount);
                }
                if (!this.particleType.equals("minecraft:flame")) {
                    object.addProperty("particleType", this.particleType);
                }
                if (this.particleSpread != 0.1) {
                    object.addProperty("particleSpread", this.particleSpread);
                }
                if (this.particleRingRadius != 0.0) {
                    object.addProperty("particleRingRadius", this.particleRingRadius);
                }
                if (this.spawnParticles) {
                    object.addProperty("spawnParticles", true);
                }
                return object;
            }

            public SimpleParticleType getParticleType() {
                try {
                    ResourceLocation particleLocation = new ResourceLocation(this.particleType);
                    ParticleType<?> registryType = NeoForgeRegistries.PARTICLE_TYPES.getValue(particleLocation);
                    if (registryType instanceof SimpleParticleType simpleType) {
                        return simpleType;
                    }
                } catch (Exception e) {
                    // Fallback to default particle type
                }
                return ParticleTypes.FLAME;
            }

            // Getters
            public boolean shouldSpawnParticles() {
                return this.spawnParticles;
            }

            public double getParticleSpread() {
                return this.particleSpread;
            }

            public double getParticleRingRadius() {
                return this.particleRingRadius;
            }

            public double getSize() {
                return this.size;
            }

            public String getTextureLocation() {
                return this.textureLocation;
            }

            public boolean hasAlternateMuzzleFlash() {
                return this.alternateMuzzleFlash;
            }

            public Vec3 getAlternatePosition() {
                return this.alternatePosition;
            }

            public int getParticleCount() {
                return this.particleCount;
            }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            if (this.flash != null) {
                tag.put("Flash", this.flash.serializeNBT());
            }
            if (this.beamOrigin != null) {
                tag.put("BeamOrigin", this.beamOrigin.serializeNBT());
            }
            if (this.muzzleFlashType != null) {
                tag.putString("MuzzleFlashType", this.muzzleFlashType);
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("Flash", Tag.TAG_COMPOUND)) {
                CompoundTag flashTag = tag.getCompound("Flash");
                if (!flashTag.isEmpty()) {
                    Flash flash = new Flash();
                    flash.deserializeNBT(flashTag);
                    this.flash = flash;
                } else {
                    this.flash = null;
                }
            }
            if (tag.contains("BeamOrigin", Tag.TAG_COMPOUND)) {
                CompoundTag originTag = tag.getCompound("BeamOrigin");
                if (!originTag.isEmpty()) {
                    BeamOrigin origin = new BeamOrigin();
                    origin.deserializeNBT(originTag);
                    this.beamOrigin = origin;
                }
            }
            if (tag.contains("MuzzleFlashType", Tag.TAG_STRING)) {
                this.muzzleFlashType = tag.getString("MuzzleFlashType");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.flash != null) {
                GunJsonUtil.addObjectIfNotEmpty(object, "flash", this.flash.toJsonObject());
            }
            if (this.beamOrigin != null) {
                GunJsonUtil.addObjectIfNotEmpty(object, "beamOrigin", this.beamOrigin.toJsonObject());
            }
            if (this.muzzleFlashType != null) {
                object.addProperty("muzzleFlashType", this.muzzleFlashType);
            }
            return object;
        }

        public Display copy() {
            Display display = new Display();
            if (this.flash != null) {
                display.flash = (Flash) this.flash.copy();
            }
            display.muzzleFlashType = this.muzzleFlashType;
            return display;
        }
    }

    public static class Modules implements INBTSerializable<CompoundTag>, IEditorMenu {
        private transient Zoom cachedZoom;

        @Optional
        @Nullable
        private Zoom zoom;
        private Attachments attachments = new Attachments();

        @Nullable
        public Zoom getZoom() {
            return this.zoom;
        }

        public Attachments getAttachments() {
            return this.attachments;
        }

        @Override
        public Component getEditorLabel() {
            return Component.translatable("Modules");
        }

        @Override
        public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                widgets.add(Pair.of(Component.translatable("Enabled Iron Sights"), () -> new DebugToggle(this.zoom != null, val -> {
                    if (val) {
                        if (this.cachedZoom != null) {
                            this.zoom = this.cachedZoom;
                        } else {
                            this.zoom = new Zoom();
                            this.cachedZoom = this.zoom;
                        }
                    } else {
                        this.cachedZoom = this.zoom;
                        this.zoom = null;
                    }
                })));

                widgets.add(Pair.of(Component.translatable("Adjust Iron Sights"), () -> new DebugButton(Component.translatable(">"), btn -> {
                    if (btn.active && this.zoom != null) {
                        Minecraft.getInstance().setScreen(ClientHandler.createEditorScreen(this.zoom));
                    }
                }, () -> this.zoom != null)));
            });
        }

        public static class Zoom extends Positioned implements IEditorMenu {
            @Optional
            private float fovModifier;

            @Override
            public CompoundTag serializeNBT() {
                CompoundTag tag = super.serializeNBT();
                tag.putFloat("FovModifier", this.fovModifier);
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                super.deserializeNBT(tag);
                if (tag.contains("FovModifier", Tag.TAG_ANY_NUMERIC)) {
                    this.fovModifier = tag.getFloat("FovModifier");
                }
            }

            public JsonObject toJsonObject() {
                JsonObject object = super.toJsonObject();
                object.addProperty("fovModifier", this.fovModifier);
                return object;
            }

            public Zoom copy() {
                Zoom zoom = new Zoom();
                zoom.fovModifier = this.fovModifier;
                zoom.xOffset = this.xOffset;
                zoom.yOffset = this.yOffset;
                zoom.zOffset = this.zOffset;
                return zoom;
            }

            @Override
            public Component getEditorLabel() {
                return Component.translatable("Zoom");
            }

            @Override
            public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    widgets.add(Pair.of(Component.translatable("FOV Modifier"), () -> new DebugSlider(0.0, 1.0, this.fovModifier, 0.01, 3, val -> {
                        this.fovModifier = val.floatValue();
                    })));
                });
            }

            public float getFovModifier() {
                return this.fovModifier;
            }

            public static Builder builder() {
                return new Builder();
            }

            public static class Builder extends AbstractBuilder<Builder> {
            }

            protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends Positioned.AbstractBuilder<T> {
                protected final Zoom zoom;

                protected AbstractBuilder() {
                    this(new Zoom());
                }

                protected AbstractBuilder(Zoom zoom) {
                    super(zoom);
                    this.zoom = zoom;
                }

                public T setFovModifier(float fovModifier) {
                    this.zoom.fovModifier = fovModifier;
                    return this.self();
                }

                @Override
                public Zoom build() {
                    return this.zoom.copy();
                }
            }
        }

        public static class Attachments implements INBTSerializable<CompoundTag> {
            @Optional
            @Nullable
            private ScaledPositioned scope;
            @Optional
            @Nullable
            private ScaledPositioned barrel;
            @Optional
            @Nullable
            private ScaledPositioned stock;
            @Optional
            @Nullable
            private ScaledPositioned underBarrel;

            @Optional
            @Nullable
            private ScaledPositioned magazine;

            @Nullable
            public ScaledPositioned getScope() {
                return this.scope;
            }

            @Nullable
            public ScaledPositioned getBarrel() {
                return this.barrel;
            }

            @Nullable
            public ScaledPositioned getStock() {
                return this.stock;
            }

            @Nullable
            public ScaledPositioned getUnderBarrel() {
                return this.underBarrel;
            }

            @Nullable
            public ScaledPositioned getMagazine() {
                return this.magazine;
            }

            @Override
            public CompoundTag serializeNBT() {
                CompoundTag tag = new CompoundTag();
                if (this.scope != null) {
                    tag.put("Scope", this.scope.serializeNBT());
                }
                if (this.barrel != null) {
                    tag.put("Barrel", this.barrel.serializeNBT());
                }
                if (this.stock != null) {
                    tag.put("Stock", this.stock.serializeNBT());
                }
                if (this.underBarrel != null) {
                    tag.put("UnderBarrel", this.underBarrel.serializeNBT());
                }
                if (this.magazine != null) {
                    tag.put("Magazine", this.magazine.serializeNBT());
                }
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                if (tag.contains("Scope", Tag.TAG_COMPOUND)) {
                    this.scope = this.createScaledPositioned(tag, "Scope");
                }
                if (tag.contains("Barrel", Tag.TAG_COMPOUND)) {
                    this.barrel = this.createScaledPositioned(tag, "Barrel");
                }
                if (tag.contains("Stock", Tag.TAG_COMPOUND)) {
                    this.stock = this.createScaledPositioned(tag, "Stock");
                }
                if (tag.contains("UnderBarrel", Tag.TAG_COMPOUND)) {
                    this.underBarrel = this.createScaledPositioned(tag, "UnderBarrel");
                }
                if (tag.contains("Magazine", Tag.TAG_COMPOUND)) {
                    this.magazine = this.createScaledPositioned(tag, "Magazine");
                }
            }

            public JsonObject toJsonObject() {
                JsonObject object = new JsonObject();
                if (this.scope != null) {
                    object.add("scope", this.scope.toJsonObject());
                }
                if (this.barrel != null) {
                    object.add("barrel", this.barrel.toJsonObject());
                }
                if (this.stock != null) {
                    object.add("stock", this.stock.toJsonObject());
                }
                if (this.underBarrel != null) {
                    object.add("underBarrel", this.underBarrel.toJsonObject());
                }
                if (this.magazine != null) {
                    object.add("magazine", this.magazine.toJsonObject());
                }
                return object;
            }

            public Attachments copy() {
                Attachments attachments = new Attachments();
                if (this.scope != null) {
                    attachments.scope = this.scope.copy();
                }
                if (this.barrel != null) {
                    attachments.barrel = this.barrel.copy();
                }
                if (this.stock != null) {
                    attachments.stock = this.stock.copy();
                }
                if (this.underBarrel != null) {
                    attachments.underBarrel = this.underBarrel.copy();
                }
                if (this.magazine != null) {
                    attachments.magazine = this.magazine.copy();
                }
                return attachments;
            }

            @Nullable
            private ScaledPositioned createScaledPositioned(CompoundTag tag, String key) {
                CompoundTag attachment = tag.getCompound(key);
                return attachment.isEmpty() ? null : new ScaledPositioned(attachment);
            }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            if (this.zoom != null) {
                tag.put("Zoom", this.zoom.serializeNBT());
            }
            tag.put("Attachments", this.attachments.serializeNBT());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("Zoom", Tag.TAG_COMPOUND)) {
                Zoom zoom = new Zoom();
                zoom.deserializeNBT(tag.getCompound("Zoom"));
                this.zoom = zoom;
            }
            if (tag.contains("Attachments", Tag.TAG_COMPOUND)) {
                this.attachments.deserializeNBT(tag.getCompound("Attachments"));
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.zoom != null) {
                object.add("zoom", this.zoom.toJsonObject());
            }
            GunJsonUtil.addObjectIfNotEmpty(object, "attachments", this.attachments.toJsonObject());
            return object;
        }

        public Modules copy() {
            Modules modules = new Modules();
            if (this.zoom != null) {
                modules.zoom = this.zoom.copy();
            }
            modules.attachments = this.attachments.copy();
            return modules;
        }
    }

    public static class Positioned implements INBTSerializable<CompoundTag> {
        @Optional
        protected double xOffset;
        @Optional
        protected double yOffset;
        @Optional
        protected double zOffset;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("XOffset", this.xOffset);
            tag.putDouble("YOffset", this.yOffset);
            tag.putDouble("ZOffset", this.zOffset);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("XOffset", Tag.TAG_ANY_NUMERIC)) {
                this.xOffset = tag.getDouble("XOffset");
            }
            if (tag.contains("YOffset", Tag.TAG_ANY_NUMERIC)) {
                this.yOffset = tag.getDouble("YOffset");
            }
            if (tag.contains("ZOffset", Tag.TAG_ANY_NUMERIC)) {
                this.zOffset = tag.getDouble("ZOffset");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.xOffset != 0) {
                object.addProperty("xOffset", this.xOffset);
            }
            if (this.yOffset != 0) {
                object.addProperty("yOffset", this.yOffset);
            }
            if (this.zOffset != 0) {
                object.addProperty("zOffset", this.zOffset);
            }
            return object;
        }

        public double getXOffset() {
            return this.xOffset;
        }

        public double getYOffset() {
            return this.yOffset;
        }

        public double getZOffset() {
            return this.zOffset;
        }

        public Positioned copy() {
            Positioned positioned = new Positioned();
            positioned.xOffset = this.xOffset;
            positioned.yOffset = this.yOffset;
            positioned.zOffset = this.zOffset;
            return positioned;
        }

        public static class Builder extends AbstractBuilder<Builder> {
        }

        protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends SuperBuilder<Positioned, T> {
            private final Positioned positioned;

            private AbstractBuilder() {
                this(new Positioned());
            }

            protected AbstractBuilder(Positioned positioned) {
                this.positioned = positioned;
            }

            public T setOffset(double xOffset, double yOffset, double zOffset) {
                this.positioned.xOffset = xOffset;
                this.positioned.yOffset = yOffset;
                this.positioned.zOffset = zOffset;
                return this.self();
            }

            public T setXOffset(double xOffset) {
                this.positioned.xOffset = xOffset;
                return this.self();
            }

            public T setYOffset(double yOffset) {
                this.positioned.yOffset = yOffset;
                return this.self();
            }

            public T setZOffset(double zOffset) {
                this.positioned.zOffset = zOffset;
                return this.self();
            }

            @Override
            public Positioned build() {
                return this.positioned.copy();
            }
        }
    }

    public static class ScaledPositioned extends Positioned {
        @Optional
        protected double scale = 1.0;

        public ScaledPositioned() {
        }

        public ScaledPositioned(CompoundTag tag) {
            this.deserializeNBT(tag);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = super.serializeNBT();
            tag.putDouble("Scale", this.scale);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            super.deserializeNBT(tag);
            if (tag.contains("Scale", Tag.TAG_ANY_NUMERIC)) {
                this.scale = tag.getDouble("Scale");
            }
        }

        @Override
        public JsonObject toJsonObject() {
            JsonObject object = super.toJsonObject();
            if (this.scale != 1.0) {
                object.addProperty("scale", this.scale);
            }
            return object;
        }

        public double getScale() {
            return this.scale;
        }

        @Override
        public ScaledPositioned copy() {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.xOffset = this.xOffset;
            positioned.yOffset = this.yOffset;
            positioned.zOffset = this.zOffset;
            positioned.scale = this.scale;
            return positioned;
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("General", this.general.serializeNBT());
        tag.put("Reloads", this.reloads.serializeNBT());
        tag.put("Projectile", this.projectile.serializeNBT());
        tag.put("Sounds", this.sounds.serializeNBT());
        tag.put("Display", this.display.serializeNBT());
        tag.put("Modules", this.modules.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("General", Tag.TAG_COMPOUND)) {
            this.general.deserializeNBT(tag.getCompound("General"));
        }
        if (tag.contains("Reloads", Tag.TAG_COMPOUND)) {
            this.reloads.deserializeNBT(tag.getCompound("Reloads"));
        }
        if (tag.contains("Projectile", Tag.TAG_COMPOUND)) {
            this.projectile.deserializeNBT(tag.getCompound("Projectile"));
        }
        if (tag.contains("Sounds", Tag.TAG_COMPOUND)) {
            this.sounds.deserializeNBT(tag.getCompound("Sounds"));
        }
        if (tag.contains("Display", Tag.TAG_COMPOUND)) {
            this.display.deserializeNBT(tag.getCompound("Display"));
        }
        if (tag.contains("Modules", Tag.TAG_COMPOUND)) {
            this.modules.deserializeNBT(tag.getCompound("Modules"));
        }
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.add("general", this.general.toJsonObject());
        object.add("reloads", this.reloads.toJsonObject());
        object.add("projectile", this.projectile.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "sounds", this.sounds.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "display", this.display.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "modules", this.modules.toJsonObject());
        return object;
    }

    public static Gun create(CompoundTag tag) {
        Gun gun = new Gun();
        gun.deserializeNBT(tag);
        if (tag.contains("MeleeDamage", Tag.TAG_ANY_NUMERIC)) {
            gun.getGeneral().setMeleeDamage(tag.getFloat("MeleeDamage"));
        }

        return gun;
    }

    public Gun copy() {
        Gun gun = new Gun();
        gun.general = this.general.copy();
        gun.reloads = this.reloads.copy();
        gun.projectile = this.projectile.copy();
        gun.sounds = this.sounds.copy();
        gun.display = this.display.copy();
        gun.modules = this.modules.copy();
        return gun;
    }

    public boolean canAttachType(@Nullable IAttachment.Type type) {
        if (this.modules.attachments != null && type != null) {
            return switch (type) {
                case SCOPE -> this.modules.attachments.scope != null;
                case BARREL -> this.modules.attachments.barrel != null;
                case STOCK -> this.modules.attachments.stock != null;
                case UNDER_BARREL -> this.modules.attachments.underBarrel != null;
                case MAGAZINE -> this.modules.attachments.magazine != null;
            };
        }
        return false;
    }

    @Nullable
    public ScaledPositioned getAttachmentPosition(IAttachment.Type type) {
        if (this.modules.attachments != null) {
            return switch (type) {
                case SCOPE -> this.modules.attachments.scope;
                case BARREL -> this.modules.attachments.barrel;
                case STOCK -> this.modules.attachments.stock;
                case UNDER_BARREL -> this.modules.attachments.underBarrel;
                case MAGAZINE -> this.modules.attachments.magazine;
            };
        }
        return null;
    }

    public boolean canAimDownSight() {
        return this.canAttachType(IAttachment.Type.SCOPE) || this.modules.zoom != null;
    }

    public static ItemStack getScopeStack(ItemStack gun) {
        CompoundTag compound = gun.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            if (attachment.contains("Scope", Tag.TAG_COMPOUND)) {
                return ItemStack.of(attachment.getCompound("Scope"));
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean hasAttachmentEquipped(ItemStack stack, Gun gun, IAttachment.Type type) {
        if (!gun.canAttachType(type))
            return false;

        CompoundTag compound = stack.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            return attachment.contains(type.getTagKey(), Tag.TAG_COMPOUND);
        }
        return false;
    }

    //This one is used for SpecialModels' attachment rendering!
    public static boolean hasCustomAttachment(ItemStack stack, IAttachment.Type type, Item customAttachment) {
        CompoundTag compound = stack.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            if (attachment.contains(type.getTagKey(), Tag.TAG_COMPOUND)) {
                ItemStack attachmentStack = ItemStack.of(attachment.getCompound(type.getTagKey()));

                return attachmentStack.getItem() == customAttachment;
            }
        }
        return false;
    }

    public static boolean hasAttachmentEquipped(ItemStack stack, IAttachment.Type type) {
        CompoundTag compound = stack.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            return attachment.contains(type.getTagKey(), Tag.TAG_COMPOUND);
        }
        return false;
    }

    public static ItemStack getAttachment(IAttachment.Type type, ItemStack gun) {
        CompoundTag compound = gun.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            if (attachment.contains(type.getTagKey(), Tag.TAG_COMPOUND)) {
                return ItemStack.of(attachment.getCompound(type.getTagKey()));
            }
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    public static Scope getScope(ItemStack gun) {
        CompoundTag compound = gun.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            if (attachment.contains("Scope", Tag.TAG_COMPOUND)) {
                ItemStack scopeStack = ItemStack.of(attachment.getCompound("Scope"));
                Scope scope = null;
                if (scopeStack.getItem() instanceof ScopeItem scopeItem) {
                    if (ScorchedGuns.isDebugging()) {
                        return Debug.getScope(scopeItem);
                    }
                    scope = scopeItem.getProperties();
                }
                return scope;
            }
        }
        return null;
    }

    public static boolean hasLaserSight(ItemStack gun) {
        return hasAttachmentEquipped(gun, IAttachment.Type.SCOPE) && getAttachment(IAttachment.Type.SCOPE, gun).getItem() instanceof LaserSightItem;
    }

    public static void removeAttachment(ItemStack gun, String attachmentStack) {
        CompoundTag compound = gun.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            if (attachment.contains(attachmentStack, Tag.TAG_COMPOUND)) {
                attachment.remove(attachmentStack);
            }
        }
    }

    public static ItemStack removeScopeStack(ItemStack gun) {
        CompoundTag compound = gun.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            if (attachment.contains("Scope", Tag.TAG_COMPOUND)) {
                attachment.remove("Scope");
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack removeBarrelStack(ItemStack gun) {
        CompoundTag compound = gun.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            if (attachment.contains("Barrel", Tag.TAG_COMPOUND)) {
                attachment.remove("Barrel");
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack removeStockStack(ItemStack gun) {
        CompoundTag compound = gun.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            if (attachment.contains("Stock", Tag.TAG_COMPOUND)) {
                attachment.remove("Stock");
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack removeUnderBarrelStack(ItemStack gun) {
        CompoundTag compound = gun.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            if (attachment.contains("Under_Barrel", Tag.TAG_COMPOUND)) {
                attachment.remove("Under_Barrel");
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack removeMagazineStack(ItemStack gun) {
        CompoundTag compound = gun.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            if (attachment.contains("Magazine", Tag.TAG_COMPOUND)) {
                attachment.remove("Magazine");
            }
        }
        return ItemStack.EMPTY;
    }

    public static float getAdditionalDamage(ItemStack gunStack) {
        CompoundTag tag = gunStack.getOrCreateTag();
        return tag.getFloat("AdditionalDamage");
    }

    public static AmmoContext findAmmo(Player player, Item item) {
        if (player.isCreative()) {
            ItemStack ammo = new ItemStack(item, Integer.MAX_VALUE);
            return new AmmoContext(ammo, null);
        }


        // Check player's main inventory for regular ammo
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isAmmo(stack, item)) {
                return new AmmoContext(stack, player.getInventory());
            }
        }

        // Check ammo pouches in the player's inventory
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof AmmoBoxItem pouch) {
                List<ItemStack> contents = AmmoBoxItem.getContents(itemStack).toList();
                for (ItemStack ammoStack : contents) {
                    if (isAmmo(ammoStack, item)) {
                        return new AmmoContext(ammoStack, null);
                    }
                }
            }
        }
        AtomicReference<AmmoContext> ammoContextRef = new AtomicReference<>(AmmoContext.NONE);
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            IItemHandlerModifiable curios = handler.getEquippedCurios();
            for (int i = 0; i < curios.getSlots(); i++) {
                ItemStack stack = curios.getStackInSlot(i);
                // Check for ammo pouches
                if (stack.getItem() instanceof AmmoBoxItem pouch) {
                    List<ItemStack> contents = AmmoBoxItem.getContents(stack).toList();
                    for (ItemStack ammoStack : contents) {
                        if (isAmmo(ammoStack, item)) {
                            ammoContextRef.set(new AmmoContext(ammoStack, null));
                            return;
                        }
                    }
                }
            }
        });

        return ammoContextRef.get();
    }


    public static boolean isAmmo(ItemStack stack, Item item) {
        return stack != null && stack.getItem() == item;
    }

    public static ItemStack[] findAmmoStack(Player player, Item item) {
        if (player.isCreative()) {
            return new ItemStack[]{new ItemStack(item, Integer.MAX_VALUE)};
        }

        List<ItemStack> ammoStacks = new ArrayList<>();

        // Check player's main inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == item) {
                ammoStacks.add(stack);
            }
        }

        // Check player's main inventory for ammo pouches
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof AmmoBoxItem pouch) {
                List<ItemStack> contents = AmmoBoxItem.getContents(itemStack).toList();
                for (ItemStack ammoStack : contents) {
                    if (ammoStack.getItem() == item) {
                        ammoStacks.add(ammoStack);
                    }
                }
            }
        }

        // Check Curios slots for the CreativeAmmoBoxItem and other ammo
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            IItemHandlerModifiable curios = handler.getEquippedCurios();
            for (int i = 0; i < curios.getSlots(); i++) {
                ItemStack stack = curios.getStackInSlot(i);
                if (stack.getItem() instanceof AmmoBoxItem) {
                    List<ItemStack> contents = AmmoBoxItem.getContents(stack).toList();
                    for (ItemStack ammoStack : contents) {
                        if (ammoStack.getItem() == item) {
                            ammoStacks.add(ammoStack);
                        }
                    }
                }
            }
        });

        return ammoStacks.toArray(new ItemStack[0]);
    }

    public static int getReserveAmmoCount(Player player, Item item) {
        if (player.isCreative()) {
            return Integer.MAX_VALUE;
        }

        AtomicInteger ammoCount = new AtomicInteger();

        // Check player's main inventory for regular ammo
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isAmmo(stack, item)) {
                ammoCount.addAndGet(stack.getCount());
            }
        }

        // Check ammo pouches in the player's inventory
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof AmmoBoxItem pouch) {
                List<ItemStack> contents = AmmoBoxItem.getContents(itemStack).toList();
                for (ItemStack ammoStack : contents) {
                    if (isAmmo(ammoStack, item)) {
                        ammoCount.addAndGet(ammoStack.getCount());
                    }
                }
            }
        }
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            IItemHandlerModifiable curios = handler.getEquippedCurios();
            for (int i = 0; i < curios.getSlots(); i++) {
                ItemStack stack = curios.getStackInSlot(i);
                if (stack.getItem() instanceof AmmoBoxItem pouch) {
                    List<ItemStack> contents = AmmoBoxItem.getContents(stack).toList();
                    for (ItemStack ammoStack : contents) {
                        if (isAmmo(ammoStack, item)) {
                            ammoCount.addAndGet(ammoStack.getCount());
                        }
                    }
                }
            }
        });

        return ammoCount.get();
    }


    public static float getFovModifier(ItemStack stack, Gun modifiedGun) {
        float modifier = 0.0F;
        if (hasAttachmentEquipped(stack, modifiedGun, IAttachment.Type.SCOPE)) {
            Scope scope = Gun.getScope(stack);
            if (scope != null) {
                if (scope.getFovModifier() < 1.0F) {
                    return Mth.clamp(scope.getFovModifier(), 0.01F, 1.0F);
                }
            }
        }
        Modules.Zoom zoom = modifiedGun.getModules().getZoom();
        return zoom != null ? modifier + zoom.getFovModifier() : 0F;
    }

    public static class Builder {
        private final Gun gun;

        private Builder() {
            this.gun = new Gun();
        }

        public static Builder create() {
            return new Builder();
        }

        public Gun build() {
            return this.gun.copy(); //Copy since the builder could be used again
        }

        public Builder setFireMode(FireMode fireMode) {
            this.gun.general.fireMode = fireMode;
            return this;
        }

        public Builder setCasingType(Item item) {
            this.gun.projectile.casingType = NeoForgeRegistries.ITEMS.getKey(item);
            return this;
        }

        public Builder setBurstAmount(int burstAmount) {
            this.gun.general.burstAmount = burstAmount;
            return this;
        }

        public Builder setFireRate(int rate) {
            this.gun.general.rate = rate;
            return this;
        }

        public Builder setFireTimer(int fireTimer) {
            this.gun.general.fireTimer = fireTimer;
            return this;
        }

        public Builder setGripType(GripType gripType) {
            this.gun.general.gripType = gripType;
            return this;
        }

        public Builder setReloadItem(Item item) {
            this.gun.reloads.reloadItem = NeoForgeRegistries.ITEMS.getKey(item);
            return this;
        }

        public Builder setMaxAmmo(int maxAmmo) {
            this.gun.reloads.maxAmmo = maxAmmo;
            return this;
        }

        public Builder setReloadType(ReloadType reloadType) {
            this.gun.reloads.reloadType = reloadType;
            return this;
        }

        public Builder setReloadTimer(int reloadTimer) {
            this.gun.reloads.reloadTimer = reloadTimer;
            return this;
        }

        public Builder setEmptyMagTimer(int emptyMagTimer) {
            this.gun.reloads.emptyMagTimer = emptyMagTimer;
            return this;
        }

        public Builder setReloadAmount(int reloadAmount) {
            this.gun.reloads.reloadAmount = reloadAmount;
            return this;
        }

        public Builder setRecoilAngle(float recoilAngle) {
            this.gun.general.recoilAngle = recoilAngle;
            return this;
        }

        public Builder setRecoilKick(float recoilKick) {
            this.gun.general.recoilKick = recoilKick;
            return this;
        }

        public Builder setRecoilDurationOffset(float recoilDurationOffset) {
            this.gun.general.recoilDurationOffset = recoilDurationOffset;
            return this;
        }

        public Builder setRecoilAdsReduction(float recoilAdsReduction) {
            this.gun.general.recoilAdsReduction = recoilAdsReduction;
            return this;
        }

        public Builder setProjectileAmount(int projectileAmount) {
            this.gun.general.projectileAmount = projectileAmount;
            return this;
        }

        public Builder setAlwaysSpread(boolean alwaysSpread) {
            this.gun.general.alwaysSpread = alwaysSpread;
            return this;
        }

        public Builder setSpread(float spread) {
            this.gun.general.spread = spread;
            return this;
        }

        public Builder setRestingSpread(float restingSpread) {
            this.gun.general.restingSpread = restingSpread;
            return this;
        }

        public Builder setSpreadAdsReduction(float spreadAdsReduction) {
            this.gun.general.spreadAdsReduction = spreadAdsReduction;
            return this;
        }

        public Builder setAmmo(Item item) {
            this.gun.projectile.item = NeoForgeRegistries.ITEMS.getKey(item);
            return this;
        }

        public Builder setEjectsCasing(boolean ejectsCasing) {
            this.gun.projectile.ejectsCasing = ejectsCasing;
            return this;
        }

        public Builder setProjectileVisible(boolean visible) {
            this.gun.projectile.visible = visible;
            return this;
        }

        public Builder setProjectileSize(float size) {
            this.gun.projectile.size = size;
            return this;
        }

        public Builder setProjectileSpeed(double speed) {
            this.gun.projectile.speed = speed;
            return this;
        }

        public Builder setProjectileLife(int life) {
            this.gun.projectile.life = life;
            return this;
        }

        public Builder setProjectileAffectedByGravity(boolean gravity) {
            this.gun.projectile.gravity = gravity;
            return this;
        }

        public Builder setProjectileTrailColor(int trailColor) {
            this.gun.projectile.trailColor = trailColor;
            return this;
        }

        public Builder setProjectileTrailLengthMultiplier(int trailLengthMultiplier) {
            this.gun.projectile.trailLengthMultiplier = trailLengthMultiplier;
            return this;
        }

        public Builder setDamage(float damage) {
            this.gun.projectile.damage = damage;
            return this;
        }

        public Builder setAdvantage(ResourceLocation advantage) {
            this.gun.projectile.advantage = advantage;
            return this;
        }

        public Builder setReduceDamageOverLife(boolean damageReduceOverLife) {
            this.gun.projectile.damageReduceOverLife = damageReduceOverLife;
            return this;
        }

        public Builder setFireSound(SoundEvent sound) {
            this.gun.sounds.fire = NeoForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setReloadSound(SoundEvent sound) {
            this.gun.sounds.reload = NeoForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setCockSound(SoundEvent sound) {
            this.gun.sounds.cock = NeoForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setSilencedFireSound(SoundEvent sound) {
            this.gun.sounds.silencedFire = NeoForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setEnchantedFireSound(SoundEvent sound) {
            this.gun.sounds.enchantedFire = NeoForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setPreFireSound(SoundEvent sound) {
            this.gun.sounds.preFire = NeoForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setMuzzleFlash(double size, double xOffset, double yOffset, double zOffset) {
            Display.Flash flash = new Display.Flash();
            flash.size = size;
            flash.xOffset = xOffset;
            flash.yOffset = yOffset;
            flash.zOffset = zOffset;
            this.gun.display.flash = flash;
            return this;
        }

        public Builder setZoom(float fovModifier, double xOffset, double yOffset, double zOffset) {
            Modules.Zoom zoom = new Modules.Zoom();
            zoom.fovModifier = fovModifier;
            zoom.xOffset = xOffset;
            zoom.yOffset = yOffset;
            zoom.zOffset = zOffset;
            this.gun.modules.zoom = zoom;
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setZoom(Modules.Zoom.Builder builder) {
            this.gun.modules.zoom = builder.build();
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setScope(float scale, double xOffset, double yOffset, double zOffset) {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.scale = scale;
            positioned.xOffset = xOffset;
            positioned.yOffset = yOffset;
            positioned.zOffset = zOffset;
            this.gun.modules.attachments.scope = positioned;
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setBarrel(float scale, double xOffset, double yOffset, double zOffset) {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.scale = scale;
            positioned.xOffset = xOffset;
            positioned.yOffset = yOffset;
            positioned.zOffset = zOffset;
            this.gun.modules.attachments.barrel = positioned;
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setStock(float scale, double xOffset, double yOffset, double zOffset) {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.scale = scale;
            positioned.xOffset = xOffset;
            positioned.yOffset = yOffset;
            positioned.zOffset = zOffset;
            this.gun.modules.attachments.stock = positioned;
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setUnderBarrel(float scale, double xOffset, double yOffset, double zOffset) {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.scale = scale;
            positioned.xOffset = xOffset;
            positioned.yOffset = yOffset;
            positioned.zOffset = zOffset;
            this.gun.modules.attachments.underBarrel = positioned;
            return this;
        }

        public Builder setMagazine(float scale, double xOffset, double yOffset, double zOffset) {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.scale = scale;
            positioned.xOffset = xOffset;
            positioned.yOffset = yOffset;
            positioned.zOffset = zOffset;
            this.gun.modules.attachments.magazine = positioned;
            return this;
        }
    }
}
