package ch.epfl.chacun;

import java.util.List;
/**
 * Direction :
 * Enumeration qui regroupe les 4 orientations (N,E,S,W)
 *
 *  @author Rayan Berrada (378940)
 *  @author Jens Cancio (379510)
 *
 */
public enum Direction {
    N,
    E,
    S,
    W;

    public final static Direction[] DIRECTIONSArray = new Direction[] {
            Direction.N, Direction.E, Direction.S, Direction.W
    };
    public final static List<Direction> ALL = List.of(DIRECTIONSArray);
    public final static int COUNT = ALL.size();

    /**
     * Effectue la rotation d'une direction
     * @param rotation la rotation donnée
     * @return une nouvelle direction
     */
    public Direction rotated(Rotation rotation){
        return DIRECTIONSArray[(this.ordinal()+ rotation.ordinal())%4];
    }

    /**
     * Donne la direction opposée de la direction actuelle
     * @return direction opposée
     */
    public Direction opposite(){
        return (DIRECTIONSArray[(2+this.ordinal())%4]);
    }
}


