package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForgeConfig;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MobEvents {

    @SubscribeEvent
    public static void onMobSpawnCheck(MobSpawnEvent.PositionCheck event) {
        var level = event.getLevel();
        if(!(level instanceof ServerLevel serverLevel)) return;

        Entity entity = event.getEntity();
        BlockPos pos = entity.getOnPos();

        var biomeKey = level.getBiome(pos).unwrapKey();

        if (biomeKey.isPresent()){
            for(var biom : Config.biomes){
             if(biom.equals(biomeKey.get().location()))
                 event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
            }
        }
    }

}
