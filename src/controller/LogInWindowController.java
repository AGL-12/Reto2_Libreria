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
import model.DBImplementation;
import model.Profile;
import threads.LoginThread;

/**
 * Controller for the Login window.
 * Handles user login and navigation to the main menu or signup window.
 */
public class LogInWindowController{

    @FXML
    private TextField TextField_Username;

    @FXML
    private PasswordField PasswordField_Password;

    @FXML
    private Button Button_LogIn;

    @FXML
    private Button Button_SignUp;

    @FXML
    private Label labelIncorrecto; // Label to show error messages

    // Controller handling business logic
    private final Controller cont = new Controller(new DBImplementation());
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
     * Attempts to log in the user.
     * If successful, opens MenuWindow; otherwise, shows an error.
     */
    @FXML
    private void logIn() {
        String username = TextField_Username.getText();
        String password = PasswordField_Password.getText();
        if (username.isEmpty() || password.isEmpty()) {
            labelIncorrecto.setText("Rellene ambos campos.");
            return;
        }
        // 1. Bloqueo visual (Para que no pulse 20 veces)
        Button_LogIn.setDisable(true);
        labelIncorrecto.setText("Conectando...");
        
        // 2. Crear y lanzar el hilo (Le pasamos 'this' para que nos pueda llamar luego)
        LoginThread hilo = new LoginThread(username, password, this);
        hilo.start();
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
    
    // Este método debe ser público para que el Hilo lo vea
    public void finalizarLogin(Profile profile) {
        
        Button_LogIn.setDisable(false); // Reactivamos botón

        if (profile != null) {
            // --- LOGIN CORRECTO: CÓDIGO DE CAMBIO DE VENTANA ---
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

        } else {
            // --- LOGIN FALLIDO ---
            labelIncorrecto.setText("Usuario o contraseña incorrectos.");
        }
    }

    // Método auxiliar para errores graves (Base de datos caída, etc)
    public void mostrarError(String mensaje) {
        Button_LogIn.setDisable(false);
        labelIncorrecto.setText("Error: " + mensaje);
    }
}
