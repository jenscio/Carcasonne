package ch.epfl.chacun.gui;

import ch.epfl.chacun.PlayerColor;
import javafx.scene.paint.Color;

/**
 * ColorMap:
 * contient des méthodes permettant de déterminer les couleurs JavaFX à utiliser pour représenter
 * à l'écran les cinq couleurs de joueur qui existent dans ChaCuN.
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */
public final class ColorMap {

    /**
     * Constructeur vide (classe non instantiable)
     */
    private ColorMap() {
    }

    /**
     * prend en argument une couleur de joueur ChaCuN et retourne la couleur JavaFX à utiliser pour dessiner,
     *  entre autre, le contour des occupants du joueur donné
     *
     * @param playerColor : la couleur du joueur
     * @return : la couleur javaFX à utiliser
     */
    public static Color strokeColor(PlayerColor playerColor) {
        if (playerColor == null) {
            throw new IllegalArgumentException();
        }
        switch (playerColor) {
            case RED, BLUE, PURPLE -> {
                return Color.WHITE;
            }
            case GREEN, YELLOW -> {
                return fillColor(playerColor).
                        deriveColor(0, 1, 0.6, 1);
            }
        }
        return null;
    }

    /**
     * retourne la couleur JavaFXà utiliser pour remplir, entre autres, les occupants du joueur donné.
     *
     * @param playerColor : la couleur du joueur
     * @return la couleur javaFX à utiliser
     */
    public static Color fillColor(PlayerColor playerColor) {
        switch (playerColor) {
            case RED -> {
                return Color.RED;
            }
            case BLUE -> {
                return Color.BLUE;
            }
            case GREEN -> {
                return Color.LIME;
            }
            case YELLOW -> {
                return Color.YELLOW;
            }
            case PURPLE -> {
                return Color.PURPLE;
            }

        }
        return null;
    }
}


