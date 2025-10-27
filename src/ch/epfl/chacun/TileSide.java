package ch.epfl.chacun;

import java.util.ArrayList;
import java.util.List;
/**
 * Représente un bord de tuile
 *
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 *
 */

public sealed interface TileSide {
    /**
     * @return: zones qui touchent le bord représenté par le récepteur
     */
    List<Zone> zones();
    /**
     * @param: that: bord de tuile
     * @return: vrai si that est de la meme sorte que this
     */
    boolean isSameKindAs(TileSide that);
    /**
     * record: bord de tuile forêt
     */
    record Forest(Zone.Forest forest) implements TileSide {
        /**
         * @return: zones qui touchent le bord représenté par le récepteur
         */
        public List<Zone> zones(){
            List<Zone> borderZone = new ArrayList<>();
            borderZone.add(forest);
            return borderZone;
        }
        /**
         * @param: that: bord de tuile
         * @return: vrai si that est de la meme sorte que this
         */
        public boolean isSameKindAs(TileSide that) {
            return that.getClass().equals(this.getClass());
        }
    }
    /**
     * Enregistrement: bord de tuile pré
     */
    record Meadow(Zone.Meadow meadow) implements TileSide{
        /**
         * @return: zones qui touchent le bord représenté par le récepteur
         */
        public List<Zone> zones(){
            List<Zone> borderZone = new ArrayList<>();
            borderZone.add(meadow);
            return borderZone;
        }
        /**
         * @param: that: bord de tuile
         * @return: vrai si that est de la meme sorte que this
         */
        public boolean isSameKindAs(TileSide that) {
            return that.getClass().equals(this.getClass());
        }
    }
    /**
     * record: bord de tuile rivière
     */
    record River(Zone.Meadow meadow1, Zone.River river, Zone.Meadow meadow2) implements TileSide{
        /**
         * @return: zones qui touchent le bord représenté par le récepteur
         */
        public  List<Zone> zones(){
            List<Zone> borderZone = new ArrayList<>();
            borderZone.add(meadow1);
            borderZone.add(river);
            borderZone.add(meadow2);

            return borderZone;
        }
        /**
         * @param: that: bord de tuile
         * @return: vrai si that est de la meme sorte que this
         */
        public boolean isSameKindAs(TileSide that) {
            return that.getClass().equals(this.getClass());
        }
    }
}