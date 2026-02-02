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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.DBImplementation;
import model.Profile;
import model.UserSession;


/**
 * Controlador de la ventana de eliminar usuarios siendo el propio usuario
 * Es una ventana que solo tiene acceso el usuario
 * @author unai azkorra
 * @version 1.0
 */
public class DeleteAccountController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DeleteAccountController.class.getName());
    private DBImplementation db = new DBImplementation();

    @FXML
    private Label LabelUsername;
    @FXML
    private TextField TextFieldPassword;
    @FXML
    private Button Button_Cancel;
    @FXML
    private Button Button_Delete;

    /**
     * al iniciar la ventana se establecen los parametros deseados antes de mostrar la interfaz al usuario
     * en este caso mostramos el "username" del usuario loggeado
     * @param location
     * @param resources 
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Profile currentUser = UserSession.getInstance().getUser();
        if (currentUser != null) {
            LabelUsername.setText(currentUser.getUsername());
        }
    }
    
    
    /**
     * Accion para auto-eliminarse la cuenta
     * @param event 
     */
    @FXML
    private void handleDelete(ActionEvent event) {
        Profile currentUser = UserSession.getInstance().getUser();
        String password = TextFieldPassword.getText();

        // Verificar que la contraseña escrita coincida con la del usuario logueado
        if (currentUser != null && currentUser.getPassword().equals(password)) {
            try {
                
                db.dropOutUser(currentUser); 

                // Cerrar la sesión del usuario
                UserSession.getInstance().setUser(null);

               
                showAlert("Cuenta eliminada", "Tu cuenta ha sido borrada correctamente. Hasta pronto.", Alert.AlertType.INFORMATION);
                
                navigateTo("/view/LogInWindow.fxml", "Login - Book&Bugs");

            } catch (Exception ex) {
                ex.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("The account could not be deleted.");
                error.setContentText(ex.getMessage());
                error.showAndWait();
                LOGGER.log(Level.SEVERE, "Error al eliminar la cuenta", ex);
                showAlert("Error", "No se pudo eliminar la cuenta. Inténtalo de nuevo.", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Error", "La contraseña introducida es incorrecta.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Accion para cancelar la operacion 
     * @param event 
     */
    @FXML
    private void cancel(ActionEvent event) {
        navigateTo("/view/MenuWindow.fxml", "Mi Menú");
    }

    /**
     * metodo para redirigir al usuario
     * @param fxmlPath indica a que ventana se le redigira
     * @param title  el titulo de la ventana a la que se le redirige
     */
    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Obtener el Stage desde el botón que disparó el evento
            Stage stage =  (Stage) LabelUsername.getScene().getWindow();
            
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error de navegación a " + fxmlPath, ex);
        }
    }

    /**
     * metodo para alertar al usuario de problemas a la hora de hacer alguna accion
     * @param title
     * @param content
     * @param type 
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}