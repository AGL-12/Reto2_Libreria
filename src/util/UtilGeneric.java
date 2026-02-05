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
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

/**
 * Clase de utilidad genérica que centraliza funciones comunes para toda la
 * aplicación. Implementa el patrón Singleton para asegurar una única instancia
 * y acceso global a utilidades como alertas, cierre de aplicación, manuales y
 * reportes.
 *
 * @version 1.0
 */
public class UtilGeneric {

    private static UtilGeneric instance;

    /**
     * Constructor privado para evitar la instanciación directa (Patrón
     * Singleton).
     */
    private UtilGeneric() {
    }

    /**
     * Obtiene la instancia única de la clase UtilGeneric. Si no existe, la
     * crea; si existe, devuelve la actual.
     *
     * * @return La instancia única de UtilGeneric.
     */
    public static UtilGeneric getInstance() {
        if (instance == null) {
            instance = new UtilGeneric();
        }
        return instance;
    }

    /**
     * Muestra una ventana de alerta genérica de JavaFX.
     *
     * * @param message El contenido del mensaje a mostrar.
     * @param type El tipo de alerta (ERROR, INFORMATION, WARNING, etc.).
     * @param title El título de la cabecera de la alerta.
     */
    public void showAlert(String message, Alert.AlertType type, String title) {
        Alert alert = new Alert(type);
        alert.setTitle("Gestión de librería");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Cierra la aplicación de forma segura. Registra el evento en el log,
     * cierra la plataforma JavaFX y termina el proceso de la JVM.
     */
    public void exit() {
        LogInfo.getInstance().logInfo("Cerrando Aplicacion.");
        Platform.exit();
        System.exit(0);
    }

    /**
     * Muestra la ventana emergente "Acerca de Nosotros". Incluye información
     * sobre los desarrolladores del proyecto y carga el logo corporativo desde
     * los recursos.
     */
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

    /**
     * Abre el Manual de Usuario en el visor de PDF predeterminado del sistema.
     * <p>
     * El método extrae el archivo PDF desde los recursos del JAR, lo copia a un
     * archivo temporal y utiliza la clase {@link java.awt.Desktop} para
     * abrirlo.
     * </p>
     */
    public void helpAction() {
        LogInfo.getInstance().logInfo("Iniciando proceso de apertura del Manual de Usuario...");
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

    /**
     * Genera y visualiza un informe técnico de stock utilizando JasperReports.
     * <p>
     * Establece una conexión JDBC directa con la base de datos, compila el
     * archivo fuente del reporte (.jrxml) y lanza el visor JasperViewer.
     * </p>
     */
    public void getJasperReport() {
        LogInfo.getInstance().logInfo("Generando informe técnico JasperReports...");
        Connection con = null;
        try {
            String url = "jdbc:mysql://localhost:3306/bookstore?useSSL=false&serverTimezone=UTC";
            String user = "root";
            String pass = "abcd*1234";

            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, user, pass);

            // CARGAR EL ARCHIVO .JRXML
            // Busca en el paquete 'reports' que creamos anteriormente
            InputStream reportStream = getClass().getResourceAsStream("/reports/InformeTecnico.jrxml");

            if (reportStream == null) {
                LogInfo.getInstance().logWarning("Error: No se encuentra /reports/InformeTecnicoDB.jrxml");
                showAlert("Error: No se encuentra /reports/InformeTecnicoDB.jrxml", Alert.AlertType.ERROR, "Error");
                return;
            }

            // COMPILAR Y LLENAR EL INFORME
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // Llenamos el informe pasando la conexión 'con' para que ejecute la Query SQL
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, con);

            // MOSTRAR VISOR
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

    /**
     * Ajusta el tamaño de la portada del libro y aplica un recorte centrado
     * (Center Crop) para mantener la estética de la interfaz.
     *
     * @param imageView El contenedor donde se mostrará la imagen.
     * @param image La imagen original del libro.
     * @param targetWidth Ancho objetivo.
     * @param targetHeight Alto objetivo.
     */
    public void cutOutImage(ImageView imageView, Image image, double targetWidth, double targetHeight) {
        // Establecemos el tamaño final que tendrá el ImageView
        imageView.setFitWidth(targetWidth);
        imageView.setFitHeight(targetHeight);

        // Algoritmo "Center Crop"
        double originalWidth = image.getWidth();
        double originalHeight = image.getHeight();

        double scaleX = targetWidth / originalWidth;
        double scaleY = targetHeight / originalHeight;

        // Elegimos el factor de escala mayor para asegurar que llenamos todo el hueco
        double scale = Math.max(scaleX, scaleY);

        // Calculamos el Viewport (la ventana de recorte sobre la imagen original)
        double viewportWidth = targetWidth / scale;
        double viewportHeight = targetHeight / scale;

        // Centramos el recorte (x, y)
        double viewportX = (originalWidth - viewportWidth) / 2;
        double viewportY = (originalHeight - viewportHeight) / 2;

        // Aplicamos la imagen y el recorte
        imageView.setImage(image);
        imageView.setViewport(new Rectangle2D(viewportX, viewportY, viewportWidth, viewportHeight));
        imageView.setSmooth(true); // Suavizado para mejor calidad
        imageView.setPreserveRatio(false); // Importante: desactivar para que obedezca al viewport
    }
}
