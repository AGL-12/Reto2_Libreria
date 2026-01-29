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
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MenuWindowController {

    private static final Logger LOGGER = Logger.getLogger(MenuWindowController.class.getName());

    private Stage stage;

    // --- Botones del FXML ---
    @FXML
    private Button btnModifyProfile;
    @FXML
    private Button btnDeleteAccount;
    @FXML
    private Button btnHistory;
    @FXML
    private Button btnBack;
    @FXML
    private Label label_Username;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Inicialización simple: NO necesita recibir el usuario por parámetro.
     */
    public void initStage(Parent root) {
        LOGGER.info("Inicializando Menú de Usuario...");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Mi Perfil - Book&Bugs");
        stage.setResizable(false);
        stage.show();
    }

    // --- Navegación: Simplemente llaman a openWindow ---

    @FXML
    private void handleModifyAction(ActionEvent event) {
        openWindow("/view/ModifyWindow.fxml", "Modificar Perfil");
    }

    @FXML
    private void handleHistoryAction(ActionEvent event) {
        openWindow("/view/ShoppingHistory.fxml", "Mi Historial");
    }

    @FXML
    private void handleDeleteAction(ActionEvent event) {
        openWindow("/view/DeleteAccount.fxml", "Borrar Cuenta");
    }

    @FXML
    private void handleBackAction(ActionEvent event) {
        // Vuelve al MainBookStore sin preocuparse del controlador
        openWindow("/view/MainBookStore.fxml", "Tienda de Libros");
    }

    // --- Método Auxiliar TOTALMENTE INDEPENDIENTE ---
    
    /**
     * Carga la nueva ventana usando el MISMO Stage, sin interactuar con su controlador.
     */
    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Cambiamos la escena del stage actual
            Scene scene = new Scene(root);
            this.stage.setScene(scene);
            this.stage.setTitle(title);
            
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al abrir la ventana: " + fxmlPath, ex);
        }
    }
}