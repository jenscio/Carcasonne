package ch.epfl.chacun;

import java.util.*;

/**
 * L'enregistrement GameState: représente l'état complet d'une partie de ChaCuN.
 *
 *  @author Rayan Berrada (378940)
 *  @author Jens Cancio (379510)
 *
 */

public record GameState(List<PlayerColor> players, TileDecks tileDecks, Tile tileToPlace, Board board, Action nextAction, MessageBoard messageBoard){

    /**
     * Action: représente la prochaine action à effectuer
     */
    public enum Action {
        START_GAME,
        PLACE_TILE,
        RETAKE_PAWN,
        OCCUPY_TILE,
        END_GAME
    }

    /**
     * Constructeur compact de GameState, se charge de garantir l'immuabilité de la classe et de valider les arguments
     * @param players: la liste de tous les joueurs de la partie, dans l'ordre dans lequel ils doivent jouer donc avec le joueur courant en tête de liste.
     * @param tileDecks: les trois tas des tuiles restantes.
     * @param tileToPlace: l'éventuelle tuile à placer, qui à été prise du sommet du tas des tuiles normales ou du tas des tuiles menhir, et qui peut être null.
     * @param board: le plateau de jeu.
     * @param nextAction: La prochaine Action à efféctuer.
     * @param messageBoard: le tableau d'affichage contenant les messages générés jusqu'à présent dans la partie.
     * @throws IllegalArgumentException si les conditions suivantes ne se remplissant pas :
     *            le nombre de joueurs est au moins égal à 2, ET
     *            soit la tuile à placer est null, soit la prochaine action est PLACE_TILE, ET
     *            ni les tas de cartes, ni le plateau de jeu, ni la prochaine action, ni le tableau d'affichage ne sont nuls.
     */
    public GameState {
        if (players.size()<2) {
            throw new IllegalArgumentException();
        }
        if (tileToPlace==null && nextAction== Action.PLACE_TILE) {
            throw new IllegalArgumentException();
        }
        if (tileDecks==null || board==null || messageBoard==null || nextAction==null) {
            throw new IllegalArgumentException();
        }
        players = List.copyOf(players);
    }



    /**
     * création de l'état initial d'une partie
     * @param players: joueurs
     * @param tileDecks: le deck de tuiles
     * @param textMaker: objet pour visualisation du MessageBoard
     * @return un GameState initial
     */
    public static GameState initial(List<PlayerColor> players, TileDecks tileDecks, TextMaker textMaker){
        return new GameState(players,tileDecks,null,Board.EMPTY, Action.START_GAME,new MessageBoard(textMaker, Collections.emptyList()));
    }

    /**
     * retourne le joueur courant
     * @return le joueur courant ou NULL si la prochaine action est START_GAME ou END_GAME
     */
    public PlayerColor currentPlayer(){
        if (nextAction== Action.START_GAME || nextAction== Action.END_GAME){
            return null;
        }
        return players.getFirst();
    }

    /**
     * freeOccupantsCount: retourne le nombre d'occupants libres.
     * @param player: joueur donné
     * @param kind: type donné
     * @return le nombre d'occupants libres.
     */
    public int freeOccupantsCount(PlayerColor player, Occupant.Kind kind){
        return Occupant.occupantsCount(kind)- board().occupantCount(player,kind);
    }

    /**
     * retourne l'ensemble des occupants potentiels de la dernière tuile posée que le joueur courant pourrait
     * effectivement placer - d'une part car il a au moins un occupant du bon type en main, et d'autre part car
     * l'aire à laquelle appartient la zone que cet occupant occuperait n'est pas déjà occupée
     * @return L'ensemble des occupants potentiels de la dernière tuile posée.
     * @throws IllegalArgumentException si le plateau est vide.
     */
    public Set<Occupant> lastTilePotentialOccupants() {
        if (board.equals(Board.EMPTY)) {
            throw new IllegalArgumentException();
        }
        Set<Occupant> lastTilePotentialOccupants = new HashSet<>();
        for (Zone zone : board.lastPlacedTile().tile().zones()) {
            switch (zone) {
                case Zone.Forest forest -> {
                    if (!board.forestArea(forest).isOccupied() && freeOccupantsCount(currentPlayer(), Occupant.Kind.PAWN) > 0) {
                        lastTilePotentialOccupants.add(new Occupant(Occupant.Kind.PAWN, forest.id()));
                    }
                }
                case Zone.Meadow meadow -> {
                    if (!board.meadowArea(meadow).isOccupied() && freeOccupantsCount(currentPlayer(), Occupant.Kind.PAWN) > 0) {
                        lastTilePotentialOccupants.add(new Occupant(Occupant.Kind.PAWN, meadow.id()));
                    }
                }
                case Zone.Water water -> {
                    if (water instanceof Zone.River river) {
                        if (!board.riverArea(river).isOccupied() && freeOccupantsCount(currentPlayer(), Occupant.Kind.PAWN) > 0) {
                            lastTilePotentialOccupants.add(new Occupant(Occupant.Kind.PAWN, river.id()));
                        }
                    }
                    if (!board.riverSystemArea(water).isOccupied() && freeOccupantsCount(currentPlayer(), Occupant.Kind.HUT) > 0) {
                        if (!(water instanceof Zone.River river && river.hasLake() )) {
                            lastTilePotentialOccupants.add(new Occupant(Occupant.Kind.HUT, water.id()));
                        }
                    }
                }
            }
        }
        return lastTilePotentialOccupants;
    }

