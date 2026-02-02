package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Admin;
import model.DBImplementation;
import model.Profile;
import model.User;
import model.UserSession;

/**
 * Controlador de la ventana de modificar los datos de los usuarios
 * es una ventana que se puede acceder desde usuario y administrador pero con diferente interface
 * @author unai azkorra
 * @version 1.0
 */
public class ModifyWindowController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ModifyWindowController.class.getName());
    private DBImplementation db = new DBImplementation(); 
    private Profile profileToModify;

    @FXML private Label LabelUsername;
    @FXML private Label LabelEmail;
    @FXML private TextField TextField_Name;
    @FXML private TextField TextField_Surname;
    @FXML private TextField TextField_Telephone;
    @FXML private PasswordField TextField_NewPass;
    @FXML private PasswordField TextField_CNewPass;
    @FXML private ComboBox<User> comboUsers;

    /**
     * metodo que se usa para inicializar la interfaz con los datos necesarios
     * se cargan los usuarios en el comboBox en la parte de Administrador
     * @param location
     * @param resources 
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Profile loggedProfile = UserSession.getInstance().getUser();

        if (loggedProfile instanceof Admin) {
            comboUsers.setVisible(true);
            comboUsers.setManaged(true);
            
            try {
                ObservableList<User> users = FXCollections.observableArrayList(db.getAllUsers());
                comboUsers.setItems(users);
            } catch (Exception e) {
                LOGGER.severe("Error al cargar usuarios: " + e.getMessage());
            }

            comboUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    fillUserData(newVal);
                }
            });
        } else {
            comboUsers.setVisible(false);
            comboUsers.setManaged(false);
            fillUserData(loggedProfile);
        }
    }

    /**
     * metodo para rellenar automaticamente los datos de usuario
     * @param p usuario seleccionado o loggeado
     */
    private void fillUserData(Profile p) {
        this.profileToModify = p;
        LabelUsername.setText(p.getUsername());
        LabelEmail.setText(p.getEmail());
        TextField_Name.setText(p.getName());
        TextField_Surname.setText(p.getSurname());
        TextField_Telephone.setText(p.getTelephone());
    }

    /**
     * metodo para guardar todos los cambios realizados en el perfil selecionado
     * @param event 
     */
    @FXML
    private void save(ActionEvent event) {
        if (profileToModify == null) return;

        String pass = TextField_NewPass.getText();
        if (!pass.isEmpty()) {
            if (!pass.equals(TextField_CNewPass.getText())) {
                new Alert(Alert.AlertType.ERROR, "Las contraseñas no coinciden").show();
                return;
            }
            profileToModify.setPassword(pass);
        }

        profileToModify.setName(TextField_Name.getText());
        profileToModify.setSurname(TextField_Surname.getText());
        profileToModify.setTelephone(TextField_Telephone.getText());

        try {
            db.modificarUser(profileToModify);
            
            if (profileToModify.getUsername().equals(UserSession.getInstance().getUser().getUsername())) {
                UserSession.getInstance().setUser(profileToModify);
            }

            new Alert(Alert.AlertType.INFORMATION, "Usuario actualizado correctamente").show();
            handleNavigation(event); 

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Error al guardar los cambios").show();
        }
    }

    /**
     * metodo para salir de la ventana de modificar 
     * que abre un metodo para abrir la ventana previa a la que se encontraba el usuario/adminsitrador
     * @param event 
     */
    @FXML
    private void cancel(ActionEvent event) {
        handleNavigation(event);
    }

    /**
     * metodo para abrir la ventana previa despues de haber presionado el boton de cancelar
     * @param event 
     */
    private void handleNavigation(ActionEvent event) {
        try {
            Profile loggedProfile = UserSession.getInstance().getUser();
            //depende si es admin o usuario va a una ventana diferente
            String fxmlPath = (loggedProfile instanceof Admin) ? "/view/OptionsAdmin.fxml" : "/view/MenuWindow.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error en la navegación", ex);
        }
    }
}