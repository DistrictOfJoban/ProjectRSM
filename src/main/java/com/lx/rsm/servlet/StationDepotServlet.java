package com.lx.rsm.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lx.rsm.Events;
import mtr.data.Depot;
import mtr.data.IGui;
import mtr.data.Station;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StationDepotServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        final AsyncContext asyncContext = request.startAsync();
        final JsonObject object = new JsonObject();
        final JsonArray stationList = new JsonArray();
        final JsonArray depotList = new JsonArray();

        // Platforms
        if(Events.overworldData != null) {
            for(Station stn : Events.overworldData.stations) {
                JsonObject stnObject = new JsonObject();
                stnObject.addProperty("color", stn.color);
                stnObject.addProperty("name", IGui.formatStationName(stn.name));

                stnObject.add("corner1", getCornerObject(stn.corner1));
                stnObject.add("corner2", getCornerObject(stn.corner2));
                stationList.add(stnObject);
            }
            for(Depot depot : Events.overworldData.depots) {
                if(depot.corner1 == null || depot.corner2 == null) continue;
                JsonObject stnObject = new JsonObject();
                stnObject.addProperty("color", depot.color);
                stnObject.addProperty("name", IGui.formatStationName(depot.name));

                stnObject.add("corner1", getCornerObject(depot.corner1));
                stnObject.add("corner2", getCornerObject(depot.corner2));
                depotList.add(stnObject);
            }
            object.add("station", stationList);
            object.add("depots", depotList);
        }

        sendResponse(response, asyncContext, object.toString());
    }
}
