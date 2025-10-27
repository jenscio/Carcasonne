package ch.epfl.chacun;

import ch.epfl.chacun.tile.Tiles4Tests;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Jenss_GameStateTest {



    @Test
    void basicGameStateTests() {

        // this test verifies that the basic mechanism of placing the initial tile, then placing tiles and occupying tiles,
        // works correctly, as well as closing forests and the associated scores.
        // The test also checks whether a menhir tile (LOGBOAT) can be played and is scored correctly.


        // start tile
        Tile tile56 = Tiles4Tests.TILES.get(56);
        // some normal tiles
        Tile tile00 = Tiles4Tests.TILES.get(00);
        Tile tile17 = Tiles4Tests.TILES.get(17);
        Tile tile27 = Tiles4Tests.TILES.get(27);
        Tile tile35 = Tiles4Tests.TILES.get(35);
        Tile tile37 = Tiles4Tests.TILES.get(37);
        Tile tile49 = Tiles4Tests.TILES.get(49);
        Tile tile61 = Tiles4Tests.TILES.get(61);
        // menhir tile
        Tile menhirTile93LogBoat = Tiles4Tests.TILES.get(93); // LOGBOAT

        // now create initial tile decks, player colors, and the textMaker object
        TileDecks initialTileDecks = new TileDecks(
                List.of(tile56), // start tile
                List.of(tile17, tile00, tile35, tile37, tile27, tile49, tile61), // normal tiles
                List.of(menhirTile93LogBoat)); // menhir tiles
        List<PlayerColor> players = List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN);
        Jenss_BasicTextMaker textMaker = new Jenss_BasicTextMaker();

        // create the initial GameState object
        GameState gameStateStart = GameState.initial(players, initialTileDecks, textMaker);

        // place the starting tile
        GameState gameStatePlacedInitialTile = gameStateStart.withStartingTilePlaced();

        // check first player is blue
        assertEquals(PlayerColor.BLUE, gameStatePlacedInitialTile.currentPlayer());

        // check that all occupants are free (as no tile has been placed)
        assertEquals(Occupant.occupantsCount(Occupant.Kind.PAWN), gameStatePlacedInitialTile.freeOccupantsCount(PlayerColor.RED, Occupant.Kind.PAWN));
        assertEquals(Occupant.occupantsCount(Occupant.Kind.HUT), gameStatePlacedInitialTile.freeOccupantsCount(PlayerColor.RED, Occupant.Kind.HUT));

        // check that the tile deck for the start tiles is empty
        assertEquals(0, gameStatePlacedInitialTile.tileDecks().deckSize(Tile.Kind.START));

        // check that the next action is PLACE_TILE
        assertEquals(GameState.Action.PLACE_TILE, gameStatePlacedInitialTile.nextAction());

        // pLace now a tile by the first player (which is BLUE)
        // next (first!) tile to play on the normal tile deck is tile 17
        assertEquals(tile17, gameStatePlacedInitialTile.tileToPlace());
        // check that the tile deck for normal tiles is: "initialTileDecks.deckSize(Tile.Kind.NORMAL) - 1"
        // (as we have already drawn one tile)
        assertEquals(initialTileDecks.deckSize(Tile.Kind.NORMAL) - 1, gameStatePlacedInitialTile.tileDecks().deckSize(Tile.Kind.NORMAL));


        // BLUE places tile 17 in (-1,0), left to 56
        PlacedTile pTile17 = new PlacedTile(tile17, PlayerColor.BLUE, Rotation.NONE, new Pos(-1, 0));
        GameState gameState = gameStatePlacedInitialTile.withPlacedTile(pTile17);

        // make another instance of this gameState, just to test equal()
        GameState gameState2 = gameStatePlacedInitialTile.withPlacedTile(pTile17);
        // compare that gameState and gameState2 are equal (ie. equal() redefined):
        //check: really needed?  assertTrue(gameState.equals(gameState2));

        // check that the next action has been updated to OCCUPY_TILE
        assertEquals(GameState.Action.OCCUPY_TILE, gameState.nextAction());

        // check that the list of potential occupants is correct: All the zones can be occupied

        Set<Occupant> expectedPotentialOccupants17 = Set.of(
                new Occupant(Occupant.Kind.PAWN, 17_0),
                new Occupant(Occupant.Kind.PAWN, 17_1), // river
                new Occupant(Occupant.Kind.PAWN, 17_2),
                new Occupant(Occupant.Kind.PAWN, 17_3), // river
                new Occupant(Occupant.Kind.PAWN, 17_4),
                new Occupant(Occupant.Kind.HUT, 17_1),
                new Occupant(Occupant.Kind.HUT, 17_3));
        assertEquals(expectedPotentialOccupants17, gameState.lastTilePotentialOccupants());

        // Now a discarded test:
        // - add NO occupant (null), check that next action is PLACE_TILE
        assertEquals(GameState.Action.PLACE_TILE, gameState.withNewOccupant(null).nextAction());

        // BLUE adds an occupant (PAWN) to river 17_1
        gameState = gameState.withNewOccupant(new Occupant(Occupant.Kind.PAWN, 17_1));
        // check that the number of free PAWN occupants has been decreased
        assertEquals(4, gameState.freeOccupantsCount(PlayerColor.BLUE, Occupant.Kind.PAWN));

        // check that next action is PLACE_TILE and next player is RED
        assertEquals(GameState.Action.PLACE_TILE, gameState.nextAction());
        assertEquals(PlayerColor.RED, gameState.currentPlayer());
        // check that the list of players is now updated with BLUE to the end
        assertEquals(List.of(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.BLUE), gameState.players());

        // now add & occupy a few more normal tiles, closing a river / a river system
        //    - add some occupants (HUT or PAWN) and check that they are reduced accordingly
        // and checking that for river, the points are allocated correctly (for river system, this is at the end)

        // turn: RED
        // next tile to play on the normal tile deck is tile 00
        assertEquals(tile00, gameState.tileToPlace());

        // assert that tile 00 has been removed from the tile deck (one tile less)
        assertEquals(initialTileDecks.deckSize(Tile.Kind.NORMAL) - 2, gameState.tileDecks().deckSize(Tile.Kind.NORMAL));

        // RED places tile 00 over tile 17 - on (-1,-1), rotated half turn
        PlacedTile pTile00 = new PlacedTile(tile00, PlayerColor.RED, Rotation.HALF_TURN, new Pos(-1, -1));
        gameState = gameState.withPlacedTile(pTile00);

        // Check the potential occupants for tile 00
        // essentially, all zones except the river (zone 1) which is already occupied by BLUE in tile 17
        Set<Occupant> expectedPotentialOccupants00  = Set.of(
                new Occupant(Occupant.Kind.PAWN, 0),
                new Occupant(Occupant.Kind.PAWN, 2),
                new Occupant(Occupant.Kind.PAWN, 3),
                new Occupant(Occupant.Kind.PAWN, 4),
//                new Occupant(Occupant.Kind.HUT, 1), // river system -> WRONG as connected to a lake so CANNOT place hut there
//                (see https://cs108.epfl.ch/p/02_tiles.html 2.3: chaque rivière qui ***n'est pas connectée à un lac*** peut potentiellement être occupée par une hutte,
                new Occupant(Occupant.Kind.HUT, 8));// river system
        assertEquals(expectedPotentialOccupants00, gameState.lastTilePotentialOccupants());


        // RED places a HUT on the lake (zone 0_8)
        gameState = gameState.withNewOccupant(new Occupant(Occupant.Kind.HUT, 8));
        // RED has now closed a river and a river system! (56_8,17_1,0_1,0_8)
        // Check points for river majority occupants (BLUE - pawn and RED - hut)
        //  Lorsqu'une rivière est fermée, les pêcheurs majoritaires remportent 1 point par tuile composant la rivière,
        //  et 1 point par poisson nageant dans la rivière elle-même ou dans l'un des éventuels lacs aux extrémités.

        // means BLUE scores 3 (no. tiles) + 3 (fishes) = 6
        assertEquals(6,gameState.messageBoard().points().get(PlayerColor.BLUE));

        // turn: GREEN
        assertEquals(PlayerColor.GREEN, gameState.currentPlayer());
        // next tile to play on the normal tile deck is tile 35
        assertEquals(tile35, gameState.tileToPlace());

        // check also that the normal tile 35 has been removed from the deck
        assertEquals(initialTileDecks.deckSize(Tile.Kind.NORMAL) - 3, gameState.tileDecks().deckSize(Tile.Kind.NORMAL));


        // GREEN places tile 35 on (0,-1), rotated half turn
        // check that the next player is GREEN
        PlacedTile pTile35 = new PlacedTile(tile35, PlayerColor.GREEN, Rotation.HALF_TURN, new Pos(0, -1));
        gameState = gameState.withPlacedTile(pTile35);
        // GREEN places a PAWN in forest 35_1
        gameState = gameState.withNewOccupant(new Occupant(Occupant.Kind.PAWN, 35_1));
        // GREEN has now closed a forest!
        // Check closed forest points for majority occupant (which is just GREEN)
        //  Lorsqu'une forêt est fermée, les cueilleurs majoritaires remportent 2 points par tuile composant
        //  la forêt, et 3 points par groupe de champignons qu'elle contient.

        // means GREEN obtains 2 tiles x 2 points = 4 (no champignons)
        assertEquals(4,gameState.messageBoard().points().get(PlayerColor.GREEN));


        // turn: BLUE
        assertEquals(PlayerColor.BLUE, gameState.currentPlayer());
        // BLUE places tile 37 below 56 (0,1), no rotation
        PlacedTile pTile37 = new PlacedTile(tile37, PlayerColor.BLUE, Rotation.NONE, new Pos(0, 1));
        gameState = gameState.withPlacedTile(pTile37);
        // BLUE places a PAWN in forest 37_0
        gameState = gameState.withNewOccupant(new Occupant(Occupant.Kind.PAWN, 37_0));

        // turn: RED
        assertEquals(PlayerColor.RED, gameState.currentPlayer());
        // RED places tile 27 to the right of the start tile (1,0)
        PlacedTile pTile27 = new PlacedTile(tile27, PlayerColor.RED, Rotation.NONE, new Pos(1, 0));
        gameState = gameState.withPlacedTile(pTile27);
        // RED places a PAWN in meadow 27_2
        gameState = gameState.withNewOccupant(new Occupant(Occupant.Kind.PAWN, 27_2));
        // RED has now closed a forest with a menhir!
        // check closed forest points for majority occupant (which is blue)
        // BLUE obtains 3 tiles x 2 = 6 points
        assertEquals(6+6,gameState.messageBoard().points().get(PlayerColor.BLUE));


        // turn: still RED
        // given that a menhir forest has been closed, RED has again the right to move
        // check this!
        assertEquals(PlayerColor.RED, gameState.currentPlayer());
        assertEquals(GameState.Action.PLACE_TILE, gameState.nextAction());
        // RED will now play a MENHIR card
        // The next (first) MENHIR card on the menhir deck is the LOGBOAT / pirogue (93)
        // check that it is the next card to play
        assertEquals(menhirTile93LogBoat, gameState.tileToPlace());
        // RED places the LOGBOARD 93 tile on (-2,0)
        PlacedTile pMenhirTile93LogBoat = new PlacedTile(menhirTile93LogBoat, PlayerColor.RED, Rotation.NONE, new Pos(-2, 0));
        gameState = gameState.withPlacedTile(pMenhirTile93LogBoat);
        // RED places a PAWN on zone 93_3 (river)
        gameState = gameState.withNewOccupant(new Occupant(Occupant.Kind.PAWN, 93_3));
        // check that the deck of menhir tiles is now reduced by one
        assertEquals(initialTileDecks.deckSize(Tile.Kind.MENHIR) - 1, gameState.tileDecks().deckSize(Tile.Kind.MENHIR));

        // RED now obtains points for the logboat!
        // Check points for the log boat (pirogue)
        //  Lorsqu'un joueur pose la tuile contenant la pirogue, il obtient 2 points par lac
        //  du réseau hydrographique dont elle fait partie.

        // so RED obtains 1 lake x2 = 2 points
        assertEquals(2,gameState.messageBoard().points().get(PlayerColor.RED));



        // turn now: GREEN
        assertEquals(PlayerColor.GREEN, gameState.currentPlayer());
        // next action is PLACE_TILE
        assertEquals(GameState.Action.PLACE_TILE, gameState.nextAction());
        // next tile to play on the normal tile deck is tile 49
        assertEquals(tile49, gameState.tileToPlace());
        // GREEN places tile 49 below tile 17 (-1,1), turned to the left
        PlacedTile pTile49 = new PlacedTile(tile49, PlayerColor.GREEN, Rotation.LEFT, new Pos(-1, 1));
        gameState = gameState.withPlacedTile(pTile49);
        // GREEN places a PAWN on zone 49_0
        gameState = gameState.withNewOccupant(new Occupant(Occupant.Kind.PAWN, 49_0));

        // turn now: BLUE
        assertEquals(PlayerColor.BLUE, gameState.currentPlayer());
        // next tile to play on the normal tile deck is tile 61
        assertEquals(tile61, gameState.tileToPlace());
        // BLUE places tile 61 below tile 93 (-2,1), no rotation
        PlacedTile pTile61 = new PlacedTile(tile61, PlayerColor.BLUE, Rotation.NONE, new Pos(-2, 1));
        gameState = gameState.withPlacedTile(pTile61);
        // BLUE cannot place anything because the area to which 61 belongs to is already occupied

        // now there are no further normal tiles to play. The game should be now ended!
        assertEquals(GameState.Action.END_GAME, gameState.nextAction());
        // the current player should be NULL
        assertEquals(null, gameState.currentPlayer());

        // Check points at the end of the game:
        // final points:
        //  Les chasseurs majoritaires d'un pré remportent 3 points par mammouth, 2 par auroch et 1 par cerf qu'il
        //  contient. Toutefois, avant que les animaux ne soient comptés, chaque smilodon présent dans le pré dévore
        //  l'un de ses cerfs, qui ne rapporte alors aucun point.
        //  Les propriétaires des huttes majoritaires d'un réseau hydrographique remportent 1 point par poisson présent
        //  dans ce réseau. De plus, si ce réseau contient le radeau, ils remportent également 1 point par lac qu'il contient.

        // Prés: GREEN scores 3 (mammuth) + 1 (deer) - 1 (smilodon) = 3 points for meadow around tile 61
        // River systems: RED scores 2 (two fishes in reseau hydro on tile 0) + 1 (one fish in reseau hygro in 93) + and one point for deer in tile 27_2
        // BLUE does not score anything at the end of the game

        assertEquals(6+6,gameState.messageBoard().points().get(PlayerColor.BLUE)); // 12
        assertEquals(2+2+1+1,gameState.messageBoard().points().get(PlayerColor.RED)); // 6
        assertEquals(4+3,gameState.messageBoard().points().get(PlayerColor.GREEN)); // 7

        // the winner is: BLUE
        // check it - there should be also a corresponding message in the messageBoard

        assertEquals(new MessageBoard.Message(textMaker.playersWon(Set.of(PlayerColor.BLUE),12),0,Collections.emptySet(),Collections.emptySet()),
                gameState.messageBoard().messages().getLast());


    }


    /// In order to make the following tests more compact than the first one, we define below
    /// a couple of helper functions for faster creating a tile deck and placing tiles + occupants

    /**
     *  initialTileDeck: helper function for quickly creating the tile decks
     */

    TileDecks initialTileDeck(List<Integer> normalTileIDs, List<Integer> menhirTileIDs) {
        List<Tile> normalTiles = new ArrayList<>();
        for (Integer i:normalTileIDs) {
            normalTiles.add(Tiles4Tests.TILES.get(i));
        }
        List<Tile> menhirTiles = new ArrayList<>();
        for (Integer i:menhirTileIDs) {
            menhirTiles.add(Tiles4Tests.TILES.get(i));
        }
        return new TileDecks(List.of(Tiles4Tests.TILES.get(56)),normalTiles,menhirTiles);
    }


    /**
     * placeTileAndAddOccupant: helper function for fast placing a tile and an occupant
     */
    GameState placeTileAndAddOccupant(GameState gs, int tileID, Rotation rotation, int posx, int posy, PlayerColor color, Occupant.Kind occupantKind, int zoneID) {

        // check that the player color is really the next one to play
        assertEquals(color,gs.currentPlayer());
        // check that the tile to place is really the next one
        assertEquals(GameState.Action.PLACE_TILE,gs.nextAction());
        Tile tile = Tiles4Tests.TILES.get(tileID);
        assertEquals(tile, gs.tileToPlace());
        PlacedTile ptile = new PlacedTile(tile, color, rotation, new Pos(posx, posy));
        gs = gs.withPlacedTile(ptile);
        assertEquals(GameState.Action.OCCUPY_TILE,gs.nextAction());
        // if occupantKind is null, just set null as occupant
        if (occupantKind == null) {
            gs = gs.withNewOccupant(null);
        } else {
            gs = gs.withNewOccupant(new Occupant(occupantKind, zoneID));
        }
        return gs;
    }


    @Test
    void testClosingForestAndUsingShamanToRemoveAnOccupant() {

        // this test verifies that the SHAMAN power allows to correctly retract a pawn

        // tile decks: generate a deck set with normal (27,35,37) and one menhir (88 = SHAMAN) tiles
        TileDecks tdeck = initialTileDeck(List.of(27,35,37),List.of(88));
        // get the game state with the start tile already placed
        GameState gameState = GameState.initial(List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN), tdeck, new Jenss_BasicTextMaker()).withStartingTilePlaced();

        // BLUE: tile 27 to the right of tile 56, occupy it with a PAWN
        gameState = placeTileAndAddOccupant(gameState,27,Rotation.NONE,1,0,PlayerColor.BLUE, Occupant.Kind.PAWN,27_3);

        // RED: tile 35 below tile 56, rotated left
        gameState = placeTileAndAddOccupant(gameState,35,Rotation.LEFT,0,1,PlayerColor.RED, Occupant.Kind.PAWN,35_0);
        // RED closed forest with menhir!
        // RED plays now a menhir tile
        // AGAIN RED: place tile 88 (SHAMAN menhir tile) below 27
        gameState = gameState.withPlacedTile(new PlacedTile(Tiles4Tests.TILES.get(88),PlayerColor.RED,Rotation.NONE,new Pos(1,1)));
        // as the SHAMAN tile was placed, now we should be in state RETAKE_PAWN
        assertEquals(GameState.Action.RETAKE_PAWN,gameState.nextAction());

        // now test two possibilities:
        // a) retake no pawn, next state should be OCCUPY_TILE and next player RED
        assertEquals(GameState.Action.OCCUPY_TILE,gameState.withOccupantRemoved(null).nextAction());
        assertEquals(PlayerColor.RED,gameState.withOccupantRemoved(null).currentPlayer());

        // b) retake the pawn RED placed in zone 35_0
        gameState = gameState.withOccupantRemoved(new Occupant(Occupant.Kind.PAWN,35_0));
        //    check that the pawn was really given back (we should have again 5 free pawns)
        assertEquals(5,gameState.freeOccupantsCount(PlayerColor.RED, Occupant.Kind.PAWN));
        // next player should still be RED and next action OCCUPY_TILE
        assertEquals(PlayerColor.RED,gameState.currentPlayer());
        assertEquals(GameState.Action.OCCUPY_TILE,gameState.nextAction());
        // now occupy the tile 88 with a hut in the river
        gameState = gameState.withNewOccupant(new Occupant(Occupant.Kind.HUT,88_1));

        // RED has now finished its turn
        // next player should now be GREEN and next action PLACE_TILE
        assertEquals(PlayerColor.GREEN,gameState.currentPlayer());
        assertEquals(GameState.Action.PLACE_TILE,gameState.nextAction());

        // GREEN: tile 37 below 88, place pawn in 37_0
        gameState = placeTileAndAddOccupant(gameState,37,Rotation.NONE,1,2,PlayerColor.GREEN, Occupant.Kind.PAWN,37_0);
        // GREEN closed (normal) forest!

        // game is now finished as there are no normal tiles left
        assertEquals(GameState.Action.END_GAME, gameState.nextAction());
        // the current player should be NULL
        assertEquals(null, gameState.currentPlayer());

        // check: (maybe, not top priority) check all points
    }


    @Test
    void testClosingForestAndUsingShamanToRemoveAnOccupantButThereIsNoOccupantToRemove() {

        // similar to the previous one, but in this case testing that the gameState
        // acts correctly when there is no occupant to be removed by the player placing the Shaman tile

        // generate the normal (27,35,37) and menhir (88: shaman) tile decks
        TileDecks tdeck = initialTileDeck(List.of(27,35,37),List.of(88));
        // get the game state with the start tile already placed
        GameState gameState = GameState.initial(List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN), tdeck, new Jenss_BasicTextMaker()).withStartingTilePlaced();

        // BLUE: tile 27 to the right of tile 56, occupy it with a PAWN
        gameState = placeTileAndAddOccupant(gameState,27,Rotation.NONE,1,0,PlayerColor.BLUE, Occupant.Kind.PAWN,27_3);

        // RED: tile 35 below tile 56, rotated left
        // RED does NOT occupy the tile 35
        gameState = placeTileAndAddOccupant(gameState,35,Rotation.LEFT,0,1,PlayerColor.RED, null,0000);
        // RED closed forest with menhir!
        // RED plays now a menhir tile
        // AGAIN RED: place tile 88 (SHAMAN menhir tile) below 27
        gameState = gameState.withPlacedTile(new PlacedTile(Tiles4Tests.TILES.get(88),PlayerColor.RED,Rotation.NONE,new Pos(1,1)));

        // the SHAMAN tile was placed, but RED had not placed any pawn so far, so he cannot retake any
        // the next state should be OCCUPY_TILE and the next player RED
        assertEquals(GameState.Action.OCCUPY_TILE,gameState.nextAction());
        assertEquals(PlayerColor.RED,gameState.currentPlayer());

        // now occupy the tile 88 with a hut in the river
        gameState = gameState.withNewOccupant(new Occupant(Occupant.Kind.HUT,88_1));

        // RED has now finished its turn
        // next player should now be GREEN and next action PLACE_TILE
        assertEquals(PlayerColor.GREEN,gameState.currentPlayer());
        assertEquals(GameState.Action.PLACE_TILE,gameState.nextAction());

        // GREEN: tile 37 below 88, place pawn in 37_0
        gameState = placeTileAndAddOccupant(gameState,37,Rotation.NONE,1,2,PlayerColor.GREEN, Occupant.Kind.PAWN,37_0);
        // GREEN closed (normal) forest!

        // game is now finished as there are no normal tiles left
        assertEquals(GameState.Action.END_GAME, gameState.nextAction());
        // the current player should be NULL
        assertEquals(null, gameState.currentPlayer());

    }


    @Test
    void testClosingForestButNoMenhirTilesLeft() {

        // similar to the previous one, but without menhir tile:
        // check that the game correctly skips to the next tile if no menhir tiles are left over

        // generate the normal (27,35,37) and menhir (NONE) tile decks
        TileDecks tdeck = initialTileDeck(List.of(27,35,37),List.of());
        // get the game state with the start tile already placed
        GameState gameState = GameState.initial(List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN), tdeck, new Jenss_BasicTextMaker()).withStartingTilePlaced();

        // BLUE: tile 27 to the right of tile 56, occupy it with a PAWN
        gameState = placeTileAndAddOccupant(gameState,27,Rotation.NONE,1,0,PlayerColor.BLUE, Occupant.Kind.PAWN,27_3);

        // RED: tile 35 below tile 56, occupy it with a PAWN
        gameState = placeTileAndAddOccupant(gameState,35,Rotation.LEFT,0,1,PlayerColor.RED, Occupant.Kind.PAWN,35_0);
        // RED closed forest with menhir!
        // RED should play now a menhir tile - but there are no menhir tiles left!
        // therefore the next player goes to GREEN.

        // next player should be GREEN now and next movement should be PLACE_TILE
        assertEquals(PlayerColor.GREEN,gameState.currentPlayer());
        assertEquals(GameState.Action.PLACE_TILE,gameState.nextAction());
        // the next tile to play should be the (normal) tile 37
        assertEquals(Tiles4Tests.TILES.get(37),gameState.tileToPlace());
    }



    @Test
    void testHuntingTrap() {

        // check that the hunting trap (fosse à pieux) works correctly

        // generate the normal and menhir (94: fosse a pieux) tile decks
        TileDecks tdeck = initialTileDeck(List.of(37,49,62,35,60,0),List.of(94));
        // get the game state with the start tile already placed
        GameState gameState = GameState.initial(List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN), tdeck, new Jenss_BasicTextMaker()).withStartingTilePlaced();

        // BLUE: tile 37 to the right of tile 56, turn left, occupy it with a PAWN
        gameState = placeTileAndAddOccupant(gameState,37,Rotation.LEFT,1,0,PlayerColor.BLUE, Occupant.Kind.PAWN,37_0);

        // RED: tile 49 below 37, PAWN
        gameState = placeTileAndAddOccupant(gameState,49,Rotation.NONE,1,1,PlayerColor.RED, Occupant.Kind.PAWN,49_0);

        // GREEN: tile 62 below 49, PAWN
        gameState = placeTileAndAddOccupant(gameState,62,Rotation.NONE,1,2,PlayerColor.GREEN, Occupant.Kind.PAWN,62_0);

        // BLUE: tile 35 below 62, PAWN
        gameState = placeTileAndAddOccupant(gameState,35,Rotation.NONE,1,3,PlayerColor.BLUE, Occupant.Kind.PAWN,35_1);

        // RED: tile 60 rotated left, left to 35, NO occupant
        gameState = placeTileAndAddOccupant(gameState,60,Rotation.LEFT,0,3,PlayerColor.RED, null,0000);

        // GREEN: tile 0 below 56, PAWN
        // this closes a MENHIR forest!
        gameState = placeTileAndAddOccupant(gameState,0,Rotation.RIGHT,0,1,PlayerColor.GREEN, Occupant.Kind.PAWN,3);

        // BLUE obtains 3 tiles x 2 = 6 points for the closed forest
        assertEquals(6,gameState.messageBoard().points().get(PlayerColor.BLUE));

        // GREEN should play again, this time the menhir tile (fosse a pieux), placing a PAWN

        gameState = placeTileAndAddOccupant(gameState,94,Rotation.HALF_TURN,0,2,PlayerColor.GREEN, Occupant.Kind.PAWN,94_0);

        //  Check points, and that "fosse a pieux" points are computed correctly,
        //  taking into account the two deers, the mammouth and the tiger that are in the adjacent meadow

        // Lorsqu'un joueur pose la tuile contenant la fosse à pieu, il remporte les points correspondant aux éventuels
        // animaux qui se trouvent dans le même pré que la fosse, et sur l'une des 8 tuiles voisines.
        // Dans tous les cas, les animaux se trouvant dans le même pré que la fosse et sur l'une des 8 tuiles voisines
        // sont ensuite annulés, c.-à-d. qu'ils sont ignorés pour le reste de la partie.

        // GREEN should score 2 x 1 points (deers) - 1 tiger + 3 points (Mammoth) = 4 points
        // assertEquals(4,gameState.messageBoard().points().get(PlayerColor.GREEN));

        //  Check that ALL the animals in the adjacent meadow are now cancelled!
        //  Une fois les points décomptés, tous les animaux présents dans le pré adjacent à la fosse sont annulés (cancelled),
        //  c.-à-d. ignorés pour le reste de la partie.

        Set<Animal> expectedCancelledAnimals = Set.of(
                new Animal(35_0_0, Animal.Kind.MAMMOTH),
                new Animal(49_2_0, Animal.Kind.TIGER),
                new Animal(62_0_0, Animal.Kind.DEER),
                new Animal(60_2_0, Animal.Kind.DEER)
                );
        assertEquals(expectedCancelledAnimals,gameState.board().cancelledAnimals());

        // game is now finished as there are no normal tiles left
        assertEquals(GameState.Action.END_GAME, gameState.nextAction());
        // the current player should be NULL
        assertEquals(null, gameState.currentPlayer());

        // Count final points:
        // for BLUE and GREEN there should be no difference
        // for RED, scores 2 x 1 points for the two deers in 49 and 37
        assertEquals(6,gameState.messageBoard().points().get(PlayerColor.BLUE));
        assertEquals(2,gameState.messageBoard().points().get(PlayerColor.RED));
        assertEquals(4,gameState.messageBoard().points().get(PlayerColor.GREEN));
    }


    @Test
    void testWildFire() {

        // check that the wildfire works correctly

        // generate the normal and menhir (85: Wildfire) tile decks
        TileDecks tdeck = initialTileDeck(List.of(37,49,62,0),List.of(85));
        // get the game state with the start tile already placed
        GameState gameState = GameState.initial(List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN), tdeck, new Jenss_BasicTextMaker()).withStartingTilePlaced();

        // BLUE: tile 37 to the right of tile 56, occupy it with a PAWN
        gameState = placeTileAndAddOccupant(gameState,37,Rotation.LEFT,1,0,PlayerColor.BLUE, Occupant.Kind.PAWN,37_0);

        // RED: tile 49 below 37, PAWN
        gameState = placeTileAndAddOccupant(gameState,49,Rotation.NONE,1,1,PlayerColor.RED, Occupant.Kind.PAWN,49_0);

        // GREEN: tile 62 below 49, no occupant
        gameState = placeTileAndAddOccupant(gameState,62,Rotation.NONE,1,2,PlayerColor.GREEN, null,0000);

        // BLUE: tile 0 below 56, PAWN
        // this closes a MENHIR forest!
        gameState = placeTileAndAddOccupant(gameState,0,Rotation.RIGHT,0,1,PlayerColor.BLUE, Occupant.Kind.PAWN,3);
        // BLUE obtains 3 tiles x 2 = 6 points for the closed forest
        assertEquals(6,gameState.messageBoard().points().get(PlayerColor.BLUE));

        // BLUE should play again, this time the Wildfire tile, placing a PAWN

        gameState = placeTileAndAddOccupant(gameState,85,Rotation.NONE,0,2,PlayerColor.BLUE, Occupant.Kind.PAWN,85_0);

        // game is now finished as there are no normal tiles left
        assertEquals(GameState.Action.END_GAME, gameState.nextAction());
        // the current player should be NULL
        assertEquals(null, gameState.currentPlayer());

        // Check points, taking into account the wildfire, the deer and the (disappeared) tiger of tile 49

        // BLUE is the majority (actually only) occupant of the meadow with the wild fire so all points go to it
        //
        // BLUE should score 1 points (deers) - 0 tiger (as the one in tile 49 flew) = 1 points
        assertEquals(6 + 1,gameState.messageBoard().points().get(PlayerColor.BLUE));
        // RED scores two deers in 37 and 49
        assertEquals(2,gameState.messageBoard().points().get(PlayerColor.RED));
        // GREEN didn't place any occupants
        assertEquals(null,gameState.messageBoard().points().get(PlayerColor.GREEN));
    }


    @Test
    void testNoThreeTurnsAllowed() {

        // - check that the player cannot play 3 tours in a row even if he closes a menhir forest with a menhir card

        // generate the normal and menhir tile decks
        TileDecks tdeck = initialTileDeck(List.of(37,35,60,27,61),List.of(90));
        // get the game state with the start tile already placed
        GameState gameState = GameState.initial(List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN), tdeck, new Jenss_BasicTextMaker()).withStartingTilePlaced();

        // BLUE: tile 37 to the right of tile 56, occupy it with a PAWN
        gameState = placeTileAndAddOccupant(gameState,37,Rotation.LEFT,1,0,PlayerColor.BLUE, Occupant.Kind.PAWN,37_0);

        // RED: tile 35 below 37, PAWN
        gameState = placeTileAndAddOccupant(gameState,35,Rotation.RIGHT,1,1,PlayerColor.RED, Occupant.Kind.PAWN,35_1);

        // GREEN: tile 60 below 35, rotated left, PAWN
        gameState = placeTileAndAddOccupant(gameState,60,Rotation.LEFT,1,2,PlayerColor.GREEN, Occupant.Kind.PAWN,60_2);

        // BLUE: tile 27 below 56, rotated right, PAWN
        // this closes a MENHIR forest!
        gameState = placeTileAndAddOccupant(gameState,27,Rotation.RIGHT,0,1,PlayerColor.BLUE, Occupant.Kind.PAWN,27_2);

        // BLUE should play again, this time the menhir tile 90, rotaded half turn, not placing anything
        gameState = placeTileAndAddOccupant(gameState,90,Rotation.HALF_TURN,1,3,PlayerColor.BLUE, null,0000);
        // this closes AGAIN a MENHIR forest, but BLUE has already played twice!
        // therefore the next player needs to be RED

        assertEquals(PlayerColor.RED,gameState.currentPlayer());
        assertEquals(GameState.Action.PLACE_TILE,gameState.nextAction());
        // the next tile to play should be the (normal) tile 61
        assertEquals(Tiles4Tests.TILES.get(61),gameState.tileToPlace());

        // RED plays tile 61, placing a pawn
        gameState = placeTileAndAddOccupant(gameState,61,Rotation.NONE,1,4,PlayerColor.RED, Occupant.Kind.PAWN,61_0);

        // game is now finished as there are no normal tiles left
        assertEquals(GameState.Action.END_GAME, gameState.nextAction());
        // the current player should be NULL
        assertEquals(null, gameState.currentPlayer());

    }


    @Test
    void testRaft() {

        // - check that the raft (radeau) can be played and scored correctly

        // generate the normal and menhir tile decks
        TileDecks tdeck = initialTileDeck(List.of(37,27),List.of(91));
        // get the game state with the start tile already placed
        GameState gameState = GameState.initial(List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN), tdeck, new Jenss_BasicTextMaker()).withStartingTilePlaced();

        // BLUE: tile 37 to the right of tile 56, occupy it with a PAWN
        gameState = placeTileAndAddOccupant(gameState,37,Rotation.LEFT,1,0,PlayerColor.BLUE, Occupant.Kind.PAWN,37_0);

        // RED: tile 27 below 56, PAWN
        // this closes a MENHIR forest!
        gameState = placeTileAndAddOccupant(gameState,27,Rotation.RIGHT,0,1,PlayerColor.RED, Occupant.Kind.PAWN,27_2);

        // BLUE obtains 3 tiles x 2 = 6 points
        assertEquals(6,gameState.messageBoard().points().get(PlayerColor.BLUE));

        // RED should play again, this time the menhir tile 91 (RAFT), placing a HUT
        gameState = placeTileAndAddOccupant(gameState,91,Rotation.NONE,-1,0,PlayerColor.RED, Occupant.Kind.HUT,91_8);

        // game is now finished as there are no normal tiles left
        assertEquals(GameState.Action.END_GAME, gameState.nextAction());
        // the current player should be NULL
        assertEquals(null, gameState.currentPlayer());

        // Check points at the end, taking into account the raft (radeau):
        // Les joueurs possédant les huttes majoritaires d'un réseau hydrographique obtiennent un point par poisson qui y nage.
        // De plus, si le réseau hydrographique contient le radeau, alors les propriétaires des huttes majoritaires
        // obtiennent en plus un point par lac qu'il contient.

        // RED has a hutte on tile 91
        // points for RED: 2 x 1 (fish) + 2 x 1 (lacs due to radeau) + 1 deer in 27_2 = 5
        assertEquals(6,gameState.messageBoard().points().get(PlayerColor.BLUE));
        assertEquals(5,gameState.messageBoard().points().get(PlayerColor.RED));
        assertEquals(null,gameState.messageBoard().points().get(PlayerColor.GREEN));
    }

    @Test
    void testPitTrap() {

        // - check that the pit trap (grande fosse a pieux) can be played and is scored correctly

        // generate the normal and menhir tile decks
        TileDecks tdeck = initialTileDeck(List.of(37,62,49,27),List.of(92));
        // get the game state with the start tile already placed
        GameState gameState = GameState.initial(List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN), tdeck, new Jenss_BasicTextMaker()).withStartingTilePlaced();

        // BLUE: tile 37 to the right of tile 56, turned left, not occupied
        gameState = placeTileAndAddOccupant(gameState,37,Rotation.LEFT,1,0,PlayerColor.BLUE, null,0000);

        // RED: tile 62 right to 37, no occupant
        gameState = placeTileAndAddOccupant(gameState,62,Rotation.NONE,2,0,PlayerColor.RED, null,0000);

        // GREEN: tile 49 right to 62, turned left, PAWN
        gameState = placeTileAndAddOccupant(gameState,49,Rotation.LEFT,3,0,PlayerColor.GREEN, Occupant.Kind.PAWN,49_2);

        // BLUE: tile 27 below 56, turned right, PAWN
        gameState = placeTileAndAddOccupant(gameState,27,Rotation.RIGHT,0,1,PlayerColor.BLUE, Occupant.Kind.PAWN,27_3);
        // this closes a menhir forest!
        // blue should get 6 points (3x2) for closing the forest
        assertEquals(6,gameState.messageBoard().points().get(PlayerColor.BLUE));

        // Blue to play again the menhir card (92: pit trap - grande fosse a pieux) below 37, rotated right, PAWN
        gameState = placeTileAndAddOccupant(gameState,92,Rotation.RIGHT,1,1,PlayerColor.BLUE, Occupant.Kind.PAWN,92_1);


        // game is now finished as there are no normal tiles left
        assertEquals(GameState.Action.END_GAME, gameState.nextAction());
        // the current player should be NULL
        assertEquals(null, gameState.currentPlayer());

        // Check points at end of game, taking into account everything related to grande fosse a pieux
        //   Les joueurs possédant les chasseurs majoritaires d'un pré obtiennent un nombre de points qui dépend des
        //   animaux qui s'y trouvent, selon les mêmes règles que pour la fosse à pieux: les cerfs mangés par
        //   les smilodons sont ignorés, et les points sont attribués en fonction des animaux restants.

        // De plus, lorsqu'un pré contient la grande fosse à pieux, les chasseurs majoritaires obtiennent une seconde
        // fois les points associés aux animaux présents dans le pré adjacent à la fosse. Afin de maximiser ces points
        // additionnels, lorsque la grande fosse à pieux se trouve dans un pré, les cerfs sont annulés en commençant par
        // ceux qui ne se trouvent pas dans le pré adjacent à la fosse.

        // The majority occupant of the meadow area around the pit trap is BLUE

        // BLUE gets for the full meadow area:
        // 3 deers = 3 points
        // BLUE gets in addition for the adjacent meadow area to the pit trap:
        // 2 deers = 2 points

        assertEquals(6 + 3 + 2,gameState.messageBoard().points().get(PlayerColor.BLUE));
        assertEquals(null,gameState.messageBoard().points().get(PlayerColor.RED));
        assertEquals(null,gameState.messageBoard().points().get(PlayerColor.GREEN));
    }



    @Test
    void testPitTrapWithTigerOutsideAdjacentMeadow() {

        // - As testPitTrap(), but with one additional tile outside the adjacent meadow, that contains a tiger

        // generate the normal and menhir tile decks
        TileDecks tdeck = initialTileDeck(List.of(37,62,49,27,17),List.of(92));
        // get the game state with the start tile already placed
        GameState gameState = GameState.initial(List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN), tdeck, new Jenss_BasicTextMaker()).withStartingTilePlaced();

        // BLUE: tile 37 to the right of tile 56, turned left, not occupied
        gameState = placeTileAndAddOccupant(gameState,37,Rotation.LEFT,1,0,PlayerColor.BLUE, null,0000);

        // RED: tile 62 right to 37, no occupant
        gameState = placeTileAndAddOccupant(gameState,62,Rotation.NONE,2,0,PlayerColor.RED, null,0000);

        // GREEN: tile 49 right to 62, turned left, PAWN
        gameState = placeTileAndAddOccupant(gameState,49,Rotation.LEFT,3,0,PlayerColor.GREEN, Occupant.Kind.PAWN,49_2);

        // BLUE: tile 27 below 56, turned right, PAWN
        gameState = placeTileAndAddOccupant(gameState,27,Rotation.RIGHT,0,1,PlayerColor.BLUE, Occupant.Kind.PAWN,27_3);
        // this closes a menhir forest!
        // blue should get 6 points (3x2) for closing the forest
        assertEquals(6,gameState.messageBoard().points().get(PlayerColor.BLUE));

        // Blue to play again the menhir card (92: pit trap - grande fosse a pieux) below 37, rotated right, PAWN
        gameState = placeTileAndAddOccupant(gameState,92,Rotation.RIGHT,1,1,PlayerColor.BLUE, Occupant.Kind.PAWN,92_1);


        // RED: tile 17 over tile 49, PAWN
        gameState = placeTileAndAddOccupant(gameState,17,Rotation.NONE,3,-1,PlayerColor.RED, Occupant.Kind.PAWN,17_2);

        // game is now finished as there are no normal tiles left
        assertEquals(GameState.Action.END_GAME, gameState.nextAction());
        // the current player should be NULL
        assertEquals(null, gameState.currentPlayer());

        // Check points at end of game, taking into account everything related to grande fosse a pieux
        //   Les joueurs possédant les chasseurs majoritaires d'un pré obtiennent un nombre de points qui dépend des
        //   animaux qui s'y trouvent, selon les mêmes règles que pour la fosse à pieux: les cerfs mangés par
        //   les smilodons sont ignorés, et les points sont attribués en fonction des animaux restants.

        // De plus, lorsqu'un pré contient la grande fosse à pieux, les chasseurs majoritaires obtiennent une seconde
        // fois les points associés aux animaux présents dans le pré adjacent à la fosse. Afin de maximiser ces points
        // additionnels, lorsque la grande fosse à pieux se trouve dans un pré, les cerfs sont annulés en commençant par
        // ceux qui ne se trouvent pas dans le pré adjacent à la fosse.

        // The majority occupant of the meadow area around the pit trap is BLUE

        // BLUE gets for the full meadow area:
        // 3 deers -1 tiger = 2 points
        // BLUE gets in addition for the adjacent meadow area to the pit trap:
        // 2 deers = 2 points (because the third deer on tile 49, which is the one outside the adjacent area, is the one eaten by the tiger)
        // RED gets one point for the deer in 17
        // GREEN gets nothing for the tiger in 49

        assertEquals(1,gameState.messageBoard().points().get(PlayerColor.RED));
        assertEquals(6 + 2 + 2,gameState.messageBoard().points().get(PlayerColor.BLUE));
        assertEquals(null,gameState.messageBoard().points().get(PlayerColor.GREEN));
    }

    @Test
    void testPitTrapWithTigerInAdjacentMeadow() {

        // - check that the pit trap (grande fosse a pieux) can be played and is scored correctly
        // - but in this case, there is a tiger in the adjacent meadow that needs to cancel
        //   a deer in the same area but outside the adjacent meadow

        // generate the normal and menhir tile decks
        TileDecks tdeck = initialTileDeck(List.of(37,49,62,27),List.of(92));
        // get the game state with the start tile already placed
        GameState gameState = GameState.initial(List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN), tdeck, new Jenss_BasicTextMaker()).withStartingTilePlaced();

        // BLUE: tile 37 to the right of tile 56, no occupant
        gameState = placeTileAndAddOccupant(gameState,37,Rotation.LEFT,1,0,PlayerColor.BLUE, null,0000);

        // RED: tile 49 right to 37, turned right, PAWN
        gameState = placeTileAndAddOccupant(gameState,49,Rotation.RIGHT,2,0,PlayerColor.RED, null,0000);

        // GREEN: tile 62 on top of 37, no occupant
        gameState = placeTileAndAddOccupant(gameState,62,Rotation.NONE,1,-1,PlayerColor.GREEN, null,0000);

        // BLUE: tile 27 below 56, no occupant
        gameState = placeTileAndAddOccupant(gameState,27,Rotation.RIGHT,0,1,PlayerColor.BLUE, null,0000);
        // this closes a menhir forest!
        // NO points allocated as no occupant


        // Blue to play again the menhir card (92: pit trap - grande fosse a pieux) and occupies it with a PAWN
        gameState = placeTileAndAddOccupant(gameState,92,Rotation.RIGHT,1,1,PlayerColor.BLUE, Occupant.Kind.PAWN,92_1);


        // game is now finished as there are no normal tiles left
        assertEquals(GameState.Action.END_GAME, gameState.nextAction());
        // the current player should be NULL
        assertEquals(null, gameState.currentPlayer());

        // Check points at end of game, taking into account everything related to grande fosse a pieux
        //   Les joueurs possédant les chasseurs majoritaires d'un pré obtiennent un nombre de points qui dépend des
        //   animaux qui s'y trouvent, selon les mêmes règles que pour la fosse à pieux: les cerfs mangés par
        //   les smilodons sont ignorés, et les points sont attribués en fonction des animaux restants.

        // De plus, lorsqu'un pré contient la grande fosse à pieux, les chasseurs majoritaires obtiennent une seconde
        // fois les points associés aux animaux présents dans le pré adjacent à la fosse. Afin de maximiser ces points
        // additionnels, lorsque la grande fosse à pieux se trouve dans un pré, les cerfs sont annulés en commençant par
        // ceux qui ne se trouvent pas dans le pré adjacent à la fosse.

        // The majority occupant of the meadow area around the pit trap is BLUE

        // BLUE gets for the full meadow area:
        // 2 deers - 1 tiger = 1 point
        // BLUE gets in addition for the adjacent meadow area to the pit trap:
        // 1 deer (because it is the other deer in tile 62 (outside adjacent meadow) that is eaten up by the tiger in 49,
        //   and NOT the deer in tile 37) = 1 point

        assertEquals(1 + 1,gameState.messageBoard().points().get(PlayerColor.BLUE));
        assertEquals(null,gameState.messageBoard().points().get(PlayerColor.RED));
        assertEquals(null,gameState.messageBoard().points().get(PlayerColor.GREEN));
    }



    // check: test if you have multiple menhir cards in the same pre?
    //  e.g. 94M (hunting trap, should cancel) and then later pit_trap. Actually it is sufficient to
    //  check in the code that the cancelled animals are ALWAYS ignored when attributing points!

    // check: check that the turn finishes when a player cannot place an occupant (neither PAWN nor HUT)


    @Test
    void testSkipTile() {

        // - check that a tile that cannot be placed is skipped over correctly

        // generate the normal and menhir (empty) tile decks
        // note that tile 17 appears multiple times, this is intentional
        TileDecks tdeck = initialTileDeck(List.of(0,17,17,17,35),List.of());
        // get the game state with the start tile already placed
        GameState gameState = GameState.initial(List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN), tdeck, new Jenss_BasicTextMaker()).withStartingTilePlaced();

        // BLUE: tile 0 to the left of tile 56, occupy it with a PAWN
        gameState = placeTileAndAddOccupant(gameState,0,Rotation.RIGHT,-1,0,PlayerColor.BLUE, Occupant.Kind.PAWN,0);

        // RED: next tile to play should be 17, but it cannot be placed anywhere as its sides are not compatible with the sides of 56 nor 0
        // therefore 17 must be skipped, and the next tile to play should be 35
        // for the sake of testing, 17 appears several times in the deck (impossible in the real game)

        // next player should be RED now and next movement should be PLACE_TILE
        assertEquals(PlayerColor.RED,gameState.currentPlayer());
        assertEquals(GameState.Action.PLACE_TILE,gameState.nextAction());
        // the next tile to play should be the (normal) tile 35
        assertEquals(Tiles4Tests.TILES.get(35),gameState.tileToPlace());

        // RED to place tile 35

        gameState = placeTileAndAddOccupant(gameState,35,Rotation.NONE,0,-1,PlayerColor.RED, null,0);

        // game is now finished as there are no normal tiles left
        assertEquals(GameState.Action.END_GAME, gameState.nextAction());
        // the current player should be NULL
        assertEquals(null, gameState.currentPlayer());

    }


}


