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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
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

/**
 * Controlador de la ventana de opciones del usuario.
 * @author unai azkorra
 */
public class MenuWindowController implements Initializable {

    @FXML private GridPane rootPane;
    @FXML private Button btnModifyProfile, btnDeleteAccount, btnHistory, btnBack;
    @FXML private Label label_Username;

    private ContextMenu globalMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (UserSession.getInstance().getUser() != null) {
            label_Username.setText(UserSession.getInstance().getUser().getUsername());
            LogInfo.getInstance().logInfo("Usuario " + label_Username.getText() + " ha entrado en su menú personal.");
        }
        initGlobalContextMenu();
    }

    private void initGlobalContextMenu() {
        globalMenu = new ContextMenu();
        globalMenu.setAutoHide(true);

        MenuItem itemModify = new MenuItem("Modificar Perfil");
        itemModify.setOnAction(e -> handleModifyAction(null));

        MenuItem itemHistory = new MenuItem("Historial de Compras");
        itemHistory.setOnAction(e -> handleHistoryAction(null));

        MenuItem itemDelete = new MenuItem("Eliminar Cuenta");
        itemDelete.setOnAction(e -> handleDeleteAction(null));

        MenuItem itemManual = new MenuItem("Manual de Usuario");
        itemManual.setOnAction(this::handleReportAction);

        MenuItem itemExit = new MenuItem("Salir");
        itemExit.setOnAction(this::handleExit);

        globalMenu.getItems().addAll(itemModify, itemHistory, itemDelete, new SeparatorMenuItem(), itemManual, itemExit);

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

    @FXML
    private void handleModifyAction(ActionEvent event) {
        openWindow("/view/ModifyWindow.fxml", "Modificar Perfil");
    }

    @FXML
    private void handleHistoryAction(ActionEvent event) {
        openWindow("/view/ShoppingHistory.fxml", "Historial de Compras");
    }

    @FXML
    private void handleDeleteAction(ActionEvent event) {
        openWindow("/view/DeleteAccount.fxml", "Eliminar Cuenta");
    }

    @FXML
    private void handleBackAction(ActionEvent event) {
        openWindow("/view/MainBookStore.fxml", "Tienda de Libros");
    }

    // --- MÉTODOS DE MENÚ ---

    @FXML
    private void handleExit(ActionEvent event) {
        LogInfo.getInstance().logInfo("Cierre de aplicación solicitado desde MenuWindow.");
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleAboutAction(ActionEvent event) {
        LogInfo.getInstance().logInfo("Visualización de 'Acerca de' en menú de usuario.");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setContentText("BookStore App v1.0\nMenú de usuario.");
        alert.showAndWait();
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
                LogInfo.getInstance().logInfo("Manual de usuario abierto desde MenuWindow.");
            }
        } catch (IOException e) {
            LogInfo.getInstance().logSevere("Error al intentar abrir el manual de usuario", e);
        }
    }

    @FXML
    private void handleInformeTecnico(ActionEvent event) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookstore", "root", "abcd*1234")) {
            InputStream is = getClass().getResourceAsStream("/reports/InformeTecnico.jrxml");
            JasperPrint jp = JasperFillManager.fillReport(JasperCompileManager.compileReport(is), null, con);
            JasperViewer.viewReport(jp, false);
            LogInfo.getInstance().logInfo("Informe técnico generado desde MenuWindow.");
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al generar informe técnico Jasper", e);
        }
    }

    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof MainBookStoreController) {
                MainBookStoreController main = (MainBookStoreController) controller;
                if (main.headerController != null) {
                    main.headerController.setMode(UserSession.getInstance().getUser(), null);
                }
            }

            Stage stage = (Stage) label_Username.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
            LogInfo.getInstance().logInfo("Navegación desde MenuWindow a: " + fxmlPath);
        } catch (IOException ex) {
            LogInfo.getInstance().logSevere("Error al abrir ventana: " + fxmlPath, ex);
        }
    }
}