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

public class DeleteAccountAdminController implements Initializable {

    private final ClassDAO dao = new DBImplementation();

    @FXML private GridPane rootPane;
    @FXML private ComboBox<User> ComboBoxUser;
    @FXML private TextField TextFieldPassword;
    @FXML private Button Button_Delete, Button_Cancel;

    private ContextMenu globalMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarUsuarios();
        initGlobalContextMenu();
        LogInfo.getInstance().logInfo("Ventana de eliminación de cuentas (Admin) inicializada.");
    }

    private void cargarUsuarios() {
        try {
            List<User> users = dao.getAllUsers();
            ComboBoxUser.setItems(FXCollections.observableArrayList(users));
        } catch (Exception ex) {
            LogInfo.getInstance().logSevere("Error al cargar la lista de usuarios", ex);
        }
    }

    @FXML
    private void delete(ActionEvent event) {
        User selectedUser = ComboBoxUser.getSelectionModel().getSelectedItem();
        String inputPassword = TextFieldPassword.getText();
        Profile adminProfile = UserSession.getInstance().getUser();

        if (selectedUser == null) {
            LogInfo.getInstance().logWarning("Intento de borrado sin seleccionar usuario.");
            showAlert("Selección vacía", "Por favor, selecciona un usuario para eliminar.", Alert.AlertType.WARNING);
            return;
        }

        if (adminProfile != null && adminProfile.getPassword().equals(inputPassword)) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Estás seguro de que deseas eliminar al usuario " + selectedUser.getUsername() + "?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();

            if (confirm.getResult() == ButtonType.YES) {
                try {
                    dao.dropOutUser(selectedUser);
                    LogInfo.getInstance().logInfo("ADMIN " + adminProfile.getUsername() + " eliminó la cuenta de: " + selectedUser.getUsername());
                    showAlert("Éxito", "Usuario eliminado correctamente.", Alert.AlertType.INFORMATION);
                    cargarUsuarios();
                    TextFieldPassword.clear();
                } catch (Exception ex) {
                    LogInfo.getInstance().logSevere("Error en base de datos al eliminar usuario", ex);
                    showAlert("Error", "No se pudo borrar el usuario.", Alert.AlertType.ERROR);
                }
            }
        } else {
            LogInfo.getInstance().logWarning("Intento de borrado fallido: contraseña de admin incorrecta.");
            showAlert("Error de autenticación", "La contraseña del administrador es incorrecta.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleInformeTecnico(ActionEvent event) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookstore?useSSL=false&serverTimezone=UTC", "root", "abcd*1234")) {
            InputStream reportStream = getClass().getResourceAsStream("/reports/InformeTecnico.jrxml");
            JasperReport jr = JasperCompileManager.compileReport(reportStream);
            JasperPrint jp = JasperFillManager.fillReport(jr, null, con);
            JasperViewer.viewReport(jp, false);
            LogInfo.getInstance().logInfo("Informe técnico generado desde administración de usuarios.");
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al generar informe técnico en admin usuarios", e);
            showAlert("Error", "No se pudo generar el informe técnico.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/OptionsAdmin.fxml"));
            Stage currentStage = (Stage) Button_Cancel.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.show();
        } catch (IOException ex) {
            LogInfo.getInstance().logSevere("Error al volver al menú de opciones admin", ex);
        }
    }

    private void initGlobalContextMenu() {
        globalMenu = new ContextMenu();
        globalMenu.setAutoHide(true);
        MenuItem itemLimpiar = new MenuItem("Limpiar Campos");
        itemLimpiar.setOnAction(e -> handleClearAction(null));
        MenuItem itemInforme = new MenuItem("Generar Informe Técnico");
        itemInforme.setOnAction(this::handleInformeTecnico);
        MenuItem itemManual = new MenuItem("Manual de Usuario");
        itemManual.setOnAction(this::handleReportAction);
        MenuItem itemExit = new MenuItem("Salir");
        itemExit.setOnAction(this::handleExit);
        globalMenu.getItems().addAll(itemLimpiar, new SeparatorMenuItem(), itemInforme, itemManual, new SeparatorMenuItem(), itemExit);
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

    @FXML private void handleClearAction(ActionEvent event) { ComboBoxUser.getSelectionModel().clearSelection(); TextFieldPassword.clear(); }
    @FXML private void handleExit(ActionEvent event) { Platform.exit(); System.exit(0); }
    @FXML private void handleReportAction(ActionEvent event) {
        try {
            InputStream is = getClass().getResourceAsStream("/documents/Manual_Usuario.pdf");
            File temp = File.createTempFile("Manual", ".pdf");
            Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Desktop.getDesktop().open(temp);
            LogInfo.getInstance().logInfo("Manual de usuario abierto desde admin.");
        } catch (IOException e) { LogInfo.getInstance().logSevere("Error al abrir manual desde admin", e); }
    }
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }
}