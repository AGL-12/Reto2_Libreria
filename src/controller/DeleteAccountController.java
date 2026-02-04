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
import java.util.logging.Level;
import java.util.logging.Logger;
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

/**
 * Controlador de la ventana de eliminar usuarios siendo el propio usuario.
 * @author unai azkorra
 * @version 1.1
 */
public class DeleteAccountController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DeleteAccountController.class.getName());
    private DBImplementation db = new DBImplementation();

    @FXML
    private GridPane rootPane; // Necesario para el menú contextual
    @FXML
    private Label LabelUsername;
    @FXML
    private TextField TextFieldPassword;
    @FXML
    private Button Button_Cancel;
    @FXML
    private Button Button_Delete;

    private ContextMenu globalMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Establecemos el nombre de usuario de la sesión
        Profile user = UserSession.getInstance().getUser();
        if (user != null) {
            LabelUsername.setText(user.getUsername());
        }
        
        initGlobalContextMenu();
    }

    // --- LÓGICA DE MENÚS Y ACCIONES ---

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

        globalMenu.getItems().addAll(
                itemLimpiar, 
                new SeparatorMenuItem(), 
                itemInforme, 
                itemManual, 
                itemAbout,
                new SeparatorMenuItem(), 
                itemExit
        );

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

    @FXML
    private void handleClearAction(ActionEvent event) {
        TextFieldPassword.clear();
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleAboutAction(ActionEvent event) {
        showAlert("Acerca de", "BookStore App v1.1\nVentana de baja de usuario.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleReportAction(ActionEvent event) {
        try {
            InputStream is = getClass().getResourceAsStream("/documents/Manual_Usuario.pdf");
            if (is != null) {
                File temp = File.createTempFile("Manual", ".pdf");
                temp.deleteOnExit();
                Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Desktop.getDesktop().open(temp);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al abrir manual", e);
        }
    }

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
            }
        } catch (Exception e) {
            showAlert("Error", "No se pudo generar el informe técnico.", Alert.AlertType.ERROR);
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) { }
        }
    }

    // --- LÓGICA ORIGINAL ---

    @FXML
    private void delete(ActionEvent event) {
        String password = TextFieldPassword.getText();
        Profile user = UserSession.getInstance().getUser();

        if (password.isEmpty()) {
            showAlert("Campo vacío", "Por favor, introduce tu contraseña para confirmar.", Alert.AlertType.WARNING);
            return;
        }

        if (user != null && user.getPassword().equals(password)) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();

            if (confirm.getResult() == ButtonType.YES) {
                try {
                    db.dropOutUser(user);
                    showAlert("Cuenta eliminada", "Tu cuenta ha sido eliminada. Volviendo al inicio.", Alert.AlertType.INFORMATION);
                    UserSession.getInstance().cleanUserSession();
                    navigateTo("/view/LogInWindow.fxml", "Inicio de Sesión");
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error al borrar usuario", ex);
                    showAlert("Error", "No se pudo eliminar la cuenta.", Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Error de autenticación", "La contraseña introducida es incorrecta.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        navigateTo("/view/MenuWindow.fxml", "Mi Menú");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) LabelUsername.getScene().getWindow();
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