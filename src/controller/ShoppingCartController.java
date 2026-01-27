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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import model.Book;
import model.ClassDAO;
import model.Contain;
import model.DBImplementation;
import model.Order;
import model.Profile;
import model.User;
import model.UserSession;

/**
 * FXML Controller class
 *
 * @author ander
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

    // Si usas el Header incluido
    @FXML
    public HeaderController headerController;

    // --- VARIABLES GLOBALES ---
    private List<Book> libros = new ArrayList<>(); // Lista para cálculos de precio
    private Order cartOder = null; // Objeto del pedido actual
    private List<Contain> containLine = null;

    private final ClassDAO dao = new DBImplementation();

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

    public void actualizarPrecioTotal() {
        double total = 0;

        // Recorremos todas las tarjetas de libros que hay en la pantalla del carrito
        for (javafx.scene.Node nodo : vBoxContenedorLibros.getChildren()) {
            if (nodo instanceof VBox) {
                // Recuperamos el controlador que guardamos antes con "setUserData"
                PreOrderController itemCtrl = (PreOrderController) nodo.getUserData();

                if (itemCtrl != null) {
                    double precioUnidad = itemCtrl.getBook().getPrice();
                    int cantidad = itemCtrl.getCantidadSeleccionada(); // Lo que diga el Spinner

                    total += (precioUnidad * cantidad);
                }
            }
        }
        lblTotal.setText("Total: " + String.format("%.2f", total) + " €");
    }

    @Override
    public void handle(ActionEvent event
    ) {
        actualizarPrecioTotal();
    }

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

            cargarVistaLibros();
            actualizarPrecioTotal();

        } catch (Exception e) {
            e.printStackTrace();
            // Aquí podrías mostrar una alerta de error al usuario
        }
        cargarVistaLibros();
        actualizarPrecioTotal();
    }

}
