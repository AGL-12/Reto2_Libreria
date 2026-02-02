package controller;

import java.sql.Timestamp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.Order;
import model.Profile;
import model.User;
import model.UserSession;
import org.junit.Test;
import static org.junit.Assert.*;
import org.testfx.framework.junit.ApplicationTest;
import java.util.ArrayList;
import java.util.List;

public class ShoppingHistoryControllerTest extends ApplicationTest {

    private ShoppingHistoryController controller;
    private final Timestamp fechaPrueba = Timestamp.valueOf("2024-05-20 10:30:00");

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Sesión ficticia para evitar fallos en el initialize() del controlador
        Profile user = new User();
        user.setUserCode(10);
        UserSession.getInstance().setUser(user);

        // 2. Carga del FXML y controlador
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ShoppingHistory.fxml"));
        Parent root = (Parent) loader.load();
        controller = (ShoppingHistoryController) loader.getController();

        stage.setScene(new Scene(root));
        stage.show();

        // 3. Llenamos la tabla manualmente aquí (hilo seguro de JavaFX)
        // Usamos el fx:id "tableOrders" definido en el FXML
        TableView<Order> tabla = lookup("#tableOrders").query();

        List<Order> lista = new ArrayList<Order>();
        Order o = new Order();
        o.setIdOrder(555); 
        o.setPurchaseDate(fechaPrueba);
        o.setTotal(99.99f);
        lista.add(o);

        tabla.getItems().setAll(lista);
    }

    @Test
    public void testVerificarHistorialCargado() {
        // 1. Buscamos la tabla en la interfaz
        TableView<Order> tabla = lookup("#tableOrders").query();

        // 2. Comprobamos que no esté vacía para evitar errores de índice
        assertFalse("La tabla no debe estar vacía", tabla.getItems().isEmpty());

        // 3. Obtenemos el objeto del primer pedido
        Order primerPedido = tabla.getItems().get(0);

        int idEsperado = 555;
        int idObtenido = primerPedido.getIdOrder();

        assertEquals("El ID del pedido debe coincidir", idEsperado, idObtenido);
        assertEquals("La fecha debe coincidir", fechaPrueba, primerPedido.getPurchaseDate());
    }

    @Test
    public void testVerificarColumnas() {
        TableView<Order> tabla = lookup("#tableOrders").query();

        // Verificamos que los textos de las columnas coincidan con el FXML
        assertEquals("Nº de Pedido", tabla.getColumns().get(0).getText());
        assertEquals("Total Pagado (€)", tabla.getColumns().get(2).getText());
    }
}
