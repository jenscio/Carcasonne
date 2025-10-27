package ch.epfl.chacun;


/**
 * Animal :
 * enregistrement qui représente le type et l'id des animaux
 *
 *  @author Rayan Berrada (378940)
 *  @author Jens Cancio (379510)
 *
 * @param id: l'identifiant de l'animal
 * @param kind: la sorte d'animal dont il s'agit
 */

public record Animal(int id, Kind kind) {
    public enum Kind {
    MAMMOTH,
    AUROCHS,
    DEER,
    TIGER
    }

    /**
     * titleId : retourne l'id de la tuile où se trouve l'animal
     * @return  l'id de la tuile
     */
    public  int tileId(){
       return this.id/100;

    }
}