package top.ribs.scguns.entity.projectile;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import top.ribs.scguns.block.AutoTurretBlock;
import top.ribs.scguns.block.BasicTurretBlock;
import top.ribs.scguns.block.EnemyTurretBlock;
import top.ribs.scguns.block.ShotgunTurretBlock;
import top.ribs.scguns.blockentity.AutoTurretBlockEntity;
import top.ribs.scguns.blockentity.BasicTurretBlockEntity;
import top.ribs.scguns.blockentity.EnemyTurretBlockEntity;
import top.ribs.scguns.blockentity.ShotgunTurretBlockEntity;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBlood;
import top.ribs.scguns.util.GunEnchantmentHelper;

import java.util.List;

public class LightningProjectileEntity extends ProjectileEntity {
    private static final int MAX_BOUNCES = 3;
    private static final double BOUNCE_RANGE = 10.0;
    private static final float HEADSHOT_EFFECT_DURATION_MULTIPLIER = 1.5f;
    private static final float BOUNCE_EFFECT_REDUCTION = 0.55f;
    private int bouncesLeft;
    private float currentDamage;

    public LightningProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.bouncesLeft = MAX_BOUNCES;
        this.currentDamage = this.getDamage();
    }

    public LightningProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.bouncesLeft = MAX_BOUNCES;
        this.currentDamage = this.getDamage();
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        livingEntity.hurt(ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, (LivingEntity) this.getOwner()), currentDamage);
        Vec3 entityPosition = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight() * 0.5, entity.getZ());
        spawnLightningArc(this.position(), entityPosition);

        if (entity instanceof LivingEntity) {
            ResourceLocation effectLocation = this.getProjectile().getImpactEffect();
            if (effectLocation != null) {
                float effectChance = this.getProjectile().getImpactEffectChance();
                if (headshot) {
                    effectChance = Math.min(1.0f, effectChance * 1.25f);
                }

                float bounceChanceMultiplier = (float)Math.pow(BOUNCE_EFFECT_REDUCTION, MAX_BOUNCES - bouncesLeft);
                effectChance *= bounceChanceMultiplier;

                if (this.random.nextFloat() < effectChance) {
                    MobEffect effect = NeoForgeRegistries.MOB_EFFECTS.getValue(effectLocation);
                    if (effect != null) {
                        int duration = this.getProjectile().getImpactEffectDuration();
                        if (headshot) {
                            duration = (int)(duration * HEADSHOT_EFFECT_DURATION_MULTIPLIER);
                        }
                        float bounceMultiplier = (float)Math.pow(BOUNCE_EFFECT_REDUCTION, MAX_BOUNCES - bouncesLeft);
                        duration = (int)(duration * bounceMultiplier);
                        int amplifier = Math.max(0, this.getProjectile().getImpactEffectAmplifier() - (MAX_BOUNCES - bouncesLeft));

                        livingEntity.addEffect(new MobEffectInstance(
                                effect,
                                duration,
                                amplifier
                        ));
                    }
                }
            }

            GunEnchantmentHelper.applyElementalPopEffect(this.getWeapon(), livingEntity);
        }

        if (bouncesLeft > 0) {
            bouncesLeft--;
            currentDamage *= 0.75F;
            LivingEntity nextTarget = findNextTarget(entity);
            if (nextTarget != null) {
                scheduleBounce(nextTarget, entityPosition);
            } else {
                this.discard();
            }
        } else {
            this.discard();
        }

        PacketHandler.getPlayChannel().sendToTracking(() -> entity, new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z, entity.getType()));
    }

    private void scheduleBounce(LivingEntity nextTarget, Vec3 previousPosition) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> bounceToNextTarget(nextTarget, previousPosition));
        }
    }

    private void bounceToNextTarget(LivingEntity nextTarget, Vec3 previousPosition) {
        Vec3 direction = nextTarget.position().subtract(this.position()).normalize();
        this.setDeltaMovement(direction.scale(1.5));
        this.setPos(nextTarget.getX(), nextTarget.getY() + nextTarget.getEyeHeight() * 0.5, nextTarget.getZ()); // Adjust to chest height
        Vec3 nextTargetPosition = new Vec3(nextTarget.getX(), nextTarget.getY() + nextTarget.getEyeHeight() * 0.5, nextTarget.getZ());
        spawnLightningArc(previousPosition, nextTargetPosition);
        this.onHitEntity(nextTarget, nextTargetPosition, this.position(), nextTargetPosition, false);
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        if (state.getBlock() instanceof AutoTurretBlock) {
            BlockEntity blockEntity = level().getBlockEntity(pos);
            if (blockEntity instanceof AutoTurretBlockEntity turret) {
                turret.onHitByLightningProjectile();
            }
        }
        if (state.getBlock() instanceof BasicTurretBlock) {
            BlockEntity blockEntity = level().getBlockEntity(pos);
            if (blockEntity instanceof BasicTurretBlockEntity turret) {
                turret.onHitByLightningProjectile();
            }
        }
        if (state.getBlock() instanceof ShotgunTurretBlock) {
            BlockEntity blockEntity = level().getBlockEntity(pos);
            if (blockEntity instanceof ShotgunTurretBlockEntity turret) {
                turret.onHitByLightningProjectile();
            }
        }
        if (state.getBlock() instanceof EnemyTurretBlock) {
            BlockEntity blockEntity = level().getBlockEntity(pos);
            if (blockEntity instanceof EnemyTurretBlockEntity turret) {
                turret.onHitByLightningProjectile();
            }
        }
        spawnLightningParticles(new Vec3(x, y + 0.1, z));
        this.discard();
    }



    @Override
    public void onExpired() {
        spawnLightningParticles(new Vec3(this.getX(), this.getY() + 0.1, this.getZ()));
    }

    private void spawnLightningArc(Vec3 start, Vec3 end) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            Vec3 direction = end.subtract(start);
            double distance = direction.length();
            direction = direction.normalize();
            double stepSize = 0.1;
            for (double d = 0; d < distance; d += stepSize) {
                Vec3 particlePos = start.add(direction.scale(d));
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
            }
        }
    }

    private void spawnLightningParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            int particleCount = 20;
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, position.x, position.y, position.z, particleCount, 0, 0, 0, 0.1);
        }
    }

    private LivingEntity findNextTarget(Entity currentTarget) {
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, currentTarget.getBoundingBox().inflate(BOUNCE_RANGE)); // Adjust range as needed
        nearbyEntities.removeIf(entity -> entity == this.getOwner() || entity == currentTarget || entity == this.getShooter());

        if (!nearbyEntities.isEmpty()) {
            return nearbyEntities.get(0);
        }
        return null;
    }

    @Override
    public void tick() {
        if (this.level().isClientSide || this.isRemoved()) {
            return;
        }

        try {
            super.tick();
        } catch (Exception e) {
            System.err.println("Exception occurred during tick: " + e.getMessage());
            e.printStackTrace();
            this.discard();
        }
    }
}
