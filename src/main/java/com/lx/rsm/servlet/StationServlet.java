package com.lx.rsm.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lx.rsm.Events;
import mtr.data.IGui;
import mtr.data.Station;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StationServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        final AsyncContext asyncContext = request.startAsync();
        final JsonArray stationList = new JsonArray();

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
        }

        sendResponse(response, asyncContext, stationList.toString());
    }
}
