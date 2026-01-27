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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import model.Profile;

/**
 * Controlador para el menú de opciones de Libro.
 */
public class BookOptionWindowController implements Initializable {

    @FXML private Button btnVolver;
    @FXML private Button btnAñadirLibro;
    @FXML private Button btnModificarLibro;
    @FXML private Button btnEliminarLibro;

    private Profile profile; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Init
    }

    @FXML
    private void createBook(ActionEvent event) {
        abrirCRUD("create", event);
    }

    @FXML
    private void modifyBook(ActionEvent event) {
        abrirCRUD("modify", event);
    }

    @FXML
    private void deleteBook(ActionEvent event) {
        abrirCRUD("delete", event);
    }

    private void abrirCRUD(String modo, ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookCRUDWindow.fxml"));
            Parent root = fxmlLoader.load();

            BookCRUDWindowController controllerWindow = fxmlLoader.getController();
            controllerWindow.setModo(modo);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestión de Libros - " + modo.toUpperCase());
            stage.show();

            // Cerrar ventana actual de forma segura
            Node source = (Node) event.getSource();
            Stage currentStage = (Stage) source.getScene().getWindow();
            currentStage.close();

        } catch (IOException ex) {
            Logger.getLogger(BookOptionWindowController.class.getName()).log(Level.SEVERE, "Error al abrir BookCRUDWindow", ex);
        }
    }

    // --- CORRECCIÓN AQUÍ ---
    // El método se ha renombrado de 'volver' a 'Return' para coincidir con tu FXML
    @FXML
    private void Return(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/OptionsAdmin.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            // Cerrar ventana actual de forma segura
            Node source = (Node) event.getSource();
            Stage currentStage = (Stage) source.getScene().getWindow();
            currentStage.close();

        } catch (IOException ex) {
            Logger.getLogger(BookOptionWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}