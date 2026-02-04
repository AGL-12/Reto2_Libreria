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
import util.LogInfo;

public class DeleteComentWindowController implements Initializable {

    private DBImplementation db = new DBImplementation();

    @FXML private VBox rootPane; 
    @FXML private TableView<Commentate> tableComments;
    @FXML private TableColumn<Commentate, String> colBook, colDate, colComment; 
    @FXML private ComboBox<User> comboUsers;

    private ContextMenu globalMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initGlobalContextMenu(); 
        setupTableColumns();
        loadInitialUsers();
        LogInfo.getInstance().logInfo("Ventana de moderación de comentarios abierta por Admin.");
    }

    private void setupTableColumns() {
        colBook.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBook() != null ? cellData.getValue().getBook().getTitle() : "Desconocido"));
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
        colComment.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCommentary()));
    }

    private void loadInitialUsers() {
        try {
            ObservableList<User> users = FXCollections.observableArrayList(db.getAllUsers());
            comboUsers.setItems(users);
            comboUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) cargarComentarios(newVal.getUsername());
            });
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al cargar usuarios para moderación de comentarios", e);
        }
    }

    private void cargarComentarios(String username) {
        try {
            ObservableList<Commentate> lista = FXCollections.observableArrayList(db.getCommentsByUser(username));
            tableComments.setItems(lista);
            LogInfo.getInstance().logInfo("Cargados comentarios del usuario: " + username);
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al cargar comentarios de " + username, e);
        }
    }

    @FXML
    private void handleDeleteComment(ActionEvent event) {
        Commentate selected = tableComments.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                db.deleteComment(selected);
                tableComments.getItems().remove(selected);
                LogInfo.getInstance().logInfo("Admin eliminó comentario ID: " + selected.getId() + " del libro: " + selected.getBook().getTitle());
                new Alert(Alert.AlertType.INFORMATION, "Comentario eliminado correctamente.").show();
            } catch (Exception e) {
                LogInfo.getInstance().logSevere("Error al eliminar comentario de la base de datos", e);
                new Alert(Alert.AlertType.ERROR, "Error al eliminar el comentario.").show();
            }
        } else {
            LogInfo.getInstance().logWarning("Intento de borrado de comentario sin selección.");
            new Alert(Alert.AlertType.WARNING, "Debes seleccionar un comentario de la tabla.").show();
        }
    }

    @FXML
    private void handleInformeTecnico(ActionEvent event) {
        Connection con = null;
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookstore?useSSL=false", "root", "abcd*1234");
            InputStream reportStream = getClass().getResourceAsStream("/reports/InformeTecnico.jrxml");
            JasperPrint jp = JasperFillManager.fillReport(JasperCompileManager.compileReport(reportStream), null, con);
            JasperViewer.viewReport(jp, false);
            LogInfo.getInstance().logInfo("Informe generado desde moderación de comentarios.");
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al generar informe Jasper técnico", e);
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) { }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/OptionsAdmin.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            LogInfo.getInstance().logInfo("Volviendo al menú de opciones admin.");
        } catch (IOException ex) {
            LogInfo.getInstance().logSevere("Error al navegar de vuelta a opciones admin", ex);
        }
    }

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
        rootPane.setOnContextMenuRequested(event -> globalMenu.show(rootPane, event.getScreenX(), event.getScreenY()));
        rootPane.setOnMousePressed(event -> { if (event.isPrimaryButtonDown() && globalMenu.isShowing()) globalMenu.hide(); });
    }

    @FXML private void handleExit(ActionEvent event) { Platform.exit(); System.exit(0); }
    @FXML private void handleReportAction(ActionEvent event) {
        try {
            InputStream is = getClass().getResourceAsStream("/documents/Manual_Usuario.pdf");
            File temp = File.createTempFile("Manual", ".pdf");
            Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Desktop.getDesktop().open(temp);
            LogInfo.getInstance().logInfo("Manual abierto desde moderación de comentarios.");
        } catch (IOException e) { LogInfo.getInstance().logSevere("Error al abrir manual", e); }
    }
}