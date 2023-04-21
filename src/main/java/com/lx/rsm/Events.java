package com.lx.rsm;

import mtr.MTR;
import mtr.data.RailwayData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class Events {
    public static RailwayData overworldData = null;
    public static boolean canFetch = true;
    public static void onServerTick(MinecraftServer server) {
        TickManager.onTick();
        canFetch = true;

        /* Siding ID : Current Running Route (Map) */
        if(MTR.isGameTickInterval(20)) {
            overworldData = RailwayData.getInstance(server.getOverworld());
            if(overworldData == null) return;
            MTRDataManager.updateTrains(server.getOverworld());
        }
    }
}