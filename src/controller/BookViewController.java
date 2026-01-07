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

/**
 * FXML Controller class
 *
 * @author mikel
 */
public class BookViewController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        refreshList();

        initContextMenu();
    }

    /**
     * Configura el menú de clic derecho (Context Menu). Requerido para el 100%
     * en controles avanzados.
     */
    private void initContextMenu() {
        /*
        ContextMenu contextMenu = new ContextMenu();

        MenuItem itemEditar = new MenuItem("Modificar Comentario");
        itemEditar.setOnAction(this::handleModify);

        MenuItem itemBorrar = new MenuItem("Eliminar Comentario");
        itemBorrar.setOnAction(this::handleDelete);

        contextMenu.getItems().addAll(itemEditar, itemBorrar);
        listViewComments.setContextMenu(contextMenu);
        */
    }

    /**
     * Refresca la lista visual pidiendo los datos actualizados al modelo.
     */
    private void refreshList() {
    }

    @FXML
    private void handleCreate(ActionEvent event) {
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Gestión de librería");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
