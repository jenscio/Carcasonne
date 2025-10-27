package ch.epfl.chacun;

import java.util.*;

import static java.lang.StringTemplate.STR;

/**
 * Classe TextMakerFr:  permet de générer tout le texte français nécessaire à l'interface graphique de ChaCuN
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */


public final class TextMakerFr implements TextMaker {


    // table associant leur nom aux couleurs des joueurs
    private final Map<PlayerColor, String> playerNames;


    // traductions en français des noms des animaux
    private static final Map<Animal.Kind, String> animalFrenchName = Map.of(
            Animal.Kind.MAMMOTH, "mammouth",
            Animal.Kind.AUROCHS, "auroch",
            Animal.Kind.DEER, "cerf",
            Animal.Kind.TIGER, "tigre");


    /**
     * Constructeur de TextMakerFr
     *
     * @param playerNames table associant leur nom aux couleurs des joueurs
     */
    public TextMakerFr(Map<PlayerColor, String> playerNames) {
        this.playerNames = new HashMap<>(playerNames);
    }

    /**
     * retourne les noms des joueurs dans l'ordre (par couleur) et separés par des commas et/ou "e".
     *
     * @param players les joueurs
     * @return les noms des joueurs dans l'ordre (par couleur) et separés par des commas et/ou "e".
     */
    private String playerNamesOrdered(Set<PlayerColor> players) {
        List<PlayerColor> orderedList = new ArrayList<>(players);
        orderedList.sort(Comparator.comparing(Enum::ordinal));

        StringBuilder s = new StringBuilder(playerName(orderedList.getFirst()));

        if (orderedList.size() > 1) {
            if (orderedList.size() > 2) {
                for (int i = 1; i < orderedList.size() - 1; i++) {
                    s.append(", ").append(playerName(orderedList.get(i)));
                }
            }
            s.append(" et ").append(playerName(orderedList.getLast())).append(" ont");
        } else {
            s.append(" a");
        }
        return s.toString();
    }


    /**
     * retourne le string donné avec une "s" à la fin si number > 1, sinon retourne le string
     *
     * @param s      le string
     * @param number le numéro
     * @return le string donné avec une "s" à la fin si number > 1, sinon retourne le string
     */
    private static String frenchPlural(String s, int number) {
        if (number > 1) s = s + "s";
        return s;
    }

    /**
     * retourne le nombre et le nom de l'animal (en singulier ou pluriel)
     *
     * @param kind   le type d'animal
     * @param number le nombre
     * @return le nombre et le nom de l'animal (en singulier ou pluriel)
     */
    private static String animalWithCounts(Animal.Kind kind, int number) {
        return STR."\{number} \{frenchPlural(animalFrenchName.get(kind), number)}";
    }


    /**
     * retourne un String avec le message indicant les points remportés
     *
     * @param players les joueurs
     * @param points  les points
     * @return String avec le message indicant les points remportés
     */
    private String playerNamesOrderedScoredPoints(Set<PlayerColor> players, int points) {
        return STR. "\{ playerNamesOrdered(players) } remporté \{ points(points) }" ;
    }


    /**
     * retourne un String avec le message indicant les points remportés en tant qu'occupant(es) majoritaire(s)
     *
     * @param players les joueurs
     * @param points  les points
     * @return String avec le message indicant les points remportés en tant qu'occupant(es) majoritaire(s)
     */
    private String playerNamesOrderedScoredPointsMajorityOccupants(Set<PlayerColor> players, int points) {
        StringBuilder s = new StringBuilder(playerNamesOrderedScoredPoints(players, points));
        if (players.size() > 1) {
            s.append(" en tant qu'occupant·e·s majoritaires");
        } else {
            s.append(" en tant qu'occupant·e majoritaire");
        }
        return s.toString();
    }


    /**
     * retourne une liste ordonnée avec les noms des animaux, leur quantité et avec separation appropriée ("," , "et")
     *
     * @param animals Map avec le type d'animaux et leur quantité
     * @return une liste ordonnée avec les noms des animaux, leur quantité et avec separation appropriée ("," , "et")
     */
    private static String animalsInOneString(Map<Animal.Kind, Integer> animals) {
        List<Animal.Kind> orderedList = new ArrayList<>(animals.keySet());
        // ordonner la liste selon l'ordinal des animaux
        orderedList.sort(Comparator.comparing(Enum::ordinal));
        // enlever les animaux sans points (tigre et autres)
        orderedList = orderedList.stream().filter(a -> animals.get(a) > 0 && a!= Animal.Kind.TIGER).toList();
        StringBuilder s = new StringBuilder(animalWithCounts(orderedList.getFirst(),
                animals.get(orderedList.getFirst())));
        if (orderedList.size() > 1) {
            if (orderedList.size() > 2) {
                for (int i = 1; i < orderedList.size() - 1; i++) {
                    s.append(", ").append(animalWithCounts(orderedList.get(i), animals.get(orderedList.get(i))));
                }
            }
            s.append(" et ").append(animalWithCounts(orderedList.getLast(), animals.get(orderedList.getLast())));
        }
        return s.toString();
    }


