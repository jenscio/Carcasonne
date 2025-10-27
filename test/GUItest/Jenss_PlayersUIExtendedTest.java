package GUItest;
import ch.epfl.chacun.*;
//import ch.epfl.chacun.gui.DecksUI;
//import ch.epfl.chacun.gui.MessageBoardUI;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.function.Consumer;

import static javafx.geometry.Pos.CENTER;


public class Jenss_PlayersUIExtendedTest extends Application {


    List<PlacedTile> tilesToPlace = new ArrayList<>();
    List<Occupant> occupantsToPlace = new ArrayList<>();
    GameState gameState = null;
    SimpleObjectProperty<GameState> gameStateO = null;

    int counter = 0;
    boolean placeNowOccupant = false;


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
        ObjectProperty<Set<Integer>> tileIdList = new SimpleObjectProperty<>();
        Node messageScrollPane = MessageBoardUI.create(messageListO,tileIdList);


        //
        // 3. create the Node with the tile to place and the decks
        //


        ObservableValue<Tile> tileToPlaceO = gameStateO.map(GameState::tileToPlace);
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
        VBox vBox = new VBox();
        vBox.getChildren().addAll(buttonNode,playersNode,messageScrollPane,tilePlaceAndDecksNode);
        // vBox.getChildren().addAll(buttonNode,playersNode,messageScrollPane);
        var rootNode = new BorderPane(vBox);
        primaryStage.setScene(new Scene(rootNode));

        primaryStage.setTitle("ChaCuN test");
        primaryStage.show();

        // place the first tile
        gameState = gameState.withStartingTilePlaced();
        gameStateO.setValue(gameState);

        // System.out.println("start method completed");
    }




    // a method that creates a gameState and prepares some tiles and occupants

    GameState prepareTilesAndInitialGameState() {
        TileDecks tdeck = initialTileDeck(List.of(37,49,62,35,60,0),List.of(94));
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
        addTileAndOccupantToList(60,Rotation.LEFT,0,3,PlayerColor.RED, null,0);
        addTileAndOccupantToList(0,Rotation.RIGHT,0,1,PlayerColor.GREEN, Occupant.Kind.PAWN,3);
        // the previous tile closes a MENHIR forest!
        // BLUE obtains 3 tiles x 2 = 6 points for the closed forest
        addTileAndOccupantToList(94,Rotation.HALF_TURN,0,2,PlayerColor.GREEN, Occupant.Kind.PAWN,94_0);
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

        if (!placeNowOccupant) {
            if (counter >= tilesToPlace.size()) {
                System.out.println("No more tiles to place");
                return;
            }
            PlacedTile nextTileToPlace = tilesToPlace.get(counter);
            System.out.println("    places tile: " + nextTileToPlace.id() + " of kind: " + nextTileToPlace.kind());
            gameState = gameState.withPlacedTile(nextTileToPlace);
            placeNowOccupant = true;
            System.out.println("DEBUG " + gameState.messageBoard().messages());
        } else {
            Occupant nextOccupantToPlace = occupantsToPlace.get(counter);
            if (nextOccupantToPlace != null) {
                System.out.println("    places occupant: " + nextOccupantToPlace.kind() + " in zone: " + nextOccupantToPlace.zoneId());
            } else {
                System.out.println("    places no occupant");
            }
            gameState = gameState.withNewOccupant(nextOccupantToPlace);
            placeNowOccupant = false;
            System.out.println("DEBUG " + gameState.messageBoard().messages());
            counter++;
        }
        gameStateO.setValue(gameState);
        System.out.println("Next player: " + gameState.currentPlayer());
    }


}
