package ch.epfl.chacun.gui;

import ch.epfl.chacun.Base32;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.List;
import java.util.function.Consumer;

/**
 *  ActionsUI:
 *  contient le code de création de l'interface graphique pour le jeu à distance
 *
 *  @author Rayan Berrada (378940)
 *  @author Jens Cancio (379510)
 *
 */


public final class ActionUI {
    /**
     * Constructeur vide (classe non instantiable)
     */
    private ActionUI() {}


    /**
     * création de l'interface graphique permettant le jeu à distance
     * @param actionListO: liste (observable) de la représentation en base32 de toutes les actions
     * @param eventHandlerAction: gestionnaire d'événement destiné à être appelé avec la
     *                          représentation en base32 d'une action si celle est valide
     * @return l'interface graphique permettant le jeu à distance
     */
    public static Node create (ObservableValue<List<String>> actionListO,
                               Consumer<String> eventHandlerAction) {
        HBox hbox = new HBox();
        hbox.setId("actions");
        hbox.getStylesheets().add("actions.css");

        Text text = new Text();
        TextField textField = new TextField();
        textField.setId("action-field");


        // utilisation de setTextFormatter:
        textField.setTextFormatter(new TextFormatter<>(change -> {
            // prendre le text à changer, le mettre en majuscules,
            // et faire un forEach pour ajouter les characters valides dans
            // le stringBuilder :
            StringBuilder newStringBuilder = new StringBuilder();
            change.getText().toUpperCase().chars().forEach(character -> {
                if (Base32.ALPHABET.contains(String.valueOf((char) character)))
                   newStringBuilder.append((char) character);
            });
            change.setText(newStringBuilder.toString());
            return change;
        }));

        textField.setOnAction(_ -> {
            String action = textField.getText().toUpperCase();
            if (!action.isEmpty() && action.length() <= 2) {
                eventHandlerAction.accept(action);
            }
            textField.clear();
        });
        // generer un bind pour montrer les 4 dernieres actions
        text.textProperty().bind(actionListO.map(ActionUI::formatActions));

        hbox.getChildren().addAll(text, textField);
        return hbox;
    }

    private static String formatActions(List<String> actions) {
        StringBuilder builder = new StringBuilder();
        int startIndex = Math.max(0, actions.size() - 4);
        for (int i = startIndex; i < actions.size(); i++) {
            builder.append(i + 1).append(":").append(actions.get(i));
            if (i < actions.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
}