/********************************************************* END OF TESTS *******************************************/

//
// Helper TextMaker class - copy/pasted from MessageBoardTest with printing of scoring added
//


class Jenss_BasicTextMaker implements TextMaker {
    private static String scorers(Set<PlayerColor> scorers) {
        String returnString = scorers.stream()
                .sorted()
                .map(Object::toString)
                .collect(Collectors.joining(",", "{", "}"));
        // System.out.println("MessageBoard SCORERS: "+returnString);
        return returnString;
    }

    private static String animals(Map<Animal.Kind, Integer> animals) {
        String returnString =  Arrays.stream(Animal.Kind.values())
                .map(k -> animals.getOrDefault(k, 0) + "×" + k)
                .collect(Collectors.joining("/"));
        // System.out.println("MessageBoard ANIMALS: "+returnString);
        return returnString;
    }

    @Override
    public String playerName(PlayerColor playerColor) {
        return playerColor.name();
    }

    @Override
    public String points(int points) {
        return String.valueOf(points);
    }

    @Override
    public String playerClosedForestWithMenhir(PlayerColor player) {
//        System.out.println("MessageBoard playerClosedForestWithMenhir: " + playerName(player));
        return playerName(player);
    }

    @Override
    public String playersScoredForest(Set<PlayerColor> scorers,
                                      int points,
                                      int mushroomGroupCount,
                                      int tileCount) {
        String returnString =  String.join("|",
                scorers(scorers),
                points(points),
                String.valueOf(mushroomGroupCount),
                String.valueOf(tileCount));
 //       System.out.println("MessageBoard playersScoredForest: " + returnString);
        return returnString;
    }

