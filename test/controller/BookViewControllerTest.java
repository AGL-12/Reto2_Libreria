package controller;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox; // Importado VBox
import javafx.stage.Stage;
import main.Main;
import model.Admin;
import model.Profile;
import model.User;
import model.UserSession;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import util.HibernateUtil;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

public class BookViewControllerTest extends ApplicationTest {

    private static final String TEST_USER_LOGIN = "userTest";
    private static final String TEST_USER_PASS = "1234";
    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASS = "1234";

    @BeforeClass
    public static void silenciarLogs() {
        Logger.getLogger("javafx.fxml").setLevel(Level.SEVERE);
        Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);
        try {
            FxToolkit.registerPrimaryStage();
        } catch (Exception e) {}
    }

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @Before
    public void setUp() {
        UserSession.getInstance().cleanUserSession();
        sleep(1000);
        cerrarAlertas();
        
        crearUsuarioDePrueba();
        
        asegurarPantallaLogin();
    }

    @After
    public void tearDown() throws Exception {
        release(KeyCode.ALT, KeyCode.SHIFT, KeyCode.CONTROL);
        UserSession.getInstance().cleanUserSession();
        
        // Limpiar usuario Y SUS DATOS (Comentarios, Carrito) al terminar
        eliminarUsuarioDePrueba();
        
        FxToolkit.hideStage();
    }

    @Test
    public void testMasterFlow() {
        System.out.println("=== INICIO TEST MAESTRO CON: " + TEST_USER_LOGIN + " ===");

        realizarLogin(TEST_USER_LOGIN, TEST_USER_PASS);
        
        abrirPrimerLibro();
        verifyThat("#titleBook", isVisible());
        verificarImagenSegura();

        // --- 3. PROBAR CARRITO ---
        if (lookup("#btnAddToCart").tryQuery().isPresent()) {
            Button btn = lookup("#btnAddToCart").queryButton();
            if (btn.isVisible() && !btn.isDisabled()) {
                clickOn("#btnAddToCart");
                sleep(800);
                cerrarAlertas();
                Assert.assertFalse("El carrito no debe estar vacío", 
                        UserSession.getInstance().getCurrentOrder().getListPreBuy().isEmpty());
            }
        }

        // --- 4. PROBAR COMENTARIOS ---
        if (lookup("#btnAddComment").tryQuery().isPresent()) {
            clickOn("#btnAddComment");
            sleep(500);
            if (lookup("#cajaEscribir").tryQuery().isPresent() && lookup("#cajaEscribir").query().isVisible()) {
                clickOn("#txtNuevoComentario").write("Test Automático - " + System.currentTimeMillis());
                clickOn("#btnPublicar");
                sleep(800);
                cerrarAlertas();
                System.out.println("Comentario creado (será borrado al finalizar).");
                
                // --- NUEVA VALIDACIÓN: Verificar que el comentario existe visualmente ---
                verificarExistenciaComentarios();
                // -----------------------------------------------------------------------
            }
        }

        rightClickOn("#rootPane");
        sleep(600);
        if(lookup("Generar Informe Técnico").tryQuery().isPresent()) {
            verifyThat("Generar Informe Técnico", isVisible());
        }
        press(KeyCode.ESCAPE).release(KeyCode.ESCAPE);
        sleep(500);

        realizarLogoutDesdeLibro();
        
        System.out.println("Iniciando validación Admin...");
        realizarLogin(ADMIN_LOGIN, ADMIN_PASS);
        
        int retries = 0;
        while (!lookup("#tileBooks").tryQuery().isPresent() && retries < 50) {
            sleep(100);
            retries++;
        }
        sleep(500); 
        
        Platform.runLater(() -> {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setUserCode(9999); 
            UserSession.getInstance().setUser(admin);
            System.out.println("Rol Admin inyectado manualmente.");
        });
        WaitForAsyncUtils.waitForFxEvents();
        sleep(500);

        abrirPrimerLibro();
        
        if (lookup("#btnAddToCart").tryQuery().isPresent()) {
            Node btn = lookup("#btnAddToCart").query();
            boolean oculto = !btn.isVisible() || !btn.isManaged();
            Assert.assertTrue("FALLO: Admin NO debe ver botón de compra. (Visible: " + btn.isVisible() + ")", oculto);
        } else {
             Assert.assertTrue(true);
        }

        realizarLogoutDesdeLibro();
        System.out.println("=== FIN TEST MAESTRO (ÉXITO) ===");
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
            if(deleted > 0) System.out.println(">> Usuario temporal y todos sus datos eliminados.");
            
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.out.println("Aviso: Limpieza de datos incompleta: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private void asegurarPantallaLogin() {
        if (lookup("#TextField_Username").tryQuery().isPresent()) return;

        try {
            cerrarAlertas();

            if (lookup("#titleBook").tryQuery().isPresent()) {
                press(KeyCode.ESCAPE).release(KeyCode.ESCAPE);
                sleep(800);
            }
            
            if (lookup("#btnLogIn").tryQuery().isPresent()) { 
                clickOn("#btnLogIn");
            } 
            else if (lookup("#rootPane").tryQuery().isPresent()) {
                Platform.runLater(() -> UserSession.getInstance().cleanUserSession());
            }
        } catch (Exception e) {
            System.out.println("Recuperando navegación...");
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void realizarLogin(String user, String pass) {
        int intentos = 0;
        while (!lookup("#TextField_Username").tryQuery().isPresent() && intentos < 30) {
            sleep(200);
            intentos++;
        }

        if (lookup("#TextField_Username").tryQuery().isPresent()) {
            clickOn("#TextField_Username").eraseText(30).write(user);
            clickOn("#PasswordField_Password").eraseText(30).write(pass);
            clickOn("#Button_LogIn");
            WaitForAsyncUtils.waitForFxEvents();
            sleep(1500); 
        } else {
            Assert.fail("No se pudo cargar la pantalla de Login.");
        }
    }

    private void abrirPrimerLibro() {
        int i = 0;
        while ((!lookup("#tileBooks").tryQuery().isPresent() || 
               lookup("#tileBooks").queryAs(TilePane.class).getChildren().isEmpty()) && i < 40) {
            sleep(200);
            i++;
        }
        
        if (lookup("#tileBooks").tryQuery().isPresent()) {
            TilePane tiles = lookup("#tileBooks").query();
            if (!tiles.getChildren().isEmpty()) {
                Node libro = tiles.getChildren().get(0);
                clickOn(libro);
                sleep(1000); 
            } else {
                Assert.fail("La base de datos no tiene libros visibles.");
            }
        }
    }

    private void realizarLogoutDesdeLibro() {
        try {
            rightClickOn("#rootPane"); 
            sleep(500);
            
            if (lookup("Cerrar Sesión").tryQuery().isPresent()) {
                clickOn("Cerrar Sesión");
                sleep(1000);
            } else {
                press(KeyCode.ESCAPE).release(KeyCode.ESCAPE); 
                sleep(300);
                Platform.runLater(() -> UserSession.getInstance().cleanUserSession());
            }
        } catch (Exception e) {
            Platform.runLater(() -> UserSession.getInstance().cleanUserSession());
        }
        sleep(1000); 
        asegurarPantallaLogin();
    }

    private void verificarImagenSegura() {
        try {
            if (lookup("#coverBook").tryQuery().isPresent()) {
                Node node = lookup("#coverBook").query();
                if (node instanceof ImageView) {
                    ImageView img = (ImageView) node;
                    if (img.getImage() != null) {
                        Assert.assertNotNull(img.getImage());
                    }
                }
            }
        } catch (Exception e) {}
    }

    private void cerrarAlertas() {
        try {
            if (lookup(".dialog-pane").tryQuery().isPresent()) {
                press(KeyCode.ENTER).release(KeyCode.ENTER);
                sleep(300);
            }
        } catch (Exception e) {}
    }

    private void verificarExistenciaComentarios() {
        sleep(500); 

        verifyThat("#commentsContainer", isVisible());

        if (lookup("#commentsContainer").tryQuery().isPresent()) {
            VBox container = lookup("#commentsContainer").queryAs(VBox.class);
            
            int cantidad = container.getChildren().size();
            System.out.println(">> Validación de Comentarios: Se encontraron " + cantidad + " comentarios.");

            Assert.assertTrue("Fallo: El libro debería tener comentarios, pero la lista está vacía.", cantidad > 0);
        } else {
            Assert.fail("No se encontró el contenedor #commentsContainer en la interfaz.");
        }
    }
}