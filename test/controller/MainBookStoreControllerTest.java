package controller;

import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import main.Main;
import model.ClassDAO;
import model.DBImplementation;
import model.Profile;
import model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.framework.junit.ApplicationTest;
import static org.testfx.matcher.base.NodeMatchers.*;
import util.HibernateUtil;

/**
 *
 * @author Alexander
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MainBookStoreControllerTest extends ApplicationTest {

    ClassDAO dao = new DBImplementation();

    // --- CREDENCIALES ---
    private static final String TEST_USER_LOGIN = "userTest";
    private static final String TEST_USER_PASS = "1234";
    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASS = "1234";

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @Test
    public void test01_HeaderEstado() {
        verifyThat("#btnLogIn", isVisible());

        Label name = lookup("#lblUserName").query();
        Assert.assertEquals("Bienvenido", name.getText());

        verifyThat("#txtSearch", isVisible());

        System.out.println("Test Header: OK. Estado inicial correcto (LogIn visible).");
    }

    @Test
    public void test02_CargaLibros() {
        int total = dao.getAllBooks().size();
        // 1. Verificamos que el contenedor de libros es visible
        verifyThat("#tileBooks", isVisible());

        // 2. Comprobamos que hay libros dentro
        TilePane tileBooks = lookup("#tileBooks").query();
        Assert.assertEquals("no muestra todos los libros", total, tileBooks.getChildren().size());
    }

    @Test
    public void test03_HeaderSearch() {
        TilePane tileBooks = lookup("#tileBooks").query();
        int totalLibros = tileBooks.getChildren().size();

        Assert.assertTrue("Se necesitan libros en la BD para probar el buscador", totalLibros > 0);

        clickOn("#txtSearch");

        write("Harry");

        // ESPERAR AL THREAD (PauseTransition de 0.5s)
        sleep(1000);

        verifyThat("#btnSearch", isVisible());

        int librosFiltrados = tileBooks.getChildren().size();
        System.out.println("Libros tras buscar 'Harry': " + librosFiltrados);

        Assert.assertTrue("El filtro debería mantener o reducir la cantidad, nunca aumentar", librosFiltrados <= totalLibros);

        // Caso: Borrar búsqueda
        clickOn("#btnSearch");
        sleep(1000); // Esperar a que se restauren
        verifyThat("#btnSearch", isInvisible());

        Assert.assertEquals("Al borrar, deben volver todos los libros",
                totalLibros,
                tileBooks.getChildren().size());

        System.out.println("Test Buscador: OK.");
    }

    @Test
    public void test04_ClickBook() {
        clickOn("1984");
        clickOn("#btnBackMain");
    }

    @Test
    public void test05_LogIn() {
        clickOn("#btnLogIn");
        clickOn("#Button_SignUp");
        clickOn("#textFieldEmail").write("test@test.test");
        clickOn("#textFieldUsername").write("test");
        clickOn("#textFieldName").write("testname");
        clickOn("#textFieldSurname").write("testsurname");
        clickOn("#textFieldTelephone").write("101010101");
        clickOn("#textFieldCardN").write("0101010101010101");
        clickOn("#textFieldPassword").write("1234");
        clickOn("#textFieldCPassword").write("1234");
        clickOn("#rButtonO");
        clickOn("#buttonSignUp");
        sleep(1000);
        type(KeyCode.ENTER);
    }

    @Test
    public void test06_HeaderLogged() {
        verifyThat("#btnOption", isVisible());
        verifyThat("#btnLogOut", isVisible());
        Label name = lookup("#lblUserName").query();
        Assert.assertEquals("testname", name.getText());
        verifyThat("#txtSearch", isVisible());
    }

    @Test
    public void test07_Delete() {
        clickOn("#btnOption");
        clickOn("#btnDeleteAccount");
        clickOn("#TextFieldPassword").write("1234");
        clickOn("#Button_Delete");
        type(KeyCode.ENTER);
        clickOn("volver");
        verifyThat("#btnLogIn", isVisible());

        Label name = lookup("#lblUserName").query();
        Assert.assertEquals("Bienvenido", name.getText());

        verifyThat("#txtSearch", isVisible());

        System.out.println("Test Header: OK. Estado inicial correcto (LogIn visible).");
    }

    @Test
    public void test08_MenusAndActions() {
        clickOn("#menuAyuda");
        clickOn("#iAcercaDe");
        type(KeyCode.ENTER);
        clickOn("#menuAcciones");
        clickOn("#iManual");
        clickOn("#menuAcciones");
        clickOn("#iJasper");
    }

    @Test
    public void test09_ContextMenu() {
        rightClickOn("#mainRoot");
        clickOn("Limpiar Busqueda");
        rightClickOn("#mainRoot");
        clickOn("Acerca de Nosotros");
        type(KeyCode.ENTER);
        clickOn("#menuArchivo");
        clickOn("#iSalir");
    }

    private void crearUsuarioDePrueba() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Profile existing = (Profile) session.createQuery("FROM Profile WHERE username = :u")
                    .setParameter("u", TEST_USER_LOGIN).uniqueResult();

            if (existing == null) {
                User testUser = new User();
                testUser.setUsername(TEST_USER_LOGIN);
                testUser.setPassword(TEST_USER_PASS);
                testUser.setEmail("test@junit.com");
                testUser.setName("Tester");
                testUser.setSurname("Selenium");
                testUser.setTelephone("600000000");
                testUser.setCardNumber("1234567890123456");
                testUser.setGender("Other");
                session.save(testUser);
                tx.commit();
                System.out.println(">> Usuario temporal creado: " + TEST_USER_LOGIN);
            }
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
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

            session.createQuery("DELETE FROM Commentate WHERE user.username = :u")
                    .setParameter("u", TEST_USER_LOGIN)
                    .executeUpdate();

            List<Integer> orderIds = session.createQuery("SELECT idOrder FROM Order WHERE idUsuer.username = :u")
                    .setParameter("u", TEST_USER_LOGIN).list();

            if (orderIds != null && !orderIds.isEmpty()) {
                session.createQuery("DELETE FROM Contain WHERE order.idOrder IN (:ids)")
                        .setParameterList("ids", orderIds).executeUpdate();

                session.createQuery("DELETE FROM Order WHERE idOrder IN (:ids)")
                        .setParameterList("ids", orderIds).executeUpdate();
            }

            int deleted = session.createQuery("DELETE FROM Profile WHERE username = :u")
                    .setParameter("u", TEST_USER_LOGIN)
                    .executeUpdate();

            tx.commit();
            if (deleted > 0) {
                System.out.println(">> Usuario temporal y todos sus datos eliminados.");
            }

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            System.out.println("Aviso: Limpieza de datos incompleta: " + e.getMessage());
        } finally {
            session.close();
        }
    }
}