    @Override
    public String playersScoredRiver(Set<PlayerColor> scorers,
                                     int points,
                                     int fishCount,
                                     int tileCount) {
        String returnString = String.join("|",
                scorers(scorers),
                points(points),
                String.valueOf(fishCount),
                String.valueOf(tileCount));
  //      System.out.println("MessageBoard playersScoredRiver: " + returnString);
        return returnString;
    }

    @Override
    public String playerScoredHuntingTrap(PlayerColor scorer,
                                          int points,
                                          Map<Animal.Kind, Integer> animals) {
        String returnString = String.join("|",
                playerName(scorer),
                String.valueOf(points),
                animals(animals));
  //      System.out.println("MessageBoard playerScoredHuntingTrap: " + returnString);
        return returnString;
    }

    @Override
    public String playerScoredLogboat(PlayerColor scorer, int points, int lakeCount) {
        String returnString = String.join("|",
                playerName(scorer),
                points(points),
                String.valueOf(lakeCount));
  //      System.out.println("MessageBoard playerScoredLogboat: " + returnString);
        return returnString;

    }

    @Override
    public String playersScoredMeadow(Set<PlayerColor> scorers,
                                      int points,
                                      Map<Animal.Kind, Integer> animals) {
        String returnString = String.join(
                "|",
                scorers(scorers),
                points(points),
                animals(animals));
    //    System.out.println("MessageBoard playersScoredMeadow: " + returnString);
        return returnString;
    }

