package controller;

import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import javafx.stage.Stage;
import main.Main;
import model.ClassDAO;
import model.DBImplementation;
import model.Order;
import model.UserSession;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.api.FxToolkit;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import org.testfx.util.WaitForAsyncUtils;

public class ShoppingHistoryControllerTest extends ApplicationTest {

    ClassDAO dao = new DBImplementation();

    @Before
    public void setUp() throws Exception {
        UserSession.getInstance().setUser(null);
    }

    @After
    public void tearDown() throws Exception {
        release(new KeyCode[]{});
        release(new MouseButton[]{});
        FxToolkit.hideStage();
    }

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @Test
    public void testSignUp() {
        clickOn("#btnLogIn");
        clickOn("#Button_SignUp");
        clickOn("#textFieldEmail").write("ejemplo@correo.com");
        clickOn("#textFieldUsername").write("Ejemplo");
        clickOn("#textFieldName").write("Ejemplo");
        clickOn("#textFieldSurname").write("Prueba");
        clickOn("#textFieldTelephone").write("123456789");
        clickOn("#textFieldCardN").write("1234567890123456");
        clickOn("#textFieldPassword").write("1234");
        clickOn("#textFieldCPassword").write("1234");
        clickOn("#rButtonM");
        clickOn("#buttonSignUp");
        // Nota: Asegúrate de que tras el registro el sistema te devuelva al login o main
    }

    private void testLogIn() {
        clickOn("#btnLogIn");
        clickOn("#TextField_Username").write("Ejemplo");
        clickOn("#PasswordField_Password").write("1234");
        clickOn("#Button_LogIn");
    }

    private void testNavegarAHistorial() {
        testLogIn();
        // Esperamos a que el Header procese el modo de usuario logueado
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnOption"); // Botón del Header para ir al menú
        clickOn("#btnAllPurchase"); // ID real en MenuWindow para ir al historial
    }

    @Test
    public void tablaVacia() {
        // Primero registramos para asegurar que el usuario existe y no tiene compras
        testSignUp();
        testNavegarAHistorial();

        verifyThat("#tableOrders", isVisible());
        TableView<Order> tabla = lookup("#tableOrders").queryAs(TableView.class);
        assertTrue("La tabla debería estar vacía para un usuario recién creado", tabla.getItems().isEmpty());
    }

    @Test
    public void testCompraYVerificacionEnHistorial() {
        testLogIn();

        // 1. Añadir un libro (Ajusta el selector si .vbox-libro no es el ID de estilo)
        clickOn("#tileBooks"); // Contenedor de libros en MainBookStore
        // Suponiendo que haces clic en el primer libro que aparece
        clickOn("#btnBuy"); // En el Header para ir a comprar

        // 2. Finalizar compra en ShoppingCart.fxml
        verifyThat("#btnBuy", isVisible()); // Validar botón de comprar
        clickOn("#btnBuy"); // Acción de compra en el carrito

        // 3. Navegar al Historial
        clickOn("#btnOption");
        clickOn("#btnAllPurchase");

        // 4. Verificación
        verifyThat("#tableOrders", isVisible());
        TableView<Order> tabla = lookup("#tableOrders").queryAs(TableView.class);
        assertTrue("El historial debería contener el pedido recién realizado",
                tabla.getItems().size() > 0);
    }
}
