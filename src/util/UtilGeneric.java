package util;

import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

public class UtilGeneric {

    private static UtilGeneric instance;

    public static UtilGeneric getInstance() {
        if (instance == null) {
            instance = new UtilGeneric();
        }
        return instance;
    }

    public void showAlert(String message, Alert.AlertType type, String title) {
        Alert alert = new Alert(type);
        alert.setTitle("Gestión de librería");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void exit() {
        LogInfo.getInstance().logInfo("Cerrando Aplicacion.");
        Platform.exit();
        System.exit(0);
    }

    public void aboutAction() {
        LogInfo.getInstance().logInfo("Mostrando ventana Acerca de Nosotros.");

        String mensaje = "Book&Bugs - Gestión de Librería v1.0\n\n"
                + "Desarrollado por el equipo de desarrollo:\n"
                + "• Alex\n"
                + "• Unai\n"
                + "• Ander\n"
                + "• Mikel\n\n"
                + "Proyecto Reto 2 - 2025";

        // Creamos la alerta
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de Nosotros");
        alert.setHeaderText("Información del Proyecto");
        alert.setContentText(mensaje);

        // --- AÑADIR LOGO ---
        try {
            String imagePath = "/images/Book&Bugs_Logo.png";
            // Usar getResourceAsStream es más seguro para comprobar nulos antes de crear la Image
            java.io.InputStream imageStream = getClass().getResourceAsStream(imagePath);

            if (imageStream != null) {
                Image logo = new Image(imageStream);
                ImageView imageView = new ImageView(logo);

                // Ajustar tamaño para que no salga gigante
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);

                // Poner la imagen a la izquierda del texto
                alert.setGraphic(imageView);

                // Opcional: Poner el logo también en el icono de la ventana de la alerta
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(logo);
            } else {
                LogInfo.getInstance().logWarning("No se encontró la imagen del logo en la ruta: " + imagePath);
            }

        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error no crítico al cargar el logo en About: " + e.getMessage(), e);
        }

        alert.showAndWait();
    }

        public void helpAction() {
        try {
            // 1. Ruta al PDF del Manual (Asegúrate de que el archivo se llame así en src/documents)
            String resourcePath = "/documents/Manual_Usuario.pdf";

            // 2. Cargar archivo
            InputStream pdfStream = getClass().getResourceAsStream(resourcePath);

            if (pdfStream == null) {
                LogInfo.getInstance().logWarning("Error: No se encuentra el manual en: " + resourcePath);
                showAlert("Error: No se encuentra el manual en: " + resourcePath, Alert.AlertType.ERROR, "Error");
                return;
            }

            // 3. Crear temporal y abrir
            File tempFile = File.createTempFile("Manual_Usuario", ".pdf");
            tempFile.deleteOnExit();
            Files.copy(pdfStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            } else {
                LogInfo.getInstance().logWarning("No se puede abrir el PDF automáticamente.");
                showAlert("No se puede abrir el PDF automáticamente.", Alert.AlertType.ERROR, "Error");
            }

        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al cargar el pdf: " + e.getMessage(), e);
            showAlert("Error al abrir el manual: " + e.getMessage(), Alert.AlertType.ERROR, "Error");
        }
    }

    public void getJasperReport() {
        Connection con = null;
        try {
            // 1. CONEXIÓN A BASE DE DATOS
            // Ajusta el usuario y contraseña a los tuyos de MySQL
            String url = "jdbc:mysql://localhost:3306/bookstore?useSSL=false&serverTimezone=UTC";
            String user = "root";
            String pass = "abcd*1234"; // <--- ¡PON TU CONTRASEÑA AQUÍ!

            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, user, pass);

            // 2. CARGAR EL ARCHIVO .JRXML
            // Busca en el paquete 'reports' que creamos anteriormente
            InputStream reportStream = getClass().getResourceAsStream("/reports/InformeTecnico.jrxml");

            if (reportStream == null) {
                LogInfo.getInstance().logWarning("Error: No se encuentra /reports/InformeTecnicoDB.jrxml");
                showAlert("Error: No se encuentra /reports/InformeTecnicoDB.jrxml", Alert.AlertType.ERROR, "Error");
                return;
            }

            // 3. COMPILAR Y LLENAR EL INFORME
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // Llenamos el informe pasando la conexión 'con' para que ejecute la Query SQL
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, con);

            // 4. MOSTRAR VISOR
            JasperViewer.viewReport(jasperPrint, false); // false = no cerrar la app al salir

        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al cargar el Informe Tecnico: " + e.getMessage(), e);
            showAlert("Error al generar informe: " + e.getMessage(), Alert.AlertType.ERROR, "Error");
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
            }
        }
    }
}
