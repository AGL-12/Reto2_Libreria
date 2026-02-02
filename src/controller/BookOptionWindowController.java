package controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controlador de la ventana de gestión del modo del Crud de libros.
 * Permite elegir entre crear y modificar libros en la proxima ventana.
 * Es una ventana intermedia
 * @author unai azkorra
 * @version 1.0
 */
public class BookOptionWindowController {

    // CAMBIO 1: Nombres de variables coincidentes con fx:id del FXML
    @FXML
    private Button btnReturn;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnModify;

    /**
     * metodo que se lanza al presionar el boton de añadir libro
     * @param event 
     */
    @FXML
    private void createBook(ActionEvent event) {
        abrirCRUD("create", event);
    }

    /**
     * metodo que se lanza al presionar el botn de modificar libro
     * @param event 
     */
    @FXML
    private void modifyBook(ActionEvent event) {
        abrirCRUD("modify", event);
    }

    /**
     * Acción para el botón "Eliminar Libro". Abre la ventana CRUD en modo
     * "delete".
     */
    @FXML
    private void deleteBook(ActionEvent event) {
        abrirCRUD("delete", event);
    }

    /**
     * metodo que se usa para abrir la ventana de CrudBook 
     * @param modo actualiza la interfaz dependiendo el modo
     * @param event 
     */
    private void abrirCRUD(String modo, ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookCRUDWindow.fxml"));
            Parent root = fxmlLoader.load();

            BookCRUDWindowController controllerWindow = fxmlLoader.getController();

            // PASAmos el modo
            controllerWindow.setModo(modo);           

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestión de Libros - " + modo.toUpperCase());
            stage.show();

            // Cerrar la ventana actual
            Stage currentStage = (Stage) btnAdd.getScene().getWindow();
            // Cerrar ventana actual de forma segura
            currentStage.close();

        } catch (IOException ex) {
            Logger.getLogger(BookOptionWindowController.class.getName()).log(Level.SEVERE, "Error al abrir BookCRUDWindow", ex);
        }
    }

    // --- CORRECCIÓN AQUÍ ---
    /**
     * metodo para retroceder en el flujo de ventanas
     * @param event 
     */
    @FXML
    private void Return(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/OptionsAdmin.fxml"));
            Parent root = fxmlLoader.load();
            
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) btnReturn.getScene().getWindow();
            currentStage.close();

        } catch (IOException ex) {
            Logger.getLogger(BookOptionWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
