package controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import model.UserSession;

public class MenuWindowController {

    private static final Logger LOGGER = Logger.getLogger(MenuWindowController.class.getName());

    // --- Botones del FXML ---
    @FXML
    private Button btnModifyProfile;
    @FXML
    private Button btnDeleteAccount;
    @FXML
    private Button btnHistory;
    @FXML
    private Button btnBack;

    /**
     * Maneja la acción de modificar perfil.
     * Pasa el evento para recuperar el Stage dinámicamente.
     */
    @FXML
    private void handleModifyAction(ActionEvent event) {
        openWindow("/view/ModifyWindow.fxml", "Modificar Perfil", event);
    }

    @FXML
    private void handleHistoryAction(ActionEvent event) {
        openWindow("/view/ShoppingHistory.fxml", "Mi Historial", event);
    }

    @FXML
    private void handleDeleteAction(ActionEvent event) {
        openWindow("/view/DeleteAccount.fxml", "Borrar Cuenta", event);
    }

    @FXML
    private void handleBackAction(ActionEvent event) {
        openWindow("/view/MainBookStore.fxml", "Tienda de Libros", event);
    }

    /**
     * Carga la nueva ventana usando el Stage obtenido del evento.
     * Esto evita el NullPointerException al no depender de un atributo Stage nulo.
     */
    private void openWindow(String fxmlPath, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            MainBookStoreController main = loader.getController();
            main.headerController.setMode(UserSession.getInstance().getUser(), null);
            // Obtenemos el Stage actual directamente desde el nodo que disparó el evento
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al abrir la ventana: " + fxmlPath, ex);
        }
    }
}