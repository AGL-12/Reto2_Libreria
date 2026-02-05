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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler; // Necesario para clases anónimas
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent; // Necesario para clic derecho
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Book;
import model.ClassDAO;
import model.Contain;
import model.DBImplementation;
import model.Order;
import model.Profile;
import model.UserSession;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import util.LogInfo;

/**
 * Controlador de la vista del Carrito de la Compra. Gestiona la visualización
 * de los libros añadidos al pedido actual (en estado de preventa), el cálculo
 * dinámico de precios totales y la finalización de la compra.
 *
 * * @author ander
 * @version 1.0
 */
public class ShoppingCartController implements Initializable, EventHandler<ActionEvent> {

    private final LogInfo logger = LogInfo.getInstance();
    @FXML
    private VBox vBoxContenedorLibros;
    @FXML
    private Label lblTotal;
    @FXML
    private VBox vBoxResumen;
    @FXML
    private Button btnComprar;
    @FXML
    private MenuBar menuBar;

    // Si usas el Header incluido
    public HeaderController headerController;

    // --- VARIABLES GLOBALES ---
    private List<Book> libros = new ArrayList<>(); // Lista para cálculos de precio
    private Order cartOder = null; // Objeto del pedido actual
    private List<Contain> containLine = null;

    private final ClassDAO dao = new DBImplementation();

