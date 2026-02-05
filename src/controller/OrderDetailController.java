package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.ClassDAO;
import model.Contain;
import model.DBImplementation;
import model.Order;

/**
 * Controlador de la vista de Detalles de Pedido. Se encarga de mostrar de forma
 * detallada los artículos contenidos en un pedido realizado anteriormente por
 * el usuario (historial).
 *
 * * @author ander
 * @version 1.0
 */
public class OrderDetailController implements Initializable {

    @FXML
    private Label lblTitulo;
    @FXML
    private TableView<Contain> tableItems;

    // Columnas de la tabla
    @FXML
    private TableColumn<Contain, String> colTitulo;
    @FXML
    private TableColumn<Contain, Integer> colCantidad;
    @FXML
    private TableColumn<Contain, String> colPrecioTotal;
    private final ClassDAO dao = new DBImplementation();

    /**
     * Inicializa el controlador. Se ejecuta al cargar la vista FXML.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("Abre la ventana");
    }

    /**
     * Recibe los datos de un pedido y los vincula a la tabla de la interfaz.
     * Muestra el título del libro, la cantidad comprada y el precio total de
     * esa línea.
     *
     * * @param order El objeto Order que contiene la información del pedido a
     * detallar.
     */
    void setOrderData(Order order) {
        lblTitulo.setText("Pedido Nº " + order.getIdOrder());
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("nombreLibro"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrecioTotal.setCellValueFactory(new PropertyValueFactory<>("totalEuros"));
        if (order.getListPreBuy() != null) {
            tableItems.getItems().setAll(order.getListPreBuy());

        }
    }
}
