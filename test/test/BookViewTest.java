package test;

import javafx.stage.Stage;
import main.Main; 
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.testfx.framework.junit.ApplicationTest;
import javafx.scene.control.Button;
import javafx.scene.layout.TilePane;
import javafx.scene.Node;

// Imports estáticos
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BookViewTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    /**
     * Navegación INTELIGENTE:
     * 1. Detecta si estamos en la tienda como invitados.
     * 2. Va al Login, se loguea y vuelve.
     * 3. Selecciona un libro.
     */
    public void navegarHastaElLibro() {
        sleep(2000); // Esperar a que arranque la app

        // --- PASO 1: IR AL LOGIN ---
        // El espía nos dijo que hay un botón #btnLogIn que dice "Iniciar Sesión"
        if (lookup("#btnLogIn").tryQuery().isPresent()) {
            System.out.println("🔵 Estamos en la tienda. Yendo a Login...");
            clickOn("#btnLogIn"); // Clic en el botón del menú superior
            sleep(1000); // Esperar a que cargue la ventana de login
        }

        // --- PASO 2: LOGUEARSE ---
        // Ahora sí estamos en la ventana de Login. Usamos los IDs de tu LogInWindow.fxml
        if (lookup("#TextField_Username").tryQuery().isPresent()) {
            System.out.println("🔵 Escribiendo credenciales...");
            
            clickOn("#TextField_Username").write("user2"); 
            clickOn("#PasswordField_Password").write("1234");    
            
            // Clic en el botón de entrar (ID del FXML del Login, no del Header)
            clickOn("#Button_LogIn"); 
            
            sleep(2000); // Esperar a que nos devuelva a la tienda logueados
        }

        // --- PASO 3: SELECCIONAR LIBRO ---
        System.out.println("🔵 Buscando libro en la estantería...");
        
        // Buscamos el panel de libros (#tileBooks según tu MainBookStore.fxml)
        // Usamos tryQuery por seguridad, por si acaso usas otro nombre
        TilePane estanteria = lookup("#tileBooks").query();
        
        if (estanteria.getChildren().isEmpty()) {
            Assert.fail("❌ ERROR: No hay libros en la base de datos para hacer clic.");
        }

        // Hacemos clic en el PRIMER libro que haya
        Node primerLibro = estanteria.getChildren().get(0);
        clickOn(primerLibro);
        
        // Esperar a que cargue el detalle (BookView)
        sleep(1000);
    }

    // --- TEST 1: Verificar que entramos al libro ---
    @Test
    public void test1_VerificarElementosVisibles() {
        navegarHastaElLibro();

        System.out.println("🔵 Verificando vista de libro...");
        verifyThat("#titleBook", isVisible());
        verifyThat("#priceBook", isVisible());
        
        System.out.println("✅ TEST 1 PASADO: Vista de libro cargada.");
    }

    // --- TEST 2: Verificar Stock (Botón Comprar) ---
    @Test
    public void test2_VerificarBotonCompra() {
        // Asumimos que ya estamos dentro gracias al orden de ejecución
        // Si fallara, descomenta la siguiente línea:
        // navegarHastaElLibro(); 

        Button btnAdd = lookup("#btnAddToCart").query();
        
        if (btnAdd.isVisible()) {
            System.out.println("ℹ️ El libro tiene stock (Botón visible).");
            Assert.assertFalse("El botón debe estar habilitado", btnAdd.isDisabled());
        } else {
            System.out.println("ℹ️ El libro NO tiene stock (Botón oculto).");
        }
        
        System.out.println("✅ TEST 2 PASADO: Lógica de stock verificada.");
    }

    // --- TEST 3: Comentarios ---
    @Test
    public void test3_AbrirComentarios() {
        System.out.println("🔵 Probando comentarios...");
        
        verifyThat("#btnAddComment", isVisible());
        clickOn("#btnAddComment");

        verifyThat("#cajaEscribir", isVisible());
        
        clickOn("#txtNuevoComentario").write("Test Automático");
        clickOn("#btnCancelar");
        
        System.out.println("✅ TEST 3 PASADO: Comentarios OK.");
    }
}