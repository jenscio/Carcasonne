package ch.epfl.chacun.gui;

import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.Tile;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.function.Consumer;

/**
 * DecksUI:
 * contient le code de création de la partie de l'interface graphique qui affiche les tas de tuiles
 * ainsi que la tuile à poser.
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */

public final class DecksUI {

    /**
     * Constructeur vide (classe non instantiable)
     */
    private DecksUI() {
    }

    /**
     * retourne le nœud JavaFX, de type Node, qui affiche les tas de tuiles ainsi que la tuile à poser
     *
     * @param tileToPlaceO:          version observable de la tuile à placer
     * @param remainingNormalTilesO: version observable du nombre de tuiles restantes dans le tas des tuiles normales
     * @param remainingMenhirTilesO: version observable du nombre de tuiles restantes dans le tas des tuiles menhir
     * @param tilePlaceholderStringO: version observable du texte à afficher à la place de la tuile à placer
     * @param noOccupantPlacedEventHandler: gestionnaire d'événement pour traiter la non pose/reprise d'occupant
     * @return le nœud qui affiche les tas de tuiles ainsi que la tuile à poser
     */
    public static Node create(ObservableValue<Tile> tileToPlaceO,
                              ObservableValue<Integer> remainingNormalTilesO,
                              ObservableValue<Integer> remainingMenhirTilesO,
                              ObservableValue<String> tilePlaceholderStringO,
                              Consumer<Occupant> noOccupantPlacedEventHandler) {

        VBox vBox = new VBox();
        //  feuilles de style à attacher au nœud
        vBox.getStylesheets().add("decks.css");

        //
        // StackPane pour la prochaine tuile à placer
        //
        StackPane tileToPlacePane = new StackPane();
        tileToPlacePane.setId("next-tile");
        // image de la tuile à placer
        ImageView tileToPlaceImgView = new ImageView();
        tileToPlacePane.getChildren().add(tileToPlaceImgView);
        // bien dimensionner les images
        tileToPlaceImgView.setFitWidth(ImageLoader.LARGE_TILE_FIT_SIZE);
        tileToPlaceImgView.setFitHeight(ImageLoader.LARGE_TILE_FIT_SIZE);

        // il faut observer la tileO et generer une image observable
        ObservableValue<Image> imageO = tileToPlaceO.map(tile -> ImageLoader.largeImageForTile(tile.id()));
        // et maintenant associer l'image observable avec la propieté image du viewer en utilisant "bind"
        tileToPlaceImgView.imageProperty().bind(imageO);

        // Text pour recouvrir la tuile à placer
        Text tileToPlaceCoverText = new Text();
        tileToPlacePane.getChildren().add(tileToPlaceCoverText);
        // bien dimensioner le texte
        tileToPlaceCoverText.setWrappingWidth(ImageLoader.LARGE_TILE_FIT_SIZE * 0.8);

        // faire un lien unidirectionnel (bind) pour associer tilePlaceholderStringO à la proprieté text
        // de tileToPlaceCoverText
        tileToPlaceCoverText.textProperty().bind(tilePlaceholderStringO);
        // un autre bind: si le texte à afficher est vide (""), mettre visibilité = false
        ObservableValue<Boolean> showTileToPlaceCoverTextO = tilePlaceholderStringO.map(s -> !s.isEmpty());
        tileToPlaceCoverText.visibleProperty().bind(showTileToPlaceCoverTextO);

        tileToPlaceCoverText.setOnMouseClicked(_ ->
            noOccupantPlacedEventHandler.accept(null) // pas d'occupant à placer
        );

        //
        // HBox pour les deux tas de tiles
        //
        HBox hBox = new HBox();
        hBox.setId("decks");

        //
        // Tas de tuiles normales
        //
        StackPane normalTilesStackPane = new StackPane();
        normalTilesStackPane.setId("NORMAL");
        hBox.getChildren().add(normalTilesStackPane);
        hBox.setAlignment(Pos.CENTER);
        // image à utiliser pour le tas de tuiles normales
        ImageView normalTilesStackPaneImgView = new ImageView();
        normalTilesStackPaneImgView.setId("NORMAL");
        normalTilesStackPane.getChildren().add(normalTilesStackPaneImgView);
        // bien dimensionner les images
        normalTilesStackPaneImgView.setFitWidth(ImageLoader.NORMAL_TILE_FIT_SIZE);
        normalTilesStackPaneImgView.setFitHeight(ImageLoader.NORMAL_TILE_FIT_SIZE);
        // texte a montrer pour le tas de tuiles normales
        Text normalTilesStackPaneText = new Text();
        normalTilesStackPane.getChildren().add(normalTilesStackPaneText);
        ObservableValue<String> remainingNormalTilesStringO = remainingNormalTilesO.map(String::valueOf);
        normalTilesStackPaneText.textProperty().bind(remainingNormalTilesStringO);
        //
        // Tas de tuiles menhir
        //
        StackPane menhirTilesStackPane = new StackPane();
        normalTilesStackPane.setId("MENHIR");
        hBox.getChildren().add(menhirTilesStackPane);
        // image à utiliser pour le tas de tuiles menhir
        ImageView menhirTilesStackPaneImgView = new ImageView();
        menhirTilesStackPaneImgView.setId("MENHIR");
        menhirTilesStackPane.getChildren().add(menhirTilesStackPaneImgView);
        // bien dimensionner les images
        menhirTilesStackPaneImgView.setFitWidth(ImageLoader.NORMAL_TILE_FIT_SIZE);
        menhirTilesStackPaneImgView.setFitHeight(ImageLoader.NORMAL_TILE_FIT_SIZE);
        // texte a montrer pour le tas de tuiles menhir
        Text menhirTilesStackPaneText = new Text();
        menhirTilesStackPane.getChildren().add(menhirTilesStackPaneText);
        ObservableValue<String> remainingMenhirTilesStringO = remainingMenhirTilesO.map(String::valueOf);
        menhirTilesStackPaneText.textProperty().bind(remainingMenhirTilesStringO);

        // mettre d'abord le noeud pour les tas, et après le noeud pour la tuile à placer
        vBox.getChildren().add(hBox);
        vBox.getChildren().add(tileToPlacePane);

        return vBox;
    }
}
