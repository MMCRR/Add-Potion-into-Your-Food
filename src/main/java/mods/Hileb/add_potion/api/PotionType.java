package mods.Hileb.add_potion.api;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public enum PotionType implements IExtensibleEnum, StringRepresentable {

    DEFAULT((effectAcceptor, level, pos, effects, owner) -> {
        if (effectAcceptor != null) {
            for (MobEffectInstance effect : effects) {
                MobEffect mobEffect = effect.getEffect().value();
                if (mobEffect.isInstantenous()) {
                    mobEffect.applyInstantenousEffect(owner, owner, effectAcceptor, effect.getAmplifier(), 1.0D);
                } else {
                    effectAcceptor.addEffect(effect, owner);
                }
            }
        }
    }),
    SPLASH((effectAcceptor, level, pos, effects, owner) -> {
        level.playSound(null, new BlockPos((int) pos.x(), (int) pos.y(), (int) pos.z()), SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        AABB aabb = AABB.ofSize(pos, 8.0, 4.0, 8.0);
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, aabb);
        for (LivingEntity victim : list) {
            if (victim.isAffectedByPotions()) {
                double dist = victim.distanceToSqr(pos);
                if (dist < 16.0D) {
                    double multiplier = 1.0D - Math.sqrt(dist) / 4.0D;
                    if (victim == effectAcceptor) {
                        multiplier = 1.0D;
                    }

                    for (MobEffectInstance effect : effects) {
                        MobEffect mobEffect = effect.getEffect().value();
                        if (mobEffect.isInstantenous()) {
                            mobEffect.applyInstantenousEffect(owner, owner, victim, effect.getAmplifier(), multiplier);
                        } else {
                            int i = (int) (multiplier * (double) effect.getDuration() + 0.5D);
                            if (i > 20) {
                                victim.addEffect(new MobEffectInstance(effect.getEffect(), i, effect.getAmplifier(), effect.isAmbient(), effect.isVisible()), owner);
                            }
                        }
                    }
                }
            }
        }
    }),
    LINGERING((effectAcceptor, level, pos, effects, owner) -> {
        level.playSound(null, new BlockPos((int) pos.x(), (int) pos.y(), (int) pos.z()), SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        AreaEffectCloud cloud = new AreaEffectCloud(level, pos.x(), pos.y() + 0.25D, pos.z());
        cloud.setOwner(owner);
        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());
        for (MobEffectInstance mobEffectInstance : effects) {
            cloud.addEffect(mobEffectInstance);
        }
        level.addFreshEntity(cloud);
    });

    @Override
    public String getSerializedName() {
        return this.name();
    }

    public void eat(@org.jetbrains.annotations.Nullable LivingEntity effectAcceptor, ServerLevel level, Vec3 pos, PotionList effects, @org.jetbrains.annotations.Nullable LivingEntity owner) {
        delegate.eat(effectAcceptor, level, pos, effects, owner);
    }

    @FunctionalInterface
    public interface EatFunction {
        void eat(@Nullable LivingEntity effectAcceptor, ServerLevel level, Vec3 pos, PotionList effects, @Nullable LivingEntity owner);
    }

    // living, effects, owner
    private final EatFunction delegate;    //Call this on server side only!

    PotionType(EatFunction delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("unused")
    public static PotionType create(String name, EatFunction delegate) {
        throw new IllegalStateException("Enum not extended");
    }

    public static final Codec<PotionType> CODEC = StringRepresentable.fromEnum(PotionType::values);

    public static final StreamCodec<FriendlyByteBuf, PotionType> STREAM_CODEC = StreamCodec.of(
            (o, potionType) -> o.writeUtf(potionType.name()),
            friendlyByteBuf -> PotionType.valueOf(friendlyByteBuf.readUtf()));
}
