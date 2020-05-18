package de.autoDrive.MapTileLoader.repository;

import de.autoDrive.MapTileLoader.entity.MapTileData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MapTileDataRepository extends JpaRepository<MapTileData, String> {

    Optional<MapTileData> findFirstByZoomLevelAndXAndYAndName(Integer zoomLevel, Integer x, Integer y, String name);
}
