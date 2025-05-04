package top.ribs.scguns.init;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.Holder;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.block.PrimedNitroKeg;
import top.ribs.scguns.entity.block.PrimedPowderKeg;
import top.ribs.scguns.entity.monster.*;
import top.ribs.scguns.entity.projectile.BrassBoltEntity;
import top.ribs.scguns.entity.projectile.*;
import top.ribs.scguns.entity.projectile.turret.TurretProjectileEntity;
import top.ribs.scguns.entity.throwable.*;

import java.util.function.BiFunction;

/**
 * Author: MrCrayfish
 */
public class ModEntities
{
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(NeoForgeRegistries.ENTITY_TYPES, Reference.MOD_ID);
    public static final Holder<EntityType<PrimedPowderKeg>> PRIMED_POWDER_KEG = REGISTER.register("primed_powder_keg",
            () -> EntityType.Builder.<PrimedPowderKeg>of(PrimedPowderKeg::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("primed_powder_keg"));
    public static final Holder<EntityType<PrimedNitroKeg>> PRIMED_NITRO_KEG = REGISTER.register("primed_nitro_keg",
            () -> EntityType.Builder.<PrimedNitroKeg>of(PrimedNitroKeg::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("primed_nitro_keg"));
    public static final Holder<EntityType<TurretProjectileEntity>> TURRET_PROJECTILE = REGISTER.register("basic_turret", () ->
            EntityType.Builder.<TurretProjectileEntity>of(TurretProjectileEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).build("basic_turret"));

    public static final Holder<EntityType<ProjectileEntity>> PROJECTILE = registerProjectile("projectile", ProjectileEntity::new);
    public static final Holder<EntityType<BearPackShellProjectileEntity>> BEARPACK_SHELL_PROJECTILE = registerBasic("bearpack_shell_projectile", BearPackShellProjectileEntity::new);
    public static final Holder<EntityType<OsborneSlugProjectileEntity>> OSBORNE_SLUG_PROJECTILE = registerBasic("osborne_slug_projectile", OsborneSlugProjectileEntity::new);
    public static final Holder<EntityType<PlasmaProjectileEntity>> PLASMA_PROJECTILE = registerBasic("plasma_projectile", PlasmaProjectileEntity::new);
    public static final Holder<EntityType<RamrodProjectileEntity>> RAMROD_PROJECTILE = registerBasic("ramrod_projectile", RamrodProjectileEntity::new);
    public static final Holder<EntityType<HogRoundProjectileEntity>> HOG_ROUND_PROJECTILE = registerBasic("hog_round_projectile", HogRoundProjectileEntity::new);
    public static final Holder<EntityType<BeowulfProjectileEntity>> BEOWULF_PROJECTILE = registerBasic("beowulf_projectile", BeowulfProjectileEntity::new);
    public static final Holder<EntityType<BlazeRodProjectileEntity>> BLAZE_ROD_PROJECTILE = registerBasic("blaze_rod_projectile", BlazeRodProjectileEntity::new);
    public static final Holder<EntityType<BasicBulletProjectileEntity>> BASIC_BULLET_PROJECTILE = registerBasic("basic_bullet_projectile", BasicBulletProjectileEntity::new);
    public static final Holder<EntityType<HardenedBulletProjectileEntity>> HARDENED_BULLET_PROJECTILE = registerBasic("hardened_bullet_projectile", HardenedBulletProjectileEntity::new);
    public static final Holder<EntityType<BuckshotProjectileEntity>> BUCKSHOT_PROJECTILE = registerBasic("buckshot_projectile", BuckshotProjectileEntity::new);
    public static final Holder<EntityType<FireRoundEntity>> FIRE_ROUND_PROJECTILE = registerBasic("fire_round_projectile", FireRoundEntity::new);
    public static final Holder<EntityType<GrenadeEntity>> GRENADE = registerBasic("grenade", GrenadeEntity::new);
    public static final Holder<EntityType<RocketEntity>> ROCKET = registerBasic("rocket", RocketEntity::new);
    public static final Holder<EntityType<MicroJetEntity>> MICROJET = registerBasic("microjet", MicroJetEntity::new);
    public static final Holder<EntityType<ShulkshotProjectileEntity>> SHULKSHOT = registerBasic("shulkshot_projectile", ShulkshotProjectileEntity::new);
    public static final Holder<EntityType<SculkCellEntity>> SCULK_CELL = registerBasic("sculk_cell", SculkCellEntity::new);
    public static final Holder<EntityType<SyringeProjectileEntity>> SYRINGE_PROJECTILE = registerBasic("syringe_projectile", SyringeProjectileEntity::new);
    public static final Holder<EntityType<KrahgRoundProjectileEntity>> KRAHG_ROUND_PROJECTILE = registerBasic("krahg_round_projectile", KrahgRoundProjectileEntity::new);
    public static final Holder<EntityType<AdvancedRoundProjectileEntity>> ADVANCED_ROUND_PROJECTILE = registerBasic("advanced_round_projectile", AdvancedRoundProjectileEntity::new);
    public static final Holder<EntityType<GibbsRoundProjectileEntity>> GIBBS_ROUND_PROJECTILE = registerBasic("gibbs_round_projectile", GibbsRoundProjectileEntity::new);
    public static final Holder<EntityType<ThrowableGrenadeEntity>> THROWABLE_GRENADE = registerBasic("throwable_grenade", ThrowableGrenadeEntity::new);
    public static final Holder<EntityType<ThrowableStunGrenadeEntity>> THROWABLE_STUN_GRENADE = registerBasic("throwable_stun_grenade", ThrowableStunGrenadeEntity::new);
    public static final Holder<EntityType<ThrowableMolotovCocktailEntity>> THROWABLE_MOLOTOV_COCKTAIL = registerBasic("throwable_molotov_cocktail", ThrowableMolotovCocktailEntity::new);
    public static final Holder<EntityType<ThrowableGasGrenadeEntity>> THROWABLE_GAS_GRENADE = registerBasic("throwable_gas_grenade", ThrowableGasGrenadeEntity::new);

    public static final Holder<EntityType<ThrowableChokeBombEntity>> THROWABLE_CHOKE_BOMB = registerBasic("throwable_choke_bomb", ThrowableChokeBombEntity::new);
    public static final Holder<EntityType<ThrowableSwarmBombEntity>> THROWABLE_SWARM_BOMB = registerBasic("throwable_swarm_bomb", ThrowableSwarmBombEntity::new);

    /* Mobs */
    public static final Holder<EntityType<CogMinionEntity>> COG_MINION = REGISTER.register("cog_minion", () -> EntityType.Builder.of(CogMinionEntity::new, MobCategory.MONSTER).sized(0.8F, 2.0F).build("cog_minion"));
    public static final Holder<EntityType<CogKnightEntity>> COG_KNIGHT = REGISTER.register("cog_knight", () -> EntityType.Builder.of(CogKnightEntity::new, MobCategory.MONSTER).sized(0.8F, 2.2F).build("cog_knight"));
    public static final Holder<EntityType<SkyCarrierEntity>> SKY_CARRIER = REGISTER.register("sky_carrier", () -> EntityType.Builder.of(SkyCarrierEntity::new, MobCategory.MONSTER).sized(1.4F, 1.7F).build("sky_carrier"));
    public static final Holder<EntityType<HiveEntity>> HIVE = REGISTER.register("hive", () -> EntityType.Builder.of(HiveEntity::new, MobCategory.MONSTER).sized(0.8F, 2.0F).build("hive"));
    public static final Holder<EntityType<SwarmEntity>> SWARM = REGISTER.register("swarm", () -> EntityType.Builder.of(SwarmEntity::new, MobCategory.MONSTER).sized(0.8F, 2.0F).build("swarm"));
    public static final Holder<EntityType<RedcoatEntity>> REDCOAT = REGISTER.register("redcoat", () -> EntityType.Builder.of(RedcoatEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F).build("redcoat"));
    public static final Holder<EntityType<SupplyScampEntity>> SUPPLY_SCAMP = REGISTER.register("supply_scamp", () -> EntityType.Builder.of(SupplyScampEntity::new, MobCategory.CREATURE).sized(1.0F, 1.3F).build("supply_scamp"));
    public static final Holder<EntityType<DissidentEntity>> DISSIDENT = REGISTER.register("dissident", () -> EntityType.Builder.of(DissidentEntity::new, MobCategory.MONSTER).sized(1.4F, 1.7F).build("dissident"));
    public static final Holder<EntityType<HornlinEntity>> HORNLIN = REGISTER.register("hornlin", () -> EntityType.Builder.of(HornlinEntity::new, MobCategory.MONSTER).sized(1.4F, 1.7F).build("hornlin"));
    public static final Holder<EntityType<ZombifiedHornlinEntity>> ZOMBIFIED_HORNLIN = REGISTER.register("zombified_hornlin", () -> EntityType.Builder.of(ZombifiedHornlinEntity::new, MobCategory.MONSTER).sized(1.4F, 1.7F).build("zombified_hornlin"));
    public static final Holder<EntityType<BlundererEntity>> BLUNDERER = REGISTER.register("blunderer", () -> EntityType.Builder.of(BlundererEntity::new, MobCategory.MONSTER).sized(1.4F, 1.7F).build("blunderer"));
    public static final Holder<EntityType<BrassBoltEntity>> BRASS_BOLT = REGISTER.register("brass_bolt", () ->
            EntityType.Builder.<BrassBoltEntity>of(BrassBoltEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .setTrackingRange(64)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("brass_bolt"));

    private static <T extends Entity> Holder<EntityType<T>> registerBasic(String id, BiFunction<EntityType<T>, Level, T> function)
    {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .setTrackingRange(100)
                .setUpdateInterval(1)
                .noSummon()
                .fireImmune()
                .noSave()
                .setShouldReceiveVelocityUpdates(true).build(id));
    }

    /**
     * Entity registration that prevents the entity from being sent and tracked by clients. Projectiles
     * are rendered separately from Minecraft's entity rendering system and their logic is handled
     * exclusively by the server, why send them to the client. Projectiles also have very short time
     * in the world and are spawned many times a tick. There is no reason to send unnecessary packets
     * when it can be avoided to drastically improve the performance of the game.
     *
     * @param id       the id of the projectile
     * @param function the factory to spawn the projectile for the server
     * @param <T>      an entity that is a projectile entity
     * @return A registry object containing the new entity type
     */
    private static <T extends ProjectileEntity> Holder<EntityType<T>> registerProjectile(String id, BiFunction<EntityType<T>, Level, T> function)
    {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .setTrackingRange(0)
                .noSummon()
                .fireImmune()
                .noSave()
                .setShouldReceiveVelocityUpdates(false)
                .setCustomClientFactory((spawnEntity, world) -> null)
                .build(id));
    }
}
