package com.lx.rsm.mixin;

import com.lx.rsm.Events;
import com.lx.rsm.MTRDataManager;
import mtr.MTR;
import mtr.data.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(value = TrainServer.class, remap = false)
public class TrainServerMixin {
    @Inject(method = "simulateTrain", at = @At("HEAD"))
    public void simulateTrain(World world, float ticksElapsed, Depot depot, DataCache dataCache, List<Map<UUID, Long>> trainPositions, Map<PlayerEntity, Set<TrainServer>> trainsInPlayerRange, Map<Long, List<ScheduleEntry>> schedulesForPlatform, Map<Long, Map<BlockPos, TrainDelay>> trainDelays, CallbackInfoReturnable<Boolean> cir) {
        if(Events.canFetch) {
            MTRDataManager.occupied.clear();

            for (final Map<UUID, Long> trainPositionsMap : trainPositions) {
                for(Map.Entry<UUID, Long> entry : trainPositionsMap.entrySet()) {
                    MTRDataManager.occupied.put(entry.getKey(), true);
                    Events.canFetch = false;
                }
            }
        }
    }
}
