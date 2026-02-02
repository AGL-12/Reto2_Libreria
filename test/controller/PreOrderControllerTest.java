package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;
import model.Book;
import org.junit.Test;
import static org.junit.Assert.*;
import org.testfx.framework.junit.ApplicationTest;

/**
 * Test para el controlador de los elementos del carrito (PreOrder).
 * No utiliza lambdas ni toca la base de datos real.
 */
public class PreOrderControllerTest extends ApplicationTest {

    private PreOrderController controller;
    private Book libroPrueba;

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Preparamos un libro de prueba (en memoria, no va a la BD)
        libroPrueba = new Book();
        libroPrueba.setTitle("Libro Carrito");
        libroPrueba.setPrice(20.0f);
        libroPrueba.setStock(10);
        libroPrueba.setCover("fondo_libro.jpg"); // Usamos una imagen que ya existe

        // 2. Cargamos el FXML de la fila del carrito
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PreOrder.fxml"));
        Parent root = (Parent) loader.load();
        controller = (PreOrderController) loader.getController();

        // 3. Inicializamos los datos en el controlador
        // Pasamos 'null' como ShoppingCartController para simplificar el test
        controller.setData(libroPrueba, null);

        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Verifica que los datos del libro se muestran correctamente en la fila.
     */
    @Test
    public void testVerificarDatosLibro() {
        // Comprobar Título
        Label lblTitle = (Label) lookup("#lblTitle").query();
        assertEquals("El título debe ser correcto", "Libro Carrito", lblTitle.getText());

        // Comprobar Precio (formateado con dos decimales según el controlador)
        Label lblPrice = (Label) lookup("#lblPrice").query();
        assertEquals("El precio debe estar formateado", "20,00 €", lblPrice.getText());
        
        // Verificar que el controlador devuelve el libro correcto
        assertNotNull("El libro no debe ser nulo", controller.getBook());
        assertEquals("Debe ser el mismo título", "Libro Carrito", controller.getBook().getTitle());
    }

    /**
     * Verifica que el selector de cantidad funciona.
     */
    @Test
    public void testGetCantidadSeleccionada() {
        // Buscamos el Spinner
        Spinner<Integer> spinner = (Spinner<Integer>) lookup("#spinnerCantidad").query();
        
        // Por defecto en setData se pone a 1
        assertEquals("La cantidad inicial debe ser 1", 1, controller.getCantidadSeleccionada());
        
        // Simulamos un cambio de valor en el hilo de la UI
        // Como no podemos usar lambdas, lo hacemos directamente ya que ApplicationTest lo permite
        spinner.getValueFactory().setValue(5);
        
        assertEquals("La cantidad seleccionada ahora debe ser 5", 5, controller.getCantidadSeleccionada());
    }
}