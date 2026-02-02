package controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.ClassDAO;
import model.DBImplementation;
import model.Profile;
import model.User;
import model.UserSession;

/**
 * Controlador de la ventana de eliminar usuarios siendo admin.
 * Valida la operación mediante la contraseña del administrador en sesión.
 * @author unai azkorra
 * @version 1.1
 */
public class DeleteAccountAdminController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DeleteAccountAdminController.class.getName());
    private final ClassDAO dao = new DBImplementation();

    @FXML
    private ComboBox<User> ComboBoxUser;
    @FXML
    private TextField TextFieldPassword;
    @FXML
    private Button Button_Delete;
    @FXML
    private Button Button_Cancel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarUsuarios();
    }

    private void cargarUsuarios() {
        try {
            List<User> users = dao.getAllUsers();
            ComboBoxUser.setItems(FXCollections.observableArrayList(users));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al cargar usuarios", ex);
        }
    }

    @FXML
    private void delete(ActionEvent event) {
        User selectedUser = ComboBoxUser.getSelectionModel().getSelectedItem();
        String inputPassword = TextFieldPassword.getText();
        
        // Obtenemos el perfil del administrador logueado desde la sesión
        Profile adminProfile = UserSession.getInstance().getUser();

        if (selectedUser == null) {
            showAlert("Selección vacía", "Por favor, selecciona un usuario para eliminar.", Alert.AlertType.WARNING);
            return;
        }

        // CAMBIO DE LÓGICA: Se compara con la contraseña del ADMIN en sesión
        if (adminProfile != null && adminProfile.getPassword().equals(inputPassword)) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Estás seguro de que deseas eliminar al usuario " + selectedUser.getUsername() + "?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();

            if (confirm.getResult() == ButtonType.YES) {
                try {
                    dao.dropOutUser(selectedUser);
                    showAlert("Éxito", "Usuario eliminado correctamente.", Alert.AlertType.INFORMATION);
                    cargarUsuarios();
                    TextFieldPassword.clear();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error al borrar en BD", ex);
                    showAlert("Error", "No se pudo borrar el usuario.", Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Error de autenticación", "La contraseña del administrador es incorrecta.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/OptionsAdmin.fxml"));
            Stage currentStage = (Stage) Button_Cancel.getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al volver al menú de administración", ex);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}