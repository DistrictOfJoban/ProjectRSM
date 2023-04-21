package com.lx.rsm.config;

import com.lx.rsm.ProjectRSM;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class PortConfig {
    public static int port = 9090;

    public static void readConfig() {
        try {
            Path configPath = FabricLoader.getInstance().getConfigDir().resolve("rsm_port.txt");
            if(Files.exists(configPath)) {
                String portFile = Files.readAllLines(configPath).get(0).trim();
                port = Integer.parseInt(portFile);
            } else {
                ProjectRSM.LOGGER.info("[RSM] Cannot find rsm_port.txt, defaulting to port 9090.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
