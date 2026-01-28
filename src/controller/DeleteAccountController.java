package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.DBImplementation;
import model.Profile;
import model.UserSession;

public class DeleteAccountController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DeleteAccountController.class.getName());
    private DBImplementation db = new DBImplementation();

    @FXML
    private Label LabelUsername;
    @FXML
    private TextField TextFieldPassword;
    @FXML
    private Button Button_Cancel;
    @FXML
    private Button Button_Delete;

    /**
     * Se ejecuta al abrir la ventana.
     * AQUÍ ESTÁ LO QUE PEDÍAS: Carga el nombre del usuario en el Label.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Profile currentUser = UserSession.getInstance().getUser();
        if (currentUser != null) {
            LabelUsername.setText(currentUser.getUsername());
        }
    }

    /**
     * Acción para borrar la cuenta propia.
     */
    @FXML
    private void handleDelete(ActionEvent event) {
        Profile currentUser = UserSession.getInstance().getUser();
        String password = TextFieldPassword.getText();

        // 1. Verificar que la contraseña escrita coincida con la del usuario logueado
        if (currentUser != null && currentUser.getPassword().equals(password)) {
            try {
                // 2. Llamada a la base de datos para borrar el perfil
                // Asegúrate de tener este método en tu DBImplementation (similar al de admin)
                db.dropOutUser(currentUser); 

                // 3. Cerrar la sesión local
                UserSession.getInstance().setUser(null);

                // 4. Mostrar mensaje y redirigir al Login
                showAlert("Cuenta eliminada", "Tu cuenta ha sido borrada correctamente. Hasta pronto.", Alert.AlertType.INFORMATION);
                
                // Navegar al Login
                navigateTo(event, "/view/LogInWindow.fxml", "Login - Book&Bugs");

            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error al eliminar la cuenta", ex);
                showAlert("Error", "No se pudo eliminar la cuenta. Inténtalo de nuevo.", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Error", "La contraseña introducida es incorrecta.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Acción para cancelar y volver al menú.
     */
    @FXML
    private void cancel(ActionEvent event) {
        navigateTo(event, "/view/MenuWindow.fxml", "Mi Menú");
    }

    /**
     * Método auxiliar para navegar reutilizando el Stage del evento (evita NullPointerException).
     */
    private void navigateTo(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Obtener el Stage desde el botón que disparó el evento
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error de navegación a " + fxmlPath, ex);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}