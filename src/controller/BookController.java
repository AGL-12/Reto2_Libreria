/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.URL;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import model.Commentate;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

/**
 * FXML Controller class
 *
 * @author mikel
 */
public class BookController implements Initializable {
    @FXML
    private ListView<Commentate> listViewComments;
    @FXML
    private Button btnCreate;
    @FXML
    private Button btnModify;
    @FXML
    private Button btnDelete;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
  // 1. Set the Cell Factory to use our new CommentCell class
        listViewComments.setCellFactory(new Callback<ListView<Commentate>, ListCell<Commentate>>() {
    @Override
    public ListCell<Commentate> call(ListView<Commentate> param) {
        // Aquí es donde se crea la celda nueva
        return new CommentCell();
    }
});
        // 2. Load dummy data for testing
        loadTestData();
    }    

  private void loadTestData() {
        long now = System.currentTimeMillis();
        Timestamp currentTimestamp = new Timestamp(now);

        // Adding test objects
        listViewComments.getItems().add(new Commentate(1, 101, "¡Me ha encantado este libro!", currentTimestamp, 5, "Mikel (Teacher)"));
        listViewComments.getItems().add(new Commentate(2, 101, "Un poco lento al principio.", currentTimestamp, 3, "Ana García"));
        listViewComments.getItems().add(new Commentate(3, 101, "El envío fue muy rápido.", currentTimestamp, 4, "Jon Pérez"));
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        showCommentForm(null);
    }

    @FXML
    private void handleModify(ActionEvent event) {
        Commentate selected = listViewComments.getSelectionModel().getSelectedItem();
        if (selected != null){
         showCommentForm(selected);   
        }else {
            showAlert("Seleccione un comentario para modificar", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Commentate selectedComment = listViewComments.getSelectionModel().getSelectedItem();
        if (selectedComment == null){
            showAlert("Por favor, selecciona un comentario de la lista", Alert.AlertType.WARNING);
            return;
    }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Borrar comentario");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Estás seguro de que quieres eliminar el comentario?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            listViewComments.getItems().remove(selectedComment);
            
            showAlert("Comentario borrado con exito.", Alert.AlertType.INFORMATION);
            
        }
        
    }
    
    private void showAlert(String message, Alert.AlertType type){
        Alert alert = new Alert(type);
        alert.setTitle("Gestión de librería");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    
    private void showCommentForm(Commentate comment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CommentFormView.fxml"));
            Parent root = loader.load();

            CommentFormViewController controller = loader.getController();
            controller.setComment(comment);

            Stage stage = new Stage();
            stage.setTitle(comment == null ? "Nuevo Comentario" : "Editar Comentario");
            stage.initModality(Modality.WINDOW_MODAL); 
            stage.initOwner(listViewComments.getScene().getWindow()); 
            stage.setScene(new Scene(root));
            stage.showAndWait(); 

            if (controller.isSaveClicked()) {
                Commentate result = controller.getComment();
                if (comment == null) {
                    listViewComments.getItems().add(result);
                } else {
                    listViewComments.refresh();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error al abrir la ventana: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
}
