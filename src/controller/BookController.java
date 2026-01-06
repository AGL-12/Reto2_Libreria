/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import model.Commentate;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.util.Callback;
import model.CommentModelImplementation;
import model.ICommentModel;



/**
 * FXML Controller class
 *
 * @author mikel
 */
public class BookController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger("BookController");

    @FXML
    private ListView<Commentate> listViewComments;
    @FXML
    private Button btnCreate;
    @FXML
    private Button btnModify;
    @FXML
    private Button btnDelete;

    private ICommentModel model;

@Override
    public void initialize(URL url, ResourceBundle rb) {
        model = new CommentModelImplementation();

        listViewComments.setCellFactory(new Callback<ListView<Commentate>, ListCell<Commentate>>() {
            @Override
            public ListCell<Commentate> call(ListView<Commentate> param) {
                return new CommentCell();
            }
        });

        refreshList();

        initContextMenu();
    }

    /**
     * Configura el menú de clic derecho (Context Menu).
     * Requerido para el 100% en controles avanzados.
     */
    private void initContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem itemEditar = new MenuItem("Modificar Comentario");
        itemEditar.setOnAction(this::handleModify);

        MenuItem itemBorrar = new MenuItem("Eliminar Comentario");
        itemBorrar.setOnAction(this::handleDelete);

        contextMenu.getItems().addAll(itemEditar, itemBorrar);
        listViewComments.setContextMenu(contextMenu);
    }

    /**
     * Refresca la lista visual pidiendo los datos actualizados al modelo.
     */
    private void refreshList() {
        try {
            listViewComments.setItems(FXCollections.observableArrayList(model.getComments()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error crítico al refrescar la lista", e);
            showAlert("Error al cargar los datos.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        showCommentForm(null);
    }

    @FXML
    private void handleModify(ActionEvent event) {
        Commentate selected = listViewComments.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showCommentForm(selected);
        } else {
            showAlert("Seleccione un comentario para modificar", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Commentate selectedComment = listViewComments.getSelectionModel().getSelectedItem();
        
        if (selectedComment == null) {
            showAlert("Por favor, selecciona un comentario de la lista", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Borrar comentario");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Estás seguro de que quieres eliminar el comentario?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            model.deleteComment(selectedComment);
            
            refreshList();
            
            LOGGER.info("Comentario borrado correctamente por usuario.");
            showAlert("Comentario borrado con éxito.", Alert.AlertType.INFORMATION);
        }
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
                    model.addComment(result);
                } 
                // MODIFICAR: Como pasamos el objeto por referencia, el modelo ya tiene los cambios en memoria.
                // Solo necesitamos refrescar la vista.
                
                refreshList();
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error IO al abrir formulario", e);
            showAlert("Error al abrir la ventana: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Gestión de librería");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}