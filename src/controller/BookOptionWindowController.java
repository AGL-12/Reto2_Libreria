package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import model.Profile;

/**
 * Controlador para el menú de opciones de Libro.
 * Corregido para coincidir con los IDs y onAction del FXML.
 */
public class BookOptionWindowController implements Initializable {

    // CAMBIO 1: Nombres de variables coincidentes con fx:id del FXML
    @FXML
    private Button btnReturn;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnModify;
    @FXML
    private Button btnDelete;

    private Profile profile; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicialización
    }

    @FXML
    private void createBook(ActionEvent event) {
        abrirCRUD("create");
    }

    @FXML
    private void modifyBook(ActionEvent event) {
        abrirCRUD("modify");
    }

    @FXML
    private void deleteBook(ActionEvent event) {
        abrirCRUD("delete");
    }

    // CAMBIO 2: El nombre del método debe ser EXACTAMENTE "Return" como pusiste en el FXML
    @FXML
    private void Return(ActionEvent event) {
        try {
            // Volver al menú de Admin
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/OptionsAdmin.fxml"));
            Parent root = fxmlLoader.load();
            
            // Si OptionsAdminController necesita el perfil, recupéralo de la sesión o pásalo aquí
            // OptionsAdminController controller = fxmlLoader.getController();
            // controller.setProfile(...);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            // Cerrar la ventana actual usando el botón correcto
            Stage currentStage = (Stage) btnReturn.getScene().getWindow();
            currentStage.close();

        } catch (IOException ex) {
            Logger.getLogger(BookOptionWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void abrirCRUD(String modo) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookCRUDWindow.fxml"));
            Parent root = fxmlLoader.load();

            BookCRUDWindowController controllerWindow = fxmlLoader.getController();
            controllerWindow.setModo(modo);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestión de Libros - " + modo.toUpperCase());
            stage.show();

            // Cerrar ventana actual
            Stage currentStage = (Stage) btnAdd.getScene().getWindow();
            currentStage.close();

        } catch (IOException ex) {
            Logger.getLogger(BookOptionWindowController.class.getName()).log(Level.SEVERE, "Error al abrir BookCRUDWindow", ex);
        }
    }
}