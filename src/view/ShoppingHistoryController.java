/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private TableView<Order> tableOrders;
    @FXML
    private TableColumn<Order, Integer> colId;
    @FXML
    private TableColumn<Order, java.sql.Timestamp> colFecha;
    @FXML
    private TableColumn<Order, Float> colTotal;

    private List<Order> allShops = new ArrayList<>();
    private final ClassDAO dao = new DBImplementation();

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

}
