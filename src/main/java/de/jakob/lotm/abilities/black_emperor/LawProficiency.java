package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;

import static de.jakob.lotm.util.BeyonderData.getSequence;

public class LawProficiency extends ToggleAbility {

    public LawProficiency(String id) {
        super(id);
    }

    @Override
    public float getSpiritualityCost() {
        return 1.5f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("black_emperor", 9);
    }

    @Override
    public boolean hasAbility(LivingEntity entity) {
        // WeaknessDetection supersedes this ability from Seq 6 downward (stronger).
        // Returning false removes it from the ability wheel entirely.
        if (BeyonderData.getSequence(entity) <= 6) return false;
        return super.hasAbility(entity);
    }


    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        if (level.getGameTime() % 20 == 0) {
            ParticleUtil.spawnCircleParticles((ServerLevel) level, ModParticles.BLACK.get(),
                    entity.position().add(0, 1.0, 0), 2.2, 4);
        }

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                entity.getBoundingBox().inflate(6))) {

            if (target == entity) continue;

            int targetSeq = getSequence(target);
            int selfSeq = getSequence(entity);

            if (targetSeq < selfSeq) {
                target.getPersistentData().remove("lotm_law_violated");
                continue;
            }

            boolean armorViolation = target.getArmorValue() < 10f;
            boolean healthViolation = target.getHealth() < target.getMaxHealth() * 0.3f;
            boolean speedViolation = target.getDeltaMovement().length() > 0.25f;

            boolean isViolating = armorViolation || healthViolation || speedViolation;

            if (isViolating) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
                target.getPersistentData().putBoolean("lotm_law_violated", true);
            } else {
                target.getPersistentData().remove("lotm_law_violated");
            }
        }
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.getPersistentData().putBoolean("lotm_law_active", true);
            entity.sendSystemMessage(Component.literal("§6Law Proficiency: ON"));

            ServerLevel serverLevel = (ServerLevel) level;
            RingEffectManager.createRingForAll(entity.position(), 3.2f, 20,
                    0.85f, 0.70f, 0.15f, 1.0f, 0.20f, 0.9f, serverLevel);
            ParticleUtil.spawnSphereParticles(serverLevel, ModParticles.BLACK.get(),
                    entity.position().add(0, 1, 0), 1.4, 16);
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.getPersistentData().remove("lotm_law_active");

            ServerLevel serverLevel = (ServerLevel) level;
            RingEffectManager.createRingForAll(entity.position(), 2.4f, 16,
                    0.62f, 0.50f, 0.10f, 1.0f, 0.16f, 0.65f, serverLevel);
            ParticleUtil.spawnSphereParticles(serverLevel, ModParticles.BLACK.get(),
                    entity.position().add(0, 1, 0), 1.0, 12);

            for (LivingEntity target : level.getEntitiesOfClass(
                    LivingEntity.class,
                    entity.getBoundingBox().inflate(6))) {
                target.getPersistentData().remove("lotm_law_violated");
            }

            entity.sendSystemMessage(Component.literal("§cLaw Proficiency: OFF"));
        }
    }
}





