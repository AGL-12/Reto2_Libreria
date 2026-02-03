package controller;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.VerticalDirection;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import javafx.stage.Stage;
import main.Main;
import model.Book;
import model.ClassDAO;
import model.Contain;
import model.DBImplementation;
import model.Order;
import model.Profile;
import model.User; // Importa tu modelo User
import model.UserSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.api.FxToolkit;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import org.testfx.util.WaitForAsyncUtils;

public class ShoppingHistoryControllerTest extends ApplicationTest {

    private static ClassDAO dao = new DBImplementation();
    private static final String TEST_USER = "UsuarioUnico";
    private static final String TEST_PASS = "1234";
    private Book testBook;
    private Order testOrder;

    @BeforeClass
    public static void setupSpec() {
        // 1. Limpieza preventiva
        Profile existente = dao.logIn(TEST_USER, TEST_PASS);
        if (existente != null) {
            dao.dropOutUser(existente);
        }

        // 2. CREACIÓN DIRECTA EN BASE DE DATOS
        // Creamos el objeto directamente usando tu modelo para que ya exista al iniciar los tests
        User nuevoUsuario = new User();
        nuevoUsuario.setUsername(TEST_USER);
        nuevoUsuario.setPassword(TEST_PASS);
        nuevoUsuario.setEmail("test@unico.com");
        nuevoUsuario.setName("Nombre");
        nuevoUsuario.setSurname("Apellido");
        nuevoUsuario.setTelephone("123456789");
        // Asegúrate de que estos setters coincidan con los de tu clase User/Profile

        dao.signUp(nuevoUsuario);
    }

    @AfterClass
    public static void tearDownSpec() {
        // Eliminación final tras todos los tests
        Profile existente = dao.logIn(TEST_USER, TEST_PASS);
        if (existente != null) {
            dao.dropOutUser(existente);
        }
    }

    @After
    public void tearDown() throws Exception {
        // Esto cierra la ventana de la aplicación después de cada test
        FxToolkit.hideStage();
        release(new KeyCode[]{}); // Libera teclas por si acaso
        release(new MouseButton[]{}); // Libera el ratón
    }

    @Before
    public void setup() throws Exception {
        // Esto vuelve a lanzar la aplicación antes de cada test
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(Main.class);
    }

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @Test
    public void verificarHistorial() {
        testLogIn();
        testNavegarAHistorial();
        verifyThat("#tableOrders", isVisible());
        TableView<Order> tabla = lookup("#tableOrders").queryAs(TableView.class);
        assertTrue("El historial debe estar vacío", tabla.getItems().isEmpty());
        testLogOut();
        testLogIn();
        String tituloLibro = "1984"; // Asegúrate de que este libro existe en tu BD
        scroll(VerticalDirection.DOWN);
        clickOn(tituloLibro);
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnAddToCart");
        clickOn("Aceptar"); 
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnBuy");
        clickOn("#btnComprar");
        clickOn("Aceptar");
        testNavegarAHistorial();
        tabla = lookup("#tableOrders").queryAs(TableView.class);
        assertTrue("La tabla no debe estar vacía tras la compra", !tabla.getItems().isEmpty());
    }

    private void testLogIn() {
        // Al empezar, el usuario ya existe gracias al @BeforeClass
        clickOn("#btnLogIn");
        clickOn("#TextField_Username").write(TEST_USER);
        clickOn("#PasswordField_Password").write(TEST_PASS);
        clickOn("#Button_LogIn");
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void testNavegarAHistorial() {
        clickOn("#btnOption");
        clickOn("#btnHistory");
    }

    private void testLogOut() {
        // 1. Si estamos en la ventana de Historial, pulsamos Volver para ir al Menú
        if (lookup("#btnVolver").tryQuery().isPresent()) {
            clickOn("#btnVolver");
            WaitForAsyncUtils.waitForFxEvents();
        }

        // 2. En el Menú (MenuWindow), abrimos el panel de opciones
        // Según tus tests previos, el ID es btnOption
        clickOn("#btnBack");

        // 3. Pulsamos el botón de cerrar sesión
        // Verifica en MenuWindow.fxml si el ID es btnLogOut o btnSalir
        clickOn("#btnLogOut");
        WaitForAsyncUtils.waitForFxEvents();
    }

}
