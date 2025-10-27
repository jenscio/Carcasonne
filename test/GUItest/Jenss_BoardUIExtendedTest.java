package GUItest;

import ch.epfl.chacun.*;
import ch.epfl.chacun.gui.BoardUI;
import ch.epfl.chacun.gui.DecksUI;
import ch.epfl.chacun.gui.MessageBoardUI;
import ch.epfl.chacun.gui.PlayersUI;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.function.Consumer;

import static javafx.geometry.Pos.CENTER;


public class Jenss_BoardUIExtendedTest extends Application {


    List<PlacedTile> tilesToPlace = new ArrayList<>();
    List<Occupant> occupantsToPlace = new ArrayList<>();
    GameState gameState = null;
    SimpleObjectProperty<GameState> gameStateO = null;

    SimpleObjectProperty<Rotation> tileToPlaceRotationP =
            new SimpleObjectProperty<>(Rotation.NONE);
    SimpleObjectProperty<Set<Occupant>> visibleOccupantsP =
            new SimpleObjectProperty<>(Set.<Occupant>of());
    SimpleObjectProperty<Set<Integer>> highlightedTilesP =
            new SimpleObjectProperty<>(Set.<Integer>of());

    int counter = 0;
    boolean placeOrRemoveNowOccupant = false;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        ////////////////////////////////////////////////////////////////
        // GUI TEST CODE STARTS HERE:
        ////////////////////////////////////////////////////////////////



        gameState = prepareTilesAndInitialGameState(); // get an initial gameState and some tiles to be placed
        gameStateO = new SimpleObjectProperty<>(gameState); // get an observable gameState property

        //
        // create a Button Node that allows to progress the game
        //
        Button buttonNode = new Button("Press here for next tile/occupant");
        buttonNode.setAlignment(CENTER);
        buttonNode.setOnAction(event -> placeNextTileAndOccupant()); // on click, place the next tile

        //
        // 1. create the Node with the player information:
        //
        Node playersNode = PlayersUI.create(gameStateO, gameState.messageBoard().textMaker());


        //
        // 2. create the Node with the messages
        //
        // Valeur observable contenant les messages
        ObservableValue<List<MessageBoard.Message>> messageListO =
                gameStateO.map(g -> g.messageBoard().messages());
        // propriété JavaFX contenant l'ensemble des identités des tuiles (vide initialement)
        ObjectProperty<Set<Integer>> msgBoardTilesToHighlightP = new SimpleObjectProperty<>();
        Node messageScrollPane = MessageBoardUI.create(messageListO,msgBoardTilesToHighlightP);

        msgBoardTilesToHighlightP.addListener((o, oldValue, newValue) -> {
                highlightedTilesP.setValue(msgBoardTilesToHighlightP.getValue());
        });

        //
        // 3. create the Node with the tile to place and the decks
        //
        //public static Node create (ObservableValue<Tile> tileO, ObservableValue<Integer> remainingNormalTilesO,
        //        ObservableValue<Integer> remainingMenhirTilesO, ObservableValue<String> tilePlaceholderStringO,
        //        Consumer<Occupant> noOccupantEventHandler) {

        ObservableValue<Tile> tileToPlaceO = gameStateO.map(gs -> gs.tileToPlace());
        ObservableValue<Integer> remainingNormalTilesO = gameStateO.map(gs -> gs.tileDecks().deckSize(Tile.Kind.NORMAL));
        ObservableValue<Integer> remainingMenhirTilesO = gameStateO.map(gs -> gs.tileDecks().deckSize(Tile.Kind.MENHIR));
        ObservableValue<String> tilePlaceholderStringO = gameStateO.map(gs -> {
                    switch (gs.nextAction()) {
                        case OCCUPY_TILE -> { return gs.messageBoard().textMaker().clickToOccupy();}
                        case RETAKE_PAWN -> { return gs.messageBoard().textMaker().clickToUnoccupy();}
                        default -> { return "";}
                    }
                }
        );
        Consumer<Occupant> noOccupantEventHandler = (e) -> System.out.println("INFO: called the noOccupantEventHandler");

        Node tilePlaceAndDecksNode = DecksUI.create(tileToPlaceO,remainingNormalTilesO,remainingMenhirTilesO,
                tilePlaceholderStringO,noOccupantEventHandler);



        //
        // now add all the nodes to an encompassing vertical box
        //
        VBox rightSideVBox = new VBox();
        rightSideVBox.getChildren().addAll(buttonNode,playersNode,messageScrollPane,tilePlaceAndDecksNode);