    /**
     * - gère la transition de START_GAME à PLACE_TILE en plaçant la tuile de départ au centre
     *   du plateau et en tirant la première tuile du tas des tuiles normales, qui devient la
     *   tuile à jouer
     * @return nouveau GameState
     * @throws IllegalArgumentException si la prochaine action n'est pas START_GAME
     */
    public GameState withStartingTilePlaced(){
        if (nextAction != Action.START_GAME) {
            throw new IllegalArgumentException();
        }
        Tile startTile = tileDecks.topTile(Tile.Kind.START);
        TileDecks newTileDeck = tileDecks.withTopTileDrawn(Tile.Kind.START);
        Board newBoard = board.withNewTile(new PlacedTile(startTile,null,Rotation.NONE,Pos.ORIGIN));
        Tile firstNormalTile = tileDecks.topTile(Tile.Kind.NORMAL);
        newTileDeck = newTileDeck.withTopTileDrawn(Tile.Kind.NORMAL);
        return new GameState(players,newTileDeck,firstNormalTile,newBoard, Action.PLACE_TILE,messageBoard);
    }



    /**
     * gère toutes les transitions à partir de PLACE_TILE, en
     * - 1. ajoutant la tuile donnée au plateau,
     * - 2. attribuant les éventuels points obtenus suite à la pose de la pirogue ou de la fosse à pieux (voir la 3.1.1.1),
     * - 3. déterminant l'action suivante qui peut être RETAKE_PAWN si la tuile posée contient le chaman
     * @param tile: la tuile a poser
     * @return nouveau GameState
     * @throws IllegalArgumentException si la prochaine action n'est pas PLACE_TILE, ou si la tuile passée est déjà occupée,
     */
    public GameState withPlacedTile(PlacedTile tile){

        // lève IAE si la prochaine action n'est pas PLACE_TILE, ou si la tuile passée est déjà occupée
        if (nextAction != Action.PLACE_TILE || tile.occupant() != null) {
            throw new IllegalArgumentException();
        }

        // ajouter la tuile donnée au plateau
        Board newBoard = board.withNewTile(tile);

        // attribuer les éventuels points obtenus suite à la pose de la pirogue ET de la fosse à pieux (voir la 3.1.1.1)
        MessageBoard newMessageBoard = messageBoard;

        // pose de la pirogue
        if (tile.specialPowerZone()!=null && tile.specialPowerZone().specialPower().equals(Zone.SpecialPower.LOGBOAT)){
            newMessageBoard = newMessageBoard.withScoredLogboat(currentPlayer(),newBoard.riverSystemArea((Zone.Water) tile.specialPowerZone()));
        }

        // pose de la fosse à pieux (voir la 3.1.1.1)
        if (tile.specialPowerZone()!=null && tile.specialPowerZone().specialPower().equals(Zone.SpecialPower.HUNTING_TRAP)){
            // ne pas prendre en compte les animaux annulés par la fosse à pieux
            Set<Animal> animalsInAdjacentMeadowList = Area.animals(newBoard.adjacentMeadow(tile.pos(), (Zone.Meadow) tile.specialPowerZone()),Collections.emptySet());
            Set<Animal> tigersInMeadow = new HashSet<>(animalsInAdjacentMeadowList.stream().filter((animal) -> animal.kind() == Animal.Kind.TIGER).toList());
            List<Animal> deersInMeadow = new ArrayList<>(animalsInAdjacentMeadowList.stream().filter((animal) -> animal.kind() == Animal.Kind.DEER).toList());
            List<Animal> cancelledDeers = new ArrayList<>();
            if (!tigersInMeadow.isEmpty()) {
                if (tigersInMeadow.size() >= deersInMeadow.size()) {
                    cancelledDeers.addAll(deersInMeadow);
                } else {
                    cancelledDeers.addAll(deersInMeadow.subList(0, tigersInMeadow.size()));
                }
            }
            newBoard = newBoard.withMoreCancelledAnimals(new HashSet<>(cancelledDeers));
            // et aussi les tigres
            newBoard = newBoard.withMoreCancelledAnimals(tigersInMeadow);

            newMessageBoard = newMessageBoard.withScoredHuntingTrap(currentPlayer(),
                    newBoard.adjacentMeadow(tile.pos(), (Zone.Meadow) tile.specialPowerZone()),
                    newBoard.cancelledAnimals());

            newBoard = newBoard.withMoreCancelledAnimals(animalsInAdjacentMeadowList);
        }


        // Déterminer l'action suivante :
        // RETAKE_PAWN:
        // si la tuile que le joueur vient de poser est celle contenant le chaman et qu'il possède au
        // moins un pion sur le plateau

        if (tile.specialPowerZone() != null && tile.specialPowerZone().specialPower()!=null &&
                tile.specialPowerZone().specialPower().equals(Zone.SpecialPower.SHAMAN)){
            if ( newBoard.occupantCount(tile.placer(), Occupant.Kind.PAWN)>0 ){
                return new GameState(players,tileDecks,null,newBoard, Action.RETAKE_PAWN,newMessageBoard);
            }
        }

        // sinon:
        // OCCUPY_TILE
        //   - si au moins une zone de la tuile que le joueur vient de poser peut être occupée,
        //     et qu'il a l'occupant nécessaire en main
        //   - voir aussi 3.1.1.2 "Possibilité d'occupation"

        // regarder "dans le futur"
        GameState potentialGameState = new GameState(players,tileDecks,tile.tile(),newBoard, Action.OCCUPY_TILE,newMessageBoard);
        // est-ce que la tuile pourrait être occupée ?
        if (!potentialGameState.lastTilePotentialOccupants().isEmpty()) {
            // si oui, retourner le nouveau game state (mais sans tileToPlace)
            return new GameState(players,tileDecks,null,newBoard, Action.OCCUPY_TILE,newMessageBoard);
        }   // sinon: passer à la fin du tour


        // gere la fin du tour
        return withTurnFinished(newBoard,newMessageBoard);
    }




