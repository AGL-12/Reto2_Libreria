/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import model.Commentate;

/**
 * FXML Controller class
 *
 * @author mikel
 */
public class CommentFormViewController implements Initializable {

    @FXML
    private Label lblDetails;
    @FXML
    private TextArea txtComment;
    @FXML
    private Label lblPoint;

    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;
    @FXML
    private EstrellaPuntuacionController estrellasController;

    private Commentate commentToEdit;
    private boolean saveClicked = false;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        if (estrellasController != null) {
            estrellasController.setEditable(true);
        }

    }

    @FXML
    private void handleSave(ActionEvent event) {
        String texto = txtComment.getText();
        if (texto == null || texto.trim().isEmpty()) {
            showAlert("El comentario no puede estar vacío.");
            return;
        }

        //Prueba si no hay usuario actual
        if (commentToEdit == null) {
            commentToEdit = new Commentate();
            commentToEdit.setTempUsername("Usuario actual");
        }

        commentToEdit.setCommentary(texto);

        if (estrellasController != null) {
            commentToEdit.setValuation((float) estrellasController.getNotaUsuario());

        }
        saveClicked = true;
        closeWindow();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        saveClicked = false;
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public Commentate getComment() {
        return commentToEdit;
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void setComment(Commentate c) {
        this.commentToEdit = c;

        if (c != null) {
            txtComment.setText(c.getCommentary());

            if (estrellasController != null) {
                estrellasController.setNota((double) c.getValuation());
            }

        } else {
            txtComment.setText("");

            if (estrellasController != null) {
                estrellasController.setNota(5); 
            }
        }
    }

}
