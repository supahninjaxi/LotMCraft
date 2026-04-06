package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CorrosionAbility extends ToggleAbility {

    // Tracks how many ticks each nearby entity has spent in the aura
    // so corruption deepens the longer they stay.
    private final Map<UUID, Integer> exposureTicks = new HashMap<>();

    // Thresholds (in ticks) for escalating corruption stages
    private static final int STAGE_1_TICKS = 20 * 3;   // 3s  — greedy, distracted
    private static final int STAGE_2_TICKS = 20 * 8;   // 8s  — irrational, retargets
    private static final int STAGE_3_TICKS = 20 * 15;  // 15s — fully corrupted, uses own abilities chaotically

    public CorrosionAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("black_emperor", 6));
    }

    @Override
    public float getSpiritualityCost() {
        // Drain per tick — moderate cost for a powerful aura
        return 2.0f;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        entity.getPersistentData().putBoolean("lotm_corrosion_active", true);
        entity.sendSystemMessage(Component.literal("§5Corrosion: ON"));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        int selfSeq = BeyonderData.getSequence(entity);

        List<LivingEntity> inRange = level.getEntitiesOfClass(
                LivingEntity.class,
                entity.getBoundingBox().inflate(10));

        // Track which UUIDs are still in range this tick so we can decay absent ones
        java.util.Set<UUID> currentlyInRange = new java.util.HashSet<>();

        for (LivingEntity target : inRange) {
            if (target == entity) continue;

            // Stronger Beyonders fully resist Corrosion
            if (BeyonderData.isBeyonder(target)) {
                int targetSeq = BeyonderData.getSequence(target);
                if (targetSeq < selfSeq) continue;
            }

            UUID id = target.getUUID();
            currentlyInRange.add(id);

            int ticks = exposureTicks.getOrDefault(id, 0) + 1;
            exposureTicks.put(id, ticks);

            applyCorruption(serverLevel, entity, target, ticks);
        }

        // Decay exposure for entities that left the aura
        exposureTicks.entrySet().removeIf(entry -> {
            if (!currentlyInRange.contains(entry.getKey())) {
                // Decay at half the rate it built up — leaving doesn't instantly cure
                int remaining = entry.getValue() - 2;
                if (remaining <= 0) return true;
                entry.setValue(remaining);
            }
            return false;
        });
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        entity.getPersistentData().remove("lotm_corrosion_active");
        exposureTicks.clear();
        entity.sendSystemMessage(Component.literal("§cCorrosion: OFF"));
    }

    // ----- Corruption logic -----

    private void applyCorruption(ServerLevel level, LivingEntity caster,
                                 LivingEntity target, int ticks) {

        // Stage 1 — Greed awakens: target is slowed and distracted
        if (ticks >= STAGE_1_TICKS) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false, false));

            // Mobs stop pursuing their current goal and wander briefly
            if (target instanceof Mob mob && level.getGameTime() % 40 == 0) {
                if (level.random.nextFloat() < 0.35f) {
                    mob.getNavigation().stop();
                }
            }

            // Players see a subtle warning that something feels off
            if (target instanceof Player player && ticks == STAGE_1_TICKS) {
                AbilityUtil.sendActionBar(player,
                        Component.literal("You feel an inexplicable greed stirring within you...")
                                .withColor(0x883399));
            }
        }

        // Stage 2 — Irrationality takes hold: mobs retarget randomly, players get confusion
        if (ticks >= STAGE_2_TICKS) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.CONFUSION, 60, 0, false, false, false));

            if (target instanceof Mob mob && level.getGameTime() % 60 == 0) {
                if (level.random.nextFloat() < 0.50f) {
                    // Pick a random nearby entity — could be an ally
                    List<LivingEntity> nearby = level.getEntitiesOfClass(
                            LivingEntity.class,
                            target.getBoundingBox().inflate(12),
                            e -> e != target && e != caster
                    );
                    if (!nearby.isEmpty()) {
                        mob.setTarget(nearby.get(level.random.nextInt(nearby.size())));
                    }
                }
            }

            if (target instanceof Player player && ticks == STAGE_2_TICKS) {
                AbilityUtil.sendActionBar(player,
                        Component.literal("The greed is overwhelming — your thoughts are scattered.")
                                .withColor(0x661188));
            }
        }

        // Stage 3 — Full corruption: Beyonder targets fire abilities chaotically
        if (ticks >= STAGE_3_TICKS) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS, 40, 1, false, false, false));

            // Every 3 seconds, a corrupted Beyonder uses a random ability on
            // the nearest entity (ignoring ally checks — irrational behavior)
            if (BeyonderData.isBeyonder(target) && level.getGameTime() % 60 == 0) {
                if (level.random.nextFloat() < 0.60f) {
                    triggerIrrationalAbility(level, target);
                }
            }

            if (target instanceof Player player && ticks == STAGE_3_TICKS) {
                AbilityUtil.sendActionBar(player,
                        Component.literal("You can no longer control yourself. Darkness consumes you.")
                                .withColor(0x440066));
            }
        }
    }

    /**
     * Forces a corrupted Beyonder target to use one of their own abilities on the
     * nearest entity regardless of ally status — mimicking irrational, greedy behavior.
     */
    private void triggerIrrationalAbility(ServerLevel level, LivingEntity target) {
        String pathway = BeyonderData.getPathway(target);
        int sequence = BeyonderData.getSequence(target);

        java.util.Random rng = new java.util.Random(level.random.nextLong());

        var randomAbility = de.jakob.lotm.LOTMCraft.abilityHandler.getRandomAbility(
                pathway, sequence, rng, false, List.of());

        if (randomAbility != null) {
            // ignoreAllies = true so the corrupted entity can hurt friends
            randomAbility.useAbility(level, target, true, true, true);
        }
    }
}