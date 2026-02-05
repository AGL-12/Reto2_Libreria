package controller;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import model.UserSession;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import util.HibernateUtil;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;

/**
 * Test de integración para la vista principal.
 * Se centra en la navegación y visualización, delegando la creación de datos al backend.
 * * @author Alexander
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MainBookStoreControllerTest extends ApplicationTest {

    private final ClassDAO dao = new DBImplementation();

    // Usuario para pruebas
    private static final String TEST_USER_LOGIN = "userHistoryTest";
    private static final String TEST_USER_PASS = "1234";

    @BeforeClass
    public static void silenciarLogs() {
        Logger.getLogger("javafx.fxml").setLevel(Level.SEVERE);
        Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);
        try {
            FxToolkit.registerPrimaryStage();
        } catch (Exception e) {
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @Before
    public void setUp() {
        // 1. Limpieza de sesión JavaFX
        UserSession.getInstance().cleanUserSession();
        sleep(500);
        cerrarAlertas();

        // 2. Gestión de datos en Backend (Borrar y Crear nuevo limpio)
        eliminarUsuarioDePrueba();
        crearUsuarioDePrueba();

        // 3. Asegurar que empezamos en la pantalla principal como invitado
        asegurarEstadoInvitado();
    }

    @After
    public void tearDown() throws Exception {
        release(new KeyCode[]{});
        release(new MouseButton[]{});
        
        UserSession.getInstance().cleanUserSession();
        eliminarUsuarioDePrueba(); // Limpieza final
        
        FxToolkit.cleanupStages();
    }

    // --- TESTS ---

    @Test
    public void test01_HeaderEstadoInvitado() {
        // Verifica que se ve el botón de Login y NO el de Logout
        verifyThat("#btnLogIn", isVisible());
        
        // Verifica que NO se ve el botón de Logout (usando tryQuery para evitar excepción si no existe)
        if (lookup("#btnLogOut").tryQuery().isPresent()) {
            verifyThat("#btnLogOut", isInvisible());
        }

        Label name = lookup("#lblUserName").query();
        Assert.assertEquals("Bienvenido", name.getText());

        verifyThat("#txtSearch", isVisible());
    }

    @Test
    public void test02_CargaLibros() {
        int total = 0;
        try {
            total = dao.getAllBooks().size();
        } catch (Exception ex) {
            Assert.fail("Error BD: " + ex.getMessage());
        }
        
        verifyThat("#tileBooks", isVisible());

        TilePane tileBooks = lookup("#tileBooks").query();
        // Espera pequeña por si la carga gráfica tarda un poco
        if (tileBooks.getChildren().size() != total) {
            sleep(1000); 
        }
        Assert.assertEquals("La interfaz no muestra todos los libros de la BD", total, tileBooks.getChildren().size());
    }

    @Test
    public void test03_HeaderSearch() {
        TilePane tileBooks = lookup("#tileBooks").query();
        int totalLibros = tileBooks.getChildren().size();
        if (totalLibros == 0) return;

        // 1. Escribir búsqueda
        clickOn("#txtSearch");
        write("Harry");
        sleep(1000); // Esperar al debounce

        // 2. Verificar botón limpiar
        verifyThat("#btnSearch", isVisible());

        // 3. Verificar filtrado
        int librosFiltrados = tileBooks.getChildren().size();
        Assert.assertTrue("El filtro debería reducir la cantidad de libros", librosFiltrados <= totalLibros);

        // 4. Limpiar búsqueda
        clickOn("#btnSearch"); // Botón X
        sleep(1000); 
        verifyThat("#btnSearch", isInvisible());

        // 5. Verificar restauración
        Assert.assertEquals("Al borrar deben volver todos los libros", totalLibros, tileBooks.getChildren().size());
    }

    @Test
    public void test04_ClickBook() {
        // Busca cualquier libro visible por su clase CSS o un ID conocido
        if (lookup(".book-item").tryQuery().isPresent()) {
             clickOn(".book-item"); 
        } else {
             clickOn("1984"); // Fallback
        }
        sleep(1000); // Esperar transición a detalle
        
        // Verificar que estamos en detalle (botón volver visible) y volver
        verifyThat("#btnBackMain", isVisible());
        clickOn("#btnBackMain");
    }

    @Test
    public void test05_LoginAndHeaderUser() {
        // Realizamos el login usando el usuario creado en setUp()
        realizarLogin();

        // Verificaciones de usuario logueado
        verifyThat("#btnOption", isVisible()); // Menú hamburguesa
        verifyThat("#btnLogOut", isVisible()); // Botón salir
        verifyThat("#btnLogIn", isInvisible()); // Botón login oculto
        
        Label name = lookup("#lblUserName").query();
        Assert.assertEquals("Tester", name.getText()); // Nombre del usuario de prueba
    }

    @Test
    public void test06_MenusAndActions() {
        // Probamos que los menús se despliegan y no dan error
        clickOn("#menuAyuda");
        clickOn("#iAcercaDe");
        cerrarAlertas(); // Cerrar modal si sale
        
        clickOn("#menuAcciones");
        clickOn("#iManual");
        
        clickOn("#menuAcciones");
        clickOn("#iJasper");
    }

    @Test
    public void test07_ContextMenu() {
        // Clic derecho en el fondo
        rightClickOn("#mainRoot");
        clickOn("Limpiar Busqueda");
        
        rightClickOn("#mainRoot");
        // Soporte para ambos nombres de menú posibles
        if(lookup("Acerca de...").tryQuery().isPresent()) {
            clickOn("Acerca de...");
        } else {
            clickOn("Acerca de Nosotros");
        }
        cerrarAlertas();
    }

    // --- MÉTODOS AUXILIARES ---

    /**
     * Realiza el proceso de login completo.
     * Si la ventana no está abierta, hace clic en el botón del header.
     */
    private void realizarLogin() {

        // 1. Abrir ventana si no está abierta
        if (!lookup("#TextField_Username").tryQuery().isPresent()) {
            verifyThat("#btnLogIn", isVisible());
            clickOn("#btnLogIn");
            sleep(800); // Esperar animación
        }

        // 2. Rellenar datos
        verifyThat("#TextField_Username", isVisible());
        clickOn("#TextField_Username").write(TEST_USER_LOGIN);
        clickOn("#PasswordField_Password").write(TEST_USER_PASS);
        
        // 3. Confirmar
        clickOn("#Button_LogIn");
        WaitForAsyncUtils.waitForFxEvents();
        sleep(1500); // Esperar cierre ventana y carga usuario
    }

    private void cerrarAlertas() {
        try {
            if (lookup(".dialog-pane").tryQuery().isPresent()) {
                type(KeyCode.ENTER);
                sleep(300);
            }
        } catch (Exception e) {}
    }

    /**
     * Asegura que la aplicación está en la pantalla principal y SIN sesión iniciada.
     */
    private void asegurarEstadoInvitado() {
        // 1. Si estamos logueados (botón logout visible), salimos.
        if (lookup("#btnLogOut").tryQuery().isPresent()) {
            if (lookup("#btnLogOut").query().isVisible()) {
                clickOn("#btnLogOut");
                sleep(500);
            }
        }

        // 2. Si la ventana de login se quedó abierta (por un test fallido), la cerramos con ESC.
        if (lookup("#TextField_Username").tryQuery().isPresent()) {
            press(KeyCode.ESCAPE).release(KeyCode.ESCAPE);
            sleep(500);
        }
    }

    // --- BACKEND (Hibernate puro) ---

    private void crearUsuarioDePrueba() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            // Verificar si existe para no duplicar
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
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            // Error crítico si no podemos crear el usuario
            throw new RuntimeException("Setup Backend Fallido: " + e.getMessage());
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
                    .setParameter("u", TEST_USER_LOGIN).executeUpdate();

            // 2. Borrar Pedidos (Hijos y Padres)
            List<Integer> orderIds = session.createQuery("SELECT idOrder FROM Order WHERE idUsuer.username = :u")
                    .setParameter("u", TEST_USER_LOGIN).list();

            if (orderIds != null && !orderIds.isEmpty()) {
                session.createQuery("DELETE FROM Contain WHERE order.idOrder IN (:ids)")
                        .setParameterList("ids", orderIds).executeUpdate();
                session.createQuery("DELETE FROM Order WHERE idOrder IN (:ids)")
                        .setParameterList("ids", orderIds).executeUpdate();
            }

            // 3. Borrar Perfil
            session.createQuery("DELETE FROM Profile WHERE username = :u")
                    .setParameter("u", TEST_USER_LOGIN).executeUpdate();

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error limpieza backend: " + e.getMessage());
        } finally {
            session.close();
        }
    }
}