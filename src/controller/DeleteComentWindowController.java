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
import model.UserSession;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

/**
 * Controlador de la ventana de eliminar comentarios
 * Es una ventana que solo tiene acceso el adminsitrador
 * filtra por usuario sus comentarios
 * @author unai azkorra
 * @version 1.0
 */
public class DeleteComentWindowController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DeleteComentWindowController.class.getName());
    private DBImplementation db = new DBImplementation();

    @FXML private VBox rootPane; // Necesario para el menú contextual
    @FXML private TableView<Commentate> tableComments;
    @FXML private TableColumn<Commentate, String> colBook;    
    @FXML private TableColumn<Commentate, String> colDate;    
    @FXML private TableColumn<Commentate, String> colComment; 
    @FXML private ComboBox<User> comboUsers;

    private ContextMenu globalMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initGlobalContextMenu(); // Inicializar menú contextual

        colBook.setCellValueFactory(cellData -> {
            if (cellData.getValue().getBook() != null) {
                return new SimpleStringProperty(cellData.getValue().getBook().getTitle());
            } else {
                return new SimpleStringProperty("Desconocido");
            }
        });

        colDate.setCellValueFactory(cellData ->  new SimpleStringProperty(cellData.getValue().getFormattedDate()));

        colComment.setCellValueFactory(cellData ->  new SimpleStringProperty(cellData.getValue().getCommentary()));

        try {
            ObservableList<User> users = FXCollections.observableArrayList(db.getAllUsers());
            comboUsers.setItems(users);
            comboUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    cargarComentarios(newVal.getUsername());
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar usuarios", e);
        }
    }

    private void cargarComentarios(String username) {
        try {
            ObservableList<Commentate> lista = FXCollections.observableArrayList(db.getCommentsByUser(username));
            tableComments.setItems(lista);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar comentarios", e);
        }
    }

    // --- NUEVOS MÉTODOS PARA MENÚS ---

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
        }
        rootPane.setOnMousePressed(event -> {
                if (event.isPrimaryButtonDown() && globalMenu.isShowing()) {
                    globalMenu.hide();
                }
            });
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleAboutAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText(null);
        alert.setContentText("BookStore App v1.0\nGestión administrativa de comentarios.");
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
            LOGGER.log(Level.SEVERE, "Error en informe", e);
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) { }
        }
    }

    // --- LÓGICA ORIGINAL ---

    @FXML
    private void handleDeleteComment(ActionEvent event) {
        Commentate selected = tableComments.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                db.deleteComment(selected);
                tableComments.getItems().remove(selected);
                new Alert(Alert.AlertType.INFORMATION, "Comentario eliminado correctamente.").show();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error al eliminar el comentario.").show();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Debes seleccionar un comentario de la tabla.").show();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OptionsAdmin.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al volver", ex);
        }
    }
}