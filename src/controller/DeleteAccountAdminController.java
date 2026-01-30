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

/**
 * Controlador de la ventana de eliminar usuarios siendo admin
 * Es una ventana que solo tiene acceso administrador
 * Cueenta con un ComboBox para seleccionar usuario a eliminar
 * @author unai azkorra
 * @version 1.0
 */
public class DeleteAccountAdminController implements Initializable{

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

    /**
     * Inicializa los componentes de la ventana y configura el comboBox para tener cargados los usuarios
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Inicializando ventana Delete Account Admin...");

        // 1. CARGAR USUARIOS
        cargarUsuarios();

        // 2. CONFIGURACIÓN INICIAL
        Button_Delete.setDisable(true);

        if (ComboBoxUser != null) {
            ComboBoxUser.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> validarBoton());
        }
        
        TextFieldPassword.textProperty().addListener((obs, oldVal, newVal) -> validarBoton());

    }

    /**
     * metodo para cargar los usuarios en el comboBox
     */
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

    /**
     * metodo para validar que los campos esten bien al momento de inicializar
     */
    private void validarBoton() {
        boolean usuarioSeleccionado = ComboBoxUser.getSelectionModel().getSelectedItem() != null;
        boolean contrasenaEscrita = !TextFieldPassword.getText().trim().isEmpty();
        Button_Delete.setDisable(!(usuarioSeleccionado && contrasenaEscrita));
    }

    /**
     * metodo para eliminar el usuario seleccionado
     * @param event se dispara al confirmar la eliminacion del usuario
     */
    @FXML
    private void delete(ActionEvent event) {
        LOGGER.info("Intentando borrar usuario desde Admin...");

        User usuarioSeleccionado = ComboBoxUser.getSelectionModel().getSelectedItem();
        String passwordEscrita = TextFieldPassword.getText();

        if (usuarioSeleccionado == null) return;

        // Validación de contraseña
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

    /**
     * se usa para cancelar la operacion y regresas a la ventana de operaciones que puede hacer el adminsitrador
     * @param event 
     */
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

    /**
     * se usa para avisar de psoibles errores al usuario de la aplicacion
     * @param title
     * @param message
     * @param type 
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    
}