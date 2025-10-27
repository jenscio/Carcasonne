package GUItest;

import ch.epfl.chacun.Occupant;
import ch.epfl.chacun.PlayerColor;
import ch.epfl.chacun.gui.Icon;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;


public class CheckColorMapAndIcon extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        ////////////////////////////////////////////////////////////////
        // GUI TEST CODE STARTS HERE:
        ////////////////////////////////////////////////////////////////

        // first a very basic test:
        // create a simple pawn icon for the blue player
        // (Icon.newFor is a static function that returns a Node type object)
        Node simplePawn = Icon.newFor(PlayerColor.BLUE, Occupant.Kind.PAWN);

        // now a bit more complete test:
        // create all pawns and huts for all colors
        //
        // first we create a map for each player color and icon (Node object)
        Map<PlayerColor,Node> pawnIconMap = new HashMap<>();
        Map<PlayerColor,Node> hutIconMap = new HashMap<>();
        // also we add a map for each player for adding a label node
        Map<PlayerColor,Node> nameLabelMap = new HashMap<>();

        // add for each player its pawn icon to the map
        // (we could use a "for" loop, but a forEach lambda is more compact to code!)
        PlayerColor.ALL.stream().forEach(c -> pawnIconMap.put(c, Icon.newFor(c, Occupant.Kind.PAWN)));
        // same for the huts
        PlayerColor.ALL.stream().forEach(c -> hutIconMap.put(c, Icon.newFor(c, Occupant.Kind.HUT)));
        // add for each player a Label node with the name of the color of the player
        PlayerColor.ALL.stream().forEach(c -> nameLabelMap.put(c, new Label(c.name())));

        // rotate the labels
        // nameLabelMap.forEach((c,g) -> g.setRotate(-90));

        // create a GridPane node where all other nodes will be added
        GridPane grid = new GridPane();

        // add the simple blue pawn to the grid pane, it will appear on the top left
        grid.addRow(0,simplePawn);

        // add three labels for each of the three categories
        grid.addRow(1,new Label("Colors:"));
        grid.addRow(2,new Label("Pawns:"));
        grid.addRow(3,new Label("Huts:"));

        // now add the nodes with the names and the icons
        // (note: this could have been implemented using a forEach lambda!)
        for (PlayerColor c:PlayerColor.ALL) {
            grid.addRow(1,nameLabelMap.get(c));
            grid.addRow(2, pawnIconMap.get(c));
            grid.addRow(3, hutIconMap.get(c));
        }

        //////////////////////////////////////////////////////////////////////////////////////
        // standard statements for displaying the "mother" node (grid in this case) on screen:
        //////////////////////////////////////////////////////////////////////////////////////

        Scene scene = new Scene(grid);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