    /**
     *  gère toutes les transitions à partir de RETAKE_PAWN, en supprimant l'occupant donné, sauf s'il vaut null,
     *  ce qui indique que le joueur ne désire pas reprendre de pion
     * @param occupant: L'occupant à enlevér
     * @return nouveau GameState
     * @throws IllegalArgumentException la prochaine action n'est pas RETAKE_PAWN, ou si l'occupant donné n'est ni null, ni un pion
     */
    public GameState withOccupantRemoved(Occupant occupant){

        if (nextAction != Action.RETAKE_PAWN ||( occupant != null && occupant.kind() != Occupant.Kind.PAWN)){
            throw new IllegalArgumentException();
        }
        Board newBoard = board;
        if (occupant != null){
            newBoard = board.withoutOccupant(occupant);
        }
        if (!lastTilePotentialOccupants().isEmpty()) {
            return new GameState(players,tileDecks,null,newBoard, Action.OCCUPY_TILE,messageBoard);
        }
        else return withTurnFinished(newBoard,messageBoard);
    }




    /**
     * - gère toutes les transitions à partir de OCCUPY_TILE en ajoutant l'occupant donné à la dernière tuile posée,
     *   sauf s'il vaut null, ce qui indique que le joueur ne désire pas placer d'occupant
     * @param occupant: L'occupant à ajouter
     * @return nouveau GameState
     * @throws IllegalArgumentException si la prochaine action n'est pas OCCUPY_TILE.
     */
    public GameState withNewOccupant(Occupant occupant){
        if (nextAction != Action.OCCUPY_TILE){
            throw new IllegalArgumentException();
        }
        Board newBoard = board;
        if (occupant != null){
            newBoard = board.withOccupant(occupant);
        }
        return withTurnFinished(newBoard,messageBoard);
    }


