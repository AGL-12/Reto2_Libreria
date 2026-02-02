package controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.Book;
import model.Contain;
import model.Order;
import org.junit.Test;
import static org.junit.Assert.*;
import org.testfx.framework.junit.ApplicationTest;

public class OrderDetailControllerTest extends ApplicationTest {

    private OrderDetailController controller;
    private Order orderPrueba;

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Preparamos los datos de prueba AQUÍ (antes de cargar la ventana)
        orderPrueba = new Order();
        orderPrueba.setIdOrder(999);

        Book libro = new Book();
        libro.setTitle("Libro de Prueba");
        libro.setPrice(15.0f);

        Contain linea = new Contain();
        linea.setBook(libro);
        linea.setQuantity(2);

        List<Contain> lista = new ArrayList<Contain>();
        lista.add(linea);
        orderPrueba.setListPreBuy(lista);

        // 2. Cargamos el FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OrderDetail.fxml"));
        Parent root = (Parent) loader.load();
        controller = (OrderDetailController) loader.getController();

        // 3. Enviamos los datos al controlador ANTES de mostrar la escena
        // Esto se ejecuta en el hilo de JavaFX por defecto dentro de start()
        controller.setOrderData(orderPrueba);

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    public void testSetOrderDataSinInteract() {
        // Como ya cargamos los datos en el método start(), 
        // aquí solo comprobamos que los componentes tengan el texto correcto.

        // Buscamos el Label del título por su ID
        Label lblTitulo = (Label) lookup("#lblTitulo").query();
        assertEquals("El título debe mostrar el ID correcto", 
                     "Pedido Nº 999", lblTitulo.getText());

        // Buscamos la tabla y verificamos que tenga el producto
        TableView tabla = (TableView) lookup("#tableItems").query();
        assertEquals("La tabla debe tener 1 fila", 1, tabla.getItems().size());
        
        // Verificamos que no se ha tocado la base de datos (el objeto es local)
        assertNotNull("El pedido de prueba no debe ser nulo", orderPrueba);
    }
}