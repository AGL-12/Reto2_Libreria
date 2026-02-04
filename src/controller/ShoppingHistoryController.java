/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import controller.OrderDetailController;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Order;
import model.ClassDAO;
import model.DBImplementation;
import model.Profile;
import model.UserSession;

/**
 * FXML Controller class
 *
 * @author ander
 */
public class ShoppingHistoryController implements Initializable {

    @FXML
    private TableColumn<Order, Integer> colId;
    @FXML
    private TableColumn<Order, java.sql.Timestamp> colFecha;
    @FXML
    private TableColumn<Order, Float> colTotal;

    private List<Order> allShops = new ArrayList<>();
    private final ClassDAO dao = new DBImplementation();

    @FXML
    private Button btnVolver;
    @FXML
    private TableView<Order> tableOrders;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Vinculamos columnas con los atributos del modelo Order.java
        colId.setCellValueFactory(new PropertyValueFactory<>("idOrder"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // 2. Obtenemos el usuario de la sesión
        Profile userLogged = UserSession.getInstance().getUser();

        if (userLogged != null) {
            // 3. Cargamos los datos desde el DAO
            allShops = dao.getHistory(userLogged.getId());

            if (allShops != null) {
                tableOrders.getItems().setAll(allShops);
            }
        }
    }

    @FXML
    private void clickFila(MouseEvent event) {
        // AQUÍ ESTÁ EL TRUCO:
        // Preguntamos al evento: "¿El contador de clics es igual a 2?"
        if (event.getClickCount() == 2) {

            // 1. Cogemos el pedido que el usuario ha pinchado
            Order pedido = tableOrders.getSelectionModel().getSelectedItem();

            // 2. Seguridad: Si ha hecho doble clic en una fila válida (no en blanco)
            if (pedido != null) {
                // 3. Abrimos la ventana
                abrirDetalle(pedido);
            }
        }
    }

    private void abrirDetalle(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OrderDetail.fxml"));
            Parent root = loader.load();

            // Pasar los datos al controlador de la ventana nueva
            OrderDetailController controller = loader.getController();
            controller.setOrderData(order);

            // Mostrar la ventana
            Stage stage = new Stage();
            stage.setTitle("Detalle Pedido " + order.getIdOrder());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void volver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MenuWindow.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // En src/controller/ShoppingHistoryController.java
    @FXML
    private void handleBackToBooks(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/BookView.fxml"));
            // Usamos tblHistory para obtener la ventana actual
            Stage stage = (Stage) tableOrders.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
