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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import model.UserSession;

/**
 * Controlador de la ventana de opciones del usuario Es una ventana que solo
 * tiene acceso el usuario es una ventana intermedia
 *
 * @author unai azkorra
 * @version 1.0
 */
public class MenuWindowController {

    private static final Logger LOGGER = Logger.getLogger(MenuWindowController.class.getName());

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
    @FXML
    private Menu menuArchivo;
    @FXML
    private MenuItem iSalir;
    @FXML
    private Menu menuAcciones;
    @FXML
    private MenuItem iManual;
    @FXML
    private MenuItem iJasper;
    @FXML
    private Menu menuAyuda;
    @FXML
    private MenuItem iAcercaDe;

    /**
     * abre la ventana para que el usuario modifique sus propios
     */
    @FXML
    private void handleModifyAction(ActionEvent event) {
        openWindow("/view/ModifyWindow.fxml", "Modificar Perfil", null);
    }

    /**
     * abre el historial de compras del usuario
     *
     * @param event
     */
    @FXML
    private void handleHistoryAction(ActionEvent event) {
        openWindow("/view/ShoppingHistory.fxml", "Mi Historial", "shop history");
    }

    /**
     * Abre la ventana para eliminar la cuenta del usuario
     *
     * @param event
     */
    @FXML
    private void handleDeleteAction(ActionEvent event) {
        openWindow("/view/DeleteAccount.fxml", "Borrar Cuenta", null);
    }

    @FXML
    private void handleExit(ActionEvent event) {
        javafx.application.Platform.exit();
        System.exit(0);
    }

    /**
     * abre la ventana principal
     *
     * @param event
     */
    @FXML
    private void handleBackAction(ActionEvent event) {
        openWindow("/view/MainBookStore.fxml", "Tienda de Libros", null);
    }

    /**
     * Recive la ruta para abrir la siguente ventana de la ejecucion.
     *
     * @param fxmlPath
     * @param title
     * @param headermode
     */
    private void openWindow(String fxmlPath, String title, String headermode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Obtener el controlador de forma genérica
            Object controller = loader.getController();

            // Solo si el controlador es de tipo MainBookStoreController, configuramos el header
            if (controller instanceof MainBookStoreController) {
                MainBookStoreController main = (MainBookStoreController) controller;
                main.headerController.setMode(UserSession.getInstance().getUser(), null);
            }
            Stage stage = (Stage) label_Username.getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al abrir la ventana: " + fxmlPath, ex);
        }
    }

    @FXML
    private void handleReportAction(ActionEvent event) {
    }

    @FXML
    private void handleInformeTecnico(ActionEvent event) {
    }

    @FXML
    private void handleAboutAction(ActionEvent event) {
    }
}
