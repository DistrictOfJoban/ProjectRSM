package com.lx.rsm;

import com.lx.rsm.Data.ExposedTrainData;
import com.lx.rsm.mixin.SidingAccessorMixin;
import com.lx.rsm.mixin.TrainAccessorMixin;
import com.lx.rsm.mixin.TrainServerAccessorMixin;
import mtr.data.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MTRDataManager {
    public static ConcurrentHashMap<UUID, Boolean> occupied = new ConcurrentHashMap<>();
    public final static List<ExposedTrainData> trainDataList = new ArrayList<>();

    public static void updateTrains(World world) {
        RailwayData data = RailwayData.getInstance(world);

        trainDataList.clear();
        /* Loop through each siding */
        for(Siding siding : data.sidings) {
            /* Loop through each train in each siding */
            for(TrainServer train : ((SidingAccessorMixin)siding).getTrains()) {
                final Vec3d[] positions = new Vec3d[train.trainCars + 1];

                /* Loop through each car in each train */
                for (int i = 0; i <= train.trainCars; i++) {
                    positions[i] = ((TrainAccessorMixin)train).getTheRoutePosition(((TrainAccessorMixin) train).getReversed() ? train.trainCars - i : i, train.spacing);
                }

                ExposedTrainData td = new ExposedTrainData(train, ((TrainServerAccessorMixin)train).getRouteId(), positions, ((TrainAccessorMixin)train).getIsManualAllowed(), ((TrainAccessorMixin)train).getInventory());

                if(td.isManual) {
                    td.isCurrentlyManual = ((TrainAccessorMixin)train).getIsCurrentlyManual();
                    if(td.isCurrentlyManual) {
                        td.accelerationSign = ((TrainAccessorMixin) train).getManualNotch();
                        td.ridingEntities = ((TrainAccessorMixin) train).getRidingEntities();
                        td.manualCooldown = ((TrainServerAccessorMixin)train).getManualCoolDown();
                        td.manualToAutomaticTime = ((TrainAccessorMixin) train).getManualToAutomaticTime();
                    }
                }

                trainDataList.add(td);
            }
        }
    }
}
