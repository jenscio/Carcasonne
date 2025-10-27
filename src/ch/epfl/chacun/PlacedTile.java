package ch.epfl.chacun;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
/**
 * PlacedTile :
 * enregistrement PlacedTile public, représente une tuile qui a été placée
 * @param tile: la tuile qui a été placée,
 * @param placer: le placeur de la tuile
 * @param rotation: la rotation appliquée à la tuile lors de son placement
 * @param pos: la position à laquelle la tuile a été placée
 * @param occupant: l'occupant de la tuile
 *  @author Rayan Berrada (378940)
 *  @author Jens Cancio (379510)
 */

public record PlacedTile(Tile tile, PlayerColor placer, Rotation rotation, Pos pos, Occupant occupant) {
    /**
     * Constructeur compact pour PlacedTile, valide les arguments en vérifiant que ni tile, ni rotation, ni pos ne sont égaux à null.
     * @throws NullPointerException : si un des arguments exceptés placer et occupant sont null
     */
    public PlacedTile {
        Objects.requireNonNull(tile);
        Objects.requireNonNull(rotation);
        Objects.requireNonNull(pos);
    }
    /**
     * constructeur secondaire qui prend les mêmes arguments que le principal, dans le même ordre, sauf le dernier (occupant). Ce constructeur secondaire appelle le principal en lui passant les arguments reçus, et null comme dernier argument (occupant). Ce constructeur a pour but de faciliter la création de tuiles placées sans occupant.
     * @param tile: la tuile qui a été placée,
     * @param placer: le placeur de la tuile
     * @param rotation: la rotation appliquée à la tuile lors de son placement
     * @param pos: la position à laquelle la tuile a été placée
     */
    public PlacedTile(Tile tile, PlayerColor placer, Rotation rotation, Pos pos) {
        this(tile, placer, rotation, pos, null);
    }
    /**
     *  retourne l'identifiant de la tuile placée
     * @return: l'identifiant de la tuile placée
     */
    public int id() {
        return tile().id();
    }
    /**
     * retourne la sorte de la tuile placée
     * @return: la sorte de la tuile placée
     */
    public Tile.Kind kind() {
        return tile.kind();
    }
    /**
     * retourne le côté de la tuile dans la direction donnée, en tenant compte de la rotation appliquée à la tuile
     * @param direction: la direction(N,W,S,E)
     * @return le côté de la tuile dans la direction donnée, en tenant compte de la rotation appliquée à la tuile
     */
    public TileSide side(Direction direction) {
        return tile.sides().get(direction.rotated(rotation.negated()).ordinal());
    }
    /**
     * retourne la zone de la tuile dont l'identifiant est celui donné, ou lève IllegalArgumentException si la tuile ne possède pas de zone avec cet identifiant
     * @param id: l'identifiant de la tuile
     * @return la zone de la tuile dont l'identifiant est celui donné
     * @throws IllegalArgumentException: si la tuile ne possède pas de zone avec cet identifiant
     */
    public Zone zoneWithId(int id){
        for(Zone zoneOfTile : tile().zones()) {
            if(id==zoneOfTile.tileId()*10+zoneOfTile.localId()){
                return zoneOfTile;
            }
        }
        throw new IllegalArgumentException();
    }
    /** retourne a zone de la tuile ayant un pouvoir spécial, il y en a au plus une par tuile, ou null s'il n'y en a aucune
     * @return la zone de la tuile ayant un pouvoir spécial, il y en a au plus une par tuile, ou null s'il n'y en a aucune
     */
    public Zone specialPowerZone() {
        for (Zone zoneOfTile : tile().zones()) {
            if (zoneOfTile.specialPower() != null) {
                return zoneOfTile;
            }
        }
        return null;
    }
    /**
     * retourne l'ensemble de tous les occupants potentiels de la tuile, ou un ensemble vide si la tuile est celle de départ qui se reconnaît au fait que son placeur est null
     * @return l'ensemble de tous les occupants potentiels de la tuile, ou un ensemble vide si la tuile est celle de départ qui se reconnaît au fait que son placeur est null
     */
    public Set<Occupant> potentialOccupants() {
        Set<Occupant> potentialOccupants = new HashSet<>();
        if (placer == null) {
            return potentialOccupants;
        }
        for (Zone zone : tile().sideZones()) {
            potentialOccupants.add(new Occupant(Occupant.Kind.PAWN, zone.id()));
            if ((zone instanceof Zone.River river)) {
                if (!river.hasLake()) {
                    potentialOccupants.add(new Occupant(Occupant.Kind.HUT, river.id()));
                }
                else { potentialOccupants.add(new Occupant(Occupant.Kind.HUT, river.lake().id())); }
            }
        }
        return potentialOccupants;
    }
    /**
     * retourne l'ensemble, éventuellement vide, des zones forêts de la tuile
     * @return l'ensemble, éventuellement vide, des zones forêts de la tuile
     */
    public Set<Zone.Forest> forestZones(){
        Set<Zone.Forest> forestZones = new HashSet<>();
        Set<Zone> zones = tile.zones();
        for (Zone zonesOfTile: zones){
            if (zonesOfTile instanceof Zone.Forest){
                forestZones.add((Zone.Forest) zonesOfTile);
            }

        }
        return forestZones;

    }
    /**
     * retourne l'ensemble, éventuellement vide, des zones prés de la tuile
     * @return l'ensemble, éventuellement vide, des zones prés de la tuile
     */
    public Set<Zone.Meadow> meadowZones(){
        Set<Zone.Meadow> meadowZones = new HashSet<>();
        Set<Zone> zones = tile.zones();
        for (Zone zonesOfTile: zones){
            if (zonesOfTile instanceof Zone.Meadow){
                meadowZones.add((Zone.Meadow) zonesOfTile);
            }

        }
        return meadowZones;

    }
    /**
     * retourne l'ensemble, éventuellement vide, des zones rivières de la tuile
     * @return l'ensemble, éventuellement vide, des zones rivières de la tuile
     */
    public Set<Zone.River> riverZones(){
        Set<Zone.River> riverZones = new HashSet<>();
        Set<Zone> zones = tile.zones();
        for (Zone zonesOfTile: zones){
            if (zonesOfTile instanceof Zone.River){
                riverZones.add((Zone.River) zonesOfTile);
            }

        }
        return riverZones;

    }
    /**
     * qui retourne une tuile placée identique au récepteur (this), mais occupée par l'occupant donné, ou lève IllegalArgumentException si le récepteur est déjà occupé
     * @param occupants:l'occupant de la tuile
     * @return une tuile placée identique au récepteur (this), mais occupée par l'occupant donné
     * @throws IllegalArgumentException si le récepteur est déjà occupé
     */
    public PlacedTile withOccupant(Occupant occupants){
        if (this.occupant!=null){
            throw new IllegalArgumentException();
        }
        else return new PlacedTile(tile,placer,rotation,pos,occupants);
    }
    /**
     * retourne une tuile placée identique au récepteur, mais sans occupant
     * @return une tuile placée identique au récepteur, mais sans occupant
     */
    public PlacedTile withNoOccupant(){
       return new PlacedTile(tile,placer,rotation,pos,null);
    }
    /**
     *retourne l'identifiant de la zone occupée par un occupant de la sorte donnée (pion ou hutte), ou -1 si la tuile n'est pas occupée, ou si l'occupant n'est pas de la bonne sorte
     * @param occupantKind: un type d'occupant
     * @return l'identifiant de la zone occupée par un occupant de la sorte donnée (pion ou hutte), ou -1 si la tuile n'est pas occupée, ou si l'occupant n'est pas de la bonne sorte
     */
    public int idOfZoneOccupiedBy(Occupant.Kind occupantKind){
        if (this.occupant==null){
            return -1;
        }
        if(this.occupant.kind().equals(occupantKind)){
            return occupant.zoneId();
        }
        else return -1;
    }
}
