package ch.epfl.chacun;

import java.util.List;

/**
 * Rotation: Enumération qui reprèsente toutes les rotations possibles
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */
public enum Rotation {
    NONE,
    RIGHT,
    HALF_TURN,
    LEFT;

    public final static Rotation[] ROTATIONSArray = new Rotation[] {
            Rotation.NONE, Rotation.RIGHT, Rotation.HALF_TURN, Rotation.LEFT
    };
    public final static List<Rotation> ALL = List.of(ROTATIONSArray);

    public final static int COUNT = ALL.size();

    /**
     * add: Addition de 2 rotation différentes
     * @param that: la rotation
     * @return l'addition de 2 rotation
     */
    public Rotation add(Rotation that){
        return ROTATIONSArray[(this.ordinal()+ that.ordinal())%4];
    }

    /**
     * negated: Retourne la rotation inverse a la rotation acutelle, pour chaque cas
     * @return la rotation inverse
     */
    public Rotation negated(){
        return (ROTATIONSArray[(4-this.ordinal())%4]);
    }

    public int quarterTurnsCW(){
        return this.ordinal();
    }

    /**
     * On peut simplement multiplier le this.ordinal par 90 pour obtenir les degrés
     * @return les degrées de la rotation
     */
    public int degreesCW(){
        return this.ordinal()*90;
    }
}
