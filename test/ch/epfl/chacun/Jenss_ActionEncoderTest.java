package ch.epfl.chacun;

import ch.epfl.chacun.tile.Tiles4Tests;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Jenss_ActionEncoderTest
{


    TextMaker frenchTextMaker = new TextMakerFr(Map.of(
            PlayerColor.RED,"Dalia",
            PlayerColor.BLUE,"Claude",
            PlayerColor.GREEN,"Bachir",
            PlayerColor.YELLOW,"Alice"));

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
    ActionEncoder.StateAction placeTileActionEncoder(GameState gs, int tileID, Rotation rotation, int posx, int posy, PlayerColor color) {

        // check that the player color is really the next one to play
        assertEquals(color,gs.currentPlayer());
        // check that the tile to place is really the next one
        assertEquals(GameState.Action.PLACE_TILE,gs.nextAction());
        Tile tile = Tiles.TILES.get(tileID);
        assertEquals(tile, gs.tileToPlace());
        PlacedTile ptile = new PlacedTile(tile, color, rotation, new Pos(posx, posy));
        return ActionEncoder.withPlacedTile(gs,ptile);
    }

    ActionEncoder.StateAction occupyTileActionEncoder(GameState gs, Occupant.Kind occupantKind, int zoneID) {
        // if occupantKind is null, just set null as occupant
        Occupant occupant = null;
        if (occupantKind != null) {
            occupant = new Occupant(occupantKind, zoneID);
        }
        return ActionEncoder.withNewOccupant(gs,occupant);
    }

    ActionEncoder.StateAction occupantRemovedTileActionEncoder(GameState gs, Occupant.Kind occupantKind, int zoneID) {
        // if occupantKind is null, just set null as occupant
        Occupant occupant = null;
        if (occupantKind != null) {
            occupant = new Occupant(occupantKind, zoneID);
        }
        return ActionEncoder.withOccupantRemoved(gs,occupant);
    }




    @Test
    void simpleExampleTest() {
        // as in 2.2.6 exemple https://cs108.epfl.ch/p/10_remote-play.html

        TileDecks tdeck = initialTileDeck(List.of(49,0),List.of());
        // get the game state with the start tile already placed
        GameState gameState = GameState.initial(List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN), tdeck, frenchTextMaker).withStartingTilePlaced();

        ActionEncoder.StateAction stateAction = new ActionEncoder.StateAction(gameState,null);

        // place tile 49 with half turn
        stateAction = placeTileActionEncoder(stateAction.gameState(),49,Rotation.HALF_TURN,0,-1,PlayerColor.BLUE);
        // verify that the base32 encoded action corresponds to 0b00000_00110 (meaning: insertion position 00000001b (1d) and rotation 10b (2d))
        assertEquals(Base32.encodeBits10(0b00000_00110),stateAction.base32Action());

        // occupy the tile with a pawn on zone 2
        stateAction = occupyTileActionEncoder(stateAction.gameState(),Occupant.Kind.PAWN,49_2);
        // verify that the base32 encoded action corresponds to 0b00010 (meaning: kind 0b (0d, pawn) and zone 10b (2d))
        assertEquals(Base32.encodeBits5(0b00010),stateAction.base32Action());

    }


    @Test
    void createActionsAndReplayTest() {

        TileDecks tdeck = initialTileDeck(List.of(37,49,62,35,60,0,27),List.of(88));

        List<String> base32Actions = new ArrayList<>(); // this is where we will add all recorded actions
        List<Set<Occupant>> occupants = new ArrayList<>();

        // get the game state with the start tile already placed
        GameState gameStateInitial = GameState.initial(List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN), tdeck, frenchTextMaker).withStartingTilePlaced();

        ActionEncoder.StateAction stateAction = new ActionEncoder.StateAction(gameStateInitial,null);

        stateAction = placeTileActionEncoder(stateAction.gameState(),37,Rotation.LEFT,1,0,PlayerColor.BLUE);
        base32Actions.add(stateAction.base32Action()); // record action
        occupants.add(stateAction.gameState().board().occupants());
        stateAction = occupyTileActionEncoder(stateAction.gameState(),Occupant.Kind.PAWN,37_0);
        base32Actions.add(stateAction.base32Action()); // record action
        occupants.add(stateAction.gameState().board().occupants());

        stateAction = placeTileActionEncoder(stateAction.gameState(),49,Rotation.NONE,1,1,PlayerColor.RED);
        base32Actions.add(stateAction.base32Action()); // etc
        occupants.add(stateAction.gameState().board().occupants());
        stateAction = occupyTileActionEncoder(stateAction.gameState(),Occupant.Kind.PAWN,49_0);
        base32Actions.add(stateAction.base32Action()); // etc
        occupants.add(stateAction.gameState().board().occupants());

        stateAction = placeTileActionEncoder(stateAction.gameState(),62,Rotation.NONE,1,2,PlayerColor.GREEN);
        base32Actions.add(stateAction.base32Action());
        occupants.add(stateAction.gameState().board().occupants());
        stateAction = occupyTileActionEncoder(stateAction.gameState(),Occupant.Kind.PAWN,62_0);
        base32Actions.add(stateAction.base32Action());
        occupants.add(stateAction.gameState().board().occupants());

        stateAction = placeTileActionEncoder(stateAction.gameState(),35,Rotation.NONE,1,3,PlayerColor.BLUE);
        base32Actions.add(stateAction.base32Action());
        occupants.add(stateAction.gameState().board().occupants());
        stateAction = occupyTileActionEncoder(stateAction.gameState(),Occupant.Kind.PAWN,35_1);
        base32Actions.add(stateAction.base32Action());
        occupants.add(stateAction.gameState().board().occupants());

        stateAction = placeTileActionEncoder(stateAction.gameState(),60,Rotation.LEFT,0,3,PlayerColor.RED);
        base32Actions.add(stateAction.base32Action());
        occupants.add(stateAction.gameState().board().occupants());
        stateAction = occupyTileActionEncoder(stateAction.gameState(),null,0000);
        base32Actions.add(stateAction.base32Action());
        occupants.add(stateAction.gameState().board().occupants());

        stateAction = placeTileActionEncoder(stateAction.gameState(),0,Rotation.RIGHT,0,1,PlayerColor.GREEN);
        base32Actions.add(stateAction.base32Action());
        occupants.add(stateAction.gameState().board().occupants());
        stateAction = occupyTileActionEncoder(stateAction.gameState(),Occupant.Kind.PAWN,3);
        base32Actions.add(stateAction.base32Action());
        occupants.add(stateAction.gameState().board().occupants());

        // the previous tile closes a MENHIR forest!
        // BLUE obtains 3 tiles x 2 = 6 points for the closed forest

        // next tile: SHAMAN (88)
        stateAction = placeTileActionEncoder(stateAction.gameState(),88,Rotation.HALF_TURN,2,1,PlayerColor.GREEN);
        base32Actions.add(stateAction.base32Action());
        occupants.add(stateAction.gameState().board().occupants());
        // player GREEN removes occupant in tile 0 zone 3
        stateAction = occupantRemovedTileActionEncoder(stateAction.gameState(),Occupant.Kind.PAWN,3);
        base32Actions.add(stateAction.base32Action());
        occupants.add(stateAction.gameState().board().occupants());
        // player GREEN places occupant in 88_1
        stateAction = occupyTileActionEncoder(stateAction.gameState(),Occupant.Kind.HUT,88_1);
        base32Actions.add(stateAction.base32Action());
        occupants.add(stateAction.gameState().board().occupants());

        stateAction = placeTileActionEncoder(stateAction.gameState(),27,Rotation.NONE,-1,0,PlayerColor.BLUE);
        base32Actions.add(stateAction.base32Action());
        occupants.add(stateAction.gameState().board().occupants());
        stateAction = occupyTileActionEncoder(stateAction.gameState(),Occupant.Kind.PAWN,27_2);
        base32Actions.add(stateAction.base32Action());
        occupants.add(stateAction.gameState().board().occupants());

        GameState finalGameState = stateAction.gameState(); // ok so this is the final state we get

        // print out all actions and their number
        for (int i = 1; i <= base32Actions.size(); i++) {
            System.out.println("action " + i + ": " + base32Actions.get(i-1));
        }

        // now replay all recorded actions using ActionEncoder.decodeAndApply

        // create a new "virgin" stateAction
        stateAction = new ActionEncoder.StateAction(gameStateInitial,null);

        for (int i = 1; i <= base32Actions.size(); i++) {
            System.out.println("replaying action " + i + ": " + base32Actions.get(i-1));

            stateAction = ActionEncoder.decodeAndApply(stateAction.gameState(),base32Actions.get(i-1));

            assertEquals(occupants.get(i-1),stateAction.gameState().board().occupants());

        }

        GameState finalReplayedGameState = stateAction.gameState(); // final state following replay

        // both final states should have EXACTLY the same content, as the same actions were performed:
        assertEquals(finalReplayedGameState,finalGameState);


        // now replay the action list in reverse order.
        // This clearly won't work :-) but it should NOT throw any exception as they should be caught
        // and null should be returned instead by decodeAndApply.
        for (int i = base32Actions.size(); i > 0; i--) {
            System.out.println("replaying action in reverse " + i + ": " + base32Actions.get(i-1));
            stateAction = ActionEncoder.decodeAndApply(finalGameState,base32Actions.get(i-1));
            assertEquals(null,stateAction);
        }
    }






}

