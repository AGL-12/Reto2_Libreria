package controller;

import java.util.List;
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
import model.UserSession;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import org.testfx.matcher.control.LabeledMatchers;
import util.HibernateUtil;

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

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @Before
    public void setUp() {
        // Limpiamos la sesión de la aplicación
        UserSession.getInstance().cleanUserSession();
        // Creamos el usuario en la BD antes de cada test para asegurar un entorno limpio
        crearUsuarioDePrueba();
        sleep(500);
    }

    @After
    public void tearDown() throws Exception {
        UserSession.getInstance().cleanUserSession();
        // Borramos el usuario y sus datos de la BD al finalizar cada test
        eliminarUsuarioDePrueba();
        FxToolkit.cleanupStages();
        release(new KeyCode[]{});
        release(new MouseButton[]{});

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
        testLogIn();
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
        testLogIn();
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
        testLogIn();
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
        testLogIn();
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
    
     private void crearUsuarioDePrueba() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Profile existing = (Profile) session.createQuery("FROM Profile WHERE username = :u")
                    .setParameter("u", TEST_USER).uniqueResult();

            if (existing == null) {
                User testUser = new User();
                testUser.setUsername(TEST_USER);
                testUser.setPassword(TEST_PASS);
                testUser.setEmail("test@history.com");
                testUser.setName("Tester");
                testUser.setSurname("History");
                testUser.setTelephone("600000000");
                testUser.setCardNumber("1234567890123456");
                testUser.setGender("Other");
                session.save(testUser);
                tx.commit();
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    private void eliminarUsuarioDePrueba() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            // 1. Borrar Comentarios
            session.createQuery("DELETE FROM Commentate WHERE user.username = :u")
                    .setParameter("u", TEST_USER).executeUpdate();

            // 2. Borrar Pedidos (Cascada manual)
            List<Integer> orderIds = session.createQuery("SELECT o.idOrder FROM Order o WHERE o.user.username = :u")
                    .setParameter("u", TEST_USER).list();

            if (orderIds != null && !orderIds.isEmpty()) {
                session.createQuery("DELETE FROM Contain WHERE id.idOrder IN (:ids)")
                        .setParameterList("ids", orderIds).executeUpdate();
                session.createQuery("DELETE FROM Order WHERE idOrder IN (:ids)")
                        .setParameterList("ids", orderIds).executeUpdate();
            }

            // 3. Borrar Perfil
            session.createQuery("DELETE FROM Profile WHERE username = :u")
                    .setParameter("u", TEST_USER).executeUpdate();

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}