    /**
     * Retourne le nom du joueur de couleur donnée.
     *
     * @param playerColor la couleur du joueur
     * @return le nom du joueur ou null si le joueur n'est pas présent dans le jeu
     */
    @Override
    public String playerName(PlayerColor playerColor) {
        return playerNames.getOrDefault(playerColor, null);
    }

    /**
     * Retourne la représentation textuelle du nombre de points donnés (p. ex. "3 points").
     *
     * @param points le nombre de points
     * @return la représentation textuelle du nombre de points
     */
    @Override
    public String points(int points) {
        return STR. "\{ points } \{ frenchPlural("point", points) }" ;
    }

    /**
     * Retourne le texte d'un message déclarant qu'un joueur a fermé une forêt avec un menhir.
     *
     * @param player le joueur ayant fermé la forêt
     * @return le texte du message
     */
    @Override
    public String playerClosedForestWithMenhir(PlayerColor player) {
        return STR.
                "\{ playerName(player) } a fermé une forêt contenant un menhir et peut donc placer une tuile menhir." ;
    }


    /**
     * Retourne le texte d'un message déclarant que les occupants majoritaires d'une forêt nouvellement
     * fermée, constituée d'un certain nombre de tuiles et comportant un certain nombre de groupes de champignons,
     * ont remporté les points correspondants.
     *
     * @param scorers            les occupants majoritaires de la forêt
     * @param points             les points remportés
     * @param mushroomGroupCount le nombre de groupes de champignons que la forêt contient
     * @param tileCount          le nombre de tuiles qui constitue la forêt
     * @return le texte du message
     */
    @Override
    public String playersScoredForest(Set<PlayerColor> scorers, int points, int mushroomGroupCount, int tileCount) {
        StringBuilder s = new StringBuilder(playerNamesOrderedScoredPointsMajorityOccupants(scorers, points)).
                append(STR. " d'une forêt composée de \{ tileCount } \{ frenchPlural("tuile", tileCount) }" );
        if (mushroomGroupCount > 0) {
            s.append(STR.
                    " et de \{mushroomGroupCount} \{ frenchPlural("groupe", mushroomGroupCount) } de champignons" );
        }
        return s.append(".").toString();
    }


    /**
     * Retourne le texte d'un message déclarant que les occupants majoritaires d'une rivière nouvellement
     * fermée, constituée d'un certain nombre de tuiles et comportant un certain nombre de poissons,
     * ont remporté les points correspondants.
     *
     * @param scorers   les occupants majoritaires de la rivière
     * @param points    les points remportés
     * @param fishCount le nombre de poissons nageant dans la rivière ou les lacs adjacents
     * @param tileCount le nombre de tuiles qui constitue la rivière
     * @return le texte du message
     */
    @Override
    public String playersScoredRiver(Set<PlayerColor> scorers, int points, int fishCount, int tileCount) {
        StringBuilder s = new StringBuilder(playerNamesOrderedScoredPointsMajorityOccupants(scorers, points)).
                append(STR. " d'une rivière composée de \{ tileCount } \{ frenchPlural("tuile", tileCount) }" );
        if (fishCount > 0) {
            s.append(STR. " et contenant \{ fishCount } \{ frenchPlural("poisson", fishCount) }" );
        }
        return s.append(".").toString();
    }


    /**
     * Retourne le texte d'un message déclarant qu'un joueur a déposé la fosse à pieux dans un pré contenant,
     * sur les 8 tuiles voisines de la fosse, certains animaux, et remporté les points correspondants.
     *
     * @param scorer  le joueur ayant déposé la fosse à pieux
     * @param points  les points remportés
     * @param animals les animaux présents dans le même pré que la fosse et sur les 8 tuiles voisines
     * @return le texte du message
     */
    @Override
    public String playerScoredHuntingTrap(PlayerColor scorer, int points, Map<Animal.Kind, Integer> animals) {
        return STR. "\{ playerNamesOrderedScoredPoints(Set.of(scorer), points) }" +
                " en plaçant la fosse à pieux dans un pré dans lequel elle est entourée de " +
                STR. "\{ animalsInOneString(animals) }." ;
    }


