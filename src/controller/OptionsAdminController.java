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
 * Proporciona acceso centralizado a la gestión de libros, usuarios y moderación 
 * de comentarios, además de herramientas de informes técnicos.
 * * @author unai azkorra
 * @version 1.0
 */
public class OptionsAdminController {

    @FXML
    private GridPane rootPane;
    @FXML
    private Button btnDeleteUser, btnEliminarComentario, btnModificarUsuario, btnLibro;

    /** Menú contextual accesible mediante clic derecho para navegación rápida. */
    private ContextMenu globalMenu;

    /**
     * Inicializa el panel de opciones administrativas.
     * Configura el menú contextual global y registra el evento en el sistema de logs.
     */
    @FXML
    public void initialize() {
        initGlobalContextMenu();
        LogInfo.getInstance().logInfo("Panel de Opciones Administrativas inicializado.");
    }

    /**
     * Inicializa y configura el menú contextual global.
     * Define las opciones de gestión, informes técnicos, acceso al manual y salida,
     * vinculándolas con sus respectivos métodos de ventana.
     */
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

    /**
     * Navega a la ventana de gestión (CRUD) de libros.
     * @param event El evento de acción disparado.
     */
    @FXML
    private void opcionesLibroWindow(ActionEvent event) {
        navigateTo("/view/BookCRUDWindow.fxml");
    }

    /**
     * Navega a la ventana de eliminación de cuentas de usuario.
     * @param event El evento de acción disparado.
     */
    @FXML
    private void deleteUserWindow(ActionEvent event) {
        navigateTo("/view/DeleteAccountAdmin.fxml");
    }

    /**
     * Navega a la ventana de moderación y eliminación de comentarios.
     * @param event El evento de acción disparado.
     */
    @FXML
    private void eliminarComentarioWindow(ActionEvent event) {
        navigateTo("/view/DeleteComentWindow.fxml");
    }

    /**
     * Navega a la ventana de modificación de perfiles de usuario.
     * @param event El evento de acción disparado.
     */
    @FXML
    private void modificarUsuarioWindow(ActionEvent event) {
        navigateTo("/view/ModifyWindow.fxml");
    }

    /**
     * Regresa a la ventana principal de la tienda (MainBookStore).
     * Configura el encabezado de la tienda con la sesión del administrador actual.
     * @param event El evento de acción disparado por el botón volver.
     */
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

    /**
     * Finaliza la ejecución de la aplicación.
     * @param event El evento de acción disparado.
     */
    @FXML
    private void handleExit(ActionEvent event) {
        LogInfo.getInstance().logInfo("Aplicación cerrada por el administrador.");
        Platform.exit();
        System.exit(0);
    }

    /**
     * Muestra un cuadro de diálogo informativo sobre el panel de administración.
     * @param event El evento de acción disparado.
     */
    @FXML
    private void handleAboutAction(ActionEvent event) {
        LogInfo.getInstance().logInfo("Visualización de 'Acerca de' en panel admin.");
        showAlert("Acerca de Nosotros", "BookStore App v1.0\nPanel de Control de Administración.", Alert.AlertType.INFORMATION);
    }

    /**
     * Abre el manual de usuario en formato PDF mediante un archivo temporal.
     * @param event El evento de acción disparado.
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
                LogInfo.getInstance().logInfo("Manual de usuario abierto desde panel admin.");
            }
        } catch (IOException e) {
            LogInfo.getInstance().logSevere("Error al abrir manual", e);
        }
    }

    /**
     * Genera y visualiza un informe técnico detallado mediante JasperReports.
     * @param event El evento de acción disparado.
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
                // Error silencioso al cerrar conexión
            }
        }
    }

    /**
     * Gestiona la navegación genérica entre las distintas vistas administrativas.
     * Obtiene el escenario (Stage) actual a través del panel raíz para evitar errores de contexto.
     * @param fxmlPath La ruta del archivo FXML que se desea cargar.
     */
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

            LogInfo.getInstance().logInfo("Navegación administrativa a: " + fxmlPath);
        } catch (IOException ex) {
            LogInfo.getInstance().logSevere("Error abriendo " + fxmlPath, ex);
        }
    }

    /**
     * Muestra una alerta personalizada al usuario.
     * @param titulo El título de la ventana de alerta.
     * @param mensaje El mensaje a mostrar.
     * @param tipo El tipo de alerta (INFORMATION, WARNING, ERROR, etc.).
     */
    private void showAlert(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}