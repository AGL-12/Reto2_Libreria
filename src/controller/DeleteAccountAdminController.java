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
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.ClassDAO;
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
 * Controlador de la ventana de eliminar usuarios siendo administrador.
 * Valida la operación mediante la contraseña del administrador en sesión para 
 * garantizar la seguridad del borrado.
 * * @author unai azkorra
 * @version 1.1
 */
public class DeleteAccountAdminController implements Initializable {

    /** Interfaz de acceso a datos. */
    private final ClassDAO dao = new DBImplementation();

    @FXML
    private GridPane rootPane; 
    @FXML
    private ComboBox<User> ComboBoxUser;
    @FXML
    private TextField TextFieldPassword;
    @FXML
    private Button Button_Delete;
    @FXML
    private Button Button_Cancel;

    /** Menú contextual accesible mediante clic derecho. */
    private ContextMenu globalMenu;

    /**
     * Inicializa la ventana cargando la lista de usuarios y configurando el menú contextual.
     * * @param location Ubicación relativa para el objeto raíz.
     * @param resources Recursos para localizar el objeto raíz.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarUsuarios();
        initGlobalContextMenu();
        LogInfo.getInstance().logInfo("Ventana de eliminación de cuentas (Admin) inicializada.");
    }

    /**
     * Obtiene todos los usuarios de la base de datos y los carga en el ComboBox.
     */
    private void cargarUsuarios() {
        try {
            List<User> users = dao.getAllUsers();
            ComboBoxUser.setItems(FXCollections.observableArrayList(users));
        } catch (Exception ex) {
            LogInfo.getInstance().logSevere("Error al cargar la lista de usuarios para el administrador", ex);
        }
    }

