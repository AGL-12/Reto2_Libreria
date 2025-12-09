/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import controller.Controller;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import model.DBImplementation;
import model.Profile;

/**
 * Controller for the Login window.
 * Handles user login and navigation to the main menu or signup window.
 */
public class LogInWindowController implements Initializable {

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
    private Controller cont = new Controller(new DBImplementation());

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

            view.SignUpWindowController controllerWindow = fxmlLoader.getController();
            controllerWindow.setCont(cont);

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
        if (username.equals("") || password.equals("")) {
            labelIncorrecto.setText("Please fill in both fields.");
        } else {
            Profile profile = cont.logIn(username, password);
            if (profile != null) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MenuWindow.fxml"));
                    Parent root = fxmlLoader.load();

                    view.MenuWindowController controllerWindow = fxmlLoader.getController();
                    controllerWindow.setUsuario(profile);
                    controllerWindow.setCont(cont);

                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.show();

                    Stage currentStage = (Stage) Button_LogIn.getScene().getWindow();
                    currentStage.close();

                } catch (IOException ex) {
                    Logger.getLogger(LogInWindowController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                labelIncorrecto.setText("The username and/or password are incorrect.");
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialization logic if needed
    }
}
