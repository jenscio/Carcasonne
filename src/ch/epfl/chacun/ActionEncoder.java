package ch.epfl.chacun;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * ActionEncoder:
 * contient des méthodes permettant d'encoder et de décoder des (paramètres) d'actions,
 * et d'appliquer ces actions à un état de jeu.
 *
 *  @author Rayan Berrada (378940)
 *  @author Jens Cancio (379510)
 *
 */
public final class ActionEncoder {

    private final static int NO_OCCUPANT_PLACED_OR_RETAKEN = 0b11111;
    private final static int ZONE_ID_FACTOR = 10;
    /**
     * Constructeur vide (classe non instantiable)
     */
    private ActionEncoder() {}

    /**
     * StateActionDecodeAndApplyException:
     * Classe imbriquée d'exception à utiliser quand l'action est invalide
     *   (utilisée dans la methode decodeAndApply et decodeAndApplyHelper)
     */
    private static class StateActionDecodeAndApplyException extends Exception{
    }

    /**
     * StateAction:
     * Classe imbriquée pour combiner l'état de jeu et les actions
      * @param gameState
     * @param base32Action
     */
    public record StateAction(GameState gameState, String base32Action){}


    /**
     * retourne la liste ordonnée des positions
     * triées dans l'ordre croissant, d'abord par leur coordonnée x, puis par leur coordonnée y
     * @param insertionPositions le set avec les positions
     * @return la liste ordonnée des positions
     */
    private static List<Pos> getOrderedPositions (Set<Pos> insertionPositions) {
        return insertionPositions.stream()
                .sorted(Comparator.comparingInt(Pos::x).thenComparingInt(Pos::y))
                .toList();
    }

    /**
     * retourne la liste des occupants triés par leur zone ID
     * @param occupants le set des occupants
     * @return la liste des occupants triés par leur zone ID
     */
    private static List<Occupant> getOrderedOccupants (Set<Occupant> occupants) {
        return occupants.stream()
                .sorted(Comparator.comparingInt(Occupant::zoneId))
                .toList();
    }


    /**
     * prends en arguments un état de jeu et une tuile placée, et retourne un objet StateAction
     * avec un gameState dont la tuile à placer a été posée, et un String avec l'action codifiée
     * @param gameState : le gameState original
     * @param tileToPlace : la tuile à placer
     * @return objet StateAction (voir ci-dessus)
     */
    public static StateAction withPlacedTile(GameState gameState, PlacedTile tileToPlace) {
        int positionOfTileToPlace= getOrderedPositions(gameState.board().
                insertionPositions()).indexOf(tileToPlace.pos());
        int rotation= tileToPlace.rotation().ordinal();
        int encodedAction= positionOfTileToPlace << 2 | rotation;
        return new StateAction(gameState.withPlacedTile(tileToPlace),Base32.encodeBits10(encodedAction));
    }


    /**
     * prends en arguments un état de jeu et un occupant à poser, et retourne un objet StateAction
     * avec un gameState avec l'occupant posé, et un String avec l'action codifiée
     * @param gameState : le gameState original
     * @param occupant : l'occupant à poser
     * @return objet StateAction (voir ci-dessus)
     */
    public static StateAction withNewOccupant (GameState gameState, Occupant occupant) {
        if(occupant==null) return new StateAction(gameState.
                withNewOccupant(null),Base32.encodeBits5(NO_OCCUPANT_PLACED_OR_RETAKEN));
        int occupantKind=occupant.kind().ordinal();
        int occupiedZone= Zone.localId(occupant.zoneId());
        int encodedAction= occupantKind << 4 | occupiedZone;
        return new StateAction(gameState.withNewOccupant(occupant),Base32.encodeBits5(encodedAction));
    }


    /**
     * prends en arguments un état de jeu et un occupant à reprendre, et retourne un objet StateAction
     * avec un gameState avec l'occupant repris, et un String avec l'action codifiée
     * @param gameState : le gameState original
     * @param occupant : l'occupant à reprendre
     * @return objet StateAction (voir ci-dessus)
     */
    public static StateAction withOccupantRemoved (GameState gameState, Occupant occupant) {
        if(occupant==null) return new StateAction(gameState.withOccupantRemoved(null),
                Base32.encodeBits5(NO_OCCUPANT_PLACED_OR_RETAKEN));
        int index= getOrderedOccupants(gameState.board().occupants()).indexOf(occupant);
        return new StateAction(gameState.withOccupantRemoved(occupant),Base32.encodeBits5(index));
    }


