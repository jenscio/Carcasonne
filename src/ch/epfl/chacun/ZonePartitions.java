package ch.epfl.chacun;

/**
 * Enregistrement ZonePartitions: regroupe les quatre partitions de zones du jeu.
 *
 *  @author Rayan Berrada (378940)
 *  @author Jens Cancio (379510)
 *
 * @param forests la partition des forêts
 * @param meadows la partition des prés
 * @param rivers la partition des rivières
 * @param riverSystems la partition des zones aquatiques (rivières et lacs)
 */
public record ZonePartitions(ZonePartition<Zone.Forest> forests, ZonePartition<Zone.Meadow> meadows, ZonePartition<Zone.River> rivers, ZonePartition<Zone.Water> riverSystems) {

    // représente un groupe de 4 partitions vides
    final public static ZonePartitions EMPTY = new ZonePartitions(new ZonePartition<>(),new ZonePartition<>(),new ZonePartition<>(),new ZonePartition<>());


    /**
     * ZonePartitions.Builder: sert de bâtisseur à la classe ZonePartitions
     */
    public static final class Builder{

        // quatre bâtisseurs de partitions de zones
        private ZonePartition.Builder<Zone.Forest> forestBuilder;
        private ZonePartition.Builder<Zone.Meadow> meadowBuilder;
        private ZonePartition.Builder<Zone.River> riverBuilder;
        private ZonePartition.Builder<Zone.Water> waterBuilder;

        /**
         * Constructeur de ZonePartitions.Builder
         * retourne un nouveau bâtisseur dont les quatre partitions sont initialement identiques à celles du
         * groupe de quatre partitions donné
         * @param initial : ZonePartitions initial à utiliser
         */
        public Builder(ZonePartitions initial){
            forestBuilder= new ZonePartition.Builder<>(initial.forests);
            meadowBuilder= new ZonePartition.Builder<>(initial.meadows);
            riverBuilder= new ZonePartition.Builder<>(initial.rivers);
            waterBuilder= new ZonePartition.Builder<>(initial.riverSystems);
            new ZonePartitions(initial.forests, initial.meadows, initial.rivers, initial.riverSystems);
        }

        /**
         * ajoute aux quatre partitions les aires correspondant aux zones de la tuile donnée
         * @param tile la tuile à ajouter
         */
        public void addTile(Tile tile){

            int[] numberOfOpenConnections = new int[10];
            for (TileSide side: tile.sides()) {
                for (Zone tileSideZone : side.zones()) {
                    numberOfOpenConnections[tileSideZone.localId()] += 1;
                    if (tileSideZone instanceof Zone.River river && river.hasLake()) {
                        numberOfOpenConnections[river.lake().localId()] += 1;
                    }
                }
            }

            for (Zone zone: tile.zones()) {
                switch (zone) {
                    case Zone.Forest forest ->
                            forestBuilder.addSingleton(forest, numberOfOpenConnections[zone.localId()]);

                    case Zone.Meadow meadow ->
                            meadowBuilder.addSingleton(meadow,numberOfOpenConnections[zone.localId()]);

                    case Zone.River river-> {
                        if(river.hasLake()){
                            numberOfOpenConnections[zone.localId()]+=1;
                            riverBuilder.addSingleton(river, numberOfOpenConnections[zone.localId()]-1);
                        } else {
                            riverBuilder.addSingleton(river, numberOfOpenConnections[zone.localId()]);
                        }
                        waterBuilder.addSingleton(river, numberOfOpenConnections[zone.localId()]);
                    }

                    default ->
                            waterBuilder.addSingleton((Zone.Water) zone, numberOfOpenConnections[zone.localId()]);
                }
            }

            for(Zone zone: tile.zones()){
                if(zone instanceof Zone.River river){
                    if(river.hasLake()){
                        waterBuilder.union(river,river.lake());
                    }
                }
            }
        }


