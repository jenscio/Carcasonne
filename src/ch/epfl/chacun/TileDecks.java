package ch.epfl.chacun;

import java.util.List;
import java.util.function.Predicate;
/**
 *  TileDecks :
 *  public et immuable
 *  représente les tas des trois sortes de tuile qui existent: départ, normale, menhir.
 *  @param startTiles: qui contient la tuile de départ (ou rien du tout)
 *  @param normalTiles: qui contient les tuiles normales restantes
 *  @param menhirTiles: qui contient les tuiles menhirs restantes
 *
 *  @author Rayan Berrada (378940)
 *  @author Jens Cancio (379510)
 *
 */
public record TileDecks(List<Tile> startTiles,List<Tile> normalTiles,List<Tile> menhirTiles) {
    /**
     * constructeur compact de TileDecks se charge de garantir l'immuabilité de la classe en copiant-au moyen de la méthode copyOf de List — chacune des trois listes reçues.
     * @param startTiles: tuiles de départ
     * @param normalTiles: tuiles normales
     * @param menhirTiles: tuiles menhir
     */

    public TileDecks{
        startTiles = List.copyOf(startTiles);
        normalTiles = List.copyOf(normalTiles);
        menhirTiles = List.copyOf(menhirTiles);
    }

    /**
     * retourne le nombre de tuiles disponibles dans le tas contenant les tuiles de la sorte donnée
     * @param kind: la sorte, le type d'une tuile
     * @return le nombre de tuiles disponibles dans le tas contenant les tuiles de la sorte donnée
     */
    public int deckSize(Tile.Kind kind){
        switch (kind) {
            case START -> {
                return startTiles.size();
            }
            case NORMAL -> {
                return normalTiles.size();
            }
            default -> {
                return menhirTiles.size();
            }
        }
    }

    /**
     * retourne la tuile au sommet du tas contenant les tuiles de la sorte donnée, ou null si le tas est vide
     * @param kind: la sorte, le type d'une tuile
     * @return: la tuile au sommet du tas contenant les tuiles de la sorte donnée, ou null si le tas est vide
     */
    public Tile topTile(Tile.Kind kind){
        if (deckSize(kind)==0){
            return null;
        }
        switch (kind) {
            case START -> {
                return startTiles.getFirst();
            }
            case NORMAL -> {
                return normalTiles.getFirst();
            }
            default -> {
                return menhirTiles.getFirst();
            }
        }
    }

    /**
     *retourne un nouveau triplet de tas égal au récepteur (this) si ce n'est que la tuile du sommet du tas contenant les tuiles de la sorte donnée en a été supprimée; lève IllegalArgumentException si ce tas est vide
     * @param kind: la sorte, le type d'une tuile
     * @return n nouveau triplet de tas égal au récepteur (this) si ce n'est que la tuile du sommet du tas contenant les tuiles de la sorte donnée en a été supprimée
     * @throws IllegalArgumentException si le tas est vide
     */
    public TileDecks withTopTileDrawn(Tile.Kind kind) {
        if (deckSize(kind) == 0) {
            throw new IllegalArgumentException();
        } else {
            List<Tile> startTilesCopy = List.copyOf(startTiles);
            List<Tile> normalTilesCopy = List.copyOf(normalTiles);
            List<Tile> menhirTilesCopy = List.copyOf(menhirTiles);

            switch (kind) {
                case START -> {
                    return new TileDecks(startTilesCopy.subList(1, startTilesCopy.size()), normalTiles, menhirTiles);
                }
                case NORMAL -> {
                    return new TileDecks(startTiles, normalTilesCopy.subList(1, normalTilesCopy.size()), menhirTiles);
                }
                default -> {
                    return new TileDecks(startTiles, normalTiles, menhirTilesCopy.subList(1, menhirTilesCopy.size()));
                }
            }
        }
    }

    /**
     * retourne un nouveau triplet de tas égal au récepteur sans les tuiles au sommet du tas contenant celles de la sorte donnée pour lesquelles la méthode test de predicate retourne faux
     * @param kind: la sorte, le type d'une tuile
     * @param predicate une expression booléenne (vraie ou fausse),
     * @return un nouveau triplet de tas égal au récepteur sans les tuiles au sommet du tas contenant celles de la sorte donnée pour lesquelles la méthode test de predicate retourne faux
     */

   public TileDecks withTopTileDrawnUntil(Tile.Kind kind, Predicate<Tile> predicate) {
        TileDecks temp = new TileDecks(startTiles, normalTiles, menhirTiles);
        while ( temp.deckSize(kind)>0 && !predicate.test(temp.topTile(kind))){
                temp = temp.withTopTileDrawn(kind);
        }

        return temp;
    }

}
