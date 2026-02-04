/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Book;
import model.ClassDAO;
import model.DBImplementation;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import util.LogInfo;

/**
 *
 * @author Alexander
 */
public class MainBookStoreController {

    @FXML
    private BorderPane mainRoot;
    @FXML
    private TilePane tileBooks;
    @FXML
    public HeaderController headerController;

    private List<Book> allBooks = new ArrayList<>();
    private final ClassDAO dao = new DBImplementation();

    @FXML
    private MenuItem menuItemReport;
    // El temporizador para el delay
    private PauseTransition pause;
    private ContextMenu globalMenu;

    public void initialize() {
        initContexMenu();
        allBooks = dao.getAllBooks();

        showBooks(allBooks);
        // 2. CONFIGURAR EL DELAY (Por ejemplo, 0.5 segundos)
        // Esto crea un timer que espera 500ms antes de disparar su acción.
        pause = new PauseTransition(Duration.seconds(0.5));

        // Qué pasa cuando el timer termina (se acabó el tiempo de espera)
        pause.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Obtenemos el texto actual del header y buscamos
                String textoABuscar = headerController.getSearchTextField().getText().trim();
                LogInfo.getInstance().logInfo("Buscando en BD: " + textoABuscar);
                searchBooks(textoABuscar);
            }
        });

        // 3. CONECTAR EL LISTENER
        if (headerController != null) {
            headerController.getSearchTextField().textProperty().addListener((obs, oldVal, newVal) -> {
                // MAGIA: Cada vez que escribes una letra...

                // A. Reiniciamos el timer desde cero (si estaba contando, se para y vuelve a empezar)
                pause.playFromStart();

                // Resultado: Si escribes rápido "Harry", el timer se reinicia 5 veces
            });
        }
    }

    private void searchBooks(String text) {
        if (text == null || text.trim().isEmpty()) {
            // Si borran el texto, mostramos la lista maestra entera
            showBooks(allBooks);
            return;
        }

        String busqueda = text.toLowerCase();

        List<Book> filteredBooks = filterBook(busqueda);

        showBooks(filteredBooks);
    }

    private void showBooks(List<Book> allBooks) {
        tileBooks.getChildren().clear();

        try {
            for (Book lib : allBooks) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookItem.fxml"));
                VBox libroBox = fxmlLoader.load();

                BookItemController itemController = fxmlLoader.getController();

                // Aquí 'lib' ya viene con el avgValuation calculado desde el DAO
                itemController.setData(lib);

                tileBooks.getChildren().add(libroBox);
            }
        } catch (IOException ex) {
            LogInfo.getInstance().logSevere("Error carga/render de libros", ex);
        }
    }

    private List<Book> filterBook(String busqueda) {
        List<Book> resultados = new ArrayList<>();

        for (Book b : allBooks) {
            boolean coincideTitulo = b.getTitle().toLowerCase().contains(busqueda);
            boolean coincideAutor = b.getAuthor() != null
                    && b.getAuthor().toString().toLowerCase().contains(busqueda);
            boolean coincideISBN = String.valueOf(b.getISBN()).contains(busqueda);
            if (coincideTitulo || coincideAutor || coincideISBN) {
                resultados.add(b);
            }
        }

        return resultados;
    }

    private void initContexMenu() {
        // 1. Inicializamos el menú
        globalMenu = new ContextMenu();
        globalMenu.setAutoHide(true);

        // --- Opción 1: Añadir al Carrito ---
        MenuItem searchClear = new MenuItem("Limpiar Busqueda");
        searchClear.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                headerController.clearSearch();
            }
        });

        // =========================================================
        // --- NUEVO: OPCIÓN INFORME TÉCNICO (JASPER) ---
        // =========================================================
        MenuItem itemInforme = new MenuItem("Generar Informe Técnico");
        itemInforme.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleInformeTecnico(event);
            }
        });
        // =========================================================

        MenuItem itemExit = new MenuItem("Salir");
        itemExit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleExit(event);
            }
        });

        MenuItem itemManual = new MenuItem("Manual de Usuario");
        itemManual.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleHelpAction(event);
            }
        });

        MenuItem itemAbout = new MenuItem("Acerca de...");
        itemAbout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleAboutAction(event);
            }
        });

        // 2. AÑADIR TODO AL MENÚ EN ORDEN
        globalMenu.getItems().addAll(
                searchClear, // 1. Comprar
                itemInforme, // 2. Informe Técnico (NUEVO)
                new SeparatorMenuItem(), // Línea separadora
                itemExit, // 4. Salir
                new SeparatorMenuItem(), // Línea separadora
                itemManual, // 5. Ayuda
                itemAbout // 6. About
        );

        // 3. Asignar eventos al panel principal (rootPane)
        if (mainRoot != null) {
            mainRoot.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    // Mostrar el menú donde se hizo clic
                    globalMenu.show(mainRoot, event.getScreenX(), event.getScreenY());
                    event.consume();
                }
            });

            // Ocultar el menú si se hace clic izquierdo fuera
            mainRoot.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.isPrimaryButtonDown() && globalMenu.isShowing()) {
                        globalMenu.hide();
                    }
                }
            });
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        // Cierra la aplicación completamente
        javafx.application.Platform.exit();
        System.exit(0);
    }

    // --- MÉTODO NUEVO: GENERAR INFORME JASPER ---
    @FXML
    private void handleInformeTecnico(ActionEvent event) {
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
                showAlert("Error: No se encuentra /reports/InformeTecnicoDB.jrxml", Alert.AlertType.ERROR);
                return;
            }

            // 3. COMPILAR Y LLENAR EL INFORME
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // Llenamos el informe pasando la conexión 'con' para que ejecute la Query SQL
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, con);

            // 4. MOSTRAR VISOR
            JasperViewer.viewReport(jasperPrint, false); // false = no cerrar la app al salir

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error al generar informe: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
            }
        }
    }

    private void handleHelpAction(ActionEvent event) {
        try {
            // 1. Ruta al PDF del Manual (Asegúrate de que el archivo se llame así en src/documents)
            String resourcePath = "/documents/Manual_Usuario.pdf";

            // 2. Cargar archivo
            InputStream pdfStream = getClass().getResourceAsStream(resourcePath);

            if (pdfStream == null) {
                showAlert("Error: No se encuentra el manual en: " + resourcePath, Alert.AlertType.ERROR);
                return;
            }

            // 3. Crear temporal y abrir
            File tempFile = File.createTempFile("Manual_Usuario", ".pdf");
            tempFile.deleteOnExit();
            Files.copy(pdfStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            } else {
                showAlert("No se puede abrir el PDF automáticamente.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error al abrir el manual: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAboutAction(ActionEvent event) {
        showAlert("BookStore App v1.0\nDesarrollado por Mikel\nProyecto Reto 2", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Gestión de librería");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleReportAction(ActionEvent event) {

        // --- HEMOS BORRADO EL BLOQUE IF DE SEGURIDAD ---
        // Ahora entra cualquier usuario (Admin o Normal)
        try {
            // CAMBIO: Ahora apuntamos al Manual de Usuario en vez de al Informe de Stock
            String resourcePath = "/documents/Manual_Usuario.pdf";

            InputStream pdfStream = getClass().getResourceAsStream(resourcePath);

            if (pdfStream == null) {
                showAlert("Error: No se encuentra el archivo en: " + resourcePath, Alert.AlertType.ERROR);
                return;
            }

            File tempFile = File.createTempFile("Manual_Usuario", ".pdf");
            tempFile.deleteOnExit();

            Files.copy(pdfStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            } else {
                showAlert("Error: No se puede abrir el visor de PDF.", Alert.AlertType.ERROR);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error al abrir el manual: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
