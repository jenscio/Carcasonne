package GUItest;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public final class JavaFXDemo extends Application {
    public static void main(String[] args) {
        launch(args);
    }


    // some application attributes
    String username = null;
    String password = null;
    int connectionAttempts = 0;

    // observable properties for the above
    SimpleObjectProperty<Integer> connectionAttemptsP = new SimpleObjectProperty<>();
    SimpleObjectProperty<String> usernameP = new SimpleObjectProperty<>();
    SimpleObjectProperty<String> passwordP = new SimpleObjectProperty<>();

    @Override
    public void start(Stage primaryStage) throws Exception {

        Label nameLabel = new Label("Nom :");           // Label: read-only text
        TextField nameField = new TextField();               // TextField: allows to enter text

        Label pwLabel = new Label("Mot de passe :");
        TextField pwField = new TextField();

        Button connectButton = new Button("Connexion");  // connection button

        Label connectionAttemptLabel = new Label("Connection attempts: ");
        Text connectionAttemptsText = new Text();        // Text: for displaying some text that can be changed

        Text warningText = new Text();
        Text warningTextB = new Text();

        GridPane grid = new GridPane();
        grid.addRow(0, nameLabel, nameField);
        grid.addRow(1, pwLabel, pwField);
        grid.add(connectButton, 0, 2, 2, 1);
        grid.addRow(3,connectionAttemptLabel,connectionAttemptsText);
        grid.addRow(4,warningText);
        grid.addRow(5,warningTextB);
        GridPane.setHalignment(connectButton, HPos.CENTER);

        // set an event when the connect button is pressed
        connectButton.setOnAction(e -> {
            System.out.println(" username is: " + nameField.getText() + "  password: " + pwField.getText());
            username = nameField.getText();
            password = pwField.getText();
            connectionAttempts++;
            connectionAttemptsP.setValue(connectionAttempts); // update the observable properties so that listeners are notified
            usernameP.setValue(username);
            passwordP.setValue(password);
        });

        // set an event when the mouse enters the username field

        // event: when the mouse enters the name label node
        nameLabel.setOnMouseEntered(e -> {
            System.out.println("mouse entered nameLabel!");
            nameLabel.setUnderline(true);
        });
        // event: when the mouse leaves the node
        nameLabel.setOnMouseExited(e -> {
            System.out.println("mouse exited nameLabel!");
            nameLabel.setUnderline(false);
        });
        // event: when the mouse clicks when over the node
        nameLabel.setOnMouseClicked(e -> {
            System.out.println("mouse clicked on textNode0!");
            System.out.println("button clicked: " + e.getButton());
            System.out.println(" ALT pressed? : " + e.isAltDown());
            System.out.println(" CTRL pressed? : " + e.isControlDown());
        });


        //
        // how to update the GUI? Option 1: use an addListener on the property we want to monitor/observe
        // disadvantage: the new value is only set once there has been a change in the observed property
        connectionAttemptsP.addListener((o,oldValue,newValue) -> {
            System.out.println("Listener: number of connection attempts has changed from " + oldValue + " to " + newValue);
            connectionAttemptsText.setText(newValue + " attempts");
        });


        //
        // how to update the GUI? Option 2: use binds

        // EITHER:

        //  2a): use an unidirectional "bind" based on a defined observable property
        //       we define an intermediate observable property that generates the right output via a javafx "map"
        ObservableValue<String> warningStringO = passwordP.map(s -> {
            if (s.length() < 5) {
                return "password too short!";
            } else {
                return "password OK";
            }
        });

        warningText.textProperty().bind(warningStringO); // the text property of "warningText" is now always assigned to the content of warningStringO


        //  OR:

        //  2b): use an unidirectional "bind" based on a JavaBeans property of the password textField (pwField)
        //       pwField.textProperty() contains the currently typed in password as an observable property
        //       so we can do a map on it as on any other observable property!
        //
        ObservableValue<String> warningStringBO = pwField.textProperty().map(s -> {
            if (s.length() < 5) {
                return "password too short!";
            } else {
                return "password OK";
            }
        });

        warningTextB.textProperty().bind(warningStringBO); // the text property of "warningTextB" is now always assigned to the content of warningStringB0





        // prepare the scene and show it

        Scene scene = new Scene(grid);
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
