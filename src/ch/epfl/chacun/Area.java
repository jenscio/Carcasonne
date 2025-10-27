package ch.epfl.chacun;
import java.util.*;

/**
 *  Area :
 *  représente une aire.
 *
 *  @author Rayan Berrada (378940)
 *  @author Jens Cancio (379510)
 *
 */

public record Area<Z extends Zone>(Set<Z> zones, List<PlayerColor> occupants, int openConnections) {

    /**
     * Constructeur compact pour Area.
     * @param zones : l'ensemble des zones constituant l'aire
     * @param occupants : les couleurs des éventuels joueurs occupant l'aire, triés par couleur
     * @param openConnections : le nombre de connexions ouvertes de l'aire
     * @throws IllegalArgumentException si openConnections n'est pas positif ou nul.
     */
    public Area{
        if (openConnections<0){
            throw new IllegalArgumentException();
        }
        zones = Set.copyOf(zones);
        List<PlayerColor> occupantsCopy = new ArrayList<>();
        occupantsCopy.addAll(occupants);
        Collections.sort(occupantsCopy);
        occupants = List.copyOf(occupantsCopy);

    }

    /**
     * retourne vrai si et seulement si la forêt donnée contient au moins un menhir
     * @param forest la forêt donnée
     * @return vrai si et seulement si la forêt donnée contient au moins un menhir
     */
    public static boolean hasMenhir(Area<Zone.Forest> forest) {
        for (Zone.Forest zf: forest.zones()) {
          if(zf.kind()== Zone.Forest.Kind.WITH_MENHIR){
                return true;
            }
        }
        return false;
    }

     /**
      * retourne le nombre de groupes de champignons que contient la forêt donnée
      * @param forest: la forêt donnée
      * @return le nombre de groupes de champignons que contient la forêt donnée
      */
    public static int mushroomGroupCount(Area<Zone.Forest> forest){
        int mushroomCount =0;
        for (Zone.Forest zf: forest.zones()) {
            if (zf.kind() != null && zf.kind().equals(Zone.Forest.Kind.WITH_MUSHROOMS)) {
                mushroomCount += 1;
            }
        }
        return mushroomCount;
    }

     /**
      * retourne l'ensemble des animaux se trouvant dans le pré donné mais qui ne font pas partie de l'ensemble des animaux annulés donné
      * @param meadow : le pré
      * @param cancelledAnimals: l'ensemble des animaux annulés
      * @return l'ensemble des animaux se trouvant dans le pré donné mais qui ne font pas partie de l'ensemble des animaux annulés donné
      */
    public static Set<Animal> animals(Area<Zone.Meadow> meadow, Set<Animal> cancelledAnimals){
        Set<Animal> animalSet= new HashSet<>();
        for (Zone.Meadow zf:  meadow.zones()) {
            for (Animal animal: zf.animals()){
                if (!cancelledAnimals.contains(animal)){
                    animalSet.add(animal);
                }
            }
        }
        return animalSet;
    }

     /**
      * retourne le nombre de poissons nageant dans la rivière donnée ou dans l'un des éventuels lacs se trouvant à ses extrémités
      * @param river: la rivière donnée
      * @return le nombre de poissons nageant dans la rivière donnée ou dans l'un des éventuels lacs se trouvant à ses extrémités
      */
    public static int riverFishCount(Area<Zone.River> river){
        int fishCount =0;
        Set<Zone.Lake> lakes = new HashSet<>();
        for (Zone.River rz: river.zones()) {
            fishCount+=rz.fishCount();
            if (rz.hasLake()){
                if (lakes.add(rz.lake())) {
                    fishCount += rz.lake().fishCount();
                }
            }
        }
        return fishCount;
    }

     /**
      * retourne le nombre de poissons nageant dans le réseau hydrographique donné
      * @param riverSystem: le réseau hydrographique donné
      * @return le nombre de poissons nageant dans le réseau hydrographique donné
      */
    public static int riverSystemFishCount(Area<Zone.Water> riverSystem){
        int fishcount = 0;
        for (Zone.Water zf: riverSystem.zones()){
            fishcount+= zf.fishCount();
        }
        return fishcount;
    }


     /**
      * retourne le nombre de lacs du réseau hydrographique donné
      * @param riverSystem: le réseau hydrographique
      * @return le nombre de lacs du réseau hydrographique donné
      */
    public static int lakeCount(Area<Zone.Water> riverSystem){
        int lakeCount = 0;
        for (Zone.Water zf: riverSystem.zones()){
            if (zf instanceof Zone.Lake){
                lakeCount+=1;
            }
        }
        return  lakeCount;
    }

     /**
      * retourne vrai si et seulement si (ssi) l'aire est fermée
      * @return  vrai si et seulement si (ssi) l'aire est fermée
      */
    public boolean isClosed(){
        return (openConnections==0);
    }

     /**
      * retourne vrai ssi l'aire est occupée par au moins un occupant
      * @return vrai ssi l'aire est occupée par au moins un occupant
      */
    public boolean isOccupied(){
        return (!occupants.isEmpty());
    }

     /**
      * retourne l'ensemble des occupants majoritaires de l'aire
      * @return l'ensemble des occupants majoritaires de l'aire
      */
    public Set<PlayerColor> majorityOccupants(){
        if (!this.occupants.isEmpty()) {
            int[] ordinalofColorOfPLayersArray = {0, 0, 0, 0, 0};
            for (PlayerColor o: this.occupants()) {
                ordinalofColorOfPLayersArray[o.ordinal()]++;
            }
            int maximumElement = ordinalofColorOfPLayersArray[0];
            for (int i = 0; i < ordinalofColorOfPLayersArray.length; i++) {
                if (ordinalofColorOfPLayersArray[i] > maximumElement) {
                    maximumElement = ordinalofColorOfPLayersArray[i];
                }
            }
            Set<PlayerColor> majorityOccupantColorSet = new HashSet<>();
            for (int a = 0; a < ordinalofColorOfPLayersArray.length; a++) {
                if (ordinalofColorOfPLayersArray[a] == maximumElement) {
                    majorityOccupantColorSet.add(PlayerColor.ALL.get(a));
                }
            }
            return majorityOccupantColorSet;
        } else {
            return new HashSet<>();
        }
    }


     /**
      * retourne l'aire résultant de la connexion du récepteur (this) à l'aire donnée (that)
      * @param that: l'aire donnée
      * @return: l'aire résultant de la connexion du récepteur (this) à l'aire donnée (that)
      */
    public Area<Z> connectTo(Area<Z> that){
        if(this==that) return new Area<>(this.zones,this.occupants,this.openConnections-2);

        Set<Z> biggerAreaZones=new HashSet<>();
        biggerAreaZones.addAll(this.zones);
        biggerAreaZones.addAll(that.zones);

        List<PlayerColor> biggerAreaOccupants=new ArrayList<>();
        biggerAreaOccupants.addAll(this.occupants);
        biggerAreaOccupants.addAll(that.occupants);

        return new Area<>(biggerAreaZones,biggerAreaOccupants,this.openConnections+that.openConnections-2);
    }

     /**
      * retourne une aire identique au récepteur, si ce n'est qu'elle est occupée par l'occupant donné
      * @param occupant : l'occupant donné
      * @return une aire identique au récepteur, si ce n'est qu'elle est occupée par l'occupant donné
      * @throws IllegalArgumentException si le récepteur est déjà occupé
      */
    public Area<Z> withInitialOccupant(PlayerColor occupant){
        if (!this.occupants.isEmpty()){
            throw new IllegalArgumentException();
        }
        List<PlayerColor> initialOccupant = new ArrayList<>();
        initialOccupant.add(occupant);
        return new Area<>(this.zones,initialOccupant,this.openConnections);
    }

     /**
      * retourne une aire identique au récepteur, mais qui comporte un occupant de la couleur donnée en moins
      * @param occupant: l'occupant donné
      * @return une aire identique au récepteur, mais qui comporte un occupant de la couleur donnée en moins
      * @throws IllegalArgumentException si le récepteur ne contient aucun occupant de la couleur donnée
      */
    public Area<Z> withoutOccupant(PlayerColor occupant){
        if (!this.occupants.contains(occupant)){
            throw new IllegalArgumentException();
        }
        List<PlayerColor> newPlayerColors = new ArrayList<>();
        int i =0;
        for (PlayerColor color: this.occupants) {
            if (!color.equals(occupant)||(i>=1)){
                newPlayerColors.add(color);
            }
            if (color.equals(occupant)){
                i+=1;
            }
        }
        return new Area<>(this.zones,newPlayerColors,this.openConnections);
    }

     /**
      * retourne une aire identique au récepteur, mais totalement dénuée d'occupants
      * @return une aire identique au récepteur, mais totalement dénuée d'occupants
      */
    public Area<Z> withoutOccupants(){
        List<PlayerColor> emptyOccupants = new ArrayList<>();
        return new Area<>(this.zones,emptyOccupants,this.openConnections);
    }

     /**
      * retourne l'ensemble de l'identité des tuiles contenant l'aire
      * @return l'ensemble de l'identité des tuiles contenant l'aire
      */
    public Set<Integer> tileIds(){
        Set<Integer> tileIds = new HashSet<>();
        for(Z zone: zones){
            tileIds.add(zone.tileId());
        }
        return tileIds;
    }

     /**
      * retourne la zone de l'aire qui possède le pouvoir spécial donné, ou null s'il n'en existe aucune
      * @param specialPower: le pouvoir donné
      * @return la zone de l'aire qui possède le pouvoir spécial donné, ou null s'il n'en existe aucune
      */
    public Zone zoneWithSpecialPower(Zone.SpecialPower specialPower){
        for(Z zone: zones){
            if (zone.specialPower() != null && zone.specialPower().equals(specialPower)){
                return zone;
            }
        }
        return null;
    }
}
