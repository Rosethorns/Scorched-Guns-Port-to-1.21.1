package top.ribs.scguns.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.Holder;
import top.ribs.scguns.Reference;
import top.ribs.scguns.particles.BulletHoleData;
import top.ribs.scguns.particles.TrailData;

/**
 * Author: MrCrayfish
 */
public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(NeoForgeRegistries.PARTICLE_TYPES, Reference.MOD_ID);

    public static final Holder<ParticleType<BulletHoleData>> BULLET_HOLE = REGISTER.register("bullet_hole", () -> new ParticleType<>(false, BulletHoleData.DESERIALIZER) {
        @Override
        public Codec<BulletHoleData> codec() {
            return BulletHoleData.CODEC;
        }
    });

    public static final Holder<SimpleParticleType> BLOOD = REGISTER.register("blood", () -> new SimpleParticleType(true));
    public static final Holder<ParticleType<TrailData>> TRAIL = REGISTER.register("trail", () -> new ParticleType<>(false, TrailData.DESERIALIZER) {
        @Override
        public Codec<TrailData> codec() {
            return TrailData.CODEC;
        }
    });
    public static final Holder<SimpleParticleType> COPPER_CASING_PARTICLE = REGISTER.register("copper_casing", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> IRON_CASING_PARTICLE = REGISTER.register("iron_casing", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> DIAMOND_STEEL_CASING_PARTICLE = REGISTER.register("diamond_steel_casing", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> SHULK_CASING_PARTICLE = REGISTER.register("shulk_casing", () -> new SimpleParticleType(true));

    public static final Holder<SimpleParticleType>BRASS_CASING_PARTICLE = REGISTER.register("brass_casing", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> SHELL_PARTICLE = REGISTER.register("shell", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> BEARPACK_PARTICLE = REGISTER.register("bearpack_shell", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> ROCKET_TRAIL = REGISTER.register("rocket_trail", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> SONIC_BLAST = REGISTER.register("sonic_blast", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> GREEN_FLAME = REGISTER.register("green_flame", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> PLASMA_RING = REGISTER.register("plasma_ring", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> SULFUR_SMOKE = REGISTER.register("sulfur_smoke", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> SULFUR_DUST = REGISTER.register("sulfur_dust", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> PLASMA_EXPLOSION = REGISTER.register("plasma_explosion", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> RAMROD_IMPACT = REGISTER.register("ramrod_impact", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> BEOWULF_IMPACT = REGISTER.register("beowulf_impact", () -> new SimpleParticleType(true));

    public static final Holder<SimpleParticleType> TURRET_MUZZLE_FLASH = REGISTER.register("turret_muzzle_flash", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> LASER = REGISTER.register("laser", () -> new SimpleParticleType(true));
    public static final Holder<SimpleParticleType> SMALL_LASER = REGISTER.register("small_laser", () -> new SimpleParticleType(true));
}
