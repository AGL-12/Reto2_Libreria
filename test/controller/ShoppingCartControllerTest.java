package controller;

import javafx.geometry.VerticalDirection;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import main.Main;
import model.ClassDAO;
import model.DBImplementation;
import model.Order;
import model.Profile;
import model.User;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.framework.junit.ApplicationTest;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.util.WaitForAsyncUtils;

public class ShoppingCartControllerTest extends ApplicationTest {

    private static ClassDAO dao = new DBImplementation();
    private static final String TEST_USER = "UsuarioUnico";
    private static final String TEST_PASS = "1234";

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

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @Test
    public void comprar() {
        testLogIn();
        String tituloLibro = "1984";
        scroll(VerticalDirection.DOWN);
        clickOn(tituloLibro);
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnAddToCart");
        clickOn("Aceptar");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnBuy");
        clickOn("#btnComprar");
        clickOn("Aceptar");
    }

    @Test
    public void aumentarCantidad() {
        testLogIn();
        String tituloLibro = "1984";
        scroll(VerticalDirection.DOWN);
        clickOn(tituloLibro);
        double precioLibro = 12.0;
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnAddToCart");
        clickOn("Aceptar");
        clickOn("#btnBuy");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn(".spinner");
        clickOn(".increment-arrow-button");
        clickOn(".increment-arrow-button");
        double totalCalculado = precioLibro * 3;
        String textoEsperado = "Total: " + String.format("%.2f", totalCalculado) + " €";
        verifyThat("#lblTotal", LabeledMatchers.hasText(textoEsperado));
        clickOn("#btnComprar");
        clickOn("Aceptar");
    }

    @Test
    public void quitar() {
        testLogIn();
        String tituloLibro = "1984";
        scroll(VerticalDirection.DOWN);
        clickOn(tituloLibro);
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnAddToCart");
        clickOn("Aceptar");
        clickOn("#btnBuy");
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat("#btnDelete", isVisible()); 
        clickOn("#btnDelete");
        WaitForAsyncUtils.waitForFxEvents();
        String totalVacio = "Total: 0,00 €";
        verifyThat("#lblTotal", LabeledMatchers.hasText(totalVacio));
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

}
