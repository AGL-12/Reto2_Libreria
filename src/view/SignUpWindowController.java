/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import controller.Controller;
import javafx.scene.control.ToggleGroup;

import exception.passwordequalspassword;

import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
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
import javafx.stage.Stage;
import model.Profile;

/**
 * Controller for the SignUp window.
 * Handles user registration and navigation to login or main menu.
 */
public class SignUpWindowController implements Initializable {

    @FXML
    private TextField textFieldEmail, textFieldName, textFieldSurname, textFieldTelephone;
    @FXML
    private TextField textFieldCardN, textFieldPassword, textFieldCPassword, textFieldUsername;
    @FXML
    private RadioButton rButtonM, rButtonW, rButtonO;
    @FXML
    private Button buttonSignUp, buttonLogIn;

    private Controller cont;
    private ToggleGroup grupOp;

    public void setCont(Controller cont) {
        this.cont = cont;
    }

    /**
     * Navigates back to login window.
     */
    @FXML
    private void login() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/LogInWindow.fxml"));
            Parent root = fxmlLoader.load();
            view.LogInWindowController controllerWindow = fxmlLoader.getController();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            Stage currentStage = (Stage) buttonLogIn.getScene().getWindow();
            currentStage.close();
        } catch (IOException ex) {
            Logger.getLogger(SignUpWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Signs up a new user and navigates to MenuWindow if successful.
     */
    @FXML
    private void signup() throws passwordequalspassword {
        String email = textFieldEmail.getText();
        String name = textFieldName.getText();
        String surname = textFieldSurname.getText();
        String telephone = textFieldTelephone.getText();
        String cardN = textFieldCardN.getText();
        String pass = textFieldPassword.getText();
        String passC = textFieldCPassword.getText();
        String username = textFieldUsername.getText();
        String gender = null;

        if (rButtonM.isSelected()) gender = "Man";
        else if (rButtonW.isSelected()) gender = "Woman";
        else if (rButtonO.isSelected()) gender = "Other";

        if (!pass.equals(passC)) throw new passwordequalspassword("No son iguales las contraseñas");

        if (cont.signUp(gender, cardN, username, pass, email, name, telephone, surname)) {
            Profile profile = cont.logIn(username, pass);
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MenuWindow.fxml"));
                Parent root = fxmlLoader.load();
                view.MenuWindowController controllerWindow = fxmlLoader.getController();
                controllerWindow.setUsuario(profile);
                controllerWindow.setCont(this.cont);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
                Stage currentStage = (Stage) buttonSignUp.getScene().getWindow();
                currentStage.close();
            } catch (IOException ex) {
                Logger.getLogger(SignUpWindowController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        grupOp = new ToggleGroup();
        rButtonM.setToggleGroup(grupOp);
        rButtonW.setToggleGroup(grupOp);
        rButtonO.setToggleGroup(grupOp);
    }
}
