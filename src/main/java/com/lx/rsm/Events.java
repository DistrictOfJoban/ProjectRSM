package com.lx.rsm;

import mtr.MTR;
import mtr.data.RailwayData;
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

            MTRDataManager.updateTrains(server.getOverworld());
        }
    }
}