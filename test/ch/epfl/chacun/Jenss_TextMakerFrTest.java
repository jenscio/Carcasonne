package ch.epfl.chacun;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Jenss_TextMakerFrTest {

    private final TextMaker frenchTextMaker = new TextMakerFr(Map.of(
            PlayerColor.RED,"Dalia",
            PlayerColor.BLUE,"Claude",
            PlayerColor.GREEN,"Bachir",
            PlayerColor.YELLOW,"Alice"));

    @Test
    void playerClosedForestWithMenhir() {
        assertEquals("Dalia a fermé une forêt contenant un menhir et peut donc placer une tuile menhir.",
                frenchTextMaker.playerClosedForestWithMenhir(PlayerColor.RED));
    }

    @Test
    void playersScoredForest() {
        assertEquals("Claude a remporté 6 points en tant qu'occupant·e majoritaire d'une forêt composée de 3 tuiles.",
                frenchTextMaker.playersScoredForest(Set.of(PlayerColor.BLUE),6,0,3));

        assertEquals("Dalia et Alice ont remporté 9 points en tant qu'occupant·e·s majoritaires d'une forêt composée de 3 tuiles et de 1 groupe de champignons.",
                frenchTextMaker.playersScoredForest(Set.of(PlayerColor.RED,PlayerColor.YELLOW),9,1,3));
    }

    @Test
    void playersScoredRiver() {
        assertEquals("Claude et Bachir ont remporté 3 points en tant qu'occupant·e·s majoritaires d'une rivière composée de 3 tuiles.",
                frenchTextMaker.playersScoredRiver(Set.of(PlayerColor.BLUE,PlayerColor.GREEN),3,0,3));
        assertEquals("Alice a remporté 8 points en tant qu'occupant·e majoritaire d'une rivière composée de 3 tuiles et contenant 5 poissons.",
                frenchTextMaker.playersScoredRiver(Set.of(PlayerColor.YELLOW),8,5,3));
    }

    @Test
    void playerScoredHuntingTrap() {
        assertEquals("Bachir a remporté 10 points en plaçant la fosse à pieux dans un pré dans lequel elle est entourée de 1 mammouth, 2 aurochs et 3 cerfs.",
                frenchTextMaker.playerScoredHuntingTrap(PlayerColor.GREEN,10,Map.of(Animal.Kind.MAMMOTH,1, Animal.Kind.AUROCHS,2,Animal.Kind.DEER,3)));
    }

    @Test
    void playerScoredLogboat() {
        assertEquals("Alice a remporté 8 points en plaçant la pirogue dans un réseau hydrographique contenant 4 lacs.",
                frenchTextMaker.playerScoredLogboat(PlayerColor.YELLOW,8,4));
    }

    @Test
    void playersScoredMeadow() {
        assertEquals("Dalia a remporté 1 point en tant qu'occupant·e majoritaire d'un pré contenant 1 cerf.",
                frenchTextMaker.playersScoredMeadow(Set.of(PlayerColor.RED),1,Map.of(Animal.Kind.DEER,1)));
        assertEquals("Claude et Bachir ont remporté 5 points en tant qu'occupant·e·s majoritaires d'un pré contenant 1 mammouth et 2 cerfs.",
                frenchTextMaker.playersScoredMeadow(Set.of(PlayerColor.BLUE,PlayerColor.GREEN),5,Map.of(Animal.Kind.MAMMOTH,1,Animal.Kind.DEER,2)));
    }

    @Test
    void playersScoredRiverSystem() {
        assertEquals("Alice a remporté 9 points en tant qu'occupant·e majoritaire d'un réseau hydrographique contenant 9 poissons.",
                frenchTextMaker.playersScoredRiverSystem(Set.of(PlayerColor.YELLOW),9,9));
        assertEquals("Dalia, Claude et Bachir ont remporté 1 point en tant qu'occupant·e·s majoritaires d'un réseau hydrographique contenant 1 poisson.",
                frenchTextMaker.playersScoredRiverSystem(Set.of(PlayerColor.RED,PlayerColor.BLUE,PlayerColor.GREEN),1,1));
    }

    @Test
    void playersScoredPitTrap() {
        assertEquals("Bachir et Alice ont remporté 12 points en tant qu'occupant·e·s majoritaires d'un pré contenant la grande fosse à pieux entourée de 2 mammouths, 2 aurochs et 2 cerfs.",
                frenchTextMaker.playersScoredPitTrap(Set.of(PlayerColor.YELLOW,PlayerColor.GREEN),12,Map.of(Animal.Kind.MAMMOTH,2, Animal.Kind.AUROCHS,2,Animal.Kind.DEER,2)));
        assertEquals("Dalia a remporté 2 points en tant qu'occupant·e majoritaire d'un pré contenant la grande fosse à pieux entourée de 1 auroch.",
                frenchTextMaker.playersScoredPitTrap(Set.of(PlayerColor.RED),2,Map.of(Animal.Kind.AUROCHS,1)));
    }

    @Test
    void playersScoredRaft() {
        assertEquals("Dalia et Claude ont remporté 10 points en tant qu'occupant·e·s majoritaires d'un réseau hydrographique contenant le radeau et 10 lacs.",
                frenchTextMaker.playersScoredRaft(Set.of(PlayerColor.RED,PlayerColor.BLUE),10,10));
        assertEquals("Alice a remporté 1 point en tant qu'occupant·e majoritaire d'un réseau hydrographique contenant le radeau et 1 lac.",
                frenchTextMaker.playersScoredRaft(Set.of(PlayerColor.YELLOW),1,1));
    }

    @Test
    void playersScoredGame() {
        assertEquals("Bachir a remporté la partie avec 111 points!",
                frenchTextMaker.playersWon(Set.of(PlayerColor.GREEN),111));
        assertEquals("Dalia et Alice ont remporté la partie avec 123 points!",
                frenchTextMaker.playersWon(Set.of(PlayerColor.RED,PlayerColor.YELLOW),123));
    }



}
