package com.lx.rsm;


import com.lx.rsm.Config.PortConfig;
import com.lx.rsm.servlet.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
//import org.eclipse.jetty.util.log.Log;
//import org.eclipse.jetty.util.log.StdErrLog;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.net.URL;
import java.util.*;

public class ProjectRSM implements ModInitializer {
    private Server webServer = null;
    public static final Logger LOGGER = LogManager.getLogger("RSM");

    @Override
    public void onInitialize() {
        LOGGER.info("[RSM] Version 1.0");
        PortConfig.readConfig();

        /* EVENTS REGISTRATION */
        ServerTickEvents.START_SERVER_TICK.register(Events::onServerTick);
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            if(webServer != null) {
                try {
                    LOGGER.info("[RSM] Stopping Web server!");
                    webServer.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        LOGGER.info("[RSM] Initializing webserver");
        initializeWebserver();
    }

    private void initializeWebserver() {
        webServer = new Server(new QueuedThreadPool(250, 10, 300000));
        final ServerConnector serverConnector = new ServerConnector(webServer);
        serverConnector.setIdleTimeout(300000);
        serverConnector.setAcceptQueueSize(100);
        webServer.setConnectors(new Connector[]{serverConnector});
        final ServletContextHandler context = new ServletContextHandler();
        webServer.setHandler(context);
        final URL url = ProjectRSM.class.getResource("/assets/rsm/website/");
        if (url != null) {
            try {
                context.setBaseResource(Resource.newResource(url.toURI()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final ServletHolder servletHolder = new ServletHolder("default", DefaultServlet.class);
        servletHolder.setInitParameter("dirAllowed", "true");
        context.addServlet(servletHolder, "/");
        context.addServlet(DataServlet.class, "/data");
        context.addServlet(StationServlet.class, "/stations");
        context.addServlet(TrainServlet.class, "/trains");
        context.addServlet(OccupiedServlet.class, "/occupy");
        context.addServlet(InfoServlet.class, "/info");

//        StdErrLog logger = new StdErrLog();
//        logger.setDebugEnabled(true);
//        Log.setLog(logger);

        serverConnector.setPort(PortConfig.port);
        try {
            webServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