    /**
     * Inicializa la ventana cargando el pedido actual del usuario desde la base
     * de datos. Si el carrito contiene elementos, procede a generar la vista.
     *
     * * @param url La ubicación relativa del archivo FXML.
     * @param rb Los recursos utilizados para localizar el objeto raíz.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.logInfo("Accediendo a la ventana ShoppingCart.");
        Profile userLogged = UserSession.getInstance().getUser();

        if (userLogged != null) {
            logger.logInfo("Cargando carrito para el usuario: " + userLogged.getName());
            cartOder = dao.cartOrder(userLogged.getId());
            if (cartOder != null && cartOder.getListPreBuy() != null && !cartOder.getListPreBuy().isEmpty()) {
                cargarVistaLibros();
            } else {
                logger.logInfo("El carrito del usuario está vacío.");
                lblTotal.setText("El carrito está vacío.");
                btnComprar.setDisable(true);
            }
        } else {
            logger.logWarning("Intento de acceso al carrito sin sesión de usuario activa.");
        }

        final ContextMenu cartMenu = new ContextMenu();
        MenuItem itemComprar = new MenuItem("Finalizar Compra");
        MenuItem itemLimpiar = new MenuItem("Limpiar Vista");

        itemComprar.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleComprar(event);
            }
        });

        itemLimpiar.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                vBoxContenedorLibros.getChildren().clear();
                lblTotal.setText("Total: 0.00 €");
                btnComprar.setDisable(true);
            }
        });

        cartMenu.getItems().addAll(itemComprar, itemLimpiar);

        vBoxContenedorLibros.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                cartMenu.show(vBoxContenedorLibros, event.getScreenX(), event.getScreenY());
            }
        });
    }

    /**
     * Limpia el contenedor visual y genera dinámicamente las filas de libros
     * (PreOrder) basándose en la lista de productos del pedido actual.
     */
    private void cargarVistaLibros() {
        Profile userLogged = UserSession.getInstance().getUser();
        vBoxContenedorLibros.getChildren().clear();
        libros.clear();
        cartOder = dao.cartOrder(userLogged.getId());
        for (Contain c : cartOder.getListPreBuy()) {
            if (c.getBook() != null) {
                libros.add(c.getBook());
            }
        }
        vBoxContenedorLibros.setSpacing(15); // Espacio entre cada fila de libro
        vBoxContenedorLibros.setFillWidth(true);
        try {
            logger.logInfo("Generando vista dinámica de libros en el carrito.");
            for (Book lib : libros) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/PreOrder.fxml"));
                HBox libroBox = fxmlLoader.load();
                PreOrderController preOrderController = fxmlLoader.getController();
                preOrderController.setData(lib, this);
                libroBox.setUserData(preOrderController);
                vBoxContenedorLibros.getChildren().add(libroBox);
            }
            actualizarPrecioTotal();

        } catch (IOException ex) {
            logger.logSevere("Error crítico al cargar la vista de libros (PreOrder.fxml).", ex);
        }
    }

    /**
     * Recorre todos los elementos visuales del carrito para calcular el precio
     * total multiplicando el precio unitario de cada libro por la cantidad
     * seleccionada en su Spinner. Actualiza la etiqueta de texto del total en
     * la interfaz.
     */
    public void actualizarPrecioTotal() {
        double total = 0;

        for (javafx.scene.Node nodo : vBoxContenedorLibros.getChildren()) {
            if (nodo instanceof HBox) {
                PreOrderController itemCtrl = (PreOrderController) nodo.getUserData();

                if (itemCtrl != null) {
                    double precioUnidad = itemCtrl.getBook().getPrice();
                    int cantidad = itemCtrl.getCantidadSeleccionada();

                    total += (precioUnidad * cantidad);
                }
            }
        }
        lblTotal.setText("Total: " + String.format("%.2f", total) + " €");
    }

    /**
     * Implementación de la interfaz EventHandler. Este método es llamado por
     * los PreOrderControllers cuando el usuario cambia la cantidad en el
     * Spinner, forzando un recalculo del total.
     *
     * * @param event Evento de acción capturado.
     */
    @Override
    public void handle(ActionEvent event) {
        actualizarPrecioTotal();
    }

    /**
     * Gestiona la lógica de finalización de compra. Sincroniza las cantidades
     * elegidas por el usuario con el objeto de la base de datos y marca el
     * pedido como pagado.
     *
     * * @param event El evento de pulsación del botón comprar.
     */
    @FXML
    private void handleComprar(ActionEvent event
    ) {
        if (cartOder != null) {
            logger.logInfo("Iniciando proceso de compra para el pedido ID: " + cartOder.getIdOrder());
            for (Node nodo : vBoxContenedorLibros.getChildren()) {
                PreOrderController ctrl = (PreOrderController) nodo.getUserData();
                for (Contain c : cartOder.getListPreBuy()) {
                    if (c.getBook().getISBN() == ctrl.getBook().getISBN()) {
                        c.setQuantity(ctrl.getCantidadSeleccionada());
                    }
                }
            }
            boolean exito = dao.buy(cartOder);

            if (exito) {
                logger.logInfo("Compra finalizada con éxito. Pedido ID: " + cartOder.getIdOrder());
                mostrarAlerta(Alert.AlertType.INFORMATION, "Compra Exitosa", "Pedido realizado correctamente.");
                vBoxContenedorLibros.getChildren().clear();
                libros.clear();
                cartOder = null;
                lblTotal.setText("Total: 0.00 €");
                btnComprar.setDisable(true);
                UserSession.getInstance().setOrder(null);

            } else {
                logger.logSevere("Fallo en la operación de compra en la base de datos.", null);
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo finalizar la compra.");
            }
        } else {
            logger.logWarning("Se pulsó comprar pero no existe un objeto de pedido activo.");
            mostrarAlerta(Alert.AlertType.WARNING, "Vacío", "No hay nada para comprar.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    /**
     * Elimina un libro específico del carrito de la compra tanto de la base de
     * datos como de la vista actual.
     *
     * @param libroAEliminar El objeto Book que se desea retirar del pedido.
     */
    public void eliminarLibroDelCarrito(Book libroAEliminar) {
        logger.logInfo("Eliminando libro '" + libroAEliminar.getTitle() + "' (ISBN: " + libroAEliminar.getISBN() + ") del carrito.");
        Profile userLogged = UserSession.getInstance().getUser();
        try {
            // 1. Obtienes la orden (que ya trae su List<Contain> cargada por Hibernate)
            Order cartOrder = dao.cartOrder(userLogged.getId());

            // 2. Buscamos el objeto Contain preciso dentro de la lista de la orden
            Contain lineaABorrar = null;
            for (Contain c : cartOrder.getListPreBuy()) {
                if (c.getBook().getISBN() == libroAEliminar.getISBN()) {
                    lineaABorrar = c;
                    break;
                }
            }

            dao.removeBookFromOrder(lineaABorrar);
            logger.logInfo("Libro eliminado correctamente de la base de datos.");
            UserSession.getInstance().refreshOrderAfterDeletion();
            cargarVistaLibros();
            actualizarPrecioTotal();

        } catch (Exception e) {
            logger.logSevere("Error al intentar eliminar un libro del pedido.", e);
        }
        cargarVistaLibros();
        actualizarPrecioTotal();
    }

    /**
     * Navega a la vista principal de libros (BookView).
     */
    @FXML
    private void handleBackToBooks(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BookView.fxml"));
            Parent root = loader.load();

            // Cambiamos la escena usando el Stage actual
            Stage stage = (Stage) vBoxContenedorLibros.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error al cargar la vista de libros: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Navega a la vista del historial de compras.
     */
    @FXML
    private void handleViewHistory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ShoppingHistory.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) vBoxContenedorLibros.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error al cargar el historial: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Finaliza la ejecución de la aplicación.
     *
     * @param event Evento de acción disparado.
     */
    @FXML
    public void handleExit(ActionEvent event) {
        // Cierra la aplicación
        javafx.application.Platform.exit();
        System.exit(0);
    }

    /**
     * Cierra la sesión del usuario actual y redirige a la pantalla de Login.
     *
     * @param event Evento de acción disparado.
     */
    @FXML
    public void handleLogOut(ActionEvent event) {
        logger.logInfo("Usuario cerrando sesión desde el carrito.");
        UserSession.getInstance().cleanUserSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LogInWindow.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) vBoxContenedorLibros.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Abre el manual de usuario en formato PDF utilizando el visor
     * predeterminado del sistema.
     *
     * * @param event Evento de acción disparado.
     */
    @FXML
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

    /**
     * Genera un informe técnico en PDF utilizando JasperReports basado en el
     * historial. Establece conexión JDBC y carga el recurso .jrxml.
     *
     * * @param event El evento de acción disparado por el botón o menú.
     */
    @FXML
    private void handleInformeTecnico(ActionEvent event) {
        logger.logInfo("Generando Informe Técnico Jasper desde el carrito.");
        Connection con = null;
        try {
            logger.logInfo("Informe generado y mostrado correctamente.");
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
            logger.logSevere("Error al procesar el informe JasperReports.", e);
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

    /**
     * Muestra una ventana de diálogo de alerta al usuario.
     *
     * * @param message Mensaje a mostrar.
     * @param alertType Tipo de alerta (ERROR, INFORMATION, etc).
     */
    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
