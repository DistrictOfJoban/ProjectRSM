package com.lx.rsm;

import com.lx.rsm.mixin.SidingAccessorMixin;
import com.lx.rsm.mixin.TrainServerAccessorMixin;
import mtr.MTR;
import mtr.data.RailwayData;
import mtr.data.Siding;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class Events {
    public static RailwayData overworldData = null;
    public static BlockPos spawnPoint = null;
    public static boolean canFetchInfo = false;
    public static void onServerTick(MinecraftServer server) {
        TickManager.onTick();
        canFetchInfo = true;

        /* Siding ID : Current Running Route (Map) */
        if(MTR.isGameTickInterval(20)) {
            overworldData = RailwayData.getInstance(server.getOverworld());

            if(overworldData == null) return;

            spawnPoint = server.getOverworld().getSpawnPos();

//            for(Siding siding : overworldData.sidings) {
//                ((SidingAccessorMixin)siding).getTrains().forEach(trainServer -> {
//                    Long ogRouteId = ProjectRSM.sidingToRouteMap.getOrDefault(trainServer.sidingId, ((TrainServerAccessorMixin)trainServer).getRouteId());
//                    if(ogRouteId != null) {
//                        ProjectRSM.sidingToRouteMap.put(trainServer.sidingId, ((TrainServerAccessorMixin)trainServer).getRouteId());
//                    } else {
//                        ProjectRSM.sidingToRouteMap.put(trainServer.sidingId, ogRouteId);
//                    }
//                });
//            }

            MTRDataManager.updateTrains(server.getOverworld());
        }
    }
}