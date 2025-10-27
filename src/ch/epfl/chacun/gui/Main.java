package ch.epfl.chacun.gui;

import ch.epfl.chacun.*;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.function.Consumer;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;

import static ch.epfl.chacun.Occupant.Kind.PAWN;

/**
 * Main:
 * classe principale du projet ChaCuN.
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */


public final class Main extends Application {

    /**
     * appelle la methode launch avec les arguments reçus de la ligne de commande
     *
     * @param args les arguments de la ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }

    // dimensions initiales du plateau de jeu
    private final int INITIAL_WIDTH = 1440;
    private final int INITIAL_HEIGHT = 1080;

    // définition des propriétés observables :
    // l'état du jeu observable
    private final SimpleObjectProperty<GameState> gameStateP = new SimpleObjectProperty<>();
    // la rotation de la tuile à placer observable
    private final SimpleObjectProperty<Rotation> tileToPlaceRotationP = new SimpleObjectProperty<>(Rotation.NONE);
    // les occupants visibles observables
    private final SimpleObjectProperty<Set<Occupant>> visibleOccupantsP = new SimpleObjectProperty<>(Set.of());
    // les tuiles mises en évidence observables
    private final SimpleObjectProperty<Set<Integer>> highlightedTileIdsP = new SimpleObjectProperty<>(Set.of());
    // la liste des actions, observables
    private final SimpleObjectProperty<List<String>> actionListP = new SimpleObjectProperty<>(List.of());


    /**
     * méthode auxiliaire:
     * ajoute une action à la liste observable des actions
     *
     * @param action : l'action à ajouter
     */
    private void addAction(String action) {
        List<String> actions = new ArrayList<>(actionListP.getValue());
        actions.add(action);
        actionListP.setValue(actions);
    }

    /**
     * méthode auxiliaire:
     * pose la prochaine tuile à placer dans position pos, et actualise la liste observable des actions
     *
     * @param pos : la position de la tuile
     */
    private void placeTile(Pos pos) {
        PlacedTile pTile = new PlacedTile(gameStateP.getValue().tileToPlace(),
                gameStateP.getValue().currentPlayer(),
                tileToPlaceRotationP.getValue(), pos);
        ActionEncoder.StateAction stateAction = ActionEncoder.withPlacedTile(gameStateP.getValue(), pTile);
        addAction(stateAction.base32Action());
        gameStateP.setValue(stateAction.gameState());
        // remettre la rotation à son état initial
        tileToPlaceRotationP.setValue(Rotation.NONE);
    }

    /**
     * méthode auxiliaire:
     * place l'occupant en actualisant la liste observable des actions
     *
     * @param occupant : l'occupant à placer
     */
    private void occupyTile(Occupant occupant) {
        ActionEncoder.StateAction stateAction = ActionEncoder.withNewOccupant(gameStateP.getValue(), occupant);
        addAction(stateAction.base32Action());
        gameStateP.setValue(stateAction.gameState());
    }


    /**
     * méthode auxiliaire:
     * reprends l'occupant en actualisant la liste observable des actions
     *
     * @param occupant : l'occupant à reprendre
     */
    private void removeOccupant(Occupant occupant) {
        ActionEncoder.StateAction stateAction = ActionEncoder.withOccupantRemoved(gameStateP.getValue(), occupant);
        addAction(stateAction.base32Action());
        gameStateP.setValue(stateAction.gameState());
    }


    /**
     * méthode auxiliaire:
     * execute l'action encodée en paramètre, et actualise la liste observable des actions
     *
     * @param action : l'action encodée
     */
    private void decodeAndExecuteAction(String action) {
        ActionEncoder.StateAction stateAction = ActionEncoder.decodeAndApply(gameStateP.getValue(), action);
        if (stateAction != null) {
            addAction(stateAction.base32Action());
            gameStateP.setValue(stateAction.gameState());
        }
    }



