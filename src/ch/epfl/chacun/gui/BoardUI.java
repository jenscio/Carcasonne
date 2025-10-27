package ch.epfl.chacun.gui;

import ch.epfl.chacun.*;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * BoardUI:
 * contient le code de création de la partie de l'interface graphique qui affiche le plateau de jeu.
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */


public final class BoardUI {
    /**
     * Constructeur vide (classe non instantiable)
     */
    private BoardUI() {
    }


    /**
     * CellData:
     * enregistrement privé pour regrouper les elements nécessaires pour
     * la representation graphique d'une case (image de fond, rotation, mélange)
     *
     * @param backgroundImage: image à mettre comme fond
     * @param rotation:        la rotation de l'image
     * @param blend:           le mélange à appliquer
     */
    private record CellData(Image backgroundImage, Rotation rotation, Blend blend) {
        // Cache pour les images des tuiles
        private final static Map<Integer, Image> tileImageCacheMap = new HashMap<>();
    }




    /**
     * méthode auxiliaire pour determiner si la position est dans la frange et la prochaine
     * action est de poser une tuile
     *
     * @param gameState : état de jeu
     * @param pos : position
     * @return vrai si la position est dans la frange et la prochaine action est de poser la tuile
     */
    private static boolean isInFringeAndPlaceTile(GameState gameState, Pos pos) {
        return gameState.board().insertionPositions().contains(pos) &&
                gameState.nextAction() == GameState.Action.PLACE_TILE;
    }


