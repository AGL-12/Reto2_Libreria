package controller;

import javafx.geometry.VerticalDirection;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.Main;
import model.ClassDAO;
import model.DBImplementation;
import model.Profile;
import model.User;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import org.testfx.matcher.control.LabeledMatchers;

/**
 * Test de integración para la gestión del carrito y el historial de compras.
 * Verifica el flujo completo del usuario y la persistencia de la sesión.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ShoppingCartControllerTest extends ApplicationTest {

    private static final ClassDAO dao = new DBImplementation();
    private static final String TEST_USER = "UsuarioUnico";
    private static final String TEST_PASS = "1234";
    private static final String NOMBRE_REAL = "NombreTest";

    @BeforeClass
    public static void setupSpec() {
        // Limpieza de datos previos para asegurar un entorno de test controlado
        Profile existente = dao.logIn(TEST_USER, TEST_PASS);
        if (existente != null) {
            dao.dropOutUser(existente);
        }

        // Creación del usuario de prueba
        User nuevoUsuario = new User();
        nuevoUsuario.setUsername(TEST_USER);
        nuevoUsuario.setPassword(TEST_PASS);
        nuevoUsuario.setEmail("test@unico.com");
        nuevoUsuario.setName(NOMBRE_REAL);
        nuevoUsuario.setSurname("Apellido");
        nuevoUsuario.setTelephone("123456789");

        dao.signUp(nuevoUsuario);
    }

    @AfterClass
    public static void tearDownSpec() {
        // Borrado del usuario tras finalizar los tests para no dejar basura en la BD
        Profile existente = dao.logIn(TEST_USER, TEST_PASS);
        if (existente != null) {
            dao.dropOutUser(existente);
        }
    }

    @After
    public void tearDown() throws Exception {
        // Esto cierra la ventana de la aplicación después de cada test
        FxToolkit.cleanupStages();
        release(new KeyCode[]{}); // Libera teclas por si acaso
        release(new MouseButton[]{}); // Libera el ratón
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Inicia la aplicación desde el punto de entrada principal
        new Main().start(stage);
    }

    /**
     * Método auxiliar para realizar el login al inicio de cada test.
     */
    private void testLogIn() {
        clickOn("#btnLogIn");
        clickOn("#TextField_Username").write(TEST_USER);
        clickOn("#PasswordField_Password").write(TEST_PASS);
        clickOn("#Button_LogIn");
    }

    private void testNavegarAHistorial() {
        clickOn("#btnOption");
        clickOn("#btnHistory");
    }

    @Test
    public void test1_comprarLibro() {
        testLogIn();
        clickOn("1984");
        clickOn("#btnAddToCart");
        clickOn("Aceptar");
        sleep(1000);
        clickOn("#btnBuy");
        verifyThat("#lblTotal", isVisible());
        clickOn("#btnComprar");
        clickOn("Aceptar");
        verifyThat("#btnComprar", node -> node.isDisabled());
    }

    @Test
    public void test2_aumentarCantidadYVerificarPrecio() {
        clickOn("1984");
        clickOn("#btnAddToCart");
        clickOn("Aceptar");
        sleep(1000);
        clickOn("#btnBuy");
        clickOn("#spinnerCantidad");
        clickOn(".increment-arrow-button");
        clickOn(".increment-arrow-button");
        double precioBase = 12.00;
        String totalEsperado = "Total: " + String.format("%.2f", precioBase * 3) + " €";
        verifyThat("#lblTotal", LabeledMatchers.hasText(totalEsperado));
        clickOn("#btnComprar");
        clickOn("Aceptar");
    }

    @Test
    public void test3_eliminarProducto() {
        clickOn("1984");
        clickOn("#btnAddToCart");
        clickOn("Aceptar");
        sleep(1000);
        clickOn("#btnBuy");
        verifyThat("#btnDelete", isVisible());
        clickOn("#btnDelete");
        verifyThat("#lblTotal", LabeledMatchers.hasText("Total: 0,00 €"));
    }

    @Test
    public void test4_navegacionHistorial() {
        clickOn("#btnOption");
        clickOn("#btnHistory");
        verifyThat("#tableOrders", isVisible());
        clickOn("Navegación");
        clickOn("Carrito");
        verifyThat("#vBoxContenedorLibros", isVisible());
        verifyThat("#btnComprar", isVisible());
    }

    @Test
    public void test5_verificarPersistenciaCarrito() {
        clickOn("1984");
        clickOn("#btnAddToCart");
        clickOn("Aceptar");
        testNavegarAHistorial();
        clickOn("Sesión");
        clickOn("Cerrar Sesión");
        verifyThat("#Button_LogIn", isVisible());
        clickOn("#TextField_Username").write(TEST_USER);
        clickOn("#PasswordField_Password").write(TEST_PASS);
        clickOn("#Button_LogIn");
        testNavegarAHistorial();
        clickOn("Navegación");
        clickOn("Carrito");
        verifyThat("#vBoxContenedorLibros", isVisible());
        VBox contenedor = lookup("#vBoxContenedorLibros").queryAs(VBox.class);
        assertTrue("El carrito debe mantener los productos tras re-loguear",
                !contenedor.getChildren().isEmpty());
    }
}
