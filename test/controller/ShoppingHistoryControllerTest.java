package controller;

import java.util.List;
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
import model.User;
import model.UserSession;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.api.FxToolkit;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import org.testfx.util.WaitForAsyncUtils;
import util.HibernateUtil;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ShoppingHistoryControllerTest extends ApplicationTest {

    // Unificamos las variables de usuario para evitar confusiones
    private static final String TEST_USER = "userTester";
    private static final String TEST_PASS = "1234";

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

    @Test
    public void test1_verificarHistorialVacio() {
        testLogIn();
        testNavegarAHistorial();
        
        verifyThat("#tableOrders", isVisible());
        TableView<Order> tabla = lookup("#tableOrders").queryAs(TableView.class);
        assertTrue("El historial debe estar vacío", tabla.getItems().isEmpty());
        
        // Volver a la tienda
        clickOn("#btnVolver");
    }

    @Test
    public void test2_verificarHistorialLleno() {
        testLogIn();
        
        // Compramos un libro para que el historial tenga datos
        clickOn("1984");
        sleep(500);
        clickOn("#btnAddToCart");
        cerrarAlertas();
        
        clickOn("#btnBuy");
        sleep(500);
        clickOn("#btnComprar");
        cerrarAlertas();
        
        testNavegarAHistorial();
        TableView<Order> tabla = lookup("#tableOrders").queryAs(TableView.class);
        assertTrue("La tabla no debe estar vacía tras la compra", !tabla.getItems().isEmpty());
    }

    @Test
    public void test3_verificarDetallesPedidos() {
        testLogIn();
        
        // Primero generamos una compra
        clickOn("1984");
        clickOn("#btnAddToCart");
        cerrarAlertas();
        clickOn("#btnBuy");
        clickOn("#btnComprar");
        cerrarAlertas();

        testNavegarAHistorial();
        TableView<Order> tabla = lookup("#tableOrders").queryAs(TableView.class);
        
        if (!tabla.getItems().isEmpty()) {
            // Doble clic en la fila para abrir el detalle
            doubleClickOn(".table-row-cell");
            sleep(800);
            verifyThat("#tableItems", isVisible());
            verifyThat("#lblTitulo", isVisible());
        } else {
            Assert.fail("No se pudo generar un pedido para probar los detalles.");
        }
    }

    private void testLogIn() {
        // Aseguramos que estamos en la pantalla de login
        if (lookup("#btnLogIn").tryQuery().isPresent()) {
            clickOn("#btnLogIn");
        }
        
        clickOn("#TextField_Username").write(TEST_USER);
        clickOn("#PasswordField_Password").write(TEST_PASS);
        clickOn("#Button_LogIn");
        WaitForAsyncUtils.waitForFxEvents();
        sleep(1000); // Tiempo para cargar la tienda
    }

    private void testNavegarAHistorial() {
        clickOn("#btnOption");
        sleep(500);
        clickOn("#btnHistory");
        sleep(500);
    }

    private void cerrarAlertas() {
        sleep(500);
        if (lookup(".dialog-pane").tryQuery().isPresent()) {
            press(KeyCode.ENTER).release(KeyCode.ENTER);
            sleep(300);
        }
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