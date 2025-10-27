package GUItest;

import ch.epfl.chacun.gui.ActionUI;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ActionsUITest extends Application {
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {

        SimpleObjectProperty<List<String>> actionListP = new SimpleObjectProperty<>();

        ArrayList<String> arrayList = new ArrayList<>(List.of("A","BC","DE","F","G","HI","JK","LM"));


        actionListP.setValue(arrayList);
        Consumer<String> actionListEventHandler =
                (e) ->{
            System.out.println("INFO: called the actionListEventHandler with: " + e);
            arrayList.add(e);
            actionListP.setValue(List.copyOf(arrayList)); // make COPY for setting new value of observable
        };

        Node actionsUINode = ActionUI.create(actionListP,actionListEventHandler);

        var rootNode = new BorderPane(actionsUINode);
        primaryStage.setScene(new Scene(rootNode));

        primaryStage.setTitle("ChaCuN ActionUI test");
        primaryStage.show();
    }
}
