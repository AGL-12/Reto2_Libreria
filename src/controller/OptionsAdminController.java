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

/**
 * Controlador de la ventana principal de administración.
 */
public class OptionsAdminController {

    @FXML
    private GridPane rootPane;
    @FXML
    private Button btnDeleteUser, btnEliminarComentario, btnModificarUsuario, btnLibro;

    private ContextMenu globalMenu;

    @FXML
    public void initialize() {
        initGlobalContextMenu();
        LogInfo.getInstance().logInfo("Panel de Opciones Administrativas inicializado.");
    }

    private void initGlobalContextMenu() {
        globalMenu = new ContextMenu();
        globalMenu.setAutoHide(true);

        MenuItem itemLibros = new MenuItem("Gestión de Libros");
        itemLibros.setOnAction(this::opcionesLibroWindow);

        MenuItem itemComentarios = new MenuItem("Eliminar Comentario");
        itemComentarios.setOnAction(this::eliminarComentarioWindow);

        MenuItem itemModUser = new MenuItem("Modificar Usuario");
        itemModUser.setOnAction(this::modificarUsuarioWindow);

        MenuItem itemDelUser = new MenuItem("Borrar Usuario");
        itemDelUser.setOnAction(this::deleteUserWindow);

        MenuItem itemInforme = new MenuItem("Generar Informe Técnico");
        itemInforme.setOnAction(this::handleInformeTecnico);

        MenuItem itemManual = new MenuItem("Manual de Usuario");
        itemManual.setOnAction(this::handleReportAction);

        MenuItem itemExit = new MenuItem("Salir");
        itemExit.setOnAction(this::handleExit);

        globalMenu.getItems().addAll(
                itemLibros, itemComentarios, itemModUser, itemDelUser,
                new SeparatorMenuItem(),
                itemInforme, itemManual,
                new SeparatorMenuItem(),
                itemExit
        );

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
            LogInfo.getInstance().logSevere("Error al volver al MainBookStore", ex);
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        LogInfo.getInstance().logInfo("Aplicación cerrada por el administrador.");
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleAboutAction(ActionEvent event) {
        LogInfo.getInstance().logInfo("Visualización de 'Acerca de' en panel admin.");
        showAlert("Acerca de Nosotros", "BookStore App v1.0\nPanel de Control de Administración.", Alert.AlertType.INFORMATION);
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
                LogInfo.getInstance().logInfo("Manual de usuario abierto desde panel admin.");
            }
        } catch (IOException e) {
            LogInfo.getInstance().logSevere("Error al abrir manual", e);
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
                LogInfo.getInstance().logInfo("Informe técnico generado por administrador.");
            }
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al generar informe técnico", e);
            showAlert("Error", "No se pudo generar el informe técnico.", Alert.AlertType.ERROR);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
            }
        }
    }

    // Dentro de OptionsAdminController.java
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();

            // CORRECCIÓN SEGURA: Usar el rootPane en lugar de un botón específico
            // para evitar ClassCastException si la llamada viene de un MenuItem
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

            LogInfo.getInstance().logInfo("Navegación administrativa a: " + fxmlPath);
        } catch (IOException ex) {
            LogInfo.getInstance().logSevere("Error abriendo " + fxmlPath, ex);
        }
    }

    private void showAlert(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