    /**
     * Retourne le texte d'un message déclarant qu'un joueur a déposé la pirogue dans un réseau hydrographique
     * comportant un certain nombre de lacs, et remporté les points correspondants.
     *
     * @param scorer    le joueur ayant déposé la pirogue
     * @param points    les points remportés
     * @param lakeCount le nombre de lacs accessibles à la pirogue
     * @return le texte du message
     */
    @Override
    public String playerScoredLogboat(PlayerColor scorer, int points, int lakeCount) {
        return STR. "\{ playerNamesOrderedScoredPoints(Set.of(scorer), points) }" +
                STR. " en plaçant la pirogue dans un réseau hydrographique contenant \{ lakeCount } " +
                STR. "\{ frenchPlural("lac", lakeCount) }." ;
    }

    /**
     * Retourne le texte d'un message déclarant que les occupants majoritaires d'un pré contenant certains
     * animaux ont remporté les points correspondants.
     *
     * @param scorers les occupants majoritaires du pré
     * @param points  les points remportés
     * @param animals les animaux présents dans le pré (sans ceux ayant été précédemment annulés)
     * @return le texte du message
     */
    @Override
    public String playersScoredMeadow(Set<PlayerColor> scorers, int points, Map<Animal.Kind, Integer> animals) {
        return STR. "\{ playerNamesOrderedScoredPointsMajorityOccupants(scorers, points) }" +
                STR. " d'un pré contenant \{ animalsInOneString(animals) }." ;
    }

    /**
     * Retourne le texte d'un message déclarant que les occupants majoritaires d'un réseau hydrographique
     * comportant un certain nombre de poissons ont remporté les points correspondants.
     *
     * @param scorers   les occupants majoritaires du réseau hydrographique
     * @param points    les points remportés
     * @param fishCount le nombre de poissons nageant dans le réseau hydrographique
     * @return le texte du message
     */
    @Override
    public String playersScoredRiverSystem(Set<PlayerColor> scorers, int points, int fishCount) {
        return STR. "\{ playerNamesOrderedScoredPointsMajorityOccupants(scorers, points) }" +
                STR. " d'un réseau hydrographique contenant \{ fishCount } " +
                STR. "\{ frenchPlural("poisson", fishCount) }." ;
    }

    /**
     * Retourne le texte d'un message déclarant que les occupants majoritaires d'un pré contenant la
     * grande fosse à pieux et, sur les 8 tuiles voisines d'elles, certains animaux, ont remporté les
     * points correspondants.
     *
     * @param scorers les occupants majoritaires du pré contenant la fosse à pieux
     * @param points  les points remportés
     * @param animals les animaux présents sur les tuiles voisines de la fosse (sans ceux précédemment annulés)
     * @return le texte du message
     */
    @Override
    public String playersScoredPitTrap(Set<PlayerColor> scorers, int points, Map<Animal.Kind, Integer> animals) {
        return STR. "\{ playerNamesOrderedScoredPointsMajorityOccupants(scorers, points) }" +
                STR. " d'un pré contenant la grande fosse à pieux entourée de \{ animalsInOneString(animals) }." ;
    }

    /**
     * Retourne le texte d'un message déclarant que les occupants majoritaires d'un réseau hydrographique
     * contenant le radeau ont remporté les points correspondants.
     *
     * @param scorers   les occupants majoritaires du réseau hydrographique comportant le radeau
     * @param points    les points remportés
     * @param lakeCount le nombre de lacs contenus dans le réseau hydrographique
     * @return le texte du message
     */
    @Override
    public String playersScoredRaft(Set<PlayerColor> scorers, int points, int lakeCount) {
        return STR. "\{ playerNamesOrderedScoredPointsMajorityOccupants(scorers, points) }" +
                STR. " d'un réseau hydrographique contenant le radeau et \{ lakeCount } " +
                STR. "\{ frenchPlural("lac", lakeCount) }." ;
    }

    /**
     * Retourne le texte d'un message déclarant qu'un ou plusieurs joueurs ont remporté la partie, avec un
     * certain nombre de points.
     *
     * @param winners l'ensemble des joueurs ayant remporté la partie
     * @param points  les points des vainqueurs
     * @return le texte du message
     */
    @Override
    public String playersWon(Set<PlayerColor> winners, int points) {
        return STR. "\{ playerNamesOrdered(winners) } remporté la partie avec \{ points(points) }!" ;
    }

    /**
     * Retourne un texte demandant au joueur actuel de cliquer sur l'occupant qu'il désire placer, ou sur le texte
     * du message s'il ne désire placer aucun occupant.
     *
     * @return le texte en question
     */
    @Override
    public String clickToOccupy() {
        return "Cliquez sur le pion ou la hutte que vous désirez placer, ou ici pour ne pas en placer.";
    }

    /**
     * Retourne un texte demandant au joueur actuel de cliquer sur le pion qu'il désire reprendre, ou sur le texte
     * du message s'il ne désire reprendre aucun pion.
     *
     * @return le texte en question
     */
    @Override
    public String clickToUnoccupy() {
        return "Cliquez sur le pion que vous désirez reprendre, ou ici pour ne pas en reprendre.";
    }
}
