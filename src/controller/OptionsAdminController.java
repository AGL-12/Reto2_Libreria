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
import javafx.scene.Node; // Importante para obtener el Stage desde el evento
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import model.UserSession;

/**
 * FXML Controller class
 *
 * @author uazko
 */
public class OptionsAdminController implements Initializable {

    @FXML
    private Button btnDeleteUser;
    @FXML
    private Button btnEliminarComentario;
    @FXML
    private Button btnModificarUsuario;
    @FXML
    private Button btnLibro;
   
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO: Si quieres cargar el nombre del admin, hazlo aquí
        // UserSession.getInstance().getUser().getUsername()...
    }    

    @FXML
    private void deleteUserWindow(ActionEvent event) {
        navigateTo(event, "/view/DeleteAccountAdmin.fxml");
    }

    @FXML
    private void eliminarComentarioWindow(ActionEvent event) {
        navigateTo(event, "/view/DeleteComentWindow.fxml");
    }

    @FXML
    private void modificarUsuarioWindow(ActionEvent event) {
        navigateTo(event, "/view/ModifyWindow.fxml");
    }

    @FXML
    private void opcionesLibroWindow(ActionEvent event) {
        navigateTo(event, "/view/BookOptionWindow.fxml");
    }
    @FXML
    private void btnVolver(ActionEvent event) {
        navigateTo(event, "/view/MainBookStore.fxml");
    }

    /**
     * Método auxiliar para navegar reutilizando la ventana y evitar errores.
     */
    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();
            MainBookStoreController main = fxmlLoader.getController();
            main.headerController.setMode(UserSession.getInstance().getUser(), null);
            
            // Obtenemos el Stage directamente del elemento que disparó el evento (Botón o Hyperlink)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException ex) {
            Logger.getLogger(OptionsAdminController.class.getName()).log(Level.SEVERE, "Error navegando a " + fxmlPath, ex);
        }
    }
}