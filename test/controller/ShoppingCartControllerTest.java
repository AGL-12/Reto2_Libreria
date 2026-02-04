package controller;

import javafx.geometry.VerticalDirection;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import main.Main;
import model.ClassDAO;
import model.DBImplementation;
import model.Profile;
import model.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import static org.testfx.api.FxAssert.verifyThat;
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

    @Override
    public void start(Stage stage) throws Exception {
        // Inicia la aplicación desde el punto de entrada principal
        new Main().start(stage);
    }

    /**
     * Método auxiliar para realizar el login al inicio de cada test.
     */
    private void testLogIn() {
        clickOn("Iniciar Sesion");
        clickOn("#TextField_Username").write(TEST_USER);
        clickOn("#PasswordField_Password").write(TEST_PASS);
        clickOn("#Button_LogIn");
    }

    @Test
    public void test1_comprarLibro() {
        testLogIn();
        
        // Localizamos el libro (debe existir en la BD con este título)
        scroll(VerticalDirection.DOWN);
        clickOn("1984");
        
        // Añadir al carrito y confirmar alerta
        clickOn("#btnAddToCart");
        clickOn("Aceptar"); 
        
        // Ir a la vista del carrito desde el Header
        clickOn("#btnBuy");
        verifyThat("#lblTotal", isVisible());
        
        // Finalizar la compra
        clickOn("#btnComprar");
        clickOn("Aceptar"); // Confirmación de éxito
        
        // El botón de comprar debe quedar deshabilitado si el carrito está vacío
        verifyThat("#btnComprar", node -> node.isDisabled());
    }

    @Test
    public void test2_aumentarCantidadYVerificarPrecio() {
        testLogIn();
        clickOn("1984");
        clickOn("#btnAddToCart");
        clickOn("Aceptar");
        
        clickOn("#btnBuy");
        
        // Interactuar con el Spinner de PreOrder.fxml
        clickOn("#spinnerCantidad");
        clickOn(".increment-arrow-button"); // Incrementa a 2
        clickOn(".increment-arrow-button"); // Incrementa a 3
        
        // Suponiendo precio de 12.00€ para "1984"
        double precioBase = 12.00;
        String totalEsperado = "Total: " + String.format("%.2f", precioBase * 3) + " €";
        
        verifyThat("#lblTotal", LabeledMatchers.hasText(totalEsperado));
    }

    @Test
    public void test3_eliminarProducto() {
        testLogIn();
        clickOn("1984");
        clickOn("#btnAddToCart");
        clickOn("Aceptar");
        
        clickOn("#btnBuy");
        
        // Verificamos botón de borrado y ejecutamos
        verifyThat("#btnDelete", isVisible());
        clickOn("#btnDelete");
        
        // El total debe volver a cero tras el borrado
        verifyThat("#lblTotal", LabeledMatchers.hasText("Total: 0.00 €"));
    }

    @Test
    public void test4_navegacionHistorial() {
        testLogIn();
        
        // Navegar a Opciones -> Historial
        clickOn("#btnOption");
        clickOn("#btnHistory");
        
        // Verificaciones en la vista de Historial
        verifyThat("#tableOrders", isVisible());
        verifyThat("#infoLabel", LabeledMatchers.hasText("Aquí puedes consultar todas tus compras confirmadas:"));
        
        // Comprobar que existe al menos una fila (de la compra del test 1)
        verifyThat("#tableOrders", (TableView table) -> table.getItems().size() > 0);
        
        // Volver al menú
        clickOn("#btnVolver");
        verifyThat("#mainVBox", isVisible());
    }

    @Test
    public void test5_verificarHeaderYSesion() {
        testLogIn();
        
        // El nombre debe aparecer en el Header
        verifyThat("#lblUserName", LabeledMatchers.hasText(NOMBRE_REAL));
        
        // Cambiar de ventana para ver si la sesión persiste en el Header
        clickOn("#btnOption");
        clickOn("#btnHistory");
        verifyThat("#lblUserName", LabeledMatchers.hasText(NOMBRE_REAL));
        
        // Logout
        clickOn("#btnLogOut");
        
        // Verificar que regresamos al Login
        verifyThat("#Button_LogIn", isVisible());
    }
}