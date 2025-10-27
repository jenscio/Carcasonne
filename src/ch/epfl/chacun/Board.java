package ch.epfl.chacun;

import java.util.*;

/**
 * Board :
 * représente le plateau de jeu.
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */

public class Board {
    // portée du plateau, qui est le nombre de cases qui séparent la case centrale de l'un des bords du plateau, soit 12
    public static final int REACH = 12;
    // numero de champs (625)
    private static final int numberFields = (REACH * 2 + 1) * (REACH * 2 + 1);
    // le plateau vide, qui ne contient absolument aucune tuile, même pas celle de départ:
    public static final Board EMPTY = new Board(new PlacedTile[numberFields], new int[0], ZonePartitions.EMPTY,
            Collections.emptySet());
    private final PlacedTile[] placedTiles;
    private final int[] indexPlacedTiles;
    private final ZonePartitions zonePartitions;
    private final Set<Animal> cancelledAnimals;

    /**
     * Constructeur de la classe Board
     *
     * @param placedTiles:      tableau de tuiles placées
     * @param indexPlacedTiles: tableau d'entiers contenant les index, dans le premier tableau
     * @param zonePartitions:   instance de ZonePartitions, dont le contenu correspond à celui du plateau
     * @param cancelledAnimals: l'ensemble des animaux annulés
     */
    private Board(PlacedTile[] placedTiles, int[] indexPlacedTiles, ZonePartitions zonePartitions,
                  Set<Animal> cancelledAnimals) {
        this.placedTiles = placedTiles;
        this.indexPlacedTiles = indexPlacedTiles;
        this.zonePartitions = zonePartitions;
        this.cancelledAnimals = cancelledAnimals;
    }

    /**
     * retourne l'index dans le tableau de tuiles placées pour la position pos
     *
     * @param pos: la position
     * @return l'index dans le tableau de tuiles placées
     */
    private int pos2Index(Pos pos) {
        return (pos.y() + REACH) * (REACH * 2 + 1) + pos.x() + REACH;
    }

    /**
     * retourne la position pour l'index dans le tableau de tuiles placées donnée
     *
     * @param index : l'index donnée
     * @return position pour l'index dans le tableau de tuiles placées donnée
     */
    private Pos index2Pos(int index) {
        return new Pos(index % (REACH * 2 + 1) - REACH, index / (REACH * 2 + 1) - REACH);
    }

    /**
     * returne vrai ssi la position est dans le tableau
     *
     * @param pos la position donnée
     * @return vrai ssi la position est dans le tableau
     */
    private boolean posInBoard(Pos pos) {
        return (pos.y() >= -REACH && pos.y() <= REACH &&
                pos.x() >= -REACH && pos.x() <= REACH);
    }

    /**
     * retourne la tuile à la position donnée, ou null s'il n'y en a aucune ou si la position se trouve hors du plateau
     *
     * @param pos: la position donnée
     * @return la tuile à la position donnée, ou null s'il n'y en a aucune ou si la position se trouve hors du plateau
     */
    public PlacedTile tileAt(Pos pos) {
        if (posInBoard(pos)) {
            return placedTiles[pos2Index(pos)];
        }
        return null;
    }

