package controller;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import main.Main;
import model.DBImplementation;
import model.Book;
import org.junit.After;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

public class BookCRUDWindowControllerTest extends ApplicationTest {

    private final DBImplementation db = new DBImplementation();
    private final String ADMIN_USER = "admin";
    private final String ADMIN_PASS = "1234";
    private final long ISBN_TEST = 999111222L;
    private final String TITULO_TEST = "Libro JUnit Flow";
    private final String TITULO_MODIFICADO = "Libro JUnit Modificado";

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @After
    public void tearDown() {
        // Limpieza del libro de prueba en la base de datos
        try {
            Book b = db.getBookData(ISBN_TEST);
            if (b != null) {
                db.deleteBook(ISBN_TEST);
            }
        } catch (Exception e) {
            System.err.println("Error en limpieza de libro: " + e.getMessage());
        }
    }

    @Test
    public void testFlujoCompletoBookCRUD() {
        // 1. LOGIN COMO ADMINISTRADOR
        clickOn("#btnLogIn");
        clickOn("#TextField_Username").write(ADMIN_USER);
        clickOn("#PasswordField_Password").write(ADMIN_PASS);
        clickOn("#Button_LogIn");
        WaitForAsyncUtils.waitForFxEvents();

        // 2. NAVEGACIÓN A OPCIONES ADMIN (Desde MainBookStore)
        clickOn("#btnOption");
        WaitForAsyncUtils.waitForFxEvents();

        // 3. ABRIR GESTIÓN DE LIBROS (Clic Derecho en el panel)
        rightClickOn("#rootPane"); 
        clickOn("Gestión de Libros"); 
        WaitForAsyncUtils.waitForFxEvents();

        // 4. CREACIÓN DE LIBRO (Modo Añadir)
        clickOn("Acciones");
        clickOn("Modo Añadir");

        // Usamos eraseText(30) en lugar de doubleClick para asegurar que el campo esté limpio
        clickOn("#txtISBN").eraseText(30).write(String.valueOf(ISBN_TEST));
        clickOn("#txtTitle").eraseText(30).write(TITULO_TEST);
        clickOn("#txtNombreAutor").eraseText(30).write("Autor");
        clickOn("#txtApellidoAutor").eraseText(30).write("JUnit");
        clickOn("#txtEditorial").eraseText(30).write("Editorial Test");
        clickOn("#txtPrice").eraseText(30).write("29.99");
        clickOn("#txtStock").eraseText(30).write("10");
        clickOn("#txtPages").eraseText(30).write("300");
        clickOn("#txtSinopsis").eraseText(100).write("Sinopsis de prueba.");
        
        clickOn("#btnConfirm");
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.ENTER); // Cerrar Alert de éxito

        // 5. MODIFICACIÓN DEL LIBRO CREADO
        clickOn("Acciones");
        clickOn("Modo Modificar");
        
        // Buscamos el libro escribiendo el ISBN y pulsando ENTER
        clickOn("#txtISBN").eraseText(30).write(String.valueOf(ISBN_TEST)).push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();

        // Modificamos el título: Seleccionamos todo y borramos
        clickOn("#txtTitle");
        push(KeyCode.CONTROL, KeyCode.A);
        push(KeyCode.BACK_SPACE);
        write(TITULO_MODIFICADO);
        
        clickOn("#btnConfirm");
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.ENTER); // Cerrar Alert de éxito

        // 6. VOLVER A LA TIENDA Y VERIFICAR
        clickOn("#btnReturn"); 
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("Home"); 
        WaitForAsyncUtils.waitForFxEvents();

        // Verificamos en la tienda usando la barra de búsqueda
        clickOn("#txtSearch").eraseText(30).write(TITULO_MODIFICADO);
        sleep(1000); 
        
        verifyThat(TITULO_MODIFICADO, isVisible());
    }
}