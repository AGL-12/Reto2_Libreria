package controller;

import exception.MyFormException;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.ClassDAO;
import model.DBImplementation;
import model.Profile;
import model.UserSession;
import util.UtilGeneric;

/**
 * Controller for the Login window. Handles user login and navigation to the
 * main menu or signup window.
 */
public class LogInWindowController {

    @FXML
    private GridPane loginRoot;

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
            stage.setTitle("Book&Bugs - Registro");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/Book&Bugs_Logo.png")));
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

        labelIncorrecto.setText("Conectando...");
        Button_LogIn.setDisable(true);

        new Thread(() -> {
            try {
                // Esta llamada ahora puede lanzar MyFormException si hay timeout
                Profile profileEncontrado = dao.logIn(username, password);

                Platform.runLater(() -> {
                    Button_LogIn.setDisable(false);
                    if (profileEncontrado != null) {
                        UserSession.getInstance().setUser(profileEncontrado);
                        OpenMain();
                    } else {
                        labelIncorrecto.setText("Usuario/Pass incorrecto");
                        labelIncorrecto.setStyle("-fx-text-fill: red;");
                    }
                });

            } catch (MyFormException e) {
                // AQUÍ CAPTURAMOS EL ERROR DE SATURACIÓN
                Platform.runLater(() -> {
                    Button_LogIn.setDisable(false);
                    // Mostramos el mensaje "El servidor está ocupado" en rojo
                    labelIncorrecto.setText(e.getMessage());
                    labelIncorrecto.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    
                    // Opcional: Mostrar alerta visual
                    util.UtilGeneric.getInstance().showAlert(e.getMessage(), javafx.scene.control.Alert.AlertType.WARNING, "Servidor Saturado");
                });
            }
        }).start();
    }

    /**
     * Metodo para volver a MainBookController.java
     */
    @FXML

    private void backToMain(ActionEvent event) {
        OpenMain();
    }

    public void OpenMain() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MainBookStore.fxml"));
            Parent root = fxmlLoader.load();

            MainBookStoreController mainUser = fxmlLoader.getController();
            mainUser.headerController.setMode(UserSession.getInstance().getUser(), null);

            Stage stage = new Stage();
            stage.setTitle("Book&Bugs");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/Book&Bugs_Logo.png")));
            stage.setScene(new Scene(root));

            stage.sizeToScene();
            stage.centerOnScreen();

            stage.show();

            // Cerramos la ventana actual
            Stage currentStage = (Stage) Button_LogIn.getScene().getWindow();
            currentStage.close();

        } catch (IOException ex) {
            Logger.getLogger(LogInWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
