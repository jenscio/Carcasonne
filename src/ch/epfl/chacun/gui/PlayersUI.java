package ch.epfl.chacun.gui;

import ch.epfl.chacun.GameState;
import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.PlayerColor;
import ch.epfl.chacun.TextMaker;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Map;

/**
 * PlayersUI:
 * contient le code de création de la partie de l'interface graphique qui affiche les informations sur les joueurs
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */

public final class PlayersUI {

    /**
     * Constructeur vide (classe non instantiable)
     */
    private PlayersUI() {
    }


    /**
     * retourne le nœud JavaFX, de type Node, à la racine du graphe de scène
     *
     * @param gameStateO: version observable de l'état actuel de la partie
     * @param textMaker:  le générateur de texte
     * @return le nœud JavaFX, de type Node, à la racine du graphe de scène
     */
    public static Node create(ObservableValue<GameState> gameStateO, TextMaker textMaker) {


        VBox vBox = new VBox();
        //  feuilles de style à attacher au nœud
        vBox.getStylesheets().add("players.css");
        // identité des nœuds
        vBox.setId("players");

        // Valeur observable contenant les points des joueurs
        ObservableValue<Map<PlayerColor, Integer>> pointsO = gameStateO.map(g -> g.messageBoard().points());

        for (PlayerColor color : gameStateO.getValue().players()) {
                TextFlow tf = new TextFlow();
                tf.getStyleClass().add("player");
                tf.setId(color.name()); // mettre comme nom du noeud la couleur
                vBox.getChildren().add(tf);


                // cercle de rayon 5 et de la même couleur que le player
                Circle circle = new Circle(5);
                circle.setFill(ColorMap.fillColor(color));
                circle.setStroke(ColorMap.strokeColor(color));
                tf.getChildren().add(circle);

                // affichage du texte "<utilisateur> : X points"
                ObservableValue<String> pointsTextO = pointsO.map(
                        p -> textMaker.playerName(color) + " : " +
                                textMaker.points(p.getOrDefault(color, 0)) + "\n");
                Text pointsText = new Text();
                // mettre à jour pointsText automatiquement avec la valeur de pointText0
                // en utilisant "bind" (lien unidirectionnel)
                pointsText.textProperty().bind(pointsTextO);
                tf.getChildren().add(pointsText);

                // huttes du joueur
                for (int i = 1; i <= Occupant.occupantsCount(Occupant.Kind.HUT); i++) {
                    Node hutIcon = Icon.newFor(color, Occupant.Kind.HUT);
                    int finalI = i;
                    ObservableValue<Double> opacityO =
                            gameStateO.map(g -> g.freeOccupantsCount(color, Occupant.Kind.HUT) >= finalI ? 1 : 0.1);
                    hutIcon.opacityProperty().bind(opacityO);
                    tf.getChildren().add(hutIcon);
                }

                // separateur
                Text separatorText = new Text("   "); // trois espaces
                tf.getChildren().add(separatorText);

                // pions du joueur
                for (int i = 1; i <= Occupant.occupantsCount(Occupant.Kind.PAWN); i++) {
                    Node pawnIcon = Icon.newFor(color, Occupant.Kind.PAWN);
                    int finalI = i;
                    ObservableValue<Double> opacityO =
                            gameStateO.map(g -> g.freeOccupantsCount(color, Occupant.Kind.PAWN) >= finalI ? 1 : 0.1);
                    pawnIcon.opacityProperty().bind(opacityO);
                    tf.getChildren().add(pawnIcon);
                }

                // montrer le joueur actuel en activant la classe de style "current"
                ObservableValue<PlayerColor> currentPlayerColorO = gameStateO.map(GameState::currentPlayer);
                currentPlayerColorO.addListener((_, _, newPlayerColor) -> {
                            if (color == newPlayerColor) {
                                tf.getStyleClass().add("current");
                            } else {
                                tf.getStyleClass().remove("current");
                            }
                        }
                );
        }

        return vBox;
    }
}
