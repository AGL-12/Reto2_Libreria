package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import model.Commentate; 
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom Cell Factory:
 * Renders a 'Commentate' object using the 'ComentView.fxml' layout.
 */
public class CommentCell extends ListCell<Commentate> {

    private Parent graphicNode;
    private ComentViewController controller; // Keeping this reference to your existing view controller

    public CommentCell() {
        // Empty constructor
    }

    @Override
    protected void updateItem(Commentate item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (graphicNode == null) {
                try {
                    // Load the FXML only once
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ComentView.fxml"));
                    graphicNode = loader.load();
                    controller = loader.getController();
                } catch (IOException ex) {
                    Logger.getLogger(CommentCell.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            // Set data into the small view controller
            // Note: calling setDatos (assuming ComentViewController still has Spanish method names)
            controller.setDatos(
                    item.getTempUsername(), 
                    item.getFormattedDate(), 
                    item.getCommentary(), 
                    item.getValuation()
            );

            setText(null);
            setGraphic(graphicNode);
        }
    }
}