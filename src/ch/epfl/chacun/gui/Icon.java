package ch.epfl.chacun.gui;

import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.PlayerColor;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;

/**
 * Icon:
 * permet d'obtenir des nouveaux éléments JavaFX (nœuds) représentant les occupants des différents joueurs.
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */
public final class Icon {

    /**
     * Constructeur vide (classe non instantiable)
     */
    private Icon() {
    }

    /**
     * retourne une nouvelle instance de SVGPath représentant l'occupant correspondant
     * @param player la couleur de joueur
     * @param occupant le type d'occupant
     * @return une nouvelle instance de SVGPath représentant l'occupant correspondant
     */
    public static Node newFor(PlayerColor player, Occupant.Kind occupant) {
        SVGPath path = new SVGPath();
        path.setFill(ColorMap.fillColor(player));
        path.setStroke(ColorMap.strokeColor(player));
        switch (occupant) {
            case Occupant.Kind.PAWN -> path.setContent("M -10 10 H -4 L 0 2 L 6 10 H 12 L 5 0 L 12 -2 L 12 -4" +
                    " L 6 -6L 6 -10 L 0 -10 L -2 -4 L -6 -2 L -8 -10 L -12 -10 L -8 6 Z");
            case Occupant.Kind.HUT  -> path.setContent("M -8 10 H 8 V 2 H 12 L 0 -10 L -12 2 H -8 Z");
        }
        return path;
    }
}
