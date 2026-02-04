package controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.UserSession;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import util.LogInfo;

public class OptionsAdminController {

    @FXML private GridPane rootPane;
    @FXML private Button btnDeleteUser, btnEliminarComentario, btnModificarUsuario, btnLibro;

    private ContextMenu globalMenu;

    @FXML
    public void initialize() {
        initGlobalContextMenu();
        LogInfo.getInstance().logInfo("Panel de Opciones Administrativas abierto.");
    }

    @FXML
    private void opcionesLibroWindow(ActionEvent event) {
        navigateTo("/view/BookCRUDWindow.fxml");
    }

    @FXML
    private void deleteUserWindow(ActionEvent event) {
        navigateTo("/view/DeleteAccountAdmin.fxml");
    }

    @FXML
    private void eliminarComentarioWindow(ActionEvent event) {
        navigateTo("/view/DeleteComentWindow.fxml");
    }

    @FXML
    private void modificarUsuarioWindow(ActionEvent event) {
        navigateTo("/view/ModifyWindow.fxml");
    }

    @FXML
    private void btnVolver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainBookStore.fxml"));
            Parent root = loader.load();
            MainBookStoreController controller = loader.getController();
            if (controller.headerController != null) {
                controller.headerController.setMode(UserSession.getInstance().getUser(), null);
            }
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            LogInfo.getInstance().logInfo("Administrador regresó a la tienda principal.");
        } catch (IOException ex) {
            LogInfo.getInstance().logSevere("Error al volver a la tienda principal", ex);
        }
    }

    @FXML
    private void handleInformeTecnico(ActionEvent event) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookstore?useSSL=false", "root", "abcd*1234")) {
            InputStream reportStream = getClass().getResourceAsStream("/reports/InformeTecnico.jrxml");
            JasperReport jr = JasperCompileManager.compileReport(reportStream);
            JasperPrint jp = JasperFillManager.fillReport(jr, null, con);
            JasperViewer.viewReport(jp, false);
            LogInfo.getInstance().logInfo("Informe técnico general generado.");
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al generar informe técnico general", e);
            showAlert("Error", "No se pudo generar el informe técnico.", Alert.AlertType.ERROR);
        }
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) btnLibro.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            LogInfo.getInstance().logInfo("Navegación administrativa a: " + fxmlPath);
        } catch (IOException ex) {
            LogInfo.getInstance().logSevere("Error al navegar a " + fxmlPath, ex);
        }
    }

    private void initGlobalContextMenu() {
        globalMenu = new ContextMenu();
        globalMenu.setAutoHide(true);
        MenuItem itemLibros = new MenuItem("Gestión de Libros");
        itemLibros.setOnAction(this::opcionesLibroWindow);
        MenuItem itemComentarios = new MenuItem("Eliminar Comentario");
        itemComentarios.setOnAction(this::eliminarComentarioWindow);
        MenuItem itemInforme = new MenuItem("Generar Informe Técnico");
        itemInforme.setOnAction(this::handleInformeTecnico);
        MenuItem itemManual = new MenuItem("Manual de Usuario");
        itemManual.setOnAction(this::handleReportAction);
        MenuItem itemExit = new MenuItem("Salir");
        itemExit.setOnAction(e -> handleExit(null));
        globalMenu.getItems().addAll(itemLibros, itemComentarios, new SeparatorMenuItem(), itemInforme, itemManual, new SeparatorMenuItem(), itemExit);
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

    @FXML private void handleExit(ActionEvent event) { Platform.exit(); System.exit(0); }
    @FXML private void handleReportAction(ActionEvent event) {
        try {
            InputStream is = getClass().getResourceAsStream("/documents/Manual_Usuario.pdf");
            File temp = File.createTempFile("Manual", ".pdf");
            Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Desktop.getDesktop().open(temp);
            LogInfo.getInstance().logInfo("Manual de usuario abierto desde panel admin.");
        } catch (IOException e) { LogInfo.getInstance().logSevere("Error al abrir manual desde panel admin", e); }
    }
    private void showAlert(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo); alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje); alert.showAndWait();
    }
}