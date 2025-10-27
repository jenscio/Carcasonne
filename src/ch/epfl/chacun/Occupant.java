package ch.epfl.chacun;

import java.util.Objects;

/**
 * Occupant: Enregistrement qui décrit les pions et les maisonettes
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 *
 *
 * @param kind: Kind of the occupant
 * @param zoneId: id of the zone where the occupant is
 */

public record Occupant(Kind kind, int zoneId ) {
    public enum Kind {
    PAWN,
    HUT
    }

    /**
     * Occupant: constructeur compact de Occupant
     * @param kind: type de l'occupant
     * @param zoneId: identifiant de la zone ou l'occupant est
     */
    public Occupant{
        Objects.requireNonNull(kind);
        if (zoneId<0){
            throw new IllegalArgumentException();
        }
    }

    /**
     * occupantsCount: retourne le nombre d'occupants de la sorte donnée que possède un joueur:
     *                 5 pour les pions, 3 pour les huttes.
     * @param kind: type de l'occupant
     * @return int nombre d'occupants
     */
    public static int occupantsCount(Kind kind){
        if (kind == Kind.PAWN){
            return 5;
        }
        else return 3;
    }
}