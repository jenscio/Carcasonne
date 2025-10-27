package ch.epfl.chacun;

import java.util.*;

/**
 * Tile
 * @param id
 * @param kind
 * @param n: Coté de la tuile du nord
 * @param e: Coté de la tuile de l'Est
 * @param s: Coté de la tuile du Sud
 * @param w: Coté de la tuile du West
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */
public record Tile(int id, Kind kind, TileSide n, TileSide e, TileSide s, TileSide w) {
    public enum Kind{
        START,
        NORMAL,
        MENHIR
    }

    /**
     * Retourne une liste contenant les 4 Tilesides de la tuile
     * @return  une liste contenant les 4 Tilesides de la tuile
     */
    public List<TileSide> sides(){
        return (new ArrayList<>(Arrays.asList(n,e,s,w)));
    }

    /**
     * Retourne l'ensemble des zones de bordure de la tuile
     * @return l'ensemble des zones de bordure de la tuile,
     */
    public Set<Zone> sideZones(){
        Set<Zone> sideZone = new HashSet<>();
        sideZone.addAll(n.zones());
        sideZone.addAll(e.zones());
        sideZone.addAll(w.zones());
        sideZone.addAll(s.zones());
        return sideZone;
    }

    /**
     * Retourne l'ensemble de toutes les zones de la tuile, lacs compris.
     * @return l'ensemble de toutes les zones de la tuile, lacs compris.
     */
    public Set<Zone> zones() {
        Set<Zone> allZones = new HashSet<>();

        for(Zone zone: sideZones() ){
            allZones.add(zone);
            if (zone instanceof Zone.River river) {
                if (((Zone.River) zone).hasLake()) {
                    allZones.add(river.lake());
                }
            }
        }
        return allZones;
    }



}