    /**
     * genere une instance graphique du plateau de jeu
     *
     * @param boardReach:              portée du plateau
     * @param gameStateO:              valeur observable de l'état du jeu
     * @param tileToPlaceRotationO:    valeur observable de la rotation à appliquer à la tuile à placer
     * @param visibleOccupantsO:       valeur observable de l'ensemble des occupants visibles
     * @param highlightedTilesO:       valeur observable de l'ensemble des identifiants des tuiles mises en évidence
     * @param rotationConsumerHandler: gestionnaire pour la rotation de la tuile à placer
     * @param posConsumerHandler:      gestionnaire pour la pose de la tuile à placer
     * @param occupantConsumerHandler: gestionnaire pour la sélection d'un occupant
     * @return l'instance graphique du plateau de jeu
     */
    public static Node create(int boardReach,
                              ObservableValue<GameState> gameStateO,
                              ObservableValue<Rotation> tileToPlaceRotationO,
                              ObservableValue<Set<Occupant>> visibleOccupantsO,
                              ObservableValue<Set<Integer>> highlightedTilesO,
                              Consumer<Rotation> rotationConsumerHandler,
                              Consumer<Pos> posConsumerHandler,
                              Consumer<Occupant> occupantConsumerHandler) {




        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStylesheets().add("board.css");
        scrollPane.setId("board-scroll-pane");
        scrollPane.setVvalue(0.5);
        scrollPane.setHvalue(0.5);

        GridPane gridPane = new GridPane();
        gridPane.setId("board-grid");
        scrollPane.setContent(gridPane);

        WritableImage emptyTileImage = new WritableImage(1, 1);
        emptyTileImage.getPixelWriter().setColor(0, 0, Color.gray(0.98));

        for (int ix = -boardReach; ix <= boardReach; ix++) {
            for (int iy = -boardReach; iy <= boardReach; iy++) {
                Group cell = new Group();
                Pos currentPos = new Pos(ix, iy);
                gridPane.add(cell, ix + boardReach, iy + boardReach); // x,y du gridPane doit >= 0

                ImageView backgroundImageView = new ImageView();
                backgroundImageView.setFitWidth(ImageLoader.NORMAL_TILE_FIT_SIZE);
                backgroundImageView.setFitHeight(ImageLoader.NORMAL_TILE_FIT_SIZE);
                cell.getChildren().add(backgroundImageView);

                // listener pour dessiner l'image de la tuile, les jetons d'annulation
                // et les occupants
                ObservableValue<PlacedTile> placedTileO = gameStateO.map(gs -> gs.board().tileAt(currentPos));
                placedTileO.addListener((_, _, newPlacedTile) -> {
                    // changement de la tuile
                    if (cell.getId() == null) {
                        cell.setId(String.valueOf(newPlacedTile.id())); // on nomme la case avec le no. de la tuile
                        // créer les noeuds javafx pour les jetons d'annulation pour chaque animal dans la tuile
                        Set<Animal> animals = new HashSet<>();
                        newPlacedTile.meadowZones().forEach(m -> animals.addAll(m.animals()));
                        for (Animal a : animals) {
                            ImageView markerImageView = new ImageView();
                            markerImageView.setFitHeight(ImageLoader.MARKER_FIT_SIZE);
                            markerImageView.setFitWidth(ImageLoader.MARKER_FIT_SIZE);
                            markerImageView.setId("marker_" + a.id()); // p.ex. marker_420
                            markerImageView.getStyleClass().add("marker"); // style class
                            cell.getChildren().add(markerImageView);
                            // definir un binding pour rendre visible ou pas le jeton
                            ObservableValue<Boolean> displayAnimalO = gameStateO.map(gs ->
                                    gs.board().cancelledAnimals().contains(a));
                            markerImageView.visibleProperty().bind(displayAnimalO);
                        }

                            // créer les noeuds javafx pour tous les occupants possibles pour la tuile
                        for (Occupant occupant : newPlacedTile.potentialOccupants()) {
                            Node occupantIcon = Icon.newFor(newPlacedTile.placer(), occupant.kind());
                            String occupantIdString = occupant.kind() == Occupant.Kind.HUT ? "hut" : "pawn";
                            occupantIdString = occupantIdString + "_" + occupant.zoneId();
                            occupantIcon.setId(occupantIdString);
                            // corriger la rotation des occupants (negation de la rotation)
                            occupantIcon.setRotate(newPlacedTile.rotation().negated().degreesCW());
                            cell.getChildren().add(occupantIcon);
                            // definir un binding pour rendre visible ou pas l'occupant
                            ObservableValue<Boolean> displayOccupantO = visibleOccupantsO.map(occ ->
                                    occ.contains(occupant));
                            occupantIcon.visibleProperty().bind(displayOccupantO);

                            // appelle le gestionnaire occupantConsumerHandler lorsque le joueur courant
                            // sélectionne un occupant
                            occupantIcon.setOnMouseClicked(e -> {
                                if (e.isStillSincePress()) {
                                    occupantConsumerHandler.accept(occupant);
                                }
                            });
                        }
                    }
                });


                // createObjectBinding pour generer un cellData observable
                ObservableValue<CellData> cellDataO = Bindings.createObjectBinding(() -> {
                            Image backgroundImage;
                            Rotation rotation = null;
                            Blend blend = null;
                            GameState gs = gameStateO.getValue();

                            // il y a une tuile à montrer? Si oui, laquelle et avec quelle rotation?
                            Tile tile = null;
                            if (cell.getId() != null) {
                                // il y a déjà une tuile placée à cet endroit
                                tile = gs.board().tileAt(currentPos).tile();         // la tuile
                                rotation = gs.board().tileAt(currentPos).rotation(); // la rotation
                            } else {
                                // pas de tuile placée à cet endroit
                                // est-ce que c'est une cellule partie de la frange, que la prochaine action
                                // est PLACE_TILE et la souris est sur elle?
                                if (isInFringeAndPlaceTile(gs, currentPos) && cell.isHover()) {
                                    // oui: l'image de fond a montrer c'est la prochaine tuile à placer
                                    tile = gs.tileToPlace(); // la tuile
                                    rotation = tileToPlaceRotationO.getValue(); // la rotation
                                }
                            }

                            // generer l'image à montrer
                            if (tile != null) {
                                // il y a une tuile à montrer
                                // chercher la tuile dans le cache (et la générer si pas présente)
                                CellData.tileImageCacheMap.computeIfAbsent(tile.id(), ImageLoader::normalImageForTile);
                                backgroundImage = CellData.tileImageCacheMap.get(tile.id());
                            } else {
                                // pas de tuile, donc afficher l'image vide
                                backgroundImage = emptyTileImage;
                                rotation = Rotation.NONE;
                            }

                            // quel est le voile à utiliser?
                            //
                            // 1. si la case contient une tuile, et que certaines tuiles sont mises en évidence mais
                            // pas celle de la case, alors elle est recouverte d'un voile noir qui l'assombrit
                            Color blendColor = null;
                            if (cell.getId() != null &&
                                    !highlightedTilesO.getValue().isEmpty() &&
                                    !highlightedTilesO.getValue().contains(Integer.valueOf(cell.getId()))) {
                                blendColor = Color.BLACK;
                            }

                            // 2. et 3.: seulement si la case fait partie de la frange
                            if (isInFringeAndPlaceTile(gs, currentPos)) {

                                // 2. si la case fait partie de la frange mais qu'elle n'est pas survolée par le curseur
                                // de la souris, alors elle est recouverte d'un voile de la couleur du joueur courant
                                if (!cell.isHover())
                                    blendColor = ColorMap.fillColor(gs.currentPlayer());

                                // 3. si la case fait partie de la frange, que le curseur de la souris la survole et
                                // que la tuile courante, avec sa rotation actuelle, ne peut pas y être placée,
                                // alors elle est recouverte d'un voile blanc.

                                if (cell.isHover()) {
                                    PlacedTile tileToPlace = new PlacedTile(
                                            gs.tileToPlace(),
                                            gs.currentPlayer(),
                                            tileToPlaceRotationO.getValue(), currentPos);
                                    if (!gs.board().canAddTile(tileToPlace)) {
                                        // tuile ne peut pas être placée
                                        blendColor = Color.WHITE;
                                    }
                                }
                            }

                            // generer le voile, si besoin
                            if (blendColor != null) {
                                // il y a un voile à utiliser
                                blend = new Blend();
                                blend.setMode(BlendMode.SRC_OVER);
                                ColorInput colorInput = new ColorInput();
                                colorInput.setPaint(blendColor);
                                colorInput.setWidth(ImageLoader.NORMAL_TILE_FIT_SIZE);
                                colorInput.setHeight(ImageLoader.NORMAL_TILE_FIT_SIZE);
                                blend.setOpacity(0.5);
                                blend.setTopInput(colorInput);
                            }

                            return new CellData(backgroundImage, rotation, blend);

                        }, gameStateO, tileToPlaceRotationO, visibleOccupantsO, highlightedTilesO,
                        cell.hoverProperty());


                // définir les bindings entre la cellule (et son imageviewer) pour les
                // proprietés image, rotation, et voile
                cell.effectProperty().bind(cellDataO.map(c -> c.blend));
                backgroundImageView.imageProperty().bind(cellDataO.map(c -> c.backgroundImage));
                cell.rotateProperty().bind(cellDataO.map(c -> c.rotation.degreesCW()));


                // gestion des évenements souris

                cell.setOnMouseClicked(e -> {

                    if (e.isStillSincePress()) {
                        if (isInFringeAndPlaceTile(gameStateO.getValue(), currentPos)) {
                            // la cellule est dans la fringe et la prochaine action est de placer une tuile
                            switch (e.getButton()) {
                                case PRIMARY ->
                                    // appelle le gestionnaire posConsumerHandler lorsque le joueur courant désire poser
                                    // la tuile à placer, c.-à-d. qu'il effectue un clic gauche sur une case de
                                    // la frange
                                    posConsumerHandler.accept(currentPos);

                                case SECONDARY -> {
                                    // appelle le gestionnaire rotationConsumerHandler lorsque le joueur courant désire
                                    // effectuer une rotation de la tuile à placer, c.-à-d. qu'il effectue un clic
                                    // droit sur une case de la frange
                                    if (e.isAltDown()) {
                                        rotationConsumerHandler.accept(Rotation.RIGHT);
                                    } else {
                                        rotationConsumerHandler.accept(Rotation.LEFT);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }

        return scrollPane;
    }

}
