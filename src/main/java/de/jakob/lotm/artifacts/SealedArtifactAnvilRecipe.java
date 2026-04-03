package de.jakob.lotm.artifacts;

import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

/**
 * Handles anvil recipes for creating sealed artifacts
 */
@EventBusSubscriber
public class SealedArtifactAnvilRecipe {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        // Check if right slot has a beyonder characteristic
        if (!(right.getItem() instanceof BeyonderCharacteristicItem characteristic)) {
            return;
        }

        // check if the item is historical summoned
        if (right.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains("VoidSummonTime")) {
            return;
        }

        // Check if left slot has a valid base item
        if (!SealedArtifactHandler.isValidBaseItem(left.getItem())) {
            return;
        }

        // Create the sealed artifact
        ItemStack result = switch (SealedArtifactHandler.getBaseTypeName(left.getItem())) {
            //case "bell" -> new ItemStack(ModItems.SEALED_ARTIFACT_BELL);
            //case "chain" -> new ItemStack(ModItems.SEALED_ARTIFACT_CHAIN);
            case "gem" -> new ItemStack(ModItems.SEALED_ARTIFACT_GEM);
            case "star" -> new ItemStack(ModItems.SEALED_ARTIFACT_STAR);
            default -> new ItemStack(ModItems.SEALED_ARTIFACT);
        };

        if (left.getCount() > 1) {
            event.setCanceled(true); // Prevent the recipe from working
            return;
        }

        // Store the base type for display
        String baseType = SealedArtifactHandler.getBaseTypeName(left.getItem());
        result.set(ModDataComponents.SEALED_ARTIFACT_BASE_TYPE, baseType);

        // Generate sealed artifact data
        //SealedArtifactData data = SealedArtifactHandler.createSealedArtifactData(characteristic, baseType);
        //result.set(ModDataComponents.SEALED_ARTIFACT_DATA, data);

        // Initialize selected ability index
        result.set(ModDataComponents.SEALED_ARTIFACT_SELECTED, 0);

        result.set(ModDataComponents.SEALED_ARTIFACT_BASE_TYPE, baseType);

        result.set(ModDataComponents.SEALED_ARTIFACT_GENERATED_SEQ, characteristic.getSequence());
        result.set(ModDataComponents.SEALED_ARTIFACT_GENERATED_PATH, characteristic.getPathway());

        result.set(ModDataComponents.SEALED_ARTIFACT_GENERATED, false);

        var level = event.getPlayer().level();
        if(!level.isClientSide)
            result.set(ModDataComponents.SEALED_ARTIFACT_GENERATED_FAILED,
                isFailed((ServerLevel) level, characteristic.getSequence()));

        // Set the result
        event.setOutput(result);
        
        // Set the cost (XP levels)
        int sequence = characteristic.getSequence();
        int cost = (sequence <= 1) ? 65 :
                   (sequence == 2) ? 50 :
                   (sequence <= 4) ? 30 :
                   (sequence <= 7) ? 20 : 10;

        event.setCost(cost);

        // Material cost is handled automatically by the anvil
        event.setMaterialCost(1);
    }

    public static boolean isFailed(ServerLevel level, int seq){
        float additional = 0.0f;

        switch (seq){
            case 6,5 -> additional += 0.1f;
            case 4,3 -> additional += 0.2f;
            case 2,1 -> additional += 0.3f;
        }

        return level.random.nextFloat() < 0.2f + additional;
    }

}