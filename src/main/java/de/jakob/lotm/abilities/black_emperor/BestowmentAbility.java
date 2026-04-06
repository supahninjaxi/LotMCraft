package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.AbilityUseEvent;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BestowmentAbility extends SelectableAbility {

    // How long the target is forced into each state.
    // Tweak these if you want the effects to last longer or shorter.
    private static final String ANXIOUS_UNTIL_KEY = "lotm_bestowment_anxious_until";
    private static final String SEALED_UNTIL_KEY = "lotm_bestowment_sealed_until";
    private static final String RASH_UNTIL_KEY = "lotm_bestowment_rash_until";
    private static final String SLUGGISH_NEXT_CAST_UNTIL_KEY = "lotm_bestowment_sluggish_next_cast_until";
    private static final String MONEY_UNTIL_KEY = "lotm_bestowment_money_until";
    private static final String MONEY_TARGET_POS_KEY = "lotm_bestowment_money_target";

    // How far the money-focus scan reaches.
    // Increase this if you want the target to search farther for ores.
    private static final int MONEY_SEARCH_RADIUS = 192;

    // How often the target gets re-steered toward the ore.
    // Lower = smoother tracking. Higher = cheaper.
    private static final int MONEY_UPDATE_INTERVAL_TICKS = 5;

    // Walking speed used by mobs when they are drawn toward the ore.
    private static final double MONEY_MOB_WALK_SPEED = 1.0D;

    // Gentle pull for players and other non-mob entities.
    private static final double MONEY_ENTITY_PULL = 0.045D;

    public BestowmentAbility(String id) {
        super(id, 4.0f);
        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("black_emperor", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 45;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.bestowment.money_focus",
                "ability.lotmcraft.bestowment.rash",
                "ability.lotmcraft.bestowment.sluggish",
                "ability.lotmcraft.bestowment.anxiety",
                "ability.lotmcraft.bestowment.will_to_fight"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 18, 1.5f);
        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.literal("No target in range.").withColor(0xFF5555));
            return;
        }

        if (!canBestow(entity, target)) {
            AbilityUtil.sendActionBar(entity,
                    Component.literal("Target resists Bestowment.").withColor(0xFF5555));
            return;
        }

        switch (abilityIndex) {
            case 0 -> bestowMoneyFocus(serverLevel, entity, target);
            case 1 -> bestowRash(serverLevel, entity, target);
            case 2 -> bestowSluggish(serverLevel, entity, target);
            case 3 -> bestowAnxiety(serverLevel, entity, target);
            case 4 -> bestowWillToFightSeal(serverLevel, entity, target);
            default -> {
            }
        }
    }

    /**
     * Making the target only focused on money.
     * Target ignores everything and moves toward the nearest ore at a walking pace.
     */
    private void bestowMoneyFocus(ServerLevel level, LivingEntity caster, LivingEntity target) {
        int duration = BlackEmperorProgression.scaleTicks(caster, 20 * 8, 20, 20 * 16);
        // Tweak this line if you want the money effect to last longer or shorter.
        long until = level.getGameTime() + duration;

        target.getPersistentData().putLong(MONEY_UNTIL_KEY, until);

        BlockPos ore = findNearestMoneyOre(level, target, MONEY_SEARCH_RADIUS);
        if (ore == null) {
            AbilityUtil.sendActionBar(caster,
                    Component.literal("No ore found nearby.").withColor(0xFF5555));
            target.getPersistentData().remove(MONEY_UNTIL_KEY);
            return;
        }

        target.getPersistentData().putLong(MONEY_TARGET_POS_KEY, ore.asLong());

        RingEffectManager.createRingForAll(target.position(), 2.8f, 20,
                0.70f, 0.38f, 0.88f, 1.0f, 0.16f, 0.8f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 1.1f, 16);

        ServerScheduler.scheduleForDuration(
                0,
                MONEY_UPDATE_INTERVAL_TICKS,
                duration,
                () -> {
                    if (!target.isAlive()) return;

                    long stored = target.getPersistentData().getLong(MONEY_TARGET_POS_KEY);
                    BlockPos currentOre = BlockPos.of(stored);

                    // If the saved ore is gone, re-scan for the nearest one.
                    if (level.getBlockState(currentOre).isAir()) {
                        BlockPos nextOre = findNearestMoneyOre(level, target, MONEY_SEARCH_RADIUS);
                        if (nextOre != null) {
                            currentOre = nextOre;
                            target.getPersistentData().putLong(MONEY_TARGET_POS_KEY, currentOre.asLong());
                        } else {
                            return;
                        }
                    }

                    Vec3 oreCenter = Vec3.atCenterOf(currentOre);
                    Vec3 toOre = oreCenter.subtract(target.position());
                    if (toOre.lengthSqr() < 0.01D) return;

                    Vec3 dir = toOre.normalize();

                    // Mobs walk there normally instead of being yanked around.
                    if (target instanceof Mob mob) {
                        mob.setTarget(null);
                        mob.getNavigation().moveTo(
                                oreCenter.x,
                                oreCenter.y,
                                oreCenter.z,
                                MONEY_MOB_WALK_SPEED
                        );
                    } else {
                        // Players and non-mob entities get a light pull instead of a hard drag.
                        target.setDeltaMovement(target.getDeltaMovement().add(
                                dir.x * MONEY_ENTITY_PULL,
                                dir.y * 0.02D,
                                dir.z * MONEY_ENTITY_PULL
                        ));
                    }

                    if (target instanceof Player player) {
                        AbilityUtil.sendActionBar(player,
                                Component.literal("Money... ore... treasure...").withColor(0xAA77FF));
                    }
                },
                () -> {
                    target.getPersistentData().remove(MONEY_TARGET_POS_KEY);
                    target.getPersistentData().remove(MONEY_UNTIL_KEY);
                },
                level
        );
    }

    /**
     * Turning the target eager and rash.
     * Spams random abilities and adds a direct damage pulse so allies can still be hurt.
     */
    private void bestowRash(ServerLevel level, LivingEntity caster, LivingEntity target) {
        int duration = BlackEmperorProgression.scaleTicks(caster, 20 * 8, 15, 20 * 15);
        // Tweak this line if you want rash mode to last longer or shorter.
        target.getPersistentData().putLong(RASH_UNTIL_KEY, level.getGameTime() + duration);

        RingEffectManager.createRingForAll(target.position(), 2.6f, 18,
                0.76f, 0.18f, 0.92f, 1.0f, 0.15f, 0.9f, level);
        ParticleUtil.createParticleSpirals(level, ModParticles.LIGHTNING.get(),
                target.position().add(0, 0.5, 0),
                0.25, 0.9, 1.4, 0.07, 8, 24, 2, 2);

        ServerScheduler.scheduleForDuration(
                0, 8, duration,
                () -> {
                    if (!target.isAlive()) return;

                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 30, 0, false, false, true));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1, false, false, true));

                    target.setDeltaMovement(target.getDeltaMovement().add(
                            (level.random.nextDouble() - 0.5D) * 0.18D,
                            0.0D,
                            (level.random.nextDouble() - 0.5D) * 0.18D
                    ));

                    triggerRashCast(level, target);

                    // Direct overcast pulse so even ally-safe targeting cannot fully nullify this state.
                    LivingEntity pulseVictim = findNearestLiving(level, target, 12);
                    if (pulseVictim != null && pulseVictim != target) {
                        float pulseDamage = BlackEmperorProgression.scaleFloat(target, 1.5f, 0.4f, 4.0f);
                        pulseVictim.hurt(target.damageSources().indirectMagic(target, target), pulseDamage);
                    }
                },
                () -> target.getPersistentData().remove(RASH_UNTIL_KEY),
                level
        );
    }

    private void triggerRashCast(ServerLevel level, LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) {
            return;
        }

        String pathway = BeyonderData.getPathway(target);
        int sequence = BeyonderData.getSequence(target);

        Random rng = new Random(level.random.nextLong());

        var randomAbility = LOTMCraft.abilityHandler.getRandomAbility(
                pathway,
                sequence,
                rng,
                false,
                List.of(this)
        );

        if (randomAbility != null) {
            randomAbility.useAbility(level, target, true, true, true);
        }
    }

    /**
     * The feeling of sluggishness.
     * Reduces spirituality and delays future casts.
     */
    private void bestowSluggish(ServerLevel level, LivingEntity caster, LivingEntity target) {
        int duration = BlackEmperorProgression.scaleTicks(caster, 20 * 10, 15, 20 * 18);
        // Tweak this line if you want sluggishness to last longer or shorter.
        long until = level.getGameTime() + duration;

        target.getPersistentData().putLong(SLUGGISH_NEXT_CAST_UNTIL_KEY, until);

        RingEffectManager.createRingForAll(target.position(), 2.3f, 18,
                0.56f, 0.12f, 0.72f, 1.0f, 0.16f, 0.8f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 0.9f, 14);

        ServerScheduler.scheduleForDuration(
                0, 20, duration,
                () -> {
                    if (!target.isAlive()) return;
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false, true));
                    BeyonderData.reduceSpirituality(target, BlackEmperorProgression.scaleFloat(target, 0.08f, 0.02f, 0.18f));
                },
                () -> target.getPersistentData().remove(SLUGGISH_NEXT_CAST_UNTIL_KEY),
                level
        );

        AbilityUtil.sendActionBar(caster,
                Component.literal("Sluggishness bestowed.").withColor(0xAA77FF));
    }

    /**
     * Making the target anxious.
     * Uses the mod's existing losing-control effect so sanity drops naturally.
     */
    private void bestowAnxiety(ServerLevel level, LivingEntity caster, LivingEntity target) {
        int duration = BlackEmperorProgression.scaleTicks(caster, 20 * 7, 15, 20 * 10);
        // Tweak this line if you want anxiety to last longer or shorter.
        target.getPersistentData().putLong(ANXIOUS_UNTIL_KEY, level.getGameTime() + duration);

        RingEffectManager.createRingForAll(target.position(), 2.4f, 18,
                0.62f, 0.18f, 0.78f, 1.0f, 0.16f, 0.85f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 1.0f, 16);

        int amplifier = BlackEmperorProgression.isSeq4Plus(caster) ? 3 : 0;
        target.addEffect(new MobEffectInstance(
                ModEffects.LOOSING_CONTROL,
                duration,
                amplifier,
                false,
                true,
                true
        ));

        ServerScheduler.scheduleForDuration(
                0, 20, duration,
                () -> {
                    if (!target.isAlive()) return;
                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 0, false, false, true));
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false, true));
                },
                () -> target.getPersistentData().remove(ANXIOUS_UNTIL_KEY),
                level
        );

        AbilityUtil.sendActionBar(caster,
                Component.literal("Anxiety bestowed.").withColor(0xAA77FF));
    }

    /**
     * Losing the will to fight.
     * Seals Beyonder abilities for a short period.
     */
    private void bestowWillToFightSeal(ServerLevel level, LivingEntity caster, LivingEntity target) {
        int duration = BlackEmperorProgression.scaleTicks(caster, 20 * 6, 20, 20 * 16);
        // Tweak this line if you want the seal to last longer or shorter.
        target.getPersistentData().putLong(SEALED_UNTIL_KEY, level.getGameTime() + duration);

        RingEffectManager.createRingForAll(target.position(), 2.5f, 20,
                0.68f, 0.14f, 0.82f, 1.0f, 0.18f, 0.9f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.LIGHTNING.get(),
                target.position().add(0, 1, 0), 1.0f, 18);

        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1, false, false, true));

        AbilityUtil.sendActionBar(caster,
                Component.literal("Will to fight sealed.").withColor(0xAA77FF));

        ServerScheduler.scheduleDelayed(duration, () ->
                target.getPersistentData().remove(SEALED_UNTIL_KEY), level);
    }

    @SubscribeEvent
    public static void onAbilityUse(AbilityUseEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide) return;

        long now = entity.level().getGameTime();
        long sealedUntil = entity.getPersistentData().getLong(SEALED_UNTIL_KEY);
        if (sealedUntil > now) {
            event.setCanceled(true);
            AbilityUtil.sendActionBar(entity,
                    Component.literal("Your Beyonder abilities are sealed.").withColor(0xFF5555));
            return;
        }

        long slowCastUntil = entity.getPersistentData().getLong(SLUGGISH_NEXT_CAST_UNTIL_KEY);
        if (slowCastUntil > now) {
            event.setCanceled(true);
            entity.getPersistentData().putLong(SLUGGISH_NEXT_CAST_UNTIL_KEY, now + 20);
            AbilityUtil.sendActionBar(entity,
                    Component.literal("You cannot focus long enough to cast.").withColor(0xFF5555));
        }
    }

    private boolean canBestow(LivingEntity caster, LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) return true;

        int casterSeq = BeyonderData.getSequence(caster);
        int targetSeq = BeyonderData.getSequence(target);

        if (targetSeq >= casterSeq) return true;
        if (targetSeq == 0 && casterSeq != 0) return false;

        int diff = casterSeq - targetSeq;
        if (diff == 1) return random.nextFloat() < 0.40f;
        return false;
    }

    private static BlockPos findNearestMoneyOre(Level level, LivingEntity entity, int radius) {
        BlockPos origin = entity.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);

                    if (!isMoneyOre(state)) continue;

                    double dist = pos.distSqr(origin);
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = pos.immutable();
                    }
                }
            }
        }

        return best;
    }

    private static boolean isMoneyOre(BlockState state) {
        return state.is(Blocks.IRON_ORE)
                || state.is(Blocks.DEEPSLATE_IRON_ORE)
                || state.is(Blocks.GOLD_ORE)
                || state.is(Blocks.DEEPSLATE_GOLD_ORE)
                || state.is(Blocks.DIAMOND_ORE)
                || state.is(Blocks.DEEPSLATE_DIAMOND_ORE);
    }

    private static LivingEntity findNearestLiving(Level level, LivingEntity source, int radius) {
        return level.getEntitiesOfClass(
                        LivingEntity.class,
                        source.getBoundingBox().inflate(radius),
                        e -> e != source && e.isAlive()
                ).stream()
                .min((a, b) -> Double.compare(a.distanceToSqr(source), b.distanceToSqr(source)))
                .orElse(null);
    }
}