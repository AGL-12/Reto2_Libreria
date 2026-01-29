package controller;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

/**
 * Controller for the Delete Account window for regular Users. This controller
 * allows a user to delete their own account.
 */
public class DeleteAccountController implements Initializable {

    // Label displaying the username of the logged-in user
    @FXML
    private Label LabelUsername;

    // TextField to enter the user's password for confirmation
    @FXML
    private TextField TextFieldPassword;

    // Buttons to cancel or execute deletion
    @FXML
    private Button Button_Cancel;
    @FXML
    private Button Button_Delete;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialization logic if needed
    }

    /**
     * Handles cancel button action. Closes the current window and returns to
     * MenuWindow.
     */
    @FXML
    private void cancel() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MenuWindow.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            Stage currentStage = (Stage) Button_Cancel.getScene().getWindow();
            currentStage.close();

        } catch (IOException ex) {
            Logger.getLogger(MenuWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles delete button action. Confirms deletion and calls the Controller
     * to remove the user account.
     */
    @FXML
    private void delete() {
        if (TextFieldPassword.getText().isEmpty()) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("Password required");
            error.setContentText("Please enter your password to delete the account.");
            error.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete account");
        alert.setHeaderText("Are you sure you want to delete your account?");
        alert.setContentText("This action cannot be undone..");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String user, password;
                user = LabelUsername.getText();
                password = TextFieldPassword.getText();
                //Boolean success = cont.dropOutUser(user, password);
                /*
                if (success) {
                    javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Deleted account");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Your account has been successfully deleted.");
                    successAlert.showAndWait();
                    
                    try {
                        javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/LogInWindow.fxml"));
                        javafx.scene.Parent root = fxmlLoader.load();

                        controller.LogInWindowController controllerWindow = fxmlLoader.getController();
                        javafx.stage.Stage stage = new javafx.stage.Stage();
                        stage.setScene(new javafx.scene.Scene(root));
                        stage.show();
                        Stage currentStage = (Stage) Button_Delete.getScene().getWindow();
                        currentStage.close();

                    } catch (IOException ex) {
                        Logger.getLogger(DeleteAccountController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    javafx.scene.control.Alert error = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText("Incorrect password");
                    error.setContentText("The password is incorrect. Please try again.");
                    error.showAndWait();
                }
                 */
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("The account could not be deleted.");
                error.setContentText(ex.getMessage());
                error.showAndWait();
            }
        }
    }
}
