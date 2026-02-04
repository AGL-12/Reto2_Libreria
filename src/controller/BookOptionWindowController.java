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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import model.UserSession;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

/**
 * Controlador de la ventana de gestión del modo del Crud de libros.
 * Permite elegir entre crear y modificar libros en la proxima ventana.
 * @author unai azkorra
 */
public class BookOptionWindowController {

    @FXML
    private Button btnReturn;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnModify;

    /**
     * Metodo que se lanza al presionar el boton de añadir libro.
     * @param event 
     */
    @FXML
    private void createBook(ActionEvent event) {
        abrirCRUD("create");
    }

    /**
     * Metodo que se lanza al presionar el boton de modificar libro.
     * @param event 
     */
    @FXML
    private void modifyBook(ActionEvent event) {
        abrirCRUD("modify");
    }

    /**
     * Metodos para el menú superior (Acciones).
     */
    @FXML
    private void handleCreateAction(ActionEvent event) {
        abrirCRUD("create");
    }

    @FXML
    private void handleModifyAction(ActionEvent event) {
        abrirCRUD("modify");
    }

    /**
     * Metodo para retroceder en el flujo de ventanas.
     * @param event 
     */
    @FXML
    private void Return(ActionEvent event) {
        navigateTo("/view/OptionsAdmin.fxml");
    }

    /**
     * Metodos de soporte para el menú (Archivo y Ayuda).
     */
    @FXML
    private void handleExit(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleAboutAction(ActionEvent event) {
        mostrarAlerta("Acerca de Nosotros", "BookStore App v1.0\nGestión de administración.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleReportAction(ActionEvent event) {
        abrirPDF("/documents/Manual_Usuario.pdf");
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
            mostrarAlerta("Error", "No se pudo generar el informe técnico.", Alert.AlertType.ERROR);
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) {}
        }
    }

    /**
     * Lógica compartida para abrir la ventana CRUD.
     */
    private void abrirCRUD(String modo) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookCRUDWindow.fxml"));
            Parent root = fxmlLoader.load();
            BookCRUDWindowController controllerWindow = fxmlLoader.getController();

            controllerWindow.setModo(modo);           

            Stage stage = (Stage) btnAdd.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestión de Libros - " + modo.toUpperCase());
            stage.show();

        } catch (IOException ex) {
            Logger.getLogger(BookOptionWindowController.class.getName()).log(Level.SEVERE, "Error al abrir BookCRUDWindow", ex);
        }
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) btnReturn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(BookOptionWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void abrirPDF(String path) {
        try {
            InputStream pdfStream = getClass().getResourceAsStream(path);
            if (pdfStream != null) {
                File tempFile = File.createTempFile("Manual_Usuario", ".pdf");
                tempFile.deleteOnExit();
                Files.copy(pdfStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(tempFile);
                }
            }
        } catch (IOException e) {
            Logger.getLogger(BookOptionWindowController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}