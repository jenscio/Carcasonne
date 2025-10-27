package ch.epfl.chacun;

import java.util.*;

/**
 * Enregistrement ZonePartition: représente une partition de zones d'un type donné
 *
 *  @author Rayan Berrada (378940)
 *  @author Jens Cancio (379510)
 *
 * @param areas l'ensemble des aires formant la partition
 */
public record ZonePartition<Z extends Zone>(Set<Area<Z>> areas) {

    /**
     * Constructeur compact de ZonePartition
     * @param areas l'ensemble des aires formant la partition
     */
    public ZonePartition{
        areas = Set.copyOf(areas);
    }

    /**
     * constructeur secondaire pour ZonePartition
     * initialise la partition avec un ensemble d'aires vide
     */
    public ZonePartition(){
        this(new HashSet<>());
    }

    /**
     * retourne l'aire contenant la zone passée en argument
     * @param zone la zone en question
     * @return l'aire contenant la zone passée en argument
     * @throws IllegalArgumentException si la zone n'appartient à aucune aire de la partition
     */
    public Area<Z> areaContaining(Z zone){
        for(Area<Z> area: areas){
            if (area.zones().contains(zone)){
                return area;
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * ZonePartition.Builder:
     * bâtisseur pour la classe ZonePartition
     * @param <Z>
     */
    public final static class Builder <Z extends Zone> {

        // ensemble d'aires
        private HashSet<Area<Z>> areas = new HashSet<>();

        /**
         * Constructeur pour ZonePartition.Builder
         * @param zonePartition partition existante
         */
        public Builder(ZonePartition<Z> zonePartition){
            areas.addAll(zonePartition.areas);
        }

        /**
         * ajoute à la partition en cours de construction une nouvelle aire inoccupée
         * @param zone la zone donnée
         * @param openConnections le nombre de connexions ouvertes donné
         */
        public void addSingleton(Z zone, int openConnections){
            Set<Z> singleZone = new HashSet<>();
            singleZone.add(zone);
            this.areas.add(new Area<>(singleZone, Collections.emptyList(),openConnections));
        }

        /**
         * ajoute à l'aire contenant la zone donnée un occupant initial de la couleur donnée
         * @param zone la zone donnée
         * @param color la couleur donnée
         * @throws IllegalArgumentException si la zone n'appartient pas à une aire de la partition,
         *         ou si l'aire est déjà occupée
         */
        public void addInitialOccupant(Z zone, PlayerColor color){
            for (Area<Z> area:areas){
                if(area.zones().contains(zone)){
                    Area<Z> areaWithInitialOccupant=area.withInitialOccupant(color);
                    areas.remove(area);
                    areas.add(areaWithInitialOccupant);
                    return;
                }
            }
            throw new IllegalArgumentException();
        }

        /**
         * supprime de l'aire contenant la zone donnée un occupant de la couleur donnée
         * @param zone la zone donnée
         * @param color la couleur donnée
         * @throws IllegalArgumentException si la zone n'appartient pas à une aire de la partition,
         *         ou si elle n'est pas occupée par au moins un occupant de la couleur donnée
         */
        public void removeOccupant(Z zone, PlayerColor color){
            for (Area<Z> area:areas){
                if(area.zones().contains(zone)){
                    Area<Z> areaWithoutOccupant=area.withoutOccupant(color);
                    areas.remove(area);
                    areas.add(areaWithoutOccupant);
                    return;
                }
            }
            throw new IllegalArgumentException();
        }

        /**
         * supprime tous les occupants de l'aire donnée
         * @param area l'aire donnée
         * @throws IllegalArgumentException si l'aire ne fait pas partie de la partition
         */
        public void removeAllOccupantsOf(Area<Z> area){
            if (!this.areas.contains(area)){
                throw new IllegalArgumentException();
            }
            this.areas.remove(area);
            this.areas.add(area.withoutOccupants());
        }

        /**
         * union: connecte entre elles les aires contenant les zones données pour en faire une aire plus grande
         * @param zone1 la premiere zone donnée
         * @param zone2 la seconde zone donnée
         * @throws IllegalArgumentException si l'une des deux zones n'appartient pas à une aire de la partition
         */
        public void union(Z zone1, Z zone2){
            Area<Z> area1 = null,area2=null;

            for(Area<Z> area:areas){
                if(area.zones().contains(zone1)) area1=area;
                if(area.zones().contains(zone2)) area2=area;
                if(area1!=null && area2!=null) break;
            }

            if(area1==null || area2==null)throw new IllegalArgumentException();

            Area<Z> newArea=area1.connectTo(area2);

            if(newArea.equals(area1)) return; // area1 and area2 are the same area

            areas.remove(area1);
            areas.remove(area2);

            areas.add(newArea);
        }

        /**
         * construit la partition de zones
         * @return nouvelle ZonePartition
         */
        public ZonePartition<Z> build(){
            return new ZonePartition<>(areas);
        }
    }

}



