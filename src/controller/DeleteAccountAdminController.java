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
import model.User;

public class DeleteAccountAdminController implements Initializable{

    private static final Logger LOGGER = Logger.getLogger(DeleteAccountAdminController.class.getName());

    private Stage stage;
    private final ClassDAO dao = new DBImplementation(); 

    // --- CORRECCIÓN: El nombre debe coincidir con fx:id="ComboBoxUser" del FXML ---
    @FXML
    private ComboBox<User> ComboBoxUser; 
    
    @FXML
    private TextField TextFieldPassword;
    @FXML
    private Button Button_Delete;
    @FXML
    private Button Button_Cancel;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Inicializando ventana Delete Account Admin...");

        // 1. CARGAR USUARIOS
        cargarUsuarios();

        // 2. CONFIGURACIÓN INICIAL
        Button_Delete.setDisable(true);

        // --- CORRECCIÓN: Usamos la variable correcta 'ComboBoxUser' ---
        if (ComboBoxUser != null) {
            ComboBoxUser.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> validarBoton());
        }
        
        TextFieldPassword.textProperty().addListener((obs, oldVal, newVal) -> validarBoton());

    }

    private void cargarUsuarios() {
        try {
            List<User> listaUsuarios = dao.getAllUsers();
            System.out.println(listaUsuarios.toString());
            
            // --- CORRECCIÓN: Usamos 'ComboBoxUser' ---
            // Aseguramos que la lista no sea nula para evitar errores
            if (listaUsuarios != null) {
                ComboBoxUser.setItems(FXCollections.observableArrayList(listaUsuarios));
            }
            
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al cargar los usuarios", ex);
            showAlert("Error", "No se pudieron cargar los usuarios: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void validarBoton() {
        // --- CORRECCIÓN: Usamos 'ComboBoxUser' ---
        boolean usuarioSeleccionado = ComboBoxUser.getSelectionModel().getSelectedItem() != null;
        boolean contrasenaEscrita = !TextFieldPassword.getText().trim().isEmpty();
        Button_Delete.setDisable(!(usuarioSeleccionado && contrasenaEscrita));
    }

    @FXML
    private void delete(ActionEvent event) {
        LOGGER.info("Intentando borrar usuario desde Admin...");

        // --- CORRECCIÓN: Usamos 'ComboBoxUser' ---
        User usuarioSeleccionado = ComboBoxUser.getSelectionModel().getSelectedItem();
        String passwordEscrita = TextFieldPassword.getText();

        if (usuarioSeleccionado == null) return;

        // Validación de contraseña (aquí comparas con la del usuario a borrar)
        if (!usuarioSeleccionado.getPassword().equals(passwordEscrita)) {
            showAlert("Error de Contraseña", "La contraseña introducida no coincide con la del usuario.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                "¿Estás seguro de que deseas ELIMINAR al usuario: " + usuarioSeleccionado.getName() + "?", 
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            try {
                // LLamada al método correcto dropOutUser que implementaste en DBImplementation
                dao.dropOutUser(usuarioSeleccionado);
                
                showAlert("Éxito", "Usuario eliminado correctamente.", Alert.AlertType.INFORMATION);

                cargarUsuarios(); // Recargar lista
                TextFieldPassword.clear();

            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error al borrar en BD", ex);
                showAlert("Error", "No se pudo borrar el usuario.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        try {
            // 1. Cargar la vista directamente (sin instanciar el loader manualmente)
            // Esto crea el controlador de OptionsAdmin automáticamente
            Parent root = FXMLLoader.load(getClass().getResource("/view/OptionsAdmin.fxml"));

            // 2. Obtener el escenario (Stage) actual usando el botón 'Cancel'
            // Esto es más seguro que usar 'this.stage' si alguna vez es null
            Stage currentStage = (Stage) Button_Cancel.getScene().getWindow();

            // 3. Cambiar la escena
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