package ch.epfl.chacun.gui;

import ch.epfl.chacun.MessageBoard;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Set;

/**
 * MessageBoardUI:
 * contient le code de création de l'interface graphique du tableau d'affichage
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */

public final class MessageBoardUI {

    /**
     * Constructeur vide (classe non instantiable)
     */
    private MessageBoardUI() {
    }


    /**
     * création de l'interface graphique du tableau d'affichage
     *
     * @param messageListO:        version observable des messages affichés sur le tableau d'affichage
     * @param tileIDsToHighlightO: l'ensemble des identités des tuiles à mettre en évidence sur le plateau
     * @return l'interface graphique du tableau d'affichage
     */
    public static Node create(ObservableValue<List<MessageBoard.Message>> messageListO,
                              ObjectProperty<Set<Integer>> tileIDsToHighlightO) {

        ScrollPane messageScrollPane = new ScrollPane();

        // identité des nœuds
        messageScrollPane.setId("message-board");
        //  feuilles de style à attacher au nœud
        messageScrollPane.getStylesheets().add("message-board.css");

        VBox vBox = new VBox();
        messageScrollPane.setContent(vBox);
        // ajuster la taille du noeud vBox
        vBox.setPrefSize(ImageLoader.LARGE_TILE_FIT_SIZE, ImageLoader.LARGE_TILE_FIT_SIZE);

        messageListO.addListener((_, oldMessageList, newMessageList) -> {
            // creer les nouvelles Text messages qui sont apparus
            // pour ceci, iterer sur tous les nouveaux elements pas presents en oldMessageList
            for (int i = oldMessageList.size(); i < newMessageList.size(); i++) {
                MessageBoard.Message newMessage = newMessageList.get(i);
                Text newMessageText = new Text(newMessage.text());
                newMessageText.setWrappingWidth(ImageLoader.LARGE_TILE_FIT_SIZE);
                vBox.getChildren().add(newMessageText);
                // maintenant associer les ID's des tuiles au set de tileID's dans la ObjectProperty set tileID's
                newMessageText.setOnMouseEntered(_ -> tileIDsToHighlightO.setValue(newMessage.tileIds()));
                newMessageText.setOnMouseExited(_ -> tileIDsToHighlightO.setValue(Set.of()));
            }
            messageScrollPane.layout();
            messageScrollPane.setVvalue(1);
        });

        return messageScrollPane;
    }


}