    /**
     * retourne la tuile dont l'identité est celle donnée
     *
     * @param tileId : la tuile donnée
     * @return la tuile dont l'identité est celle donné
     * @throws IllegalArgumentException si cette tuile ne se trouve pas sur le plateau,
     */
    public PlacedTile tileWithId(int tileId) {
        for (PlacedTile placedTile : placedTiles) {
            if (placedTile != null && placedTile.id() == tileId) {
                return placedTile;
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * retourne l'ensemble des animaux annulés
     *
     * @return l'ensemble des animaux annulés
     */
    public Set<Animal> cancelledAnimals() {
        return Collections.unmodifiableSet(cancelledAnimals);
    }

    /**
     * retourne la totalité des occupants se trouvant sur les tuiles du plateau
     *
     * @return la totalité des occupants se trouvant sur les tuiles du plateau
     */
    public Set<Occupant> occupants() {
        Set<Occupant> occupants = new HashSet<>();
        for (PlacedTile placedTile : placedTiles) {
            if (placedTile != null && placedTile.occupant() != null) {
                occupants.add(placedTile.occupant());
            }
        }
        return occupants;
    }


    /**
     * retourne l'aire forêt contenant la zone donnée
     *
     * @param forest: la zone donnée
     * @return l'aire forêt contenant la zone donnée
     * @throws IllegalArgumentException si la zone en question n'appartient pas au plateau
     */
    public Area<Zone.Forest> forestArea(Zone.Forest forest) {
        return zonePartitions.forests().areaContaining(forest);
    }

    /**
     * retourne l'aire pré contenant la zone donnée
     *
     * @param meadow: la zone donnée
     * @return l'aire pré contenant la zone donnée
     */
    public Area<Zone.Meadow> meadowArea(Zone.Meadow meadow) {
        return zonePartitions.meadows().areaContaining(meadow);
    }

    /**
     * retourne l'aire rivière contenant la zone donnée
     *
     * @param river: la zone donnée
     * @return l'aire rivière contenant la zone donnée
     */
    public Area<Zone.River> riverArea(Zone.River river) {
        return zonePartitions.rivers().areaContaining(river);
    }

    /**
     * retourne l'aire réseau hydrographique contenant la zone donnée
     *
     * @param water: la zone donnée
     * @return l'aire réseau hydrographique contenant la zone donnée
     */
    public Area<Zone.Water> riverSystemArea(Zone.Water water) {
        return zonePartitions.riverSystems().areaContaining(water);
    }

    /**
     * retourne l'ensemble de toutes les aires pré du plateau
     *
     * @return l'ensemble de toutes les aires pré du plateau
     */
    public Set<Area<Zone.Meadow>> meadowAreas() {
        return zonePartitions.meadows().areas();
    }

    /**
     * retourne l'ensemble de toutes les aires réseau hydrographique du plateau
     *
     * @return l'ensemble de toutes les aires réseau hydrographique du plateau
     */
    public Set<Area<Zone.Water>> riverSystemAreas() {
        return zonePartitions.riverSystems().areas();
    }

    /**
     * retourne le pré adjacent à la zone donnée, sous la forme d'une aire qui ne contient que les
     * zones de ce pré mais tous les occupants du pré complet
     *
     * @param pos:        la position de la tuile
     * @param meadowZone: la zone donnée
     * @return le pré adjacent à la zone donnée (voir ci-dessus)
     */
    public Area<Zone.Meadow> adjacentMeadow(Pos pos, Zone.Meadow meadowZone) {
        // areaMeadow: Area qui contient meadowZone
        Area<Zone.Meadow> areaMeadow = zonePartitions.meadows().areaContaining(meadowZone);
        Set<Zone.Meadow> adjacentMeadowZones = new HashSet<>();
        for (int ix = pos.x() - 1; ix <= pos.x() + 1; ix++) {       // chercher toutes les positions entourant "pos"
            for (int iy = pos.y() - 1; iy <= pos.y() + 1; iy++) {
                Pos tempPos = new Pos(ix, iy);
                if (posInBoard(tempPos)) { // ignorer positions hors plateau
                    PlacedTile pt = tileAt(tempPos);
                    if (pt != null) { // ssi il y a un PlacedTile posé à cet emplacement
                        for (Zone.Meadow mZone : pt.meadowZones()) {  // pour toutes les meadows dans le PlacedTile:
                            if (areaMeadow.zones().contains(mZone)) { // sont-ils dans le Area?
                                adjacentMeadowZones.add(mZone);       // si oui, l'ajouter au Set
                            }
                        }
                    }
                }
            }
        }
        // retourne le nouveau area avec tous les occupants et connexions = 0
        return new Area<>(adjacentMeadowZones, areaMeadow.occupants(), 0);
    }


    /**
     * retourne le nombre d'occupants de la sorte donnée appartenant au joueur donné et se trouvant sur le plateau
     *
     * @param player:       le joueur donné
     * @param occupantKind: la sorte d'occupant donnée
     * @return le nombre d'occupants de la sorte donnée appartenant au joueur donné et se trouvant sur le plateau
     */
    public int occupantCount(PlayerColor player, Occupant.Kind occupantKind) {
        int occupantCount = 0;
        for (PlacedTile placedTile : placedTiles) {
            if (placedTile != null && placedTile.placer() != null &&
                    placedTile.placer().equals(player) &&
                    placedTile.occupant() != null && placedTile.occupant().kind().equals(occupantKind)) {
                occupantCount += 1;
            }
        }
        return occupantCount;
    }

    /**
     * retourne l'ensemble des positions d'insertions du plateau
     *
     * @return l'ensemble des positions d'insertions du plateau
     */
    public Set<Pos> insertionPositions() {
        Set<Pos> insertPos = new HashSet<>();
        for (int i = 0; i < numberFields; i++) {
            if (placedTiles[i] == null) {
                Pos currentPos = index2Pos(i);
                for (Direction direction : Direction.ALL) {
                    if (posInBoard(currentPos.neighbor(direction)) && tileAt(currentPos.neighbor(direction)) != null) {
                        insertPos.add(currentPos);
                    }
                }
            }
        }
        return insertPos;
    }

    /**
     * retourne la dernière tuile posée, qui peut être la tuile de départ si la
     * première tuile normale n'a pas encore été placée. ou null si le plateau est vide
     *
     * @return la dernière tuile posée (voir ci-dessus)
     */
    public PlacedTile lastPlacedTile() {
        if (indexPlacedTiles.length > 0) {
            Pos lastPlacedTile = index2Pos(indexPlacedTiles[indexPlacedTiles.length - 1]);
            return tileAt(lastPlacedTile);
        }
        return null;
    }

    /**
     * retourne l'ensemble de toutes les aires forêts qui ont été fermées suite à la pose de la dernière tuile,
     * ou un ensemble vide si le plateau est vide,
     *
     * @return l'ensemble de toutes les aires forêts fermées (voir ci-dessus)
     */
    public Set<Area<Zone.Forest>> forestsClosedByLastTile() {
        Set<Area<Zone.Forest>> forestSet = new HashSet<>();
        if (lastPlacedTile() == null) {
            return forestSet;
        }
        for (Zone.Forest forest : lastPlacedTile().forestZones()) {
            if (forestArea(forest).openConnections() == 0) {
                forestSet.add(forestArea(forest));
            }
        }
        return forestSet;
    }

    /**
     * retourne l'ensemble de toutes les aires rivières qui ont été fermées suite à la pose de la dernière tuile,
     * ou un ensemble vide si le plateau est vide,
     *
     * @return l'ensemble de toutes les aires rivières fermées (voir ci-dessus)
     */
    public Set<Area<Zone.River>> riversClosedByLastTile() {
        Set<Area<Zone.River>> riverSet = new HashSet<>();
        if (lastPlacedTile() == null) {
            return riverSet;
        }
        for (Zone.River river : lastPlacedTile().riverZones()) {
            if (riverArea(river).openConnections() == 0) {
                riverSet.add(riverArea(river));
            }
        }
        return riverSet;
    }


    /**
     * retourne vrai ssi la tuile placée donnée pourrait être ajoutée au plateau
     *
     * @param tile la tuile placée
     * @return vrai ssi la tuile placée donnée pourrait être ajoutée au plateau
     */
    public boolean canAddTile(PlacedTile tile) {
        // verifier que la position est bien dans les insertionPositions()
        if (!insertionPositions().contains(tile.pos())) {
            return false;
        }
        // pour toutes les directions, regarder si le cote des cellules placées est compatible
        for (Direction dir : Direction.ALL) {
            if (posInBoard(tile.pos().neighbor(dir))) { // ne regarder hors tableau
                PlacedTile neighbor = tileAt(tile.pos().neighbor(dir));
                // si la cellulle est occupée et PAS compatible, on ne peut pas ajouter la tuile
                if (neighbor != null &&
                        !neighbor.side(dir.opposite()).isSameKindAs(tile.side(dir))) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * retourne vrai ssi la tuile donnée pourrait être posée sur l'une des positions d'insertion du plateau
     * (eventuellement après rotation)
     *
     * @param tile la tuile donnée
     * @return vrai ssi la tuile donnée pourrait être posée (voir ci-dessus)
     */
    public boolean couldPlaceTile(Tile tile) {
        // s'il y a au moins une position compatible pour une des insertionPositions(), retourner true
        for (Pos p : insertionPositions()) {
            for (Rotation r : Rotation.ALL) { // essayer toutes les rotations
                if (canAddTile(new PlacedTile(tile, null, r, p))) {
                    return true;
                }
            }
        }
        // aucune combination marche
        return false;
    }


    /**
     * retourne un plateau identique au récepteur, mais avec la tuile donnée en plus
     *
     * @param tile : la tuile donnée
     * @return un plateau identique au récepteur, mais avec la tuile donnée en plus
     * @throws IllegalArgumentException si le plateau n'est pas vide et la tuile donnée ne peut pas être ajoutée
     *         au plateau
     */
    public Board withNewTile(PlacedTile tile) {
        if (indexPlacedTiles.length != 0 && !canAddTile(tile)) {
            // plateau pas vide et pas possible d'ajouter la tuile? Lever exception
            throw new IllegalArgumentException();
        }

        // faire des copies des attributs pour bien assurer la immuabilité
        PlacedTile[] newPlacedTiles = placedTiles.clone();
        int newIndex = pos2Index(tile.pos());
        newPlacedTiles[newIndex] = tile;
        // ajouter une position au index des placed tiles copié
        int[] newIndexPlacedTiles = Arrays.copyOf(indexPlacedTiles, indexPlacedTiles.length + 1);
        newIndexPlacedTiles[indexPlacedTiles.length] = newIndex;
        Set<Animal> newCancelledAnimals = Collections.unmodifiableSet(cancelledAnimals);

        // generer une nouvelle partition avec le builder, en utilisant l'ancienne
        ZonePartitions.Builder newZonePartitionsBuilder = new ZonePartitions.Builder(zonePartitions);
        newZonePartitionsBuilder.addTile(tile.tile());

        // maintenant il faut connecter la nouvelle tuile avec les autres existantes!

        for (Direction dir : Direction.ALL) { // regarder dans toutes les directions
            Pos otherTilePos = tile.pos().neighbor(dir);
            if (tileAt(otherTilePos) != null) {
                // il y a une autre tuile à cet endroit:
                // il faut connecter les deux cotes opposes des deux tuiles
                newZonePartitionsBuilder.connectSides(tile.side(dir), tileAt(otherTilePos).side(dir.opposite()));
            }
        }

        ZonePartitions newZonePartitions = newZonePartitionsBuilder.build();

        return new Board(newPlacedTiles, newIndexPlacedTiles, newZonePartitions, newCancelledAnimals);
    }

    /**
     * retourne un plateau identique au récepteur, mais avec l'occupant donné en plus
     *
     * @param occupant: l'occupant à placer
     * @return un plateau identique au récepteur, mais avec l'occupant donné en plus
     * @throws IllegalArgumentException si la tuile sur laquelle se trouverait l'occupant est déjà occupée
     */
    public Board withOccupant(Occupant occupant) {

        // quelle est la tuile à occuper?
        int tileID = Zone.tileId(occupant.zoneId());
        PlacedTile currentTile = tileWithId(tileID);
        // lever une exception si la tuile est déjà occupé
        if (currentTile.occupant() != null) {
            throw new IllegalArgumentException();
        }

        // nouvelle tuile avec occupant, pour remplacer l'ancienne
        PlacedTile newTile = currentTile.withOccupant(occupant);

        // créer un nouvel zonePartitions builder
        ZonePartitions.Builder newZonePartitionsBuilder = new ZonePartitions.Builder(zonePartitions);

        // ajouter l'occupant avec les bons attributs
        newZonePartitionsBuilder.addInitialOccupant(newTile.placer(), occupant.kind(),
                newTile.zoneWithId(occupant.zoneId()));

        // créer un nouveau array pour les tuiles placées et remplacer l'ancienne tuile par la nouvelle
        PlacedTile[] newPlacedTiles = placedTiles.clone();
        newPlacedTiles[pos2Index(currentTile.pos())] = newTile;

        // pas de changement dans cet index: clone OK
        int[] newIndexPlacedTiles = indexPlacedTiles.clone();

        // nouvelle partition prête
        ZonePartitions newZonePartitions = newZonePartitionsBuilder.build();


        Set<Animal> newCancelledAnimals = Set.copyOf(cancelledAnimals);

        return new Board(newPlacedTiles, newIndexPlacedTiles, newZonePartitions, newCancelledAnimals);

    }

    /**
     * retourne un plateau identique au récepteur, mais avec l'occupant donné en moins
     *
     * @param occupant: l'occupant à placer
     * @return un plateau identique au récepteur, mais avec l'occupant donné en moins
     * @throws IllegalArgumentException si la tuile n'est pas occupée par l'occupant donné
     */
    public Board withoutOccupant(Occupant occupant) {

        int tileID = Zone.tileId(occupant.zoneId());
        PlacedTile currentTile = tileWithId(tileID);
        if (!currentTile.occupant().equals(occupant)) {
            throw new IllegalArgumentException();
        }

        // nouvelle tuile, mais vidée du occupant
        PlacedTile newTile = currentTile.withNoOccupant();

        ZonePartitions.Builder newZonePartitionsBuilder = new ZonePartitions.Builder(zonePartitions);
        // dans ce cas, l'occupant est toujours un pawn (les huttes sont jamais enlevées)
        newZonePartitionsBuilder.removePawn(currentTile.placer(), currentTile.zoneWithId(occupant.zoneId()));

        PlacedTile[] newPlacedTiles = placedTiles.clone();
        newPlacedTiles[pos2Index(currentTile.pos())] = newTile;

        int[] newIndexPlacedTiles = indexPlacedTiles.clone();

        Set<Animal> newCancelledAnimals = Set.copyOf(cancelledAnimals);

        ZonePartitions newZonePartitions = newZonePartitionsBuilder.build();

        return new Board(newPlacedTiles, newIndexPlacedTiles, newZonePartitions, newCancelledAnimals);
    }

    /**
     * retourne un plateau identique au récepteur mais sans aucun occupant dans les forêts et les rivières données
     *
     * @param forests: Set avec les forêts données
     * @param rivers:  Set avec les rivières données
     * @return un plateau identique au récepteur mais sans aucun occupant dans les forêts et les rivières données
     */
    public Board withoutGatherersOrFishersIn(Set<Area<Zone.Forest>> forests, Set<Area<Zone.River>> rivers) {

        ZonePartitions.Builder zonePartitionBuilder = new ZonePartitions.Builder(zonePartitions);
        for (Area<Zone.Forest> forestArea : forests) {
            zonePartitionBuilder.clearGatherers(forestArea);
        }
        for (Area<Zone.River> riverArea : rivers) {
            zonePartitionBuilder.clearFishers(riverArea);
        }


        PlacedTile[] newPlacedTiles = placedTiles.clone();
        for (PlacedTile placedTile : this.placedTiles) {
            if (placedTile != null) {
                for (Zone z : placedTile.forestZones()) {
                    for (Area<Zone.Forest> forestArea : forests) {
                        if (placedTile.occupant() != null && forestArea.zones().contains(z) &&
                                placedTile.occupant().zoneId() == z.id()) {
                            PlacedTile newPlacedTile = placedTile.withNoOccupant();
                            newPlacedTiles[pos2Index(placedTile.pos())] = newPlacedTile;
                        }
                    }
                }
                for (Zone z : placedTile.riverZones()) {
                    for (Area<Zone.River> riverArea : rivers) {
                        if (placedTile.occupant() != null && riverArea.zones().contains(z)
                                && placedTile.occupant().zoneId() == z.id()
                                && placedTile.occupant().kind() != Occupant.Kind.HUT) {
                            PlacedTile newPlacedTile = placedTile.withNoOccupant();
                            newPlacedTiles[pos2Index(placedTile.pos())] = newPlacedTile;
                        }
                    }
                }
            }
        }

        int[] newIndexPlacedTiles = indexPlacedTiles.clone();
        return new Board(newPlacedTiles, newIndexPlacedTiles, zonePartitionBuilder.build(), cancelledAnimals());
    }


    /**
     * retourne un plateau identique au récepteur mais avec l'ensemble des animaux donnés ajouté à
     * l'ensemble des animaux annulés
     *
     * @param newlyCancelledAnimals: l'ensemble des animaux donnés
     * @return un plateau identique au récepteur mais avec l'ensemble des animaux donnés ajouté à
     * l'ensemble des animaux annulés
     */
    public Board withMoreCancelledAnimals(Set<Animal> newlyCancelledAnimals) {
        Set<Animal> allCanceledAnimals = new HashSet<>(cancelledAnimals);
        allCanceledAnimals.addAll(newlyCancelledAnimals);
        return new Board(this.placedTiles, this.indexPlacedTiles, this.zonePartitions, allCanceledAnimals);
    }

    /**
     * redefinition de equals afin de garantir que les instances de Board sont comparées correctement
     *
     * @param o: l'objet à comparer
     * @return vrai ssi les instances de Board sont identiques
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        if (this.cancelledAnimals.size() != board.cancelledAnimals.size()) return false;
        for (Animal animal : this.cancelledAnimals) {
            if (!board.cancelledAnimals.contains(animal)) return false;
        }
        return Arrays.equals(placedTiles, board.placedTiles) && Arrays.equals(indexPlacedTiles, board.indexPlacedTiles)
                && Objects.equals(zonePartitions, board.zonePartitions);
    }

    /**
     * redefinition de hashCode pour Board
     *
     * @return nouveau hashCode pour Board
     */
    @Override
    public int hashCode() {
        int firstInt = Arrays.hashCode(placedTiles);
        int secondInt = Arrays.hashCode(indexPlacedTiles);
        return Objects.hash(firstInt, secondInt, zonePartitions, cancelledAnimals);
    }
}



