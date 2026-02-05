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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.DBImplementation;
import model.Profile;
import model.UserSession;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import util.LogInfo;

/**
 * Controlador de la ventana de eliminar usuarios siendo el propio usuario.
 * Permite al usuario en sesión dar de baja su cuenta tras una confirmación por contraseña.
 * * @author unai azkorra
 * @version 1.1
 */
public class DeleteAccountController implements Initializable {

    /** Implementación de la base de datos para operaciones de borrado. */
    private DBImplementation db = new DBImplementation();

    @FXML
    private GridPane rootPane; 
    @FXML
    private Label LabelUsername;
    @FXML
    private TextField TextFieldPassword;
    @FXML
    private Button Button_Cancel;
    @FXML
    private Button Button_Delete;

    /** Menú contextual de la ventana. */
    private ContextMenu globalMenu;

    /**
     * Inicializa la ventana obteniendo el usuario de la sesión actual y configurando
     * los componentes visuales iniciales.
     * * @param location Ubicación relativa para el objeto raíz.
     * @param resources Recursos para localizar el objeto raíz.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Profile user = UserSession.getInstance().getUser();
        if (user != null) {
            LabelUsername.setText(user.getUsername());
        }
        
        initGlobalContextMenu();
        LogInfo.getInstance().logInfo("Ventana de eliminación de cuenta propia inicializada.");
    }

    /**
     * Gestiona la lógica de eliminación de la cuenta propia del usuario.
     * Valida que la contraseña coincida con la del perfil actual y solicita confirmación final.
     * Si el borrado es exitoso, cierra la sesión y navega a la ventana de LogIn.
     * * @param event El evento de acción disparado por el botón de eliminar.
     */
    @FXML
    private void delete(ActionEvent event) {
        String password = TextFieldPassword.getText();
        Profile user = UserSession.getInstance().getUser();

        if (password.isEmpty()) {
            LogInfo.getInstance().logWarning("Intento de eliminación de cuenta sin introducir contraseña.");
            showAlert("Campo vacío", "Por favor, introduce tu contraseña para confirmar.", Alert.AlertType.WARNING);
            return;
        }

        if (user != null && user.getPassword().equals(password)) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();

            if (confirm.getResult() == ButtonType.YES) {
                try {
                    db.dropOutUser(user);
                    LogInfo.getInstance().logInfo("El usuario " + user.getUsername() + " ha eliminado su propia cuenta exitosamente.");
                    showAlert("Cuenta eliminada", "Tu cuenta ha sido eliminada. Volviendo al inicio.", Alert.AlertType.INFORMATION);
                    UserSession.getInstance().cleanUserSession();
                    navigateTo("/view/LogInWindow.fxml", "Inicio de Sesión");
                } catch (Exception ex) {
                    LogInfo.getInstance().logSevere("Error crítico en la base de datos al intentar eliminar la cuenta del usuario: " + user.getUsername(), ex);
                    showAlert("Error", "No se pudo eliminar la cuenta.", Alert.AlertType.ERROR);
                }
            }
        } else {
            LogInfo.getInstance().logWarning("Fallo de autenticación en eliminación de cuenta para el usuario: " + (user != null ? user.getUsername() : "Desconocido"));
            showAlert("Error de autenticación", "La contraseña introducida es incorrecta.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Genera un informe técnico de la aplicación mediante JasperReports.
     * * @param event El evento de acción disparado.
     */
    @FXML
    private void handleInformeTecnico(ActionEvent event) {
        Connection con = null;
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookstore?useSSL=false", "root", "abcd*1234");
            InputStream reportStream = getClass().getResourceAsStream("/reports/InformeTecnico.jrxml");
            if (reportStream != null) {
                JasperReport jr = JasperCompileManager.compileReport(reportStream);
                JasperPrint jp = JasperFillManager.fillReport(jr, null, con);
                JasperViewer.viewReport(jp, false);
                LogInfo.getInstance().logInfo("Informe técnico generado desde la ventana de eliminación de cuenta.");
            }
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al generar el informe Jasper técnico desde eliminación de cuenta", e);
            showAlert("Error", "No se pudo generar el informe técnico.", Alert.AlertType.ERROR);
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) { }
        }
    }

    /**
     * Realiza la navegación hacia otra ventana FXML.
     * * @param fxmlPath Ruta relativa del archivo FXML.
     * @param title Título de la nueva ventana.
     */
    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) LabelUsername.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
            LogInfo.getInstance().logInfo("Navegación desde eliminación de cuenta hacia: " + fxmlPath);
        } catch (IOException ex) {
            LogInfo.getInstance().logSevere("Error de navegación en la ventana de eliminación de cuenta hacia: " + fxmlPath, ex);
        }
    }

    /**
     * Inicializa el menú contextual global asociado al panel raíz.
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
        }
        rootPane.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && globalMenu.isShowing()) {
                globalMenu.hide();
            }
        });
    }

    /**
     * Limpia el contenido del campo de contraseña.
     * * @param event El evento de acción disparado.
     */
    @FXML
    private void handleClearAction(ActionEvent event) {
        TextFieldPassword.clear();
    }

    /**
     * Solicita el cierre controlado de la aplicación.
     * * @param event El evento de acción disparado.
     */
    @FXML
    private void handleExit(ActionEvent event) {
        LogInfo.getInstance().logInfo("Cierre de aplicación solicitado desde la ventana de baja de usuario.");
        Platform.exit();
        System.exit(0);
    }

    /**
     * Muestra una ventana de información "Acerca de".
     * * @param event El evento de acción disparado.
     */
    @FXML
    private void handleAboutAction(ActionEvent event) {
        showAlert("Acerca de", "BookStore App v1.1\nVentana de baja de usuario.", Alert.AlertType.INFORMATION);
    }

    /**
     * Abre el manual de usuario de la aplicación en formato PDF.
     * * @param event El evento de acción disparado.
     */
    @FXML
    private void handleReportAction(ActionEvent event) {
        try {
            InputStream is = getClass().getResourceAsStream("/documents/Manual_Usuario.pdf");
            if (is != null) {
                File temp = File.createTempFile("Manual", ".pdf");
                temp.deleteOnExit();
                Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Desktop.getDesktop().open(temp);
                LogInfo.getInstance().logInfo("Manual de usuario abierto desde la ventana de eliminación de cuenta.");
            }
        } catch (IOException e) {
            LogInfo.getInstance().logSevere("Error al intentar abrir el manual de usuario desde eliminación de cuenta", e);
        }
    }

    /**
     * Cancela la operación de borrado y regresa al menú principal del usuario.
     * * @param event El evento de acción disparado por el botón cancelar.
     */
    @FXML
    private void cancel(ActionEvent event) {
        navigateTo("/view/MenuWindow.fxml", "Mi Menú");
    }

    /**
     * Crea y muestra un cuadro de diálogo de alerta personalizado.
     * * @param title Título de la alerta.
     * @param content Texto descriptivo de la alerta.
     * @param type Tipo de alerta (INFORMATION, WARNING, ERROR, etc.).
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}