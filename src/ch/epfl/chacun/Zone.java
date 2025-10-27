package ch.epfl.chacun;

import java.util.List;

/**
 * Représente la Zone d'une tuile
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */

public sealed interface Zone {

    /**
     * SpecialPower: Enumeration de "SpeciaPowers" que certaines zones peuvent avoir
     */
    enum SpecialPower {
        SHAMAN,
        LOGBOAT,
        HUNTING_TRAP,
        PIT_TRAP,
        WILD_FIRE,
        RAFT
    }

    /**
     * retourne la tileId d'une zone grace a la zoneId
     * @param zoneId
     * @return tileId
     */
    static int tileId(int zoneId){
        return zoneId/10;
    }
    /**
     * retourne la localId d'une zone grace a la zoneId
     * @param zoneId
     * @return localId
     */
    static int localId(int zoneId){
        return zoneId%10;
    }

    /**
     * Retourne la tileId de la case
     * @return tileId
     */
    default int tileId(){return id()/10;}

    /**
     * Retourne la localId de la case
     * @return localId
     */
    default int localId(){return id()%10;}


    int id();

    /**
     * SpecialPower
     * Generalement les zones n'ont pas de "SpecialPower"
     * @return null
     */
    default SpecialPower specialPower(){
       return null;
    }

    /**
     * Forest: type de zone
     * @param id
     * @param kind
     */
    record Forest(int id, Kind kind) implements Zone {

        public enum Kind{
            PLAIN,
            WITH_MENHIR,
            WITH_MUSHROOMS
        }
    }
    sealed interface Water extends Zone {
        int fishCount();

    }

    /**
     * Lake: type de zone/water
     * @param id
     * @param fishCount
     * @param specialPower
     */
    record Lake(int id,int fishCount, SpecialPower specialPower) implements Water{}

    /**
     * Meadow: type de zone
     * @param id
     * @param animals
     * @param specialPower
     */
    record Meadow(int id, List<Animal> animals, SpecialPower specialPower) implements Zone{
        public Meadow{ animals = List.copyOf(animals);}
    }

    /**
     * River: type de zone/water
     * @param id
     * @param fishCount
     * @param lake
     */
    record River(int id,int fishCount,Lake lake) implements Water{
        public boolean hasLake(){
            return lake!=null;
        }
    }

}
