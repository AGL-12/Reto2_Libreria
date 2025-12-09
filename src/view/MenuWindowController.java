/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import static com.sun.org.apache.bcel.internal.Repository.instanceOf;
import controller.Controller;
import java.io.IOException;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Admin;
import model.Profile;
import model.User;

/**
 * Controller for the main Menu window.
 * Handles navigation to modify, delete, and logout actions.
 */
public class MenuWindowController implements Initializable {

    @FXML
    private Button Button_Delete;

    @FXML
    private Button Button_Modify;

    @FXML
    private Button Button_LogOut;

    @FXML
    private Label label_Username;

    private Profile profile;
    private Controller cont;

    public void setUsuario(Profile profile) {
        this.profile = profile;
        label_Username.setText(profile.getUsername());
    }

    public void setCont(Controller cont) {
        this.cont = cont;
    }

    public Controller getCont() {
        return cont;
    }

    /**
     * Opens the Modify window.
     */
    @FXML
    private void modifyVentana(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/ModifyWindow.fxml"));
            javafx.scene.Parent root = fxmlLoader.load();

            view.ModifyWindowController controllerWindow = fxmlLoader.getController();
            controllerWindow.setProfile(profile);
            controllerWindow.setCont(this.cont);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) Button_Modify.getScene().getWindow();
            currentStage.close();

        } catch (IOException ex) {
            Logger.getLogger(MenuWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Opens the Delete Account window depending on profile type.
     * Users open DeleteAccount; Admins open DeleteAccountAdmin.
     */
    @FXML
    private void delete() {
        try {
            FXMLLoader fxmlLoader;
            if (profile instanceof User) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/view/DeleteAccount.fxml"));
                javafx.scene.Parent root = fxmlLoader.load();
                view.DeleteAccountController controllerWindow = fxmlLoader.getController();
                controllerWindow.setProfile(profile);
                controllerWindow.setCont(cont);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
                Stage currentStage = (Stage) Button_Delete.getScene().getWindow();
                currentStage.close();

            } else if (profile instanceof Admin) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/view/DeleteAccountAdmin.fxml"));
                javafx.scene.Parent root = fxmlLoader.load();
                view.DeleteAccountAdminController controllerWindow = fxmlLoader.getController();
                controllerWindow.setProfile(profile);
                controllerWindow.setCont(cont);
                controllerWindow.setComboBoxUser();

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
                Stage currentStage = (Stage) Button_Delete.getScene().getWindow();
                currentStage.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(MenuWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Closes the current window (used for logout).
     */
    @FXML
    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialization logic if needed
    }
}
