package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Book;
import model.ClassDAO;
import model.Contain;
import model.DBImplementation;
import model.Order;
import model.Profile;
import model.UserSession;

/**
 * Controlador de la vista del Carrito de la Compra. Gestiona la visualización
 * de los libros añadidos al pedido actual (en estado de preventa), el cálculo
 * dinámico de precios totales y la finalización de la compra.
 *
 * * @author ander
 * @version 1.0
 */
public class ShoppingCartController implements Initializable, EventHandler<ActionEvent> {

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
        Profile userLogged = UserSession.getInstance().getUser();

        if (userLogged != null) {
            // 1. Cargar el objeto pedido completo desde la BD
            cartOder = dao.cartOrder(userLogged.getId());

            // 2. Si existe pedido y tiene líneas, cargamos la vista
            if (cartOder != null && cartOder.getListPreBuy() != null && !cartOder.getListPreBuy().isEmpty()) {
                cargarVistaLibros();
            } else {
                lblTotal.setText("El carrito está vacío.");
                btnComprar.setDisable(true);
            }
        }
    }

    /**
     * Limpia el contenedor visual y genera dinámicamente las filas de libros
     * (PreOrder) basándose en la lista de productos del pedido actual.
     */
    private void cargarVistaLibros() {
        Profile userLogged = UserSession.getInstance().getUser();
        // 1. LIMPIEZA OBLIGATORIA: Borramos lo visual y lo lógico para empezar de cero
        vBoxContenedorLibros.getChildren().clear();
        libros.clear();
        cartOder = dao.cartOrder(userLogged.getId());

        // 2. Llenamos la lista GLOBAL 'libros' con los datos del pedido
        // (Antes creabas una lista local 'books' aquí, por eso fallaba el precio luego)
        for (Contain c : cartOder.getListPreBuy()) {
            if (c.getBook() != null) {
                libros.add(c.getBook());
            }
        }

        try {
            // 3. Generamos la parte visual para cada libro
            for (Book lib : libros) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/PreOrder.fxml"));
                //VBox libroBox = fxmlLoader.load();
                HBox libroBox = fxmlLoader.load();

                // Configuramos el controlador pequeño
                PreOrderController preOrderController = fxmlLoader.getController();
                preOrderController.setData(lib, this);
                libroBox.setUserData(preOrderController);

                // Añadimos al VBox principal
                vBoxContenedorLibros.getChildren().add(libroBox);
            }

            // 4. Calculamos el total inicial
            actualizarPrecioTotal();

        } catch (IOException ex) {
            Logger.getLogger(ShoppingCartController.class.getName()).log(Level.SEVERE, "Error cargando vista", ex);
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

        // Recorremos todas las tarjetas de libros
        for (javafx.scene.Node nodo : vBoxContenedorLibros.getChildren()) {
            // CAMBIO: Verificar si es HBox, ya que PreOrder.fxml usa un HBox como raíz
            if (nodo instanceof HBox) {
                // Recuperamos el controlador desde el userData
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
            // Sincronizamos las cantidades del Spinner con el objeto pedido antes de enviar a BD
            for (Node nodo : vBoxContenedorLibros.getChildren()) {
                PreOrderController ctrl = (PreOrderController) nodo.getUserData();
                for (Contain c : cartOder.getListPreBuy()) {
                    if (c.getBook().getISBN() == ctrl.getBook().getISBN()) {
                        c.setQuantity(ctrl.getCantidadSeleccionada());
                    }
                }
            }
            // Usamos el ID del pedido que ya tenemos cargado en memoria
            // Asegúrate de que tu DAO tiene el método 'buy(int idPedido)'
            boolean exito = dao.buy(cartOder);

            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Compra Exitosa", "Pedido realizado correctamente.");

                // Limpiamos pantalla y datos
                vBoxContenedorLibros.getChildren().clear();
                libros.clear();
                cartOder = null;
                lblTotal.setText("Total: 0.00 €");
                btnComprar.setDisable(true);
                UserSession.getInstance().setOrder(null);

            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo finalizar la compra.");
            }
        } else {
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
     * * @param libroAEliminar El objeto Book que se desea retirar del pedido.
     */
    public void eliminarLibroDelCarrito(Book libroAEliminar) {
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
            UserSession.getInstance().refreshOrderAfterDeletion();
            cargarVistaLibros();
            actualizarPrecioTotal();

        } catch (Exception e) {
            e.printStackTrace();
            // Aquí podrías mostrar una alerta de error al usuario
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

    // --- MÉTODOS DE GESTIÓN (Ya existentes en tu código, asegúrate de que el FXML los llame) ---

    @FXML
    public void handleExit(ActionEvent event) {
        // Cierra la aplicación
        javafx.application.Platform.exit();
        System.exit(0);
    }

    @FXML
    public void handleLogOut(ActionEvent event) {
        // Limpia la sesión y vuelve al Login
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

    @FXML
    private void handleReportAction(ActionEvent event) {
        // Genera el informe JasperReports
        // (Mantén el código de conexión JDBC que ya tienes implementado)
    }

    @FXML
    private void handleHelpAction(ActionEvent event) {
        // Abre el PDF del manual de usuario
    }

    @FXML
    private void handleAboutAction(ActionEvent event) {
        // Muestra información de la app
        showAlert("BookStore App v1.0\nDesarrollado por Mikel\nProyecto Reto 2", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    private void handleInformeTecnico(ActionEvent event) {
        // Aquí invocas tu lógica de JasperReports (JasperPrint, JasperViewer, etc.)
        mostrarAlerta(Alert.AlertType.INFORMATION, "Informe", "Generando informe técnico...");
    }

    private void showAlert(String bookStore_App_v10Desarrollado_por_MikelPr, Alert.AlertType alertType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