        //
        // create the game board
        //



        Node gameBoard = BoardUI.create(12,
                                gameStateO,
                                tileToPlaceRotationP,
                                visibleOccupantsP,
                                highlightedTilesP,
                                rotation -> {
                                    System.out.println("Rotate: " + rotation);
                                    Rotation currentRot = tileToPlaceRotationP.getValue();
                                    currentRot = currentRot.add(rotation);
                                    tileToPlaceRotationP.setValue(currentRot);
                                },
                                pos -> {
                                    System.out.println("Place: " + pos);
                                },
                                occupant -> {
                                    System.out.println("Select: " + occupant);
                                }
        );



        // now add both main game nodes to a single one
        HBox hBox = new HBox();
        hBox.getChildren().addAll(gameBoard,rightSideVBox);

        //

        var rootNode = new BorderPane(hBox);
        primaryStage.setScene(new Scene(rootNode));

        primaryStage.setTitle("ChaCuN test");
        primaryStage.show();

        // place the start tile
        gameState = gameState.withStartingTilePlaced();
        gameStateO.setValue(gameState);
        tileToPlaceRotationP.setValue(tilesToPlace.get(0).rotation()); // initial rotation of the first tile

        // System.out.println("start method completed");
    }




    // a method that creates a gameState and prepares some tiles and occupants

    GameState prepareTilesAndInitialGameState() {
        TileDecks tdeck = initialTileDeck(List.of(37,49,62,35,60,0,27),List.of(88));
        TextMaker frenchTextMaker = new TextMakerFr(Map.of(
                PlayerColor.RED,"Dalia",
                PlayerColor.BLUE,"Claude",
                PlayerColor.GREEN,"Bachir",
                PlayerColor.YELLOW,"Alice"));

        gameState = GameState.initial(List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN), tdeck, frenchTextMaker);

        addTileAndOccupantToList(37,Rotation.LEFT,1,0,PlayerColor.BLUE, Occupant.Kind.PAWN,37_0);
        addTileAndOccupantToList(49,Rotation.NONE,1,1,PlayerColor.RED, Occupant.Kind.PAWN,49_0);
        addTileAndOccupantToList(62,Rotation.NONE,1,2,PlayerColor.GREEN, Occupant.Kind.PAWN,62_0);
        addTileAndOccupantToList(35,Rotation.NONE,1,3,PlayerColor.BLUE, Occupant.Kind.PAWN,35_1);
        addTileAndOccupantToList(60,Rotation.LEFT,0,3,PlayerColor.RED, null,0000);
        addTileAndOccupantToList(0,Rotation.RIGHT,0,1,PlayerColor.GREEN, Occupant.Kind.PAWN,3);
        // the previous tile closes a MENHIR forest!
        // BLUE obtains 3 tiles x 2 = 6 points for the closed forest
        // next tile: SHAMAN (88)
        addTileAndOccupantToList(88,Rotation.HALF_TURN,2,1,PlayerColor.GREEN, Occupant.Kind.PAWN,3);
        addTileAndOccupantToList(27,Rotation.NONE,-1,0,PlayerColor.BLUE, Occupant.Kind.PAWN,27_2);

        // no tiles remaining, game over
        return gameState;
    }



    /// a couple of helper functions for faster creating a tile deck and placing tiles + occupants

    /**
     *  initialTileDeck: helper function for quickly creating the tile decks
     */

    TileDecks initialTileDeck(List<Integer> normalTileIDs, List<Integer> menhirTileIDs) {
        List<Tile> normalTiles = new ArrayList<>();
        for (Integer i:normalTileIDs) {
            normalTiles.add(Tiles.TILES.get(i));
        }
        List<Tile> menhirTiles = new ArrayList<>();
        for (Integer i:menhirTileIDs) {
            menhirTiles.add(Tiles.TILES.get(i));
        }
        return new TileDecks(List.of(Tiles.TILES.get(56)),normalTiles,menhirTiles);
    }


    /**
     * addTileAndAddOccupant: helper function for fast placing a tile and an occupant
     */
    void addTileAndOccupantToList(int tileID, Rotation rotation, int posx, int posy, PlayerColor color, Occupant.Kind occupantKind, int zoneID) {

        Tile tile = Tiles.TILES.get(tileID);
        PlacedTile ptile = new PlacedTile(tile, color, rotation, new Pos(posx, posy));
        tilesToPlace.add(ptile);
        if (occupantKind != null) {
            occupantsToPlace.add(new Occupant(occupantKind, zoneID));
        } else {
            occupantsToPlace.add(null);
        }
    }

    void placeNextTileAndOccupant() {
        System.out.println("Current player: " + gameState.currentPlayer());

        if (placeOrRemoveNowOccupant == false) {
            // place a tile
            if (counter >= tilesToPlace.size()) {
                System.out.println("No more tiles to place");
                return;
            }
            PlacedTile nextTileToPlace = tilesToPlace.get(counter);
            System.out.println("    places tile: " + nextTileToPlace.id() + " of kind: " + nextTileToPlace.kind());
            gameState = gameState.withPlacedTile(nextTileToPlace);
            Set<Occupant> occupantsToShow = new HashSet<>();
            occupantsToShow.addAll(gameState.board().occupants()); // show the current board occupants
            if (gameState.nextAction() == GameState.Action.RETAKE_PAWN) {
                // Just placed a shaman and can now retake a pawn!
                // Highlight the tiles which have Occupants placed by the current player
                // Set<Integer> tilesToHighlight = new HashSet<>();
                // for (Occupant o: gameState.board().occupants()) {
                //    // not elegant loop but we cannot access all tiles from the board (private attribute) so we cannot do a filter
                //    if (gameState.board().tileWithId(o.zoneId()/10).placer() == gameState.currentPlayer())
                //        tilesToHighlight.add(o.zoneId()/10);
                //}
                // highlightedTilesP.setValue(tilesToHighlight);
            } else {
                // assume the next action is occupy tile...
                // highlightedTilesP.setValue(Set.of(gameState.board().lastPlacedTile().id()));
                occupantsToShow.addAll(gameState.lastTilePotentialOccupants()); // show also the potential occupants of the tile just placed
            }
            visibleOccupantsP.setValue(occupantsToShow);
            placeOrRemoveNowOccupant = true;
        } else {
            // place (or remove) an occupant
            Occupant nextOccupantToPlaceOrRemove = occupantsToPlace.get(counter);
            if (nextOccupantToPlaceOrRemove == null) {
                System.out.println("    places/removes no occupant");
            } else {
                System.out.println("    places/removes occupant: " + nextOccupantToPlaceOrRemove.kind() + " in zone: " + nextOccupantToPlaceOrRemove.zoneId());
            }
            if (gameState.nextAction() == GameState.Action.RETAKE_PAWN) {
                // Next step is to REMOVE an occupant
                gameState = gameState.withOccupantRemoved(nextOccupantToPlaceOrRemove);
                placeOrRemoveNowOccupant = true; // next step now is to place an occupant!
            } else {
                // otherwise, **assume** next action is to place an occupant
                if (gameState.board().lastPlacedTile().id() == 88) {
                    // if the last tile placed is 88, just hardcode that it places a hut in the river
                    nextOccupantToPlaceOrRemove = new Occupant(Occupant.Kind.HUT,88_1);
                }
                gameState = gameState.withNewOccupant(nextOccupantToPlaceOrRemove);
                placeOrRemoveNowOccupant = false; // next step is to place a tile (or end of game)
            }
            Set<Occupant> occupantsToShow = new HashSet<>();
            occupantsToShow.addAll(gameState.board().occupants());
            visibleOccupantsP.setValue(occupantsToShow);
            //highlightedTilesP.setValue(Set.of());
            if (counter +1 < tilesToPlace.size()) {
                tileToPlaceRotationP.setValue(tilesToPlace.get(counter+1).rotation()); // rotation of the next tile to place
            }
            if (!(gameState.board().lastPlacedTile().id() == 88 && gameState.nextAction() == GameState.Action.OCCUPY_TILE)) {
                // this if is a gigantic hack to NOT increment the counter of what card is being played if we just played card 88
                // which is a bit special because it first retakes then occupies
                counter++;
            } else {
                // next step is OCCUPY_TILE after retaking pawn, so show the occupants inclusive potential occupants
                occupantsToShow = new HashSet<>();
                occupantsToShow.addAll(gameState.board().occupants()); // show the current board occupants
                occupantsToShow.addAll(gameState.lastTilePotentialOccupants()); // and also the potential occupants of the tile just placed
                visibleOccupantsP.setValue(occupantsToShow);
                // highlightedTilesP.setValue(Set.of(gameState.board().lastPlacedTile().id()));
            }
        }
        gameStateO.setValue(gameState);
        System.out.println("Next player: " + gameState.currentPlayer());
    }


}