    /**
     * Gère la fin de tour
     * Operations:
     * - Détermine les forêts et rivières fermées par la pose de la dernière tuile, et attribuer les points
     *   correspondants à leurs occupants majoritaires
     * - Retire les pions des forêts et rivières fermées après avoir comptabilisé les points
     *   (avec withoutGatherersOrFishersIn())
     * - Détermine si le joueur courant devrait pouvoir jouer un second tour, car il a fermé au moins une
     *   forêt contenant un menhir au moyen d'une tuile normale
     * - Passe la main au prochain joueur si le joueur courant n'a pas le droit ou la possibilité de jouer une tuile menhir,
     * - Elimine du sommet du tas contenant la prochaine tuile à jouer la totalité de celles qu'il n'est pas
     *   possible de placer sur le plateau, s'il y en a
     * - Termine la partie si le joueur courant a terminé son ou ses tour(s) et qu'il ne reste plus de tuile normale jouable.
     * @param newBoard : le tableau de jeu en construction
     * @param newMessageBoard: le tableau de message en construction
     * @return nouveau GameState
     */
    private GameState withTurnFinished(Board newBoard, MessageBoard newMessageBoard) {

        // determiner forets et rivieres, attribuer les points
        for (Area<Zone.Forest> forest : newBoard.forestsClosedByLastTile()) {
            newMessageBoard = newMessageBoard.withScoredForest(forest);
        }
        for (Area<Zone.River> river : newBoard.riversClosedByLastTile()) {
            newMessageBoard = newMessageBoard.withScoredRiver(river);
        }

        // retirer les pions des forêts et rivières fermées
        newBoard = newBoard.withoutGatherersOrFishersIn(newBoard.forestsClosedByLastTile(), newBoard.riversClosedByLastTile());

        // déterminer si le joueur courant devrait pouvoir jouer un second tour, car il a fermé au moins une
        // forêt contenant un menhir au moyen d'une tuile normale
        boolean canPlaySecondRound = false;
        if (!newBoard.forestsClosedByLastTile().isEmpty() && newBoard.lastPlacedTile().kind().equals(Tile.Kind.NORMAL)) {
            for (Area<Zone.Forest> forest : newBoard.forestsClosedByLastTile()) {
                if (Area.hasMenhir(forest) && !tileDecks.menhirTiles().isEmpty()) {
                    // trouvée une forêt fermée contentant un menhir
                    newMessageBoard = newMessageBoard.withClosedForestWithMenhir(currentPlayer(), forest);
                    canPlaySecondRound = true;
                    break;
                }
            }
        }


        // Eliminer du sommet du tas contenant la prochaine tuile à jouer la totalité de celles qu'il n'est pas
        // possible de placer sur le plateau, s'il y en a
        TileDecks newtileDecks = tileDecks;
        Tile newTileToPlace = null;
        if (canPlaySecondRound) {
            if (tileDecks.deckSize(Tile.Kind.MENHIR) > 0) {
                newtileDecks = tileDecks.withTopTileDrawnUntil(Tile.Kind.MENHIR, newBoard::couldPlaceTile);
                if (newtileDecks.deckSize(Tile.Kind.MENHIR) > 0) {
                    newTileToPlace = newtileDecks.topTile(Tile.Kind.MENHIR);
                    newtileDecks = newtileDecks.withTopTileDrawn(Tile.Kind.MENHIR);
                } else canPlaySecondRound = false;
            } else canPlaySecondRound = false;
        }


        // passer la main au prochain joueur si le joueur courant n'a pas le droit
        // ou la possibilité de jouer une tuile menhir

        List<PlayerColor> newPlayers = new ArrayList<>(players);
        if (!canPlaySecondRound) {
            newPlayers.add(newPlayers.getFirst());
            newPlayers.removeFirst();
            if (tileDecks.deckSize(Tile.Kind.NORMAL) > 0) {
                newtileDecks = tileDecks.withTopTileDrawnUntil(Tile.Kind.NORMAL, newBoard::couldPlaceTile);
                if (newtileDecks.deckSize(Tile.Kind.NORMAL) > 0) {
                    newTileToPlace = newtileDecks.topTile(Tile.Kind.NORMAL);
                    newtileDecks = newtileDecks.withTopTileDrawn(Tile.Kind.NORMAL);
                }
            }
        }


        // Terminer la partie si le joueur courant a terminé son ou ses tour(s) et qu'il ne reste plus de tuile normale jouable.

        if (newTileToPlace == null) {
            return withFinalPointsCounted(newBoard,newMessageBoard);
        }

        return new GameState(newPlayers,newtileDecks,newTileToPlace,newBoard, Action.PLACE_TILE,newMessageBoard);

    }




