package de.autoDrive.MapTileLoader.service;

import de.autoDrive.MapTileLoader.entity.MapInfo;
import de.autoDrive.MapTileLoader.entity.MapTileData;
import de.autoDrive.MapTileLoader.helper.ImageHelper;
import de.autoDrive.MapTileLoader.repository.MapInfoRepository;
import de.autoDrive.MapTileLoader.repository.MapTileDataRepository;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import static de.autoDrive.NetworkServer.rest.MapTileInfo.DIMENSIONS;
import static de.autoDrive.NetworkServer.rest.MapTileInfo.TILE_SIZE;

@Service
@Transactional
public class MapService {

    private final static Logger LOG = LoggerFactory.getLogger(MapService.class);


    @Autowired
    private MapTileDataRepository mapTileDataRepository;

    @Autowired
    private MapInfoRepository mapInfoRepository;


    public byte[] getMap(String name, int level, int x, int y) throws IOException {
        Optional<MapTileData> tile = mapTileDataRepository.findFirstByZoomLevelAndXAndYAndName(level, x, y, name);
        return tile.map(MapTileData::getData).orElse(null);
    }

    public void createTiles(String name, Integer level) throws IOException {
        LOG.info("createTiles {}, level: {}", name, level);

        int w = DIMENSIONS[level];
        int h = DIMENSIONS[level];

        MapInfo mapInfo = mapInfoRepository.findFirstByName(name).orElseGet(() -> {
            MapInfo mi = new MapInfo();
            mi.setName(name);
            mapInfoRepository.save(mi);
            return mi;
        });

        BufferedImage scaledImage = getScaleImage(name, w);

        for (int x = 0; x < w; x += TILE_SIZE) {
            for (int y = 0; y < h; y += TILE_SIZE) {
                BufferedImage cutoutImage = scaledImage.getSubimage(x, y, x + TILE_SIZE <= w ? TILE_SIZE:w - x, y + TILE_SIZE <= h ? TILE_SIZE:h - y);
                byte[] byteArray = toByteArrayAutoClosable(cutoutImage);
                LOG.info("level: {}, x: {}, y: {}, size: {}", level, x, y, byteArray.length);
                MapTileData mapTile = new MapTileData();
                mapTile.setData(byteArray);
                mapTile.setName(name);
                mapTile.setX(x);
                mapTile.setY(y);
                mapTile.setZoomLevel(level);
                mapTile.setMapInfo(mapInfo);
                mapTileDataRepository.save(mapTile);
            }
            mapTileDataRepository.flush();
        }
    }

    public BufferedImage getScaleImage(String name, int dimension) {
        BufferedImage image = ImageHelper.loadImage("/images/"+name+".png");
        return Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, dimension, dimension);
    }

    private byte[] toByteArrayAutoClosable(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        }
    }

    @Async
    public void runTileCheck(Integer zoomLevel, String name) throws IOException {
        LOG.info("runTileCheck {}, zoomLevel: {}", name, zoomLevel);

        byte[] data = getMap(name, zoomLevel, 0, 0);
        if (data == null) {
            try {
                createTiles(name, zoomLevel);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
