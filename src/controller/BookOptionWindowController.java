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
 * Controlador para el menú de opciones de Libro (Añadir, Modificar, Eliminar).
 * Actúa como intermediario enviando el "modo" a BookCRUDWindow.
 */
public class BookOptionWindowController implements Initializable {

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
        // Inicialización si fuera necesaria
    }

    /**
     * Acción para el botón "Añadir Libro". Abre la ventana CRUD en modo
     * "create".
     */
    @FXML
    private void createBook(ActionEvent event) {
        abrirCRUD("create");
    }

    /**
     * Acción para el botón "Modificar Libro". Abre la ventana CRUD en modo
     * "modify".
     */
    @FXML
    private void modifyBook(ActionEvent event) {
        abrirCRUD("modify");
    }

    /**
     * Acción para el botón "Eliminar Libro". Abre la ventana CRUD en modo
     * "delete".
     */
    @FXML
    private void deleteBook(ActionEvent event) {
        abrirCRUD("delete");
    }

    /**
     * Método auxiliar para cargar la ventana y pasar los datos. Evita repetir
     * código en cada botón.
     */
    private void abrirCRUD(String modo) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookCRUDWindow.fxml"));
            Parent root = fxmlLoader.load();

            // Obtener el controlador de la siguiente ventana
            BookCRUDWindowController controllerWindow = fxmlLoader.getController();

            // PASAR LOS DATOS VITALES
            controllerWindow.setModo(modo);           // <--- AQUÍ PASAMOS EL MODO ("create", "modify", etc.)

            // Mostrar la nueva ventana
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestión de Libros - " + modo.toUpperCase());
            stage.show();

            // Cerrar la ventana actual (LibroOptionWindow)
            Stage currentStage = (Stage) btnAdd.getScene().getWindow();
            currentStage.close();

        } catch (IOException ex) {
            Logger.getLogger(BookOptionWindowController.class.getName()).log(Level.SEVERE, "Error al abrir BookCRUDWindow", ex);
        }
    }

    private void volver(ActionEvent event) {
        try {
            // Volver al menú de Admin (OptionsAdmin)
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

    @FXML
    private void Return(ActionEvent event) {
    }
}
