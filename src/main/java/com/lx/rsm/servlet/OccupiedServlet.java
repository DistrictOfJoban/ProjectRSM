package com.lx.rsm.servlet;

import com.google.gson.JsonObject;
import com.lx.rsm.Events;
import com.lx.rsm.MTRDataManager;
import com.lx.rsm.mixin.PathDataAccessorMixin;
import com.lx.rsm.mixin.SidingAccessorMixin;
import mtr.data.Siding;
import mtr.data.TrainServer;
import mtr.path.PathData;
import net.minecraft.util.math.BlockPos;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class OccupiedServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        final Set<String> occupiedRails = new HashSet<>();

        startSSE(request, response, 1000, true, ((writer, diffManager, x, z) -> {
            if(Events.overworldData != null) {
                for(Siding siding : Events.overworldData.sidings) {
                    Set<TrainServer> trainServers = new HashSet<>(((SidingAccessorMixin)siding).getTrains());
                    for(TrainServer train : trainServers) {
                        for(PathData pathData : train.path) {
                            JsonObject occupiedSectionObject = new JsonObject();
                            final boolean occupied = MTRDataManager.occupied.containsKey(pathData.getRailProduct());
                            final BlockPos pos1 = pathData.startingPos;
                            final BlockPos pos2 = ((PathDataAccessorMixin)pathData).getEndingPos();
                            final String id = String.valueOf(getUniquePosId(pos1, pos2));

                            if(!occupied) {
                                if(occupiedRails.contains(id)) {
                                    sendStreamData("remove", id, writer);
                                    occupiedRails.remove(id);
                                }
                            } else {
                                if(occupiedRails.contains(id)) continue;

                                final BlockPos cameraPos = new BlockPos(x, 0, z);
                                final int dist = Math.min(getManhattanDistance(pos1, cameraPos), getManhattanDistance(pos2, cameraPos));
                                if(dist > RADIUS_THRESHOLD) {
                                    continue;
                                }

                                occupiedSectionObject.addProperty("uuid", id);
                                occupiedSectionObject.add("pos1", getXZObject(pos1));
                                occupiedSectionObject.add("pos2", getXZObject(pos2));

                                if(!diffManager.needUpdate(id, occupiedSectionObject.toString())) continue;
                                sendStreamData("add", occupiedSectionObject.toString(), writer);
                                diffManager.storeDifference(id, occupiedSectionObject.toString());
                                occupiedRails.add(id);
                            }
                        }
                    }
                }
            }
        }));
    }
}