    /**
     * méthode principale de javaFX
     *
     * @throws IllegalArgumentException si nombre de joueur inférieur à 2 ou supérieur à 5,
     *   graine qui ne correspond pas à un entier dans la plage donnée plus haut
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        // obtention des nombres des joueurs
        List<String> playerNames = getParameters().getUnnamed();
        Preconditions.checkArgument(playerNames.size()>=2 && playerNames.size()<=5);
        Map<PlayerColor,String> playerColorMap = new HashMap<>();
        List<PlayerColor> playerColors = new ArrayList<>();
        Iterator<String> i1 = playerNames.iterator();
        Iterator<PlayerColor> i2 = PlayerColor.ALL.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            PlayerColor playerColor = i2.next();
            playerColorMap.put(playerColor, i1.next());
            playerColors.add(playerColor);
        }

        // obtenir graine et initialiser générateur de numéros aléatoires
        Map<String,String> seedMap = getParameters().getNamed();
        Preconditions.checkArgument(seedMap.size() <= 1);
        RandomGeneratorFactory<RandomGenerator> randomGeneratorFactory = RandomGeneratorFactory.getDefault();
        RandomGenerator generator;
        if (seedMap.size() == 1) {
            // vérifier que c'est bien l'argument "seed"
            Preconditions.checkArgument(seedMap.containsKey("seed"));
            long seed = Long.parseUnsignedLong(seedMap.get("seed"));
            generator = randomGeneratorFactory.create(seed);
        } else {
            generator = randomGeneratorFactory.create();
        }
        // mélanger les tuiles avec le générateur
        List<Tile> tilesToShuffle = new ArrayList<>(List.copyOf(Tiles.TILES));
        Collections.shuffle(tilesToShuffle,generator);
        // créer les tas de tuiles
        Map<Tile.Kind,List<Tile>> partitionedTiles = tilesToShuffle.stream()
                .collect(Collectors.groupingBy(Tile::kind));
        TileDecks tileDecks =  new TileDecks(
                partitionedTiles.get(Tile.Kind.START),
                partitionedTiles.getOrDefault(Tile.Kind.NORMAL, List.of()),
                partitionedTiles.getOrDefault(Tile.Kind.MENHIR, List.of()));

        // etat de jeu initial
        gameStateP.setValue(GameState.initial(playerColors,tileDecks,new TextMakerFr(playerColorMap)));

        //
        // création du graphe de scene
        //
        BorderPane mainBorderPane = new BorderPane();
        primaryStage.setScene(new Scene(mainBorderPane));
        primaryStage.setTitle("ChaCuN");
        primaryStage.setWidth(INITIAL_WIDTH);
        primaryStage.setHeight(INITIAL_HEIGHT);

        Consumer<Occupant> occupantHandler = (occupant) -> {
            // gestionnaire d'événement pour la selection d'un occupant
            //  verifier que l'occupant peut être selectionné
            switch (gameStateP.getValue().nextAction()){
                case OCCUPY_TILE -> {
                    if (occupant == null ||
                            (Zone.tileId(occupant.zoneId()) == gameStateP.getValue().
                            board().lastPlacedTile().id())) {
                        occupyTile(occupant);
                    }
                }
                case RETAKE_PAWN -> {
                    if (occupant == null ||
                            (gameStateP.getValue().board().tileWithId(Zone.tileId(occupant.zoneId())).placer() ==
                            gameStateP.getValue().currentPlayer()
                                    && occupant.kind() == PAWN))
                        removeOccupant(occupant);
                }
            }
        };


        Node gameBoard = BoardUI.create(Board.REACH,
                gameStateP, tileToPlaceRotationP, visibleOccupantsP, highlightedTileIdsP,
                rotation -> {
                    // gestionnaire d'événement pour la rotation
                    Rotation currentRotation = tileToPlaceRotationP.getValue();
                    currentRotation = currentRotation.add(rotation);
                    tileToPlaceRotationP.setValue(currentRotation);
                },
                pos -> {
                    // gestionnaire d'événement pour la pose d'une tuile
                    PlacedTile tileToPlace = new PlacedTile(
                            gameStateP.getValue().tileToPlace(),
                            gameStateP.getValue().currentPlayer(),
                            tileToPlaceRotationP.getValue(), pos);
                    if (gameStateP.getValue().board().canAddTile(tileToPlace)) {
                        placeTile(pos);
                    }
                },
                occupantHandler);

        mainBorderPane.setCenter(gameBoard);


        // contrôle de la visibilité des occupants
        ObservableValue<GameState.Action> nextActionO = gameStateP.map(GameState::nextAction);
        nextActionO.addListener(( _ , _ , newValue) -> {
            Set<Occupant> visibleOccupants = new HashSet<>(gameStateP.getValue().board().occupants());
            if (newValue== GameState.Action.OCCUPY_TILE) {
                visibleOccupants.addAll(gameStateP.getValue().lastTilePotentialOccupants());
            }
            visibleOccupantsP.setValue(visibleOccupants);
        });

        //
        // creation des noeuds coté droit
        //
        BorderPane sideBorderPane = new BorderPane();
        mainBorderPane.setRight(sideBorderPane);

        // noeud avec les informations sur les joueurs
        Node playersNode = PlayersUI.create(gameStateP,
                gameStateP.getValue().messageBoard().textMaker());
        sideBorderPane.setTop(playersNode);

        // noeud avec les messages
        // Valeur observable contenant les messages
        ObservableValue<List<MessageBoard.Message>> messageListO =
                gameStateP.map(g -> g.messageBoard().messages());
        Node messageScrollPane = MessageBoardUI.create(messageListO, highlightedTileIdsP);
        sideBorderPane.setCenter(messageScrollPane);

        //
        // Tas de tuiles, tuile à placer et actions
        //

        VBox vBox = new VBox();
        sideBorderPane.setBottom(vBox);

        // actions:
        Node actionsUINode = ActionUI.create(actionListP, this::decodeAndExecuteAction);
        vBox.getChildren().add(actionsUINode);


        // Tas de tuiles et tuile à placer:
        ObservableValue<Tile> tileToPlaceO = gameStateP.map(GameState::tileToPlace);
        ObservableValue<Integer> remainingNormalTilesO =
                gameStateP.map(gs -> gs.tileDecks().deckSize(Tile.Kind.NORMAL));
        ObservableValue<Integer> remainingMenhirTilesO =
                gameStateP.map(gs -> gs.tileDecks().deckSize(Tile.Kind.MENHIR));
        ObservableValue<String> tilePlaceholderStringO =
                gameStateP.map(gs -> {
                            switch (gs.nextAction()) {
                                // montrer les messages à visualiser pour pose et reprise d'un occupant
                                case OCCUPY_TILE -> {
                                    return gs.messageBoard().textMaker().clickToOccupy();
                                }
                                case RETAKE_PAWN -> {
                                    return gs.messageBoard().textMaker().clickToUnoccupy();
                                }
                                default -> {
                                    return "";
                                }
                            }
                        }
                );
        Node tilePlaceAndDecksNode = DecksUI.create(tileToPlaceO,
                remainingNormalTilesO,
                remainingMenhirTilesO,
                tilePlaceholderStringO,
                occupantHandler
        );
        vBox.getChildren().add(tilePlaceAndDecksNode);


        // visualiser les noeuds
        primaryStage.show();
        // démarrage du jeu
        gameStateP.setValue(gameStateP.getValue().withStartingTilePlaced());
    }

}