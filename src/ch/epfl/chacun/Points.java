package ch.epfl.chacun;

import static ch.epfl.chacun.Preconditions.checkArgument;

/**
 * Points
 * Classe qui décrit quelle action donne combien de points
 *
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */
public final class Points {
    /**
     * Retourne retourne le nombre de points obtenus par les cueilleurs majoritaires d'une forêt fermée
     * constituée de tileCount tuiles et comportant des groupes de champignons
     * @param tileCount: le nombre de tuiles de la forêt fermée
     * @param mushroomGroupCount: le nombre de groupes de champignons
     * @return le nombre de points gagnés par les occupants majoritaires de la forêt
     * @throws IllegalArgumentException si l'aire contient moins d'une tuile ou moins de zero groupes de champignons
     */
    public static int forClosedForest(int tileCount, int mushroomGroupCount){
        checkArgument(tileCount>1 && mushroomGroupCount>=0);
        return 2*tileCount+3*mushroomGroupCount;
    }
    /**
     * Retourne le nombre de points obtenus par les pêcheurs majoritaires d'une rivière fermée constituée de tileCount
     * tuiles et dans laquelle nagent des poissons,
     * @param tileCount: le nombre de tuiles de la rivière fermée
     * @param fishCount: le nombre de poissons
     * @return le nombre de points gagnés par les occupants majoritaires de la rivière fermée
     * @throws IllegalArgumentException si l'aire contient moins d'une tuile ou moins de zero poissons
     */
    public static int forClosedRiver(int tileCount, int fishCount){
        checkArgument(tileCount>1 && fishCount>=0);
        return tileCount+fishCount;
    }

    /**
     * Retourne le nombre de points obtenus par les chasseurs majoritaires d'un pré comportant
     * des mammouths, des aurochs et des cerfs, les cerfs dévorés par des smilodons n'étant pas inclus dans deerCount,
     * @param mammothCount: le nombre de mammouths
     * @param aurochsCount: le nombre d'aurochs
     * @param deerCount: le nombre de cerfs
     * @return le nombre de points gagnés par les occupants majoritaires du pré
     * @throws IllegalArgumentException si le pré contient moins de zero mammouth ou moins de zeros aurochs ou moins
     * de zero cerfs
     */
    public static int forMeadow(int mammothCount, int aurochsCount, int deerCount){
        checkArgument(mammothCount>=0 && aurochsCount>=0 && deerCount>=0);
        return 3*mammothCount+2*aurochsCount+deerCount;
    }

    /**
     * Retourne le nombre de points obtenus par les pêcheurs majoritaires d'un réseau hydrographique
     * @param fishCount: nombres de poissons
     * @return le nombre de points obtenus par les pêcheurs majoritaires d'un réseau hydrographique
     * @throws IllegalArgumentException si fishCount<0
     */
    public static int forRiverSystem(int fishCount){
        checkArgument(fishCount>=0);
        return fishCount;
    }

    /**
     * Retourne le nombre de points obtenus par le joueur déposant la pirogue dans un réseau hydrographique
     * comportant lakeCount lacs,
     * @param lakeCount: nombres de lacs
     * @return le nombre de points obtenus par le joueur déposant la pirogue dans un réseau hydrographique
     * @throws IllegalArgumentException si lakeCount<=0
     */
    public static int forLogboat(int lakeCount){
        checkArgument(lakeCount>0);
        return 2*lakeCount;
    }

    /**
     * Retourne le nombre de points supplémentaires obtenus par les pêcheurs majoritaires du réseau hydrographique
     * contenant le radeau
     * @param lakeCount: nombres de lacs
     * @return le nombre de points supplémentaires obtenus par les pêcheurs majoritaires du réseau hydrographique
     * contenant le radeau
     * @throws IllegalArgumentException si lakeCount<=0
     */
    public static int forRaft(int lakeCount){
        checkArgument(lakeCount>0);
        return lakeCount;
    }

}
