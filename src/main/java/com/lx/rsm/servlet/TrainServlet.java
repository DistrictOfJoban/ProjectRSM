package com.lx.rsm.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lx.rsm.data.ExposedTrainData;
import com.lx.rsm.Events;
import com.lx.rsm.MTRDataManager;
import com.lx.rsm.mixin.PathDataAccessorMixin;
import com.lx.rsm.mixin.TrainAccessorMixin;
import com.lx.rsm.mixin.TrainServerAccessorMixin;
import mtr.data.*;
import mtr.path.PathData;
import net.minecraft.util.math.BlockPos;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class TrainServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        startSSE(request, response, 1000, true, ((writer, diffManager, x, z) -> {
            List<ExposedTrainData> trainDataList = new ArrayList<>(MTRDataManager.trainDataList);
            for (ExposedTrainData train : trainDataList) {
                final BlockPos cameraPos = new BlockPos(x, 0, z);
                final JsonObject trainJsonData = new JsonObject();
                trainJsonData.addProperty("uuid", train.train.id);
                trainJsonData.addProperty("isManual", train.isCurrentlyManual);
                trainJsonData.addProperty("length", train.train.spacing / train.train.trainCars);
                trainJsonData.addProperty("passengers", ((TrainAccessorMixin) train.train).getRidingEntities().size());
                trainJsonData.addProperty("started", ((TrainAccessorMixin) train.train).getIsOnRoute());
                int closestDistFromCamera = 10000;

                final JsonArray poses = new JsonArray();
                for(int i = 0; i < train.train.trainCars; i++) {
                    final JsonArray frontBackPosSet = new JsonArray();
                    /* Loop, one is for front and one is for back */
                    for(int j = 0; j < 2; j++) {
                        boolean isFront = j == 0;
                        double railProgress = getRailProgress(train.train.getRailProgress(), i, train.train.spacing);
                        if(!isFront) railProgress = railProgress - train.train.spacing;
                        int pathIndex = train.train.getIndex(railProgress, true);
                        PathData path = train.train.path.get(pathIndex);
                        BlockPos railPos1 = path.startingPos;
                        BlockPos railPos2 = ((PathDataAccessorMixin)path).getEndingPos();
                        double railLength = path.rail.getLength();
                        double nodeEndDistance = ((TrainAccessorMixin) train.train).getDistances().get(Math.max(0, pathIndex)) - railProgress;
                        double ratio = (railLength - nodeEndDistance) / railLength;
                        BlockPos finalRenderedPos = getCenter(ratio, railPos1, railPos2);

                        closestDistFromCamera = Math.min(closestDistFromCamera, getManhattanDistance(new BlockPos(finalRenderedPos.getX(), finalRenderedPos.getY(), finalRenderedPos.getZ()), cameraPos));
                        frontBackPosSet.add(getXZObject(getCenter(ratio, railPos1, railPos2)));
                    }

                    poses.add(frontBackPosSet);
                }
                trainJsonData.add("poses", poses);

                if(closestDistFromCamera > RADIUS_THRESHOLD) continue;

                String destination = null;
                int color = 0;
                if (Events.overworldData != null) {
                    Route curRoute = Events.overworldData.dataCache.routeIdMap.get(((TrainServerAccessorMixin) train.train).getRouteId());
                    if (curRoute != null) {
                        color = curRoute.color;
                        if(!curRoute.platformIds.isEmpty()) {
                            Route.RoutePlatform platform = curRoute.platformIds.get(curRoute.platformIds.size() - 1);
                            Station station = Events.overworldData.dataCache.platformIdToStation.get(platform.platformId);
                            if(station != null) {
                                destination = IGui.formatStationName(station.name);
                            }
                        }
                    }
                }

                trainJsonData.addProperty("id", train.train.trainId);
                trainJsonData.addProperty("color", color);
                trainJsonData.addProperty("speed", ((TrainAccessorMixin) train.train).getSpeed() * 20 * 3.6);
                trainJsonData.addProperty("cars", train.train.trainCars);
                trainJsonData.addProperty("dest", destination);

                if(!diffManager.needUpdate(String.valueOf(train.train.id), trainJsonData.toString())) {
                    continue;
                }
                sendStreamData(trainJsonData.toString(), writer);
                diffManager.storeDifference(String.valueOf(train.train.id), trainJsonData.toString());
            }
        }));

    }

    public final double getRailProgress(double railProgress, int car, int spacing) {
        return railProgress - (car * spacing);
    }

    private BlockPos getCenter(double ratio, BlockPos pos1, BlockPos pos2) {
        int x1 = pos1.getX();
        int x2 = pos2.getX();
        int z1 = pos1.getZ();
        int z2 = pos2.getZ();

        double x = x1 + (x2 - x1) * ratio;
        double z = z1 + (z2 - z1) * ratio;

        return new BlockPos(x, 0, z);
    }
}
