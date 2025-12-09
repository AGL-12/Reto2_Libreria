/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import controller.Controller;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleGroup;

import exception.passwordequalspassword;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.Stage;
import model.Profile;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

/**
 * FXML Controller class for deleting user accounts as an Admin.
 */
public class DeleteAccountAdminController implements Initializable {

    @FXML
    private ComboBox<String> ComboBoxUser; // ComboBox with all users

    @FXML
    private TextField TextFieldPassword; // Password field for confirmation

    private Controller cont; // Controller to handle business logic
    private Profile profile; // Currently logged-in admin

    @FXML
    private Button Button_Cancel; // Button to cancel the action
    @FXML
    private Button Button_Delete; // Button to delete selected user

    // Set the controller instance
    public void setCont(Controller cont) {
        this.cont = cont;
    }

    // Set the current admin profile
    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    // Populate the ComboBox with users from the controller
    public void setComboBoxUser() {
        List<String> users = cont.comboBoxInsert();
        ComboBoxUser.getItems().clear();
        ComboBoxUser.getItems().addAll(users);
    }

    // Cancel button action: returns to MenuWindow
    @FXML
    private void cancel() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/MenuWindow.fxml"));
            javafx.scene.Parent root = fxmlLoader.load();

            view.MenuWindowController controllerWindow = fxmlLoader.getController();
            controllerWindow.setUsuario(profile);
            controllerWindow.setCont(this.cont);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();

            // Close current window
            Stage currentStage = (Stage) Button_Cancel.getScene().getWindow();
            currentStage.close();

        } catch (IOException ex) {
            Logger.getLogger(MenuWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Delete button action: deletes the selected user
    @FXML
    private void delete() {
        if (TextFieldPassword.getText().isEmpty()) {
            javafx.scene.control.Alert error = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("Password required");
            error.setContentText("Please enter your password to delete the account.");
            error.showAndWait();
            return;
        }
        
        if (ComboBoxUser.getValue() == null || ComboBoxUser.getValue().isEmpty()) {
            javafx.scene.control.Alert error = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("User not selected");
            error.setContentText("Please select a user to delete.");
            error.showAndWait();
            return;
        }
        
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete account");
        alert.setHeaderText("Are you sure you want to delete this account?");
        alert.setContentText("This action cannot be undone.");

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            try {
                String userToDelete = ComboBoxUser.getValue();
                String adminPassword = TextFieldPassword.getText();
                String adminUsername = profile.getUsername();
                
                Boolean success = cont.dropOutAdmin(userToDelete, adminUsername, adminPassword);
                if (success) {
                    javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Deleted account");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("The account has been successfully deleted.");
                    successAlert.showAndWait();
                    
                    try {
                        javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/MenuWindow.fxml"));
                        javafx.scene.Parent root = fxmlLoader.load();

                        view.MenuWindowController controllerWindow = fxmlLoader.getController();
                        controllerWindow.setUsuario(profile);
                        controllerWindow.setCont(this.cont);
                        javafx.stage.Stage stage = new javafx.stage.Stage();
                        stage.setScene(new javafx.scene.Scene(root));
                        stage.show();
                        Stage currentStage = (Stage) Button_Cancel.getScene().getWindow();
                        currentStage.close();

                    } catch (IOException ex) {
                        Logger.getLogger(MenuWindowController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    javafx.scene.control.Alert error = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText("Incorrect password");
                    error.setContentText("The password is incorrect. Please try again.");
                    error.showAndWait();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.scene.control.Alert error = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("The account could not be deleted.");
                error.setContentText(ex.getMessage());
                error.showAndWait();
            }
        } else {
            System.out.println("Deletion cancelled by the user.");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialization logic can be added here if needed
    }
}
