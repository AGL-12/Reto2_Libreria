package controller;


import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Admin;
import model.DBImplementation;
import model.Profile;
import model.User;
import model.UserSession;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import util.LogInfo;

/**
 * Controlador de la ventana de modificación de perfil. Permite a los usuarios
 * actualizar sus datos personales y a los administradores gestionar la
 * información de cualquier usuario registrado en el sistema.
 *
 * * @author unai azkorra
 * @version 1.0
 */
public class ModifyWindowController implements Initializable {

    /**
     * Implementación de la lógica de acceso a datos.
     */
    private DBImplementation db = new DBImplementation();

    /**
     * Perfil que está siendo modificado actualmente.
     */
    private Profile profileToModify;

    /**
     * Menú contextual para acciones rápidas.
     */
    private ContextMenu contextMenu;

    @FXML
    private GridPane rootPane;
    @FXML
    private Label LabelUsername, LabelEmail;
    @FXML
    private TextField TextField_Name, TextField_Surname, TextField_Telephone;
    @FXML
    private PasswordField TextField_NewPass, TextField_ConfirmPass;
    @FXML
    private ComboBox<User> comboUsers;
    @FXML
    private Button Button_SaveChanges, Button_Cancel;

    /**
     * Inicializa la ventana configurando los datos del usuario, el menú
     * contextual y registrando la actividad en el log.
     *
     * * @param url Ubicación relativa para el objeto raíz.
     * @param rb Recursos para localizar el objeto raíz.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupUserData();
        initContextMenu();
        LogInfo.getInstance().logInfo("Ventana de modificación de perfil inicializada.");
    }

    /**
     * Configura la interfaz según el tipo de perfil logueado. Si es
     * Administrador, habilita el ComboBox para elegir usuarios. Si es Usuario,
     * carga directamente sus propios datos.
     */
    private void setupUserData() {
        Profile loggedProfile = UserSession.getInstance().getUser();
        if (loggedProfile instanceof Admin) {
            comboUsers.setVisible(true);
            loadUsers();
            comboUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    fillFields(newVal);
                }
            });
        } else {
            comboUsers.setVisible(false);
            fillFields(loggedProfile);
        }
    }

    /**
     * Carga todos los usuarios registrados desde la base de datos al ComboBox.
     */
    private void loadUsers() {
        try {
            ObservableList<User> users = FXCollections.observableArrayList(db.getAllUsers());
            comboUsers.setItems(users);
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al cargar la lista de usuarios para modificar", e);
        }
    }

    /**
     * Rellena los campos de texto de la interfaz con los datos del perfil
     * seleccionado.
     *
     * * @param p El perfil cuyos datos se van a cargar en la ventana.
     */
    private void fillFields(Profile p) {
        this.profileToModify = p;
        LabelUsername.setText(p.getUsername());
        LabelEmail.setText(p.getEmail());
        TextField_Name.setText(p.getName());
        TextField_Surname.setText(p.getSurname());
        TextField_Telephone.setText(p.getTelephone());
    }

        @FXML
    private void save(ActionEvent event) {
        if (!TextField_NewPass.getText().equals(TextField_ConfirmPass.getText())) {
            LogInfo.getInstance().logWarning("Intento de cambio de contraseña fallido: las contraseñas no coinciden.");
            new Alert(Alert.AlertType.ERROR, "Las contraseñas no coinciden").show();
            return;
        }

        try {
            profileToModify.setName(TextField_Name.getText());
            profileToModify.setSurname(TextField_Surname.getText());
            profileToModify.setTelephone(TextField_Telephone.getText());
            if (!TextField_NewPass.getText().isEmpty()) {
                profileToModify.setPassword(TextField_NewPass.getText());
            }

            db.modificarUser(profileToModify);
            LogInfo.getInstance().logInfo("Perfil actualizado correctamente para el usuario: " + profileToModify.getUsername());
            new Alert(Alert.AlertType.INFORMATION, "Usuario actualizado correctamente").show();
            handleNavigation(event);

        } catch (Exception ex) {
            LogInfo.getInstance().logSevere("Error al persistir cambios del perfil en la base de datos", ex);
            new Alert(Alert.AlertType.ERROR, "Error al guardar los cambios").show();
        }
    }

    /**
     * Muestra una alerta informativa "Acerca de".
     *
     * * @param event El evento de acción disparado.
     */
    @FXML
    private void handleAboutAction(ActionEvent event) {
        LogInfo.getInstance().logInfo("Visualización de 'Acerca de' en ventana de modificación.");
        new Alert(Alert.AlertType.INFORMATION, "BookStore App v1.0").show();
    }

    /**
     * Genera un informe técnicoJasper y lo visualiza mediante JasperViewer.
     *
     * * @param event El evento de acción disparado.
     */
    @FXML
    private void handleInformeTecnico(ActionEvent event) {
        Connection con = null;
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookstore?useSSL=false&serverTimezone=UTC", "root", "abcd*1234");
            InputStream reportStream = getClass().getResourceAsStream("/reports/InformeTecnico.jrxml");
            if (reportStream != null) {
                JasperReport jr = JasperCompileManager.compileReport(reportStream);
                JasperPrint jp = JasperFillManager.fillReport(jr, null, con);
                JasperViewer.viewReport(jp, false);
                LogInfo.getInstance().logInfo("Informe técnico generado desde la ventana de modificación.");
            }
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al generar el informe Jasper técnico desde modificación", e);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
            }
        }
    }

    /**
     * Gestiona la redirección del usuario tras guardar cambios o cancelar.
     * Redirige a la ventana de administración si es Admin, o al menú personal
     * si es Usuario.
     *
     * * @param event El evento que originó la navegación.
     */
    private void handleNavigation(ActionEvent event) {
        try {
            Profile loggedProfile = UserSession.getInstance().getUser();
            String fxmlPath = (loggedProfile instanceof Admin) ? "/view/OptionsAdmin.fxml" : "/view/MenuWindow.fxml";
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            LogInfo.getInstance().logInfo("Redirección tras modificación de perfil a: " + fxmlPath);
        } catch (IOException ex) {
            LogInfo.getInstance().logSevere("Error de navegación tras guardar cambios de perfil", ex);
        }
    }

    /**
     * Inicializa el menú contextual (clic derecho) con opciones de guardado,
     * cancelación y acceso al manual.
     */
    private void initContextMenu() {
        contextMenu = new ContextMenu();
        MenuItem itemSave = new MenuItem("Guardar Cambios");
        itemSave.setOnAction(e -> save(new ActionEvent(Button_SaveChanges, null)));
        MenuItem itemCancel = new MenuItem("Cancelar/Volver");
        itemCancel.setOnAction(e -> cancel(new ActionEvent(Button_Cancel, null)));
        SeparatorMenuItem sep = new SeparatorMenuItem();
        MenuItem itemManual = new MenuItem("Manual de Usuario");
        itemManual.setOnAction(this::handleReportAction);
        contextMenu.getItems().addAll(itemSave, itemCancel, sep, itemManual);

        rootPane.setOnContextMenuRequested(event -> {
            contextMenu.show(rootPane, event.getScreenX(), event.getScreenY());
        });
        rootPane.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && contextMenu.isShowing()) {
                contextMenu.hide();
            }
        });
    }

    /**
     * Cancela la operación actual y regresa a la ventana correspondiente.
     *
     * * @param event El evento de acción disparado por el botón cancelar.
     */
    @FXML
    private void cancel(ActionEvent event) {
        handleNavigation(event);
    }

    /**
     * Solicita el cierre inmediato de la aplicación.
     *
     * * @param event El evento de acción disparado.
     */
    @FXML
    private void handleExit(ActionEvent event) {
        LogInfo.getInstance().logInfo("Cierre de aplicación desde modificación de perfil.");
        Platform.exit();
        System.exit(0);
    }

    /**
     * Abre el manual de usuario en formato PDF mediante la creación de un
     * archivo temporal.
     *
     * * @param event El evento de acción disparado.
     */
    @FXML
    private void handleReportAction(ActionEvent event) {
        try {
            InputStream is = getClass().getResourceAsStream("/documents/Manual_Usuario.pdf");
            File temp = File.createTempFile("Manual", ".pdf");
            Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Desktop.getDesktop().open(temp);
            LogInfo.getInstance().logInfo("Manual de usuario abierto desde ventana de modificación.");
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al intentar abrir el manual de usuario", e);
        }
    }
}
