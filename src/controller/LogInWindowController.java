package controller;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.ClassDAO;
import model.DBImplementation;
import model.Profile;

/**
 * Controller for the Login window. Handles user login and navigation to the
 * main menu or signup window.
 */
public class LogInWindowController {

    @FXML
    private TextField TextField_Username;

    @FXML
    private PasswordField PasswordField_Password;

    @FXML
    private Button Button_LogIn;

    @FXML
    private Button Button_SignUp;

    @FXML
    private Label labelIncorrecto;
    
    private final ClassDAO dao = new DBImplementation();

    /**
     * Opens the SignUp window.
     */
    @FXML
    private void signUp() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/SignUpWindow.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("SignUp");
            stage.setScene(new Scene(root));
            stage.show();

            // Close current window
            Stage currentStage = (Stage) Button_SignUp.getScene().getWindow();
            currentStage.close();
        } catch (IOException ex) {
            Logger.getLogger(LogInWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Attempts to log in the user. If successful, opens MenuWindow; otherwise,
     * shows an error.
     */
    @FXML
    private void logIn() {
        String username = TextField_Username.getText();
        String password = PasswordField_Password.getText();
        if (username.isEmpty() || password.isEmpty()) {
            labelIncorrecto.setText("Rellene ambos campos.");
            return;
        }
        // Llamada SÍNCRONA (pero muy rápida porque Hibernate tarda milisegundos)
        // El retardo de 30s ocurre en un hilo fantasma que no vemos.
        Profile profile = dao.logIn(username, password);

        if (profile != null) {
            abrirMenu(profile);
        } else {
            labelIncorrecto.setText("Incorrecto");
        }
    }

    @FXML
    private void backToMain(ActionEvent event) {
        try {
            // 1. Cargamos la vista GRANDE (Main)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainBookStore.fxml")); // O el nombre de tu FXML principal
            Parent root = loader.load();

            // Aquí recuperas el controlador del Main si necesitas pasarle datos de vuelta
            // HeaderController mainController = loader.getController();
            // mainController.setControl(Control);
            Stage oldStage = (Stage) Button_LogIn.getScene().getWindow();
            Stage newStage = new Stage();

            // 2. IMPORTANTE: Volvemos al estilo con barra de título y botones
            newStage.initStyle(StageStyle.DECORATED);

            newStage.setScene(new Scene(root));
            newStage.sizeToScene();

            // 3. Centramos en pantalla
            newStage.centerOnScreen();

            newStage.show();
            oldStage.close();
        } catch (IOException ex) {
            Logger.getLogger(LogInWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void abrirMenu(Profile profile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MenuWindow.fxml"));
            Parent root = fxmlLoader.load();

            MenuWindowController controllerWindow = fxmlLoader.getController();
            controllerWindow.setUsuario(profile);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            // Cerramos la ventana actual
            Stage currentStage = (Stage) Button_LogIn.getScene().getWindow();
            currentStage.close();

        } catch (IOException ex) {
            Logger.getLogger(LogInWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
