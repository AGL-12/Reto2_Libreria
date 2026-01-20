/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * FXML Controller class
 *
 * @author ander
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
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("Abre la ventana");
    }

    void setOrderData(Order order) {
        // 1. Título de la ventana
        lblTitulo.setText("Pedido Nº " + order.getIdOrder());

        // 2. Vinculación AUTOMÁTICA (Magia de JavaFX)
        // Busca "getNombreLibro()" en Contain
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("nombreLibro"));

        // Busca "getQuantity()" en Contain
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Busca "getTotalEuros()" en Contain
        colPrecioTotal.setCellValueFactory(new PropertyValueFactory<>("totalEuros"));

        // 3. Cargar datos
        // Asumimos que la lista ya viene cargada del historial
        if (order.getListPreBuy() != null) {
            tableItems.getItems().setAll(order.getListPreBuy());

        }
    }
}