        /**
         * connecte les deux bords de tuiles donnés, en connectant entre elles les aires correspondantes
         * @param s1 bord 1
         * @param s2 bord 2
         * @throws IllegalArgumentException si les deux bords ne sont pas de la même sorte
         */
        public void connectSides(TileSide s1, TileSide s2){
            switch (s1){
                case TileSide.Meadow(Zone.Meadow m1)
                        when s2 instanceof TileSide.Meadow(Zone.Meadow m2) ->
                        meadowBuilder.union(m1,m2);
                case TileSide.Forest(Zone.Forest f1)
                        when s2 instanceof TileSide.Forest(Zone.Forest f2) ->
                        forestBuilder.union(f1,f2);
                case TileSide.River(Zone.Meadow m1s1, Zone.River r1s1, Zone.Meadow m2s1)
                        when s2 instanceof TileSide.River(Zone.Meadow m1s2,Zone.River r1s2, Zone.Meadow m2s2) -> {
                    meadowBuilder.union(m1s1, m2s2);
                    riverBuilder.union(r1s1,r1s2);
                    meadowBuilder.union(m2s1,m1s2);
                    waterBuilder.union(r1s1,r1s2);
                }

                default -> throw new IllegalArgumentException();
            }

        }

        /**
         * ajoute un occupant initial, de la sorte donnée et appartenant au joueur donné, à l'aire contenant la
         * zone donnée
         * @param player le joueur donnée
         * @param occupantKind la sorte donnée
         * @param occupiedZone la zone donnée
         * @throws IllegalArgumentException si la sorte d'occupant donnée ne peut pas occuper une zone de la sorte donnée
         */
        public void addInitialOccupant(PlayerColor player, Occupant.Kind occupantKind, Zone occupiedZone) {
            switch (occupiedZone) {
                case Zone.Forest forest -> {
                    if (occupantKind != Occupant.Kind.PAWN) {
                        throw new IllegalArgumentException();
                    }
                    forestBuilder.addInitialOccupant(forest, player);
                }
                case Zone.Meadow meadow -> {
                    if (occupantKind != Occupant.Kind.PAWN) {
                        throw new IllegalArgumentException();
                    }
                    meadowBuilder.addInitialOccupant(meadow, player);
                }
                case Zone.Water water -> {
                    if (occupantKind.equals(Occupant.Kind.PAWN)){
                        if (water instanceof Zone.River) {
                            riverBuilder.addInitialOccupant((Zone.River) water, player);
                        } else {
                            throw new IllegalArgumentException();
                        }
                    }
                    else {
                        waterBuilder.addInitialOccupant((Zone.Water) occupiedZone, player);
                    }
                }
                default -> throw new IllegalArgumentException();

            }
        }

        /**
         * supprime un occupant -un pion — appartenant au joueur donné de l'aire contenant la zone
         * donnée
         * @param player le joueur donné
         * @param occupiedZone la zone donnée
         */
        public void removePawn(PlayerColor player, Zone occupiedZone){
            switch (occupiedZone) {
                case Zone.Forest forest -> forestBuilder.removeOccupant(forest,player);
                case Zone.Meadow meadow -> meadowBuilder.removeOccupant(meadow,player);
                case Zone.River river -> riverBuilder.removeOccupant(river,player);
                default -> throw new IllegalArgumentException();
            }
        }

        /**
         * supprime tous les occupants—des pions jouant le rôle de cueilleurs—de la forêt donnée
         * @param forest la forêt donnée
         */
        public void clearGatherers(Area<Zone.Forest> forest){
            forestBuilder.removeAllOccupantsOf(forest);
        }

        /**
         * supprime tous les occupants—des pions jouant le rôle de pêcheurs—de la rivière donnée
         * @param river la rivière donnée
         */
        public void clearFishers(Area<Zone.River> river){
            riverBuilder.removeAllOccupantsOf(river);
        }

        /**
         * retourne le groupe de quatre partitions en cours de construction
         * @return le groupe de quatre partitions en cours de construction
         */
        public ZonePartitions build(){
            return new ZonePartitions(forestBuilder.build(),meadowBuilder.build(),riverBuilder.build(),waterBuilder.build());
        }
    }
}