    /**
     * retourne un StateAction composée de l'état du jeu résultant de l'application de l'action correspondante à
     * l'état passé en argument, et de la chaîne de caractères représentant l'action
     * @param gameState : le gameState original
     * @param base32EncodedAction : l'action encodée en base32
     * @return objet StateAction (voir ci-dessus)
     */

    public static StateAction decodeAndApply (GameState gameState, String base32EncodedAction) {
        try {
            return decodeAndApplyHelper(gameState,base32EncodedAction);
        }
        catch (StateActionDecodeAndApplyException exception){
            return null;
        }
    }


    /**
     * methode auxiliaire pour decoder et appliquer l'action encodée en base32
     *
     * @param gameState le gameState original
     * @param base32EncodedAction l'action encodée en base32
     * @return l'action encodée en base32
     * @throws StateActionDecodeAndApplyException si l'action est invalide
     */
    private static StateAction decodeAndApplyHelper (GameState gameState,
                                                    String base32EncodedAction)
            throws StateActionDecodeAndApplyException {

        if(!Base32.isValid(base32EncodedAction)){
            throw new StateActionDecodeAndApplyException();
        }
        int decodedAction = Base32.decode(base32EncodedAction);


        switch (gameState.nextAction()) {

            case PLACE_TILE -> {
                if (base32EncodedAction.length() != 2)  throw new StateActionDecodeAndApplyException();
                Tile getTile= gameState.tileToPlace();
                int position = decodedAction>>2;
                List<Pos> orderedPositions = getOrderedPositions(gameState.board().insertionPositions());
                if (position + 1 > orderedPositions.size())  throw new StateActionDecodeAndApplyException();
                Pos tilePosition = orderedPositions.get(position);
                int rotation= decodedAction & 0b11;
                PlacedTile placedTile = new PlacedTile(getTile,gameState.currentPlayer(),
                        Rotation.ROTATIONSArray[rotation],tilePosition,null);
                if (!gameState.board().canAddTile(placedTile))  throw new StateActionDecodeAndApplyException();

                return withPlacedTile(gameState,placedTile);
            }

            case OCCUPY_TILE -> {
                if (base32EncodedAction.length() != 1)  throw new StateActionDecodeAndApplyException();
                if (decodedAction == NO_OCCUPANT_PLACED_OR_RETAKEN) return withNewOccupant(gameState,null);
                int sortOfOccupant= decodedAction>>4;
                int localZoneId= decodedAction & 0b1111;
                if (localZoneId>9)  throw new StateActionDecodeAndApplyException();
                int zoneId = gameState.board().lastPlacedTile().id()* ZONE_ID_FACTOR + localZoneId;
                Occupant.Kind occupantKind = (sortOfOccupant==1) ? Occupant.Kind.HUT : Occupant.Kind.PAWN;
                Occupant occupantToPlace = new Occupant(occupantKind,zoneId);
                if (!gameState.lastTilePotentialOccupants().contains(occupantToPlace))
                    throw new StateActionDecodeAndApplyException();
                return withNewOccupant(gameState,new Occupant(occupantKind,zoneId));
            }

            case RETAKE_PAWN -> {
                if (base32EncodedAction.length() != 1) throw new StateActionDecodeAndApplyException();
                if (decodedAction == NO_OCCUPANT_PLACED_OR_RETAKEN) return withOccupantRemoved(gameState,null);
                if (gameState.board().occupants().size() < decodedAction + 1)
                    throw new StateActionDecodeAndApplyException();
                Occupant occupantToRemove = getOrderedOccupants(gameState.board().occupants()).
                        get(decodedAction);
                if ((occupantToRemove.kind() != Occupant.Kind.PAWN) ||
                        (gameState.board().tileWithId(Zone.tileId(occupantToRemove.zoneId())).placer() !=
                                gameState.currentPlayer()))
                    throw new StateActionDecodeAndApplyException();

                return withOccupantRemoved(gameState,occupantToRemove);
            }

            default -> throw new StateActionDecodeAndApplyException();
        }
    }



}
