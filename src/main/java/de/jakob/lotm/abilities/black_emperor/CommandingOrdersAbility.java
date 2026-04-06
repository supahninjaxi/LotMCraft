package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class CommandingOrdersAbility extends ToggleAbility {

    private static final String COMMAND_ORDER_LAST_TICK_KEY = "lotm_commanding_orders_last_tick";
    private static final long COMMAND_ORDER_COOLDOWN_TICKS = 40;

    private static final int COMMAND_ORDER_RANGE = 25;

    public CommandingOrdersAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("black_emperor", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 3.0f;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        entity.sendSystemMessage(Component.literal("§5Commanding Orders: ON"));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        // No passive aura here.
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        entity.sendSystemMessage(Component.literal("§cCommanding Orders: OFF"));
    }

    /**
     * Chat format:
     *   TargetName command
     *
     * Examples:
     *   Steve kneel
     *   Zombie halt
     */
    public static boolean handleAuthorityChat(LivingEntity caster, String rawMessage) {
        if (caster == null || caster.level().isClientSide) return false;
        if (!(caster.level() instanceof ServerLevel serverLevel)) return false;

        // This only works while Commanding Presence is active.
        if (!CommandingPresenceAbility.isActive(caster.getUUID())) return false;

        String message = rawMessage.trim();
        if (message.isEmpty()) return false;

        String[] parts = message.split("\\s+", 2);
        if (parts.length < 2) return false;

        String targetName = parts[0].trim();
        String commandRaw = parts[1].trim().toLowerCase(Locale.ROOT);

        String command;
        if (commandRaw.startsWith("kneel")) {
            command = "kneel";
        } else if (commandRaw.startsWith("halt") || commandRaw.startsWith("stop")) {
            command = "halt";
        } else if (commandRaw.startsWith("retreat") || commandRaw.startsWith("back")) {
            command = "retreat";
        } else if (commandRaw.startsWith("advance") || commandRaw.startsWith("forward")) {
            command = "advance";
        } else if (commandRaw.startsWith("silence") || commandRaw.startsWith("submit")) {
            command = "silence";
        } else {
            return false;
        }

        long now = serverLevel.getGameTime();
        long lastOrderTick = caster.getPersistentData().getLong(COMMAND_ORDER_LAST_TICK_KEY);
        if (now - lastOrderTick < COMMAND_ORDER_COOLDOWN_TICKS) {
            if (caster instanceof Player player) {
                AbilityUtil.sendActionBar(player,
                        Component.literal("Orders are still settling.").withColor(0xFF5555));
            }
            return true;
        }

        LivingEntity target = findNamedTarget(serverLevel, caster, targetName);
        if (target == null) {
            if (caster instanceof Player player) {
                AbilityUtil.sendActionBar(player,
                        Component.literal("No named target in range.").withColor(0xFF5555));
            }
            return true;
        }

        if (!canBeCommandedBy(caster, target)) {
            if (caster instanceof Player player) {
                AbilityUtil.sendActionBar(player,
                        Component.literal("That target resists the order.").withColor(0xFF5555));
            }
            return true;
        }

        caster.getPersistentData().putLong(COMMAND_ORDER_LAST_TICK_KEY, now);

        int casterSeq = BeyonderData.getSequence(caster);
        applyAuthorityOrder(serverLevel, caster, target, casterSeq, command);

        if (caster instanceof Player player) {
            AbilityUtil.sendActionBar(player,
                    Component.literal("Order issued: " + targetName + " " + command)
                            .withColor(0xAA77FF));
        }

        return true;
    }

    private static LivingEntity findNamedTarget(ServerLevel level, LivingEntity caster, String targetName) {
        String wanted = targetName.toLowerCase(Locale.ROOT);

        List<LivingEntity> nearby = level.getEntitiesOfClass(
                LivingEntity.class,
                caster.getBoundingBox().inflate(COMMAND_ORDER_RANGE),
                target -> target != caster && target.isAlive()
        );

        for (LivingEntity target : nearby) {
            String actual = target.getName().getString().toLowerCase(Locale.ROOT);
            if (actual.equals(wanted)) {
                return target;
            }
        }

        return null;
    }

    private static void applyAuthorityOrder(ServerLevel level, LivingEntity caster, LivingEntity target, int casterSeq, String command) {
        if (target instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }

        Vec3 away = target.position().subtract(caster.position()).normalize();

        switch (command) {
            case "kneel" -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1, false, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 25, 0, false, false, false));
                target.setDeltaMovement(target.getDeltaMovement().scale(0.20D));
                lockHeadDown(target, caster, casterSeq);
            }
            case "halt" -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, false, false));
                target.setDeltaMovement(Vec3.ZERO);
                lockHeadDown(target, caster, casterSeq);

                // This is what makes Zombie halt etc. actually feel like a stop.
                if (target instanceof Mob mob) {
                    mob.setNoAi(true);
                    ServerScheduler.scheduleDelayed(20 * 3, () -> {
                        if (mob.isAlive()) {
                            mob.setNoAi(false);
                        }
                    }, level);
                }
            }
            case "retreat" -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false, false));
                target.setDeltaMovement(
                        away.x * 0.65D,
                        0.12D,
                        away.z * 0.65D
                );
                target.hasImpulse = true;
            }
            case "advance" -> {
                Vec3 toCaster = caster.position().subtract(target.position()).normalize();
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 0, false, false, false));

                if (target instanceof Mob mob) {
                    mob.setNoAi(false);
                    mob.getNavigation().moveTo(caster.getX(), caster.getY(), caster.getZ(), 1.0D);
                } else {
                    target.setDeltaMovement(target.getDeltaMovement().add(
                            toCaster.x * 0.35D,
                            0.04D,
                            toCaster.z * 0.35D
                    ));
                    target.hasImpulse = true;
                }
            }
            case "silence" -> {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0, false, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 35, 0, false, false, false));
                lockHeadDown(target, caster, casterSeq);

                if (BeyonderData.isBeyonder(target)) {
                    DisabledAbilitiesComponent component = target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                    component.disableAbilityUsageForTime("commanding_silence", 20 * 14, target);
                }
            }
        }

        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 0.8, 12);
    }

    private static boolean canBeCommandedBy(LivingEntity caster, LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) return true;

        int casterSeq = BeyonderData.getSequence(caster);
        int targetSeq = BeyonderData.getSequence(target);

        return targetSeq >= casterSeq;
    }

    private static void lockHeadDown(LivingEntity target, LivingEntity caster, int casterSeq) {
        if (!BeyonderData.isBeyonder(target)) {
            target.setXRot(Math.max(target.getXRot(), 40.0f));
            target.xRotO = target.getXRot();
            return;
        }

        int targetSeq = BeyonderData.getSequence(target);
        if (targetSeq < casterSeq + 2) return;

        float forcedPitch = 36.0f + Math.min(18.0f, (targetSeq - casterSeq) * 4.0f);

        target.setXRot(forcedPitch);
        target.xRotO = forcedPitch;
        target.setYRot(target.getYRot());
        target.yRotO = target.getYRot();
        target.yBodyRot = target.getYRot();
        target.yBodyRotO = target.getYRot();
        target.yHeadRot = target.getYRot();
        target.yHeadRotO = target.getYRot();
        target.setDeltaMovement(target.getDeltaMovement().multiply(0.88D, 1.0D, 0.88D));
    }

    public static boolean isActive(UUID uuid) {
        return false;
    }

    @SubscribeEvent
    public static void onPresenceChallenged(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        if (!CommandingPresenceAbility.isActive(victim.getUUID())) return;

        LivingEntity attacker = resolveAttacker(event.getSource());
        if (attacker == null) return;

        if (BeyonderData.isBeyonder(attacker)) {
            int attackerSeq = BeyonderData.getSequence(attacker);
            int casterSeq = BeyonderData.getSequence(victim);

            if (attackerSeq <= casterSeq) {
                attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1, false, false, false));
                attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2, false, false, false));
            }
        }

        Vec3 pushDir = attacker.position().subtract(victim.position()).normalize();
        attacker.setDeltaMovement(pushDir.x * 0.45, 0.28, pushDir.z * 0.45);
        attacker.hasImpulse = true;
    }

    private static LivingEntity resolveAttacker(DamageSource source) {
        var direct = source.getDirectEntity();
        if (direct instanceof LivingEntity le) return le;
        if (direct instanceof Projectile p && p.getOwner() instanceof LivingEntity le) return le;

        var owner = source.getEntity();
        if (owner instanceof LivingEntity le) return le;

        return null;
    }
}
