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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Commentate;
import model.DBImplementation;
import model.User;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import util.LogInfo;

/**
 * Controlador de la ventana de eliminar comentarios. Acceso restringido a
 * administradores para moderar comentarios filtrados por usuario.
 * Permite visualizar, buscar y eliminar comentarios específicos de la base de datos.
 *
 * @author unai azkorra
 * @version 1.0
 */
public class DeleteComentWindowController implements Initializable {

    /** Implementación de la lógica de acceso a datos. */
    private DBImplementation db = new DBImplementation();

    @FXML
    private VBox rootPane;
    @FXML
    private TableView<Commentate> tableComments;
    @FXML
    private TableColumn<Commentate, String> colBook;
    @FXML
    private TableColumn<Commentate, String> colDate;
    @FXML
    private TableColumn<Commentate, String> colComment;
    @FXML
    private ComboBox<User> comboUsers;

    /** Menú contextual global para acciones rápidas. */
    private ContextMenu globalMenu;

    /**
     * Inicializa la ventana configurando las columnas de la tabla, cargando la lista
     * de usuarios en el ComboBox y estableciendo los listeners de selección.
     * * @param location Ubicación relativa para el objeto raíz.
     * @param resources Recursos para localizar el objeto raíz.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initGlobalContextMenu();

        // Configuración de celdas para la columna de Libro
        colBook.setCellValueFactory(cellData -> {
            if (cellData.getValue().getBook() != null) {
                return new SimpleStringProperty(cellData.getValue().getBook().getTitle());
            } else {
                return new SimpleStringProperty("Desconocido");
            }
        });

        // Configuración de celdas para fecha y contenido del comentario
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
        colComment.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCommentary()));

        try {
            // Carga inicial de usuarios para el filtro
            ObservableList<User> users = FXCollections.observableArrayList(db.getAllUsers());
            comboUsers.setItems(users);
            
            // Listener para cargar comentarios automáticamente al seleccionar un usuario
            comboUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    cargarComentarios(newVal.getUsername());
                }
            });
            LogInfo.getInstance().logInfo("Ventana de moderación de comentarios inicializada correctamente.");
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al cargar la lista de usuarios en moderación de comentarios", e);
        }
    }

    /**
     * Recupera de la base de datos y muestra en la tabla todos los comentarios 
     * asociados a un nombre de usuario específico.
     * * @param username Nombre de usuario para filtrar los comentarios.
     */
    private void cargarComentarios(String username) {
        try {
            ObservableList<Commentate> lista = FXCollections.observableArrayList(db.getCommentsByUser(username));
            tableComments.setItems(lista);
            LogInfo.getInstance().logInfo("Comentarios cargados para el usuario: " + username);
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al cargar comentarios para el usuario: " + username, e);
        }
    }

    /**
     * Inicializa el menú contextual (clic derecho) con opciones de informes, 
     * manual de usuario y salida.
     */
    private void initGlobalContextMenu() {
        globalMenu = new ContextMenu();
        globalMenu.setAutoHide(true);

        MenuItem itemInforme = new MenuItem("Generar Informe Técnico");
        itemInforme.setOnAction(this::handleInformeTecnico);

        MenuItem itemManual = new MenuItem("Manual de Usuario");
        itemManual.setOnAction(this::handleReportAction);

        MenuItem itemExit = new MenuItem("Salir");
        itemExit.setOnAction(this::handleExit);

        globalMenu.getItems().addAll(itemInforme, itemManual, new SeparatorMenuItem(), itemExit);

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
     * Solicita el cierre de la aplicación de forma controlada.
     * * @param event Evento de acción disparado.
     */
    @FXML
    private void handleExit(ActionEvent event) {
        LogInfo.getInstance().logInfo("Cierre de aplicación solicitado desde moderación de comentarios.");
        Platform.exit();
        System.exit(0);
    }

    /**
     * Muestra un cuadro de diálogo informativo sobre la versión y propósito de la ventana.
     * * @param event Evento de acción disparado.
     */
    @FXML
    private void handleAboutAction(ActionEvent event) {
        LogInfo.getInstance().logInfo("Visualización de 'Acerca de' en moderación de comentarios.");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText(null);
        alert.setContentText("BookStore App v1.0\nGestión administrativa de comentarios.");
        alert.showAndWait();
    }

    /**
     * Abre el manual de usuario en formato PDF mediante la creación de un archivo temporal.
     * * @param event Evento de acción disparado.
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
                LogInfo.getInstance().logInfo("Manual de usuario abierto desde moderación de comentarios.");
            }
        } catch (IOException e) {
            LogInfo.getInstance().logSevere("Error al abrir el manual de usuario", e);
        }
    }

    /**
     * Genera un informe técnico detallado utilizando JasperReports y lo muestra en un visor.
     * * @param event Evento de acción disparado.
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
                LogInfo.getInstance().logInfo("Informe técnico generado desde moderación de comentarios.");
            }
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al generar el informe técnico Jasper", e);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                // Error silencioso en cierre de conexión
            }
        }
    }

    /**
     * Elimina el comentario seleccionado de la tabla y de la base de datos.
     * Requiere que el usuario haya seleccionado una fila previamente.
     * * @param event Evento de acción disparado por el botón de eliminar.
     */
    @FXML
    private void handleDeleteComment(ActionEvent event) {
        Commentate selected = tableComments.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                db.deleteComment(selected);
                tableComments.getItems().remove(selected);
                LogInfo.getInstance().logInfo("Comentario eliminado correctamente por el administrador.");
                new Alert(Alert.AlertType.INFORMATION, "Comentario eliminado correctamente.").show();
            } catch (Exception e) {
                LogInfo.getInstance().logSevere("Error al eliminar el comentario de la base de datos", e);
                new Alert(Alert.AlertType.ERROR, "Error al eliminar el comentario.").show();
            }
        } else {
            LogInfo.getInstance().logWarning("Intento de eliminación de comentario sin selección previa.");
            new Alert(Alert.AlertType.WARNING, "Debes seleccionar un comentario de la tabla.").show();
        }
    }

    /**
     * Regresa a la ventana anterior de opciones para administradores.
     * * @param event Evento de acción disparado por el botón volver.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Obtenemos el stage a través del nodo raíz del layout
            Stage stage = (Stage) rootPane.getScene().getWindow();

            // Carga del recurso FXML de destino
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OptionsAdmin.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            LogInfo.getInstance().logInfo("Navegación atrás desde gestión de comentarios realizada.");
        } catch (IOException ex) {
            LogInfo.getInstance().logSevere("Error al volver a OptionsAdmin: " + ex.getMessage(), ex);
        }
    }
}