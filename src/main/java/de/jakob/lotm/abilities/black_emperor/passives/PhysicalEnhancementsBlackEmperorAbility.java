package de.jakob.lotm.abilities.black_emperor.passives;

import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.util.BeyonderData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicalEnhancementsBlackEmperorAbility extends PhysicalEnhancementsAbility {

    public PhysicalEnhancementsBlackEmperorAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "black_emperor", 9
        ));
    }

    @Override
    public List<PhysicalEnhancement> getEnhancements() {
        return List.of();
    }

    @Override
    protected List<PhysicalEnhancement> getEnhancementsForSequence(int sequenceLevel) {
        return switch (sequenceLevel) {
            case 9 -> List.of(
                    new PhysicalEnhancement(EnhancementType.SPEED, 1),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 1)
            );

            case 8 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 7),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 5)
            );

            case 7 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 6),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 5)
            );

            case 6 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 8),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 6)
            );

            case 5 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 10),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 8)
            );

            case 4 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 16),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 13)
            );

            case 3 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 4),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 18),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 5)
            );

            case 2 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 5),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 24),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 6)
            );

            case 1 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 4),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 30),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 18)
            );

            case 0 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 6),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 5),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 36),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 8)
            );

            default -> List.of();
        };
    }

    @Override
    protected int getCurrentSequenceLevel(net.minecraft.world.entity.LivingEntity entity) {
        return BeyonderData.getSequence(entity);
    }
}