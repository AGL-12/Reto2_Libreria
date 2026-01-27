package controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.DBImplementation;
import model.User;
import model.UserSession;

public class ModifyWindowController {

    private static final Logger LOGGER = Logger.getLogger(ModifyWindowController.class.getName());

    private Stage stage;
    private DBImplementation db = new DBImplementation(); 
    private User currentUser;

    // --- Componentes FXML ---
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtLastName; // Asegúrate de que este fx:id coincida con tu FXML
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtAddress;  // Asegúrate de que este fx:id coincida con tu FXML
    @FXML
    private PasswordField pwdNewPassword;
    @FXML
    private PasswordField pwdConfirmPassword;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void initStage(Parent root) {
        LOGGER.info("Inicializando ventana de Modificar Perfil...");

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Modificar Perfil - Book&Bugs");
        stage.setResizable(false);

        // 1. OBTENER EL USUARIO DE LA SESIÓN
        currentUser = (User) UserSession.getInstance().getUser();

        // 2. RELLENAR LOS CAMPOS
        if (currentUser != null) {
            txtName.setText(currentUser.getName());
            // Si tienes estos campos en el FXML, descoméntalos:
            // txtLastName.setText(currentUser.getSurname());
            // txtEmail.setText(currentUser.getEmail());
            // txtAddress.setText(currentUser.getAddress());

            // Bloquear email
            if(txtEmail != null) {
                txtEmail.setDisable(true);
            }
        }
        stage.show();
    }

    // --- CORRECCIÓN AQUÍ: EL NOMBRE DEL MÉTODO DEBE SER 'cancel' ---
    @FXML
    private void cancel(ActionEvent event) {
        LOGGER.info("Cancelando modificación...");
        goBackToMenu();
    }

    // --- CORRECCIÓN AQUÍ: COMPRUEBA SI TU FXML LLAMA A 'save' O 'handleSaveAction' ---
    // He puesto 'save' porque suele ser lo estándar si 'cancel' falló.
    @FXML
    private void save(ActionEvent event) {
        LOGGER.info("Validando y guardando cambios de perfil...");

        if (txtName.getText().trim().isEmpty()) {
            showAlert("Error", "El nombre no puede estar vacío.", Alert.AlertType.ERROR);
            return;
        }

        String newPass = pwdNewPassword.getText();
        String confirmPass = pwdConfirmPassword.getText();

        if (!newPass.isEmpty()) {
            if (!newPass.equals(confirmPass)) {
                showAlert("Error", "Las nuevas contraseñas no coinciden.", Alert.AlertType.ERROR);
                return;
            }
            currentUser.setPassword(newPass); 
        }

        currentUser.setName(txtName.getText());
        // currentUser.setSurname(txtLastName.getText());
        // currentUser.setAddress(txtAddress.getText());

        try {
            // db.updateUser(currentUser); // Descomenta cuando tengas el método update
            
            UserSession.getInstance().setUser(currentUser); // Actualizar sesión
            showAlert("Éxito", "Perfil actualizado correctamente.", Alert.AlertType.INFORMATION);
            goBackToMenu();

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al actualizar en la BD", ex);
            showAlert("Error", "No se pudo actualizar el perfil.", Alert.AlertType.ERROR);
        }
    }

    private void goBackToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MenuWindow.fxml"));
            Parent root = loader.load();
            
            MenuWindowController controller = loader.getController();
            controller.setStage(this.stage);
            controller.initStage(root);

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al volver al menú", ex);
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