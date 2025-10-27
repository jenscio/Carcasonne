package ch.epfl.chacun;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnotherMessageBoardTest {

    /**
     * Tests pour vérifier que points fonctionne
     */
    @Test
    void pointsWorksOnNonTrivialCase() {
        Text textMaker = new Text();
        MessageBoard messageBoard = new MessageBoard(textMaker, messages());

        Map<PlayerColor, Integer> expectedMap = new HashMap<>();
        expectedMap.put(PlayerColor.RED, 2);
        expectedMap.put(PlayerColor.BLUE, 2);
        expectedMap.put(PlayerColor.YELLOW, 6);
        expectedMap.put(PlayerColor.GREEN, 8);

        assertEquals(expectedMap, messageBoard.points());
    }

    /**
     * Tests pour vérifier que withScoredForest fonctionne
     */
    @Test
    void withScoredForestWorksOnNonOccupiedForest() {
        Text textMaker = new Text();

        Zone.Forest forest1 = new Zone.Forest(1, null);
        Zone.Forest forest2 = new Zone.Forest(2, Zone.Forest.Kind.WITH_MENHIR);
        Zone.Forest forest3 = new Zone.Forest(3, null);
        Set<Zone.Forest> forestSet = new HashSet<>();
        Collections.addAll(forestSet, forest1, forest2, forest3);
        List<PlayerColor> playerColors = new ArrayList<>();

        Area<Zone.Forest> forestArea = new Area<>(forestSet, playerColors, 0);

        MessageBoard messageBoard = new MessageBoard(textMaker, messages());

        assertEquals(messageBoard, messageBoard.withScoredForest(forestArea));
    }

    // @Test
    // pas clair si correctement develope - pas de mushrooms declares dans le messageBoard a verifier
    void withScoredForestWorksOnOccupiedForest() {
        Text textMaker = new Text();

        Zone.Forest forest1 = new Zone.Forest(501, null);
        Zone.Forest forest2 = new Zone.Forest(602, Zone.Forest.Kind.WITH_MENHIR);
        Zone.Forest forest3 = new Zone.Forest(703, null);
        Set<Zone.Forest> forestSet = new HashSet<>();
        Collections.addAll(forestSet, forest1, forest2, forest3);
        List<PlayerColor> playerColors = new ArrayList<>();
        playerColors.add(PlayerColor.RED);

        Area<Zone.Forest> forestArea = new Area<>(forestSet, playerColors, 0);

        MessageBoard messageBoard = new MessageBoard(textMaker, messages());

        Set<PlayerColor> playerColorSet = new HashSet<>();
        playerColorSet.add(PlayerColor.RED);
        Set<Integer> tileIds = new HashSet<>();
        Collections.addAll(tileIds, 50, 60, 70);

        MessageBoard.Message expectedMessage = new MessageBoard.
                Message(textMaker.playersScoredForest(playerColorSet, 3, 4, 5),
                3, playerColorSet, tileIds);

        List<MessageBoard.Message> messageList = messages();
        messageList.add(expectedMessage);

        MessageBoard expectedMessageBoard = new MessageBoard(textMaker, messageList);

        assertEquals(expectedMessageBoard, messageBoard.withScoredForest(forestArea));

    }

    private List<MessageBoard.Message> messages() {
        Text textMaker = new Text();
        Set<PlayerColor> playerColorsSet1 = new HashSet<>();
        Collections.addAll(playerColorsSet1, PlayerColor.RED, PlayerColor.BLUE);
        Set<Integer> tileIds1 = new HashSet<>();
        MessageBoard.Message message1 = new MessageBoard.Message("text1", 2, playerColorsSet1, tileIds1);

        Set<PlayerColor> playerColorsSet2 = new HashSet<>();
        playerColorsSet2.add(PlayerColor.YELLOW);
        Set<Integer> tileIds2 = new HashSet<>();
        tileIds2.add(2);
        MessageBoard.Message message2 = new MessageBoard.Message("text2", 3, playerColorsSet2, tileIds2);

        Set<PlayerColor> playerColorsSet3 = new HashSet<>();
        playerColorsSet3.add(PlayerColor.GREEN);
        Set<Integer> tileIds3 = new HashSet<>();
        MessageBoard.Message message3 = new MessageBoard.Message("text3", 4, playerColorsSet3, tileIds3);

        Set<PlayerColor> playerColorsSet4 = new HashSet<>();
        playerColorsSet4.add(PlayerColor.YELLOW);
        Set<Integer> tileIds4 = new HashSet<>();
        MessageBoard.Message message4 = new MessageBoard.Message("text4", 3, playerColorsSet4, tileIds4);

        Set<PlayerColor> playerColorsSet5 = new HashSet<>();
        playerColorsSet5.add(PlayerColor.GREEN);
        Set<Integer> tileIds5 = new HashSet<>();
        MessageBoard.Message message5 = new MessageBoard.Message("text5", 4, playerColorsSet5, tileIds5);

        List<MessageBoard.Message> messages = new ArrayList<>();
        Collections.addAll(messages, message1, message2, message3, message4, message5);

        return messages;
    }
    private class Text implements TextMaker {

        @Override
        public String playerName(PlayerColor playerColor) {
            return new StringJoiner(" ").add(playerColor.toString()).toString();
        }

        @Override
        public String points(int points) {
            return new StringJoiner(" ").add(String.valueOf(points)).toString();
        }

        @Override
        public String playerClosedForestWithMenhir(PlayerColor player) {
            return new StringJoiner(" ").add(player.toString()).toString();
        }

        @Override
        public String playersScoredForest(Set<PlayerColor> scorers, int points, int mushroomGroupCount, int tileCount) {
            return new StringJoiner(" ")
                    .add("cSCORERS: "+scorers.toString())
                    .add(" POINTS:" + String.valueOf(points))
                    .add(" MUSHGC:" + String.valueOf(mushroomGroupCount))
                    .add(" TileCount: "+ String.valueOf(tileCount))
                    .toString();
        }

        @Override
        public String playersScoredRiver(Set<PlayerColor> scorers, int points, int fishCount, int tileCount) {
            return new StringJoiner(" ")
                    .add(scorers.toString())
                    .add(String.valueOf(points))
                    .add(String.valueOf(fishCount))
                    .add(String.valueOf(tileCount))
                    .toString();
        }

        @Override
        public String playerScoredHuntingTrap(PlayerColor scorer, int points, Map<Animal.Kind, Integer> animals) {
            return new StringJoiner(" ")
                    .add(scorer.toString())
                    .add(String.valueOf(points))
                    .add(animals.toString())
                    .toString();
        }

        @Override
        public String playerScoredLogboat(PlayerColor scorer, int points, int lakeCount) {
            return new StringJoiner(" ")
                    .add(scorer.toString())
                    .add(String.valueOf(points))
                    .add(String.valueOf(lakeCount))
                    .toString();
        }

        @Override
        public String playersScoredMeadow(Set<PlayerColor> scorers, int points, Map<Animal.Kind, Integer> animals) {
            return new StringJoiner(" ")
                    .add(scorers.toString())
                    .add(String.valueOf(points))
                    .add(animals.toString())
                    .toString();
        }

        @Override
        public String playersScoredRiverSystem(Set<PlayerColor> scorers, int points, int fishCount) {
            return new StringJoiner(" ")
                    .add(scorers.toString())
                    .add(String.valueOf(points))
                    .add(String.valueOf(fishCount))
                    .toString();
        }

        @Override
        public String playersScoredPitTrap(Set<PlayerColor> scorers, int points, Map<Animal.Kind, Integer> animals) {
            return new StringJoiner(" ")
                    .add(scorers.toString())
                    .add(String.valueOf(points))
                    .add(animals.toString())
                    .toString();
        }

        @Override
        public String playersScoredRaft(Set<PlayerColor> scorers, int points, int lakeCount) {
            return new StringJoiner(" ")
                    .add(scorers.toString())
                    .add(String.valueOf(points))
                    .add(String.valueOf(lakeCount))
                    .toString();
        }

        @Override
        public String playersWon(Set<PlayerColor> winners, int points) {
            return new StringJoiner(" ")
                    .add(winners.toString())
                    .add(String.valueOf(points))
                    .toString();
        }

        @Override
        public String clickToOccupy() {
            return null;
        }

        @Override
        public String clickToUnoccupy() {
            return null;
        }
    }
}
