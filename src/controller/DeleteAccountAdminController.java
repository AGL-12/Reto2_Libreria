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
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private GridPane rootPane; // Necesario para el menú contextual
    @FXML
    private ComboBox<User> ComboBoxUser;
    @FXML
    private TextField TextFieldPassword;
    @FXML
    private Button Button_Delete;
    @FXML
    private Button Button_Cancel;

    private ContextMenu globalMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarUsuarios();
        initGlobalContextMenu();
    }

    private void cargarUsuarios() {
        try {
            List<User> users = dao.getAllUsers();
            ComboBoxUser.setItems(FXCollections.observableArrayList(users));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al cargar usuarios", ex);
        }
    }

    // --- LÓGICA DE MENÚS Y ACCIONES ADICIONALES ---

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
        // Con esto ocultamos el menú si se hace click izquierdo fuera
            rootPane.setOnMousePressed(event -> {
                if (event.isPrimaryButtonDown() && globalMenu.isShowing()) {
                    globalMenu.hide();
                }
            });
    }

    @FXML
    private void handleClearAction(ActionEvent event) {
        ComboBoxUser.getSelectionModel().clearSelection();
        TextFieldPassword.clear();
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleAboutAction(ActionEvent event) {
        showAlert("Acerca de Nosotros", "BookStore App v1.0\nGestión de administración de usuarios.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleReportAction(ActionEvent event) {
        try {
            InputStream pdfStream = getClass().getResourceAsStream("/documents/Manual_Usuario.pdf");
            if (pdfStream != null) {
                File tempFile = File.createTempFile("Manual_Usuario", ".pdf");
                tempFile.deleteOnExit();
                Files.copy(pdfStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(tempFile);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al abrir el manual", e);
        }
    }

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
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar informe técnico", e);
            showAlert("Error", "No se pudo generar el informe técnico.", Alert.AlertType.ERROR);
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) { }
        }
    }

    // --- LÓGICA ORIGINAL SIN MODIFICAR ---

    @FXML
    private void delete(ActionEvent event) {
        User selectedUser = ComboBoxUser.getSelectionModel().getSelectedItem();
        String inputPassword = TextFieldPassword.getText();
        
        Profile adminProfile = UserSession.getInstance().getUser();

        if (selectedUser == null) {
            showAlert("Selección vacía", "Por favor, selecciona un usuario para eliminar.", Alert.AlertType.WARNING);
            return;
        }

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