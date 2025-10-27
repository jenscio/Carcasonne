package ch.epfl.chacun;

/**
 * Enregistrement qui décrit la position.
 * @param x: position sur l'axe X
 * @param y: position sur l'axe Y
 *
 *
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */
public record Pos(int x, int y ) {

    public final static Pos ORIGIN = new Pos(0,0);

    /**
     * Retourne une nouvelle position après un déplacement de (dX,dY)
     * @param dX: déplacement sur l'axe X
     * @param dY: déplacement sur l'axe Y
     * @return la nouvelle position
     */
    public Pos translated(int dX, int dY) {
        return new Pos(this.x + dX, this.y + dY);
    }

    /**
     * neighbor: Position des cases à coté de la case actuelle
     * @param direction: direction
     * @return position du voisin de la direction donnée
     */
    public Pos neighbor(Direction direction){

        switch (direction.ordinal()){
            case 0 -> {
                return new Pos(this.x,this.y-1);
            }
            case 1 -> {
                return new Pos(this.x+1,this.y);
            }
            case 2 -> {
                return new Pos(this.x,this.y+1);
            }
            default -> {
                return new Pos(this.x-1,this.y);
            }
        }
    }




}