    /**
     * Gere la fin de partie
     * Cette méthode est appellé si aucune tuile du tas des tuiles normales ne peut être jouée, alors la partie est terminée.
     * @param newBoard : le tableau de jeu en construction
     * @param newMessageBoard: le tableau de message en construction
     * @return nouveau GameState
     */
    private GameState withFinalPointsCounted(Board newBoard, MessageBoard newMessageBoard){

        // points dans meadowAreas (yc feu et grande fosse à pieux)

        for (Area<Zone.Meadow> meadowArea : newBoard.meadowAreas()) {
            Set<Animal> animalsInMeadow = Area.animals(meadowArea, newBoard.cancelledAnimals());
            Set<Animal> tigersInMeadow = new HashSet<>(animalsInMeadow.stream().filter((animal) -> animal.kind() == Animal.Kind.TIGER).toList());
            List<Animal> deersInMeadow = new ArrayList<>(animalsInMeadow.stream().filter((animal) -> animal.kind() == Animal.Kind.DEER).toList());

            // est-ce qu'on a le feu dans le pré actuel ?
            Zone fireZone = meadowArea.zoneWithSpecialPower(Zone.SpecialPower.WILD_FIRE);
            if (fireZone != null) {
                // si oui, enlever tous les tigres
               // newBoard = newBoard.withMoreCancelledAnimals(tigersInMeadow);
                tigersInMeadow = new HashSet<>();
            }

            // est-ce qu'on a la grande fosse à pieux dans le pré actuel ?
            Zone pittrapZone = meadowArea.zoneWithSpecialPower(Zone.SpecialPower.PIT_TRAP);
            Area<Zone.Meadow> adjacentMeadow = null;
            if (pittrapZone != null) {
                // si oui, reordonner les cerfs pour mettre ceux dehors de la pit trap zone comme les premiers
                // pour les annuler en preference
                adjacentMeadow = newBoard.adjacentMeadow(newBoard.tileWithId(pittrapZone.tileId()).pos(), (Zone.Meadow) pittrapZone);
                Set<Animal> animalsInAdjacentMeadow = Area.animals(adjacentMeadow, newBoard.cancelledAnimals());
                deersInMeadow.sort(Comparator.comparing(animalsInAdjacentMeadow::contains));
            }

            // maintenant on procède a annuler les cerfs
            List<Animal> cancelledDeers = new ArrayList<>();
            if (!tigersInMeadow.isEmpty()) {
                if (tigersInMeadow.size() >= deersInMeadow.size()) {
                    cancelledDeers.addAll(deersInMeadow);
                } else {
                    cancelledDeers.addAll(deersInMeadow.subList(0, tigersInMeadow.size()));
                }
            }
            newBoard = newBoard.withMoreCancelledAnimals(new HashSet<>(cancelledDeers));
            // et aussi les tigres
            // newBoard = newBoard.withMoreCancelledAnimals(tigersInMeadow);

            // on fait le decompte des points pour le meadow

            newMessageBoard = newMessageBoard.withScoredMeadow(meadowArea, newBoard.cancelledAnimals());

            // et aussi pour la pit trap zone si presente

            if (pittrapZone != null) {
                newMessageBoard = newMessageBoard.withScoredPitTrap(adjacentMeadow, newBoard.cancelledAnimals());
            }
        }


        // décompte des points rapportés par les réseaux hydrographiques à leurs huttes majoritaires
        // en tenant en compte l'éventuelle présence du radeau :

        for (Area<Zone.Water> riverSystemArea :newBoard.riverSystemAreas()) {
            newMessageBoard = newMessageBoard.withScoredRiverSystem(riverSystemArea);
            Zone raftZone = riverSystemArea.zoneWithSpecialPower(Zone.SpecialPower.RAFT);
            if (raftZone != null ) {
                newMessageBoard = newMessageBoard.withScoredRaft(riverSystemArea);
            }
        }

        // décompte des points finaux :
        int maxScore;
        Set<PlayerColor> winners = new HashSet<>();
        if (!newMessageBoard.points().isEmpty()){
            maxScore = newMessageBoard.points().values().stream().sorted().toList().getLast();
            newMessageBoard.points().forEach((k,v) -> {if (v==maxScore) winners.add(k);});
        } else {
            maxScore = 0;
            winners.addAll(players);
        }
        newMessageBoard = newMessageBoard.withWinners(winners,maxScore);


        return new GameState (players,tileDecks,null,newBoard, Action.END_GAME,newMessageBoard);
    }

}
