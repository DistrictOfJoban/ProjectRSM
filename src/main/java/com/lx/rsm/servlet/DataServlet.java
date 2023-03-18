package com.lx.rsm.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lx.rsm.Events;
import com.lx.rsm.mixin.PathDataAccessorMixin;
import com.lx.rsm.mixin.SidingAccessorMixin;
import mtr.data.*;
import mtr.path.PathData;
import net.minecraft.util.math.BlockPos;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        startSSE(request, response, 2000, false, (writer, diffManager, x, z) -> {
            if(Events.overworldData != null) {
                // Platform
                for(Platform platform : Events.overworldData.platforms) {
                    JsonObject platformObject = new JsonObject();
                    List<BlockPos> position = platform.getOrderedPositions(new BlockPos(0, 0, 0), false);
                    BlockPos pos1 = position.get(0);
                    BlockPos pos2 = position.get(1);
                    /* Filtering */
                    BlockPos cameraPos = new BlockPos(x, 0, z);
                    int dist1 = getManhattanDistance(cameraPos, pos1);
                    int dist2 = getManhattanDistance(cameraPos, pos2);
                    int closestDistFromCamera = Math.min(dist1, dist2);
                    if(closestDistFromCamera > (RADIUS_THRESHOLD / 2)) {
                        continue;
                    }
                    /* Filter end */
                    platformObject.add("pos1", getXZObject(pos1));
                    platformObject.add("pos2", getXZObject(pos2));
                    platformObject.addProperty("type", String.valueOf(RailType.PLATFORM));
                    platformObject.addProperty("color", RailType.PLATFORM.color);
                    platformObject.addProperty("length", pos1.getManhattanDistance(pos2));
                    platformObject.addProperty("dwell", platform.getDwellTime() / 2);
                    platformObject.addProperty("platNumber", platform.name);
                    JsonArray routeList = new JsonArray();
                    for(Route route : Events.overworldData.routes) {
                        for(Route.RoutePlatform plat : route.platformIds) {
                            if(plat.platformId == platform.id) {
                                JsonObject rtObject = new JsonObject();
                                rtObject.addProperty("color", route.color);
                                rtObject.addProperty("name", IGui.formatStationName(route.name));
                                routeList.add(rtObject);
                            }
                        }
                    }
                    platformObject.add("rtVia", routeList);
                    sendStreamData(platformObject.toString(), writer);
                }

                final Set<String> addedRailId = new HashSet<>();
                //Get Path
                for(Siding siding : Events.overworldData.sidings) {
                    Set<TrainServer> trains = ((SidingAccessorMixin)siding).getTrains();
                    for(TrainServer train : trains) {
                        for(PathData pathData : train.path) {
                            // We already handled platform separately
                            if(pathData.rail.railType == RailType.PLATFORM) continue;
                            JsonObject pathObject = new JsonObject();

                            final BlockPos pos1 = pathData.startingPos;
                            final BlockPos pos2 = ((PathDataAccessorMixin)pathData).getEndingPos();
                            /* Filtering */
                            BlockPos cameraPos = new BlockPos(x, 0, z);
                            int dist1 = getManhattanDistance(cameraPos, pos1);
                            int dist2 = getManhattanDistance(cameraPos, pos2);
                            int closestDistFromCamera = Math.min(dist1, dist2);
                            if(closestDistFromCamera > (RADIUS_THRESHOLD / 2)) {
                                continue;
                            }
                            /* Filter end */
                            Rail railDetail = pathData.rail;
                            String id = String.valueOf(getUniquePosId(pos1, pos2));

                            if(addedRailId.contains(id)) continue;

                            pathObject.add("pos1", getXZObject(pos1));
                            pathObject.add("pos2", getXZObject(pos2));
                            pathObject.addProperty("uuid", id);
                            pathObject.addProperty("type", String.valueOf(railDetail.railType));
                            pathObject.addProperty("color", railDetail.railType.color);
                            pathObject.addProperty("length", railDetail.getLength());

                            sendStreamData(pathObject.toString(), writer);
                            addedRailId.add(id);
                        }
                    }
                }
            }
        });
    }
}
