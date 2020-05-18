package de.autoDrive.MapTileLoader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class TileService implements CommandLineRunner {

    private final static Logger LOG = LoggerFactory.getLogger(MapService.class);

    @Autowired
    private MapService mapService;

    @Override
    public void run(String... args) throws Exception {

        LOG.info("Check Maps.");

        for (int i = 0; i < 7; i++) {

            mapService.runTileCheck(i,"FELSBRUNN");
        }
    }

}
