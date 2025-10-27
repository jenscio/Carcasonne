package ch.epfl.chacun;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Jenss_AreaTest {

    @Test
    void checkConnectToWorksWithIdenticalClones() {
        Set<Zone.River> setZoneRiver = new HashSet<>();
        var z1 = new Zone.River(1_1, 0, null);
        setZoneRiver.add(z1);
        List<PlayerColor> occupantList = new ArrayList<>();
        occupantList.add(PlayerColor.RED);
        Area myarea1 = new Area<>(setZoneRiver,occupantList,3);
        Area myarea2 = new Area<>(setZoneRiver,occupantList,3);
        assertEquals(myarea1,myarea2);
    }

    @Test
    void checkOrderingWorksForOccupants() {
        Set<Zone.River> setZoneRiver = new HashSet<>(List.of(new Zone.River(1_1, 0, null)));
        // initial player list: GREEN, BLUE, RED, GREEN
        List<PlayerColor> occupantListUnordered = new ArrayList<>(List.of(PlayerColor.GREEN,PlayerColor.BLUE,PlayerColor.RED,PlayerColor.GREEN));
        // ordered player list: RED, BLUE, GREEN, GREEN
        List<PlayerColor> occupantListOrdered = new ArrayList<>(List.of(PlayerColor.RED,PlayerColor.BLUE,PlayerColor.GREEN,PlayerColor.GREEN));
        Area myarea = new Area<>(setZoneRiver,occupantListUnordered,3);
        assertEquals(occupantListOrdered,myarea.occupants());
    }

    @Test
    void multipleOccupantsOfSameColor() {
        Set<Zone.River> setZoneRiver = new HashSet<>(List.of(new Zone.River(1_1, 0, null)));
        // (unordered) player list: RED, BLUE, RED, GREEN
        List<PlayerColor> occupantList = new ArrayList<>(List.of(PlayerColor.RED,PlayerColor.BLUE,PlayerColor.RED,PlayerColor.GREEN));
        // player list with ONE red removed (must be ordered): RED, BLUE, GREEN
        List<PlayerColor> occupantListWithoutRed = new ArrayList<>(List.of(PlayerColor.RED, PlayerColor.BLUE,PlayerColor.GREEN));
        Area myarea = new Area<>(setZoneRiver,occupantList,3);
        Area myAreaWithoutRed = myarea.withoutOccupant(PlayerColor.RED);
        assertEquals(occupantListWithoutRed,myAreaWithoutRed.occupants());
    }
}