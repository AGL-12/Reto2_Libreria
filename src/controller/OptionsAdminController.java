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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import model.UserSession;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

public class OptionsAdminController {

    @FXML
    private Button btnDeleteUser, btnEliminarComentario, btnModificarUsuario, btnLibro;

    // --- MÉTODOS DE NAVEGACIÓN ---

    @FXML
    private void opcionesLibroWindow(ActionEvent event) {
        // CAMBIO: Ahora apunta a BookOptionWindow.fxml en lugar de BookCRUDWindow.fxml
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
        navigateTo("/view/MainBookStore.fxml");
    }

    // --- MÉTODOS DEL MENÚ SUPERIOR (ACCIONES Y ARCHIVO) ---

    @FXML
    private void handleExit(ActionEvent event) {
        javafx.application.Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleAboutAction(ActionEvent event) {
        showAlert("BookStore App v1.0", "Panel de Administración de la Librería.", Alert.AlertType.INFORMATION);
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
            showAlert("Error", "No se pudo abrir el manual.", Alert.AlertType.ERROR);
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
            showAlert("Error", "No se pudo generar el informe.", Alert.AlertType.ERROR);
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) {}
        }
    }

    /**
     * Lógica de navegación genérica.
     */
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();

            Object controller = fxmlLoader.getController();
            if (controller instanceof MainBookStoreController) {
                ((MainBookStoreController) controller).headerController.setMode(UserSession.getInstance().getUser(), null);
            }

            // Usamos cualquier botón para obtener la ventana actual
            Stage stage = (Stage) btnLibro.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(OptionsAdminController.class.getName()).log(Level.SEVERE, "Error abriendo " + fxmlPath, ex);
        }
    }

    private void showAlert(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}