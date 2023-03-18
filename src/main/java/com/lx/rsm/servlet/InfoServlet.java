package com.lx.rsm.servlet;

import com.google.gson.JsonObject;
import com.lx.rsm.Events;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InfoServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        final AsyncContext asyncContext = request.startAsync();

        final JsonObject dataObject = new JsonObject();

        if(Events.overworldData != null) {
            dataObject.add("spawnpoint", getPosObject(Events.spawnPoint));
        }

        sendResponse(response, asyncContext, dataObject.toString());
    }
}