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
 * FXML Controller class
 *
 * @author uazko
 */
public class OptionsAdminController {

    @FXML
    private Button btnDeleteUser;
    @FXML
    private Button btnEliminarComentario;
    @FXML
    private Button btnModificarUsuario;
    @FXML
    private Button btnLibro;

    @FXML
    private void deleteUserWindow(ActionEvent event) {
        navigateTo("/view/DeleteAccountAdmin.fxml");
    }

    @FXML
    private void eliminarComentarioWindow(ActionEvent event) {
        navigateTo("/view/DeleteComentWindow.fxml");
    }

    @FXML
    private void modificarUsuarioWindow(ActionEvent event) {
        navigateTo("/view/ModifyWindow.fxml");
    }

    @FXML
    private void opcionesLibroWindow(ActionEvent event) {
        navigateTo("/view/BookOptionWindow.fxml");
    }

    @FXML
    private void btnVolver(ActionEvent event) {
        navigateTo("/view/MainBookStore.fxml");
    }

    /**
     * Método auxiliar para navegar reutilizando la ventana y evitar errores.
     */
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();

            // Obtenemos el Stage directamente del elemento que disparó el evento (Botón o Hyperlink)
            Stage stage = (Stage) btnDeleteUser.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException ex) {
            Logger.getLogger(OptionsAdminController.class.getName()).log(Level.SEVERE, "Error navegando a " + fxmlPath, ex);
        }
    }
}
