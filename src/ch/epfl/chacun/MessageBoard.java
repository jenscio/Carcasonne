package ch.epfl.chacun;

import java.util.*;

/**
 *  MessageBoard :
 *  représente le contenu du tableau d'affichage.
 *
 *  @author Rayan Berrada (378940)
 *  @author Jens Cancio (379510)
 *
 */


public record MessageBoard(TextMaker textMaker, List<Message> messages) {
    /**
     * constructeur compact de MessageBoard garantit l'immuabilité de la classe
     * @param messages:  la liste des messages affichés sur le tableau, du plus ancien au plus récent.
     */
    public MessageBoard {
        messages = List.copyOf(messages);
    }
    /**
     * @return une table associant à tous les joueurs figurant dans les gagnants (scorers) d'au moins un message, le nombre total de points obtenus
     */
    public Map<PlayerColor, Integer> points(){
        Map<PlayerColor, Integer> p = new HashMap<>();
        for (Message message: messages){
            if (message.points>0){
                for (PlayerColor scorer: message.scorers){
                    if (!p.containsKey(scorer)) {
                        p.put(scorer, message.points);
                    }
                    else {
                        p.put(scorer, message.points()+p.get(scorer));
                    }
                }
            }
        }
        return p;
    }
    /**
     * @param forest: une aire de Forest
     * @return retourne un tableau d'affichage identique au récepteur, sauf si la forêt donnée est occupée, auquel cas le tableau contient un nouveau message signalant que ses occupants majoritaires ont remporté les points associés à sa fermeture,
     */
    public MessageBoard withScoredForest(Area<Zone.Forest> forest){
        if(!forest.isOccupied()) return new MessageBoard(textMaker, messages);

        Set<PlayerColor> scorers=forest.majorityOccupants();

        int mushroomCount=Area.mushroomGroupCount(forest);
        int pointsEarned=Points.forClosedForest(forest.tileIds().size(),mushroomCount);
        Set<Integer> tileIds=forest.tileIds();

        String messageText=textMaker.playersScoredForest(scorers,pointsEarned,mushroomCount,tileIds.size());

        List<Message> newMessagesHistory=new ArrayList<>(messages);

        Message newMessage= new Message(messageText,pointsEarned,scorers,tileIds);
        newMessagesHistory.add(newMessage);

        return new MessageBoard(textMaker, newMessagesHistory);
    }
    /**
     * @param forest: une aire de Forest
     * @param player: un joueur
     * @return retourne un tableau d'affichage identique au récepteur, mais avec un nouveau message signalant que le joueur donné a le droit de jouer un second tour après avoir fermé la forêt donnée, car elle contient un ou plusieurs menhirs,
     */
    public MessageBoard withClosedForestWithMenhir(PlayerColor player, Area<Zone.Forest> forest){
        Set<PlayerColor> scorers= new HashSet<>();
        Set<Integer> tileIds=forest.tileIds();
        Message message= new Message(textMaker.playerClosedForestWithMenhir(player),0,scorers,tileIds);
        List<Message> newMessages= new ArrayList<>(this.messages);
        newMessages.add(message);
        return new MessageBoard(this.textMaker,newMessages);

    }
    /**
     * @param river: une aire de River
     * @return retourne un tableau d'affichage identique au récepteur, sauf si la rivière donnée est occupée, auquel cas le tableau contient un nouveau message signalant que ses occupants majoritaires ont remporté les points associés à sa fermeture,
     */
    public MessageBoard withScoredRiver(Area<Zone.River> river){
        if(!river.isOccupied()) return new MessageBoard(textMaker, messages);

        Set<PlayerColor> scorers=river.majorityOccupants();

        int fishCount=Area.riverFishCount(river);
        int pointsEarned=Points.forClosedRiver(river.tileIds().size(),fishCount);
        Set<Integer> tileIds=river.tileIds();

        String messageText=textMaker.playersScoredRiver(scorers,pointsEarned,fishCount,tileIds.size());

        List<Message> newMessagesHistory=new ArrayList<>(messages);

        Message newMessage= new Message(messageText,pointsEarned,scorers,tileIds);
        newMessagesHistory.add(newMessage);

        return new MessageBoard(textMaker, newMessagesHistory);
    }
    /**
     * @param adjacentMeadow: une aire de Meadow
     * @param scorer: un joueur
     * @param cancelledAnimals: les cerfs annulés
     * @return retourne un tableau d'affichage identique au récepteur, sauf si la pose de la fosse à pieux a permis au
     *  joueur donné, qui l'a posée, de remporter des points, auquel cas le tableau contient un nouveau message
     *  signalant cela
     */
    public MessageBoard withScoredHuntingTrap(PlayerColor scorer, Area<Zone.Meadow> adjacentMeadow, Set<Animal> cancelledAnimals){
        Set<Animal> animalsSet= Area.animals(adjacentMeadow,Set.of());
        int mammothCount=0, aurochsCOunt=0, deerCount=0;
        for(Animal animal:animalsSet){
            if(animal.kind().equals(Animal.Kind.MAMMOTH)){
                mammothCount+=1;
            }
            if(animal.kind().equals(Animal.Kind.AUROCHS)){
                aurochsCOunt+=1;
            }
            if(animal.kind().equals(Animal.Kind.DEER) && !cancelledAnimals.contains(animal)){
                deerCount+=1;
            }
        }
        int pointsEarned= Points.forMeadow(mammothCount,aurochsCOunt,deerCount);
        if(pointsEarned<=0){
            return new MessageBoard(textMaker,messages);
        }
        Set<Integer> tileIds=adjacentMeadow.tileIds();
        Map<Animal.Kind, Integer> animalMap= new HashMap<>();
        animalMap.put(Animal.Kind.MAMMOTH,mammothCount);
        animalMap.put(Animal.Kind.AUROCHS,aurochsCOunt);
        animalMap.put(Animal.Kind.DEER,deerCount);
        Message newMessage= new Message(textMaker().playerScoredHuntingTrap(scorer,pointsEarned,animalMap),pointsEarned,Set.of(scorer),tileIds);
        List<Message> newMessagesHistory=new ArrayList<>(messages);
        newMessagesHistory.add(newMessage);
        return new MessageBoard(textMaker,newMessagesHistory);
    }
    /**
     * @param riverSystem: une aire de Water
     * @param scorer: un joueur
     * @return retourne un tableau d'affichage identique au récepteur, sauf si le réseau hydrographique donné est occupé et que les points qu'il rapporte à ses occupants majoritaires sont supérieurs à 0, auquel cas le tableau contient un nouveau message signalant que ces joueurs-là ont remporté les points en question
     */

