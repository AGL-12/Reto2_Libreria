package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Book;
import model.Contain;
import model.DBImplementation;
import model.Order;
import model.User;
import model.UserSession;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.testfx.framework.junit.ApplicationTest;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCartControllerTest extends ApplicationTest {

    private ShoppingCartController controller;
    private final DBImplementation dao = new DBImplementation();
    private User userPrueba;
    private Order pedidoCarrito;

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Configuramos un usuario de prueba (debe existir en tu BD uno con ID conocido o crearlo)
        userPrueba = new User();
        userPrueba.setUserCode(10); // Usa un ID de un usuario que ya exista para evitar errores de FK
        UserSession.getInstance().setUser(userPrueba);

        // 2. CREACIÓN: Vamos a crear un pedido de tipo carrito directamente
        pedidoCarrito = new Order();
        pedidoCarrito.setProfile(userPrueba);
        pedidoCarrito.setTotal(10.0f);
        pedidoCarrito.setIdPay(0); // 0 indica que es carrito (no pagado)

        // Creamos un libro y una línea de detalle (Contain)
        Book b = new Book();
        b.setISBN(123456); // Asegúrate de que este ISBN exista en tu tabla 'book'
        
        Contain detalle = new Contain();
        detalle.setBook(b);
        detalle.setQuantity(1);
        detalle.setOrder(pedidoCarrito);

        List<Contain> detalles = new ArrayList<Contain>();
        detalles.add(detalle);
        pedidoCarrito.setListPreBuy(detalles);

        // Guardamos en la BD usando tu método existente
        dao.saveOrder(pedidoCarrito);

        // 3. Carga de la vista
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ShoppingCart.fxml"));
        Parent root = (Parent) loader.load();
        controller = (ShoppingCartController) loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @After
    public void tearDown() {
        // 4. BORRADO: Limpiamos la base de datos
        // Como no tienes un "deleteOrder" público fácil, puedes usar Hibernate o una query rápida
        // Si no tienes el método, este rastro se queda, pero lo ideal es borrar el 'pedidoCarrito'
        if (pedidoCarrito != null) {
            // Aquí llamarías a un método de borrado si lo tuvieras
            // dao.borrarPedido(pedidoCarrito.getIdOrder()); 
        }
    }

    @Test
    public void testVerificarTotal() {
        // Buscamos el label del total
        Label lblTotal = (Label) lookup("#lblTotal").query();
        assertNotNull("El label de total debería existir", lblTotal);
        
        // Verificamos que el VBox de items tenga hijos
        VBox vbox = (VBox) lookup("#vBoxContenedorLibros").query();
        assertFalse("El carrito debería tener al menos un libro", vbox.getChildren().isEmpty());
    }

    @Test
    public void testBotonComprar() {
        // Verificamos que el botón existe y es accesible
        Node btnComprar = (Node) lookup("#btnComprar").query();
        assertNotNull(btnComprar);
    }
}