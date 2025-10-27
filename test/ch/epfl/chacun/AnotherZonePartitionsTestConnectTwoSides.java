package ch.epfl.chacun;

import ch.epfl.chacun.tile.Tiles4Tests;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnotherZonePartitionsTestConnectTwoSides {



    @Test
    void connectTwoSidesShouldRaiseOrNotException() {

        // Use the automatically generated tiles from Tiles4Tests
        Tile tile56 = Tiles4Tests.TILES.get(56);
        Tile tile17 = Tiles4Tests.TILES.get(17);
        Tile tile27 = Tiles4Tests.TILES.get(27);
        Tile tile80 = Tiles4Tests.TILES.get(80);

        ZonePartitions.Builder zonePartitionsBuilder = new ZonePartitions.Builder(ZonePartitions.EMPTY);
        zonePartitionsBuilder.addTile(tile56);
        zonePartitionsBuilder.addTile(tile17);
        zonePartitionsBuilder.addTile(tile27);
        zonePartitionsBuilder.addTile(tile80);

        // this should work
        assertDoesNotThrow(()-> zonePartitionsBuilder.connectSides(tile17.e(),tile56.w()));
        // this should work
        assertDoesNotThrow(()-> zonePartitionsBuilder.connectSides(tile56.e(),tile27.w()));

        // incompatible types - should raise exception
        assertThrows(IllegalArgumentException.class,() ->zonePartitionsBuilder.connectSides(tile17.w(),tile80.s()));

        ZonePartitions zps = zonePartitionsBuilder.build();

    }
}