    public MessageBoard withScoredLogboat(PlayerColor scorer, Area<Zone.Water> riverSystem){
        int lakeCount = 0;

        for (Zone.Water riverAndLakes: riverSystem.zones()){
            if (riverAndLakes instanceof Zone.Lake){
                lakeCount+=1;
            }
        }

        int pointsEarned = Points.forLogboat(lakeCount);
        Set<Integer> tileIds=riverSystem.tileIds();

        String messageText=textMaker.playerScoredLogboat(scorer,pointsEarned,lakeCount);
        List<Message> newMessagesHistory=new ArrayList<>(messages);

        Set<PlayerColor> scorers = new HashSet<>();
        scorers.add(scorer);

        Message newMessage= new Message(messageText,pointsEarned,scorers,tileIds);
        newMessagesHistory.add(newMessage);

        return new MessageBoard(textMaker, newMessagesHistory);

    }
    /**
     * @param meadow: une aire de Meadow
     * @param cancelledAnimals: le Set d'animaux annnulés ou supporimé(dévorés par exemple)
     * @return retourne un tableau d'affichage identique au récepteur, sauf si le pré donné est occupé et que les points qu'il rapporte à ses occupants majoritaires — calculés en faisant comme si les animaux annulés donnés n'existaient pas — sont supérieurs à 0, auquel cas le tableau contient un nouveau message signalant que ces joueurs-là ont remporté les points en question,
     */
    public MessageBoard withScoredMeadow(Area<Zone.Meadow> meadow, Set<Animal> cancelledAnimals){
        if(!meadow.isOccupied()) return new MessageBoard(textMaker, messages);

        Set<PlayerColor> scorers=meadow.majorityOccupants();

        if(scorers.isEmpty()) return new MessageBoard(textMaker, messages);

        Set<Animal> animalSet = Area.animals(meadow,cancelledAnimals);
        int mammothCount = 0;
        int aurochsCount = 0;
        int deerCount = 0;
        int tigerCount =0;

        for (Animal animal: animalSet){
            switch (animal.kind()){
                case DEER -> deerCount+=1;
                case AUROCHS -> aurochsCount+=1;
                case MAMMOTH -> mammothCount+=1;
                default -> tigerCount+=1;
            }
        }
        int pointsEarned = Points.forMeadow(mammothCount,aurochsCount,deerCount);
        if (pointsEarned==0){ return new MessageBoard(textMaker, messages);}
        Set<Integer> tileIds=meadow.tileIds();
        Map<Animal.Kind,Integer> animalMap = new HashMap<>();
        animalMap.put(Animal.Kind.MAMMOTH,mammothCount);
        animalMap.put(Animal.Kind.AUROCHS,aurochsCount);
        animalMap.put(Animal.Kind.DEER,deerCount);
        animalMap.put(Animal.Kind.TIGER,tigerCount);

        String messageText=textMaker.playersScoredMeadow(scorers,pointsEarned,animalMap);
        List<Message> newMessagesHistory=new ArrayList<>(messages);
        Message newMessage= new Message(messageText,pointsEarned,scorers,tileIds);
        newMessagesHistory.add(newMessage);

        return new MessageBoard(textMaker, newMessagesHistory);
    }
    /**
     * @param riverSystem: une aire de Water
     * @return  retourne un tableau d'affichage identique au récepteur, sauf si le réseau hydrographique donné est occupé et que les points qu'il rapporte à ses occupants majoritaires sont supérieurs à 0, auquel cas le tableau contient un nouveau message signalant que ces joueurs-là ont remporté les points en question
    */
    public MessageBoard withScoredRiverSystem(Area<Zone.Water> riverSystem){
        if(!riverSystem.isOccupied() ||Area.riverSystemFishCount(riverSystem)==0 ) return new MessageBoard(textMaker, messages);

        Set<PlayerColor> scorers=riverSystem.majorityOccupants();
        int fishCount = Area.riverSystemFishCount(riverSystem);
        int pointsEarned = Points.forRiverSystem(fishCount);
        if(pointsEarned==0) return new MessageBoard(textMaker, messages);
        Set<Integer> tileIds=riverSystem.tileIds();
        String messageText=textMaker.playersScoredRiverSystem(scorers,pointsEarned,fishCount);
        List<Message> newMessagesHistory=new ArrayList<>(messages);
        Message newMessage= new Message(messageText,pointsEarned,scorers,tileIds);
        newMessagesHistory.add(newMessage);

        return new MessageBoard(textMaker, newMessagesHistory);

    }
    /**
     * @param adjacentMeadow: une aire de Meadow adjacents
     * @param cancelledAnimals: le Set d'animaux annnulés ou supporimé(dévorés par exemple)
     * @return retourne un tableau d'affichage identique au récepteur, sauf si le pré donné, qui contient la grande fosse à pieux, est occupé et que les points — calculés en faisant comme si les animaux annulés donnés n'existaient pas — qu'il rapporte à ses occupants majoritaires sont supérieurs à 0, auquel cas le tableau contient un nouveau message signalant que ces joueurs-là ont remporté les points en question ; comme pour la « petite » fosse à pieux, le pré donné comporte les mêmes occupants que le pré contenant la fosse, mais uniquement les zones se trouvant à sa portée
     */
    public MessageBoard withScoredPitTrap(Area<Zone.Meadow> adjacentMeadow, Set<Animal> cancelledAnimals){
        Set<PlayerColor> majorityOccupants= adjacentMeadow.majorityOccupants();
        if(majorityOccupants.isEmpty()) return new MessageBoard(textMaker, messages);
        Set<Animal> animalsSet= Area.animals(adjacentMeadow,cancelledAnimals);
        int mammothCount=0,aurochsCOunt=0,deerCount=0,tigerCount=0;
        for(Animal animal:animalsSet){
            if(animal.kind().equals(Animal.Kind.MAMMOTH)){
                mammothCount+=1;
            }
            if(animal.kind().equals(Animal.Kind.AUROCHS)){
                aurochsCOunt+=1;
            }
            if(animal.kind().equals(Animal.Kind.DEER)){
                deerCount+=1;
            }
            if(animal.kind().equals(Animal.Kind.TIGER)) {
                tigerCount += 1;
            }
        }
        int pointsEarned= Points.forMeadow(mammothCount,aurochsCOunt,deerCount);
        if (pointsEarned==0){ return new MessageBoard(textMaker, messages);}
        Map<Animal.Kind, Integer> animalMap= new HashMap<>();
        animalMap.put(Animal.Kind.MAMMOTH,mammothCount);
        animalMap.put(Animal.Kind.AUROCHS,aurochsCOunt);
        animalMap.put(Animal.Kind.DEER,deerCount);
        animalMap.put(Animal.Kind.TIGER,tigerCount);
        Set<Integer> tileIds=adjacentMeadow.tileIds();
        Message newMessage= new Message(textMaker.playersScoredPitTrap(majorityOccupants,pointsEarned,animalMap),pointsEarned,majorityOccupants,tileIds);
        if(pointsEarned<=0 || !adjacentMeadow.isOccupied()){
            return new MessageBoard(textMaker,messages);
        }
        List<Message> newMessagesHistory=new ArrayList<>(messages);
        newMessagesHistory.add(newMessage);
        return new MessageBoard(textMaker,newMessagesHistory);
    }
    /**
     * @param riverSystem: une aire de Water
     * @return  retourne un tableau d'affichage identique au récepteur, sauf si le réseau hydrographique donné, qui contient le radeau, est occupé, auquel cas le tableau contient un nouveau message signalant que ses occupants majoritaires ont remporté les points correspondants
     */
    public MessageBoard withScoredRaft(Area<Zone.Water> riverSystem){
        if(!riverSystem.isOccupied()) return new MessageBoard(textMaker, messages);

        Set<PlayerColor> scorers=riverSystem.majorityOccupants();
        int lakeCount = 0;

        for (Zone.Water riverAndLakes: riverSystem.zones()){
            if (riverAndLakes instanceof Zone.Lake){
                lakeCount+=1;
            }
        }
        int pointsEarned = Points.forRaft(lakeCount);
        Set<Integer> tileIds=riverSystem.tileIds();

        String messageText=textMaker.playersScoredRaft(scorers,pointsEarned,lakeCount);

        List<Message> newMessagesHistory=new ArrayList<>(messages);
        Message newMessage= new Message(messageText,pointsEarned,scorers,tileIds);
        newMessagesHistory.add(newMessage);

        return new MessageBoard(textMaker, newMessagesHistory);



    }
    /**
     * @param winners: un Set de Players, les gagnants
     * @param points: nombre de points remportés
     * @return  retourne un tableau d'affichage identique au récepteur, mais avec un nouveau message signalant que le(s) joueur(s) donné(s) a/ont remporté la partie avec le nombre de points donnés
     */
    public MessageBoard withWinners(Set<PlayerColor> winners, int points){
        if(points<0) throw new IllegalArgumentException();
        if(points>0&& winners.isEmpty()) throw new IllegalArgumentException();
        List<Message> newMessages = new ArrayList<>(messages);
        String winnersMessageText = textMaker.playersWon(winners,points);
        Message winnersMessage = new Message(winnersMessageText,0,Collections.emptySet(),Collections.emptySet());
        newMessages.add(winnersMessage);
        return new MessageBoard(textMaker,newMessages);
    }

    /*
     *  Message :
     * enregistrement Message, imbriqué dans l'enregistrement MessageBoard
     *  représente un message affiché sur le tableau d'affichage
     */

    public record Message(String text, int points, Set<PlayerColor> scorers,Set<Integer> tileIds){
        /**
         * Constructeur compact de Message, qui vérifie que le texte passé n'est pas null, que les points ne sont pas inférieurs à 0, et copie les deux ensembles pour garantir l'immuabilité.
         * @param text:  le texte du message
         * @param points: les points associés au message qui peuvent valoir 0, par exemple si le message ne signale pas un gain de points,
         * @param scorers: l'ensemble des joueurs ayant remportés les points, qui peut être vide si le message ne signale pas un gain de points
         * @param tileIds, les identifiants des tuiles concernées par le message, ou un ensemble vide si le message ne concerne pas un ensemble de tuiles.
         * @throws IllegalArgumentException si les conditions suivantes ne se remplissant pas :
         *           - les points sont négatifs
         * @throws NullPointerException si les conditions suivantes ne se remplissant pas :
         *           - le text est null
         */
        public Message{
            if (text==null){
                throw new NullPointerException();
            }
            if (points<0){
                throw new IllegalArgumentException();
            }
            scorers = Set.copyOf(scorers);
            tileIds = Set.copyOf(tileIds);
        }
    }
}