    @Override
    public String playersScoredRiverSystem(Set<PlayerColor> scorers, int points, int fishCount) {
        String returnString = String.join(
                "|",
                scorers(scorers),
                points(points),
                String.valueOf(fishCount));
   //     System.out.println("MessageBoard playersScoredRiverSystem: " + returnString);
        return returnString;
    }

    @Override
    public String playersScoredPitTrap(Set<PlayerColor> scorers,
                                       int points,
                                       Map<Animal.Kind, Integer> animals) {
        String returnString = String.join("|",
                scorers(scorers),
                String.valueOf(points),
                animals(animals));
//        System.out.println("MessageBoard playersScoredPitTrap: " + returnString);
        return returnString;
    }

    @Override
    public String playersScoredRaft(Set<PlayerColor> scorers, int points, int lakeCount) {
        String returnString = String.join("|",
                scorers(scorers),
                String.valueOf(points),
                String.valueOf(lakeCount));
//        System.out.println("MessageBoard playersScoredRaft: " + returnString);
        return returnString;
    }

    @Override
    public String playersWon(Set<PlayerColor> winners, int points) {
        String returnString = String.join("|",
                scorers(winners),
                points(points));
 //       System.out.println("MessageBoard playersWon: " + returnString);
        return returnString;
    }

    @Override
    public String clickToOccupy() {
        return "clickToOccupy";
    }

    @Override
    public String clickToUnoccupy() {
        return "clickToUnoccupy";
    }
}
