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
import model.UserSession;

/**
 * Controlador de la ventana de opciones para administrador es una ventana que
 * se puede acceder desde adminsitrador es una ventana intermedia
 *
 * @author unai azkorra
 * @version 1.0
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

    /**
     * pasa al metodo de navigacion la ruta para abrir la ventana de eliminar
     * usuario
     *
     * @param event
     */
    @FXML
    private void deleteUserWindow(ActionEvent event) {
        navigateTo("/view/DeleteAccountAdmin.fxml");
    }

    /**
     * pasa al metodo de navigacion la ruta para abrir la ventana de eliminar
     * comentario
     *
     * @param event
     */
    @FXML
    private void eliminarComentarioWindow(ActionEvent event) {
        navigateTo("/view/DeleteComentWindow.fxml");
    }

    /**
     * pasa al metodo de navigacion la ruta para abrir la ventana de modificar
     * usuario
     *
     * @param event
     */
    @FXML
    private void modificarUsuarioWindow(ActionEvent event) {
        navigateTo("/view/ModifyWindow.fxml");
    }

    /**
     * pasa al metodo de navigacion la ruta para abrir la ventana de libro
     *
     * @param event
     */
    @FXML
    private void opcionesLibroWindow(ActionEvent event) {
        navigateTo("/view/BookOptionWindow.fxml");
    }

    /**
     * pasa al metodo de navigacion la ruta para abrir la ventana principal de
     * la aplicacion
     *
     * @param event
     */
    @FXML
    private void btnVolver(ActionEvent event) {
        navigateTo("/view/MainBookStore.fxml");
    }

    /**
     * metodo para abrir la ventana correspondiente
     *
     * @param event
     * @param fxmlPath
     */
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();

            // Obtener el controlador de forma genérica
            Object controller = fxmlLoader.getController();

            // Solo si el controlador es de tipo MainBookStoreController, configuramos el header
            if (controller instanceof MainBookStoreController) {
                MainBookStoreController main = (MainBookStoreController) controller;
                main.headerController.setMode(UserSession.getInstance().getUser(), null);
            }

            // El resto del código para mostrar la ventana permanece igual
            Stage stage = (Stage) btnDeleteUser.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException ex) {
            Logger.getLogger(OptionsAdminController.class.getName()).log(Level.SEVERE, "Error navegando a " + fxmlPath, ex);
        }
    }
}