    /**
     * Maneja la lógica de eliminación de un usuario.
     * Verifica que haya un usuario seleccionado y que la contraseña introducida coincida 
     * con la del administrador logueado antes de proceder.
     * * @param event El evento de acción disparado por el botón de eliminar.
     */
    @FXML
    private void delete(ActionEvent event) {
        User selectedUser = ComboBoxUser.getSelectionModel().getSelectedItem();
        String inputPassword = TextFieldPassword.getText();
        Profile adminProfile = UserSession.getInstance().getUser();

        if (selectedUser == null) {
            LogInfo.getInstance().logWarning("Intento de eliminación sin usuario seleccionado.");
            showAlert("Selección vacía", "Por favor, selecciona un usuario para eliminar.", Alert.AlertType.WARNING);
            return;
        }

        if (adminProfile != null && adminProfile.getPassword().equals(inputPassword)) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Estás seguro de que deseas eliminar al usuario " + selectedUser.getUsername() + "?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();

            if (confirm.getResult() == ButtonType.YES) {
                try {
                    dao.dropOutUser(selectedUser);
                    LogInfo.getInstance().logInfo("ADMIN " + adminProfile.getUsername() + " eliminó al usuario " + selectedUser.getUsername());
                    showAlert("Éxito", "Usuario eliminado correctamente.", Alert.AlertType.INFORMATION);
                    cargarUsuarios();
                    TextFieldPassword.clear();
                } catch (Exception ex) {
                    LogInfo.getInstance().logSevere("Error en base de datos al eliminar usuario por admin", ex);
                    showAlert("Error", "No se pudo borrar el usuario.", Alert.AlertType.ERROR);
                }
            }
        } else {
            LogInfo.getInstance().logWarning("Fallo de autenticación de administrador al intentar eliminar una cuenta.");
            showAlert("Error de autenticación", "La contraseña del administrador es incorrecta.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Muestra información sobre la aplicación.
     * * @param event El evento de acción disparado.
     */
    @FXML
    private void handleAboutAction(ActionEvent event) {
        LogInfo.getInstance().logInfo("Visualización de 'Acerca de' en administración de cuentas.");
        showAlert("Acerca de Nosotros", "BookStore App v1.0\nGestión de administración de usuarios.", Alert.AlertType.INFORMATION);
    }

    /**
     * Genera un informe técnico detallado mediante JasperReports.
     * Abre una conexión SQL temporal para alimentar el reporte.
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
                LogInfo.getInstance().logInfo("Informe técnico generado desde administración de cuentas.");
            }
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al generar informe técnico Jasper", e);
            showAlert("Error", "No se pudo generar el informe técnico.", Alert.AlertType.ERROR);
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) { }
        }
    }

    /**
     * Configura el menú contextual global de la ventana con opciones de limpieza, 
     * manual, informes y salida.
     */
    private void initGlobalContextMenu() {
        globalMenu = new ContextMenu();
        globalMenu.setAutoHide(true);
        MenuItem itemLimpiar = new MenuItem("Limpiar Campos");
        itemLimpiar.setOnAction(e -> handleClearAction(null));
        MenuItem itemInforme = new MenuItem("Generar Informe Técnico");
        itemInforme.setOnAction(this::handleInformeTecnico);
        MenuItem itemManual = new MenuItem("Manual de Usuario");
        itemManual.setOnAction(this::handleReportAction);
        MenuItem itemAbout = new MenuItem("Acerca de...");
        itemAbout.setOnAction(this::handleAboutAction);
        MenuItem itemExit = new MenuItem("Salir");
        itemExit.setOnAction(this::handleExit);
        globalMenu.getItems().addAll(itemLimpiar, new SeparatorMenuItem(), itemInforme, itemManual, itemAbout, new SeparatorMenuItem(), itemExit);
        
        if (rootPane != null) {
            rootPane.setOnContextMenuRequested(event -> {
                globalMenu.show(rootPane, event.getScreenX(), event.getScreenY());
                event.consume();
            });
            rootPane.setOnMousePressed(event -> {
                if (event.isPrimaryButtonDown() && globalMenu.isShowing()) {
                    globalMenu.hide();
                }
            });
        }
    }

    /**
     * Limpia la selección del ComboBox de usuarios y el campo de contraseña.
     * * @param event El evento de acción disparado.
     */
    @FXML 
    private void handleClearAction(ActionEvent event) { 
        ComboBoxUser.getSelectionModel().clearSelection(); 
        TextFieldPassword.clear(); 
    }

    /**
     * Finaliza la ejecución de la aplicación.
     * * @param event El evento de acción disparado.
     */
    @FXML 
    private void handleExit(ActionEvent event) { 
        LogInfo.getInstance().logInfo("Aplicación cerrada por administrador.");
        Platform.exit(); 
        System.exit(0); 
    }

    /**
     * Abre el manual de usuario en PDF creando un archivo temporal.
     * * @param event El evento de acción disparado.
     */
    @FXML 
    private void handleReportAction(ActionEvent event) {
        try {
            InputStream is = getClass().getResourceAsStream("/documents/Manual_Usuario.pdf");
            File temp = File.createTempFile("Manual", ".pdf");
            Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Desktop.getDesktop().open(temp);
            LogInfo.getInstance().logInfo("Manual de usuario abierto desde administración de cuentas.");
        } catch (IOException e) { 
            LogInfo.getInstance().logSevere("Error al abrir manual", e); 
        }
    }

    /**
     * Cancela la operación actual y regresa al menú de opciones de administrador.
     * * @param event El evento de acción disparado por el botón cancelar.
     */
    @FXML
    private void cancel(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/OptionsAdmin.fxml"));
            Stage currentStage = (Stage) Button_Cancel.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.show();
            LogInfo.getInstance().logInfo("Cancelación y retorno al menú de administración.");
        } catch (IOException ex) {
            LogInfo.getInstance().logSevere("Error al navegar de vuelta a OptionsAdmin", ex);
        }
    }

    /**
     * Crea y muestra una ventana de alerta personalizada.
     * * @param title Título de la ventana.
     * @param message Mensaje a mostrar.
     * @param type Tipo de alerta (INFORMATION, WARNING, ERROR, etc.).
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type); 
        alert.setTitle(title); 
        alert.setHeaderText(null); 
        alert.setContentText(message); 
        alert.showAndWait();
    }
}