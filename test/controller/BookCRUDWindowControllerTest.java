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
import static org.testfx.matcher.control.LabeledMatchers.hasText;

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

        // 2. NAVEGACIÓN A OPCIONES ADMIN
        clickOn("#btnOption");
        WaitForAsyncUtils.waitForFxEvents();

        // 3. ABRIR GESTIÓN DE LIBROS
        rightClickOn("#rootPane"); 
        clickOn("Gestión de Libros"); 
        WaitForAsyncUtils.waitForFxEvents();

        // 4. VERIFICACIÓN DE INICIO AUTOMÁTICO EN MODO CREACIÓN
        // Comprobamos que el botón de confirmar ya tiene el texto correcto sin pulsar nada
        verifyThat("#btnConfirm", hasText("Añadir Libro"));

        // Rellenamos los datos directamente (ya que estamos en Modo Añadir por defecto)
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

        // 5. CAMBIO A MODO MODIFICAR Y BÚSQUEDA
        clickOn("Acciones");
        clickOn("Modo Modificar");
        
        // Verificamos que el botón cambió su texto
        verifyThat("#btnConfirm", hasText("Modificar Libro"));
        
        // Buscamos el libro (el ISBN se bloqueará tras el ENTER según la nueva lógica)
        clickOn("#txtISBN").eraseText(30).write(String.valueOf(ISBN_TEST)).push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();

        // Modificamos el título
        clickOn("#txtTitle");
        push(KeyCode.CONTROL, KeyCode.A);
        push(KeyCode.BACK_SPACE);
        write(TITULO_MODIFICADO);
        
        clickOn("#btnConfirm");
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.ENTER); // Cerrar Alert de éxito

        // 6. VERIFICAR QUE TRAS CONFIRMAR SE LIMPIAN CAMPOS Y ISBN SE REHABILITA
        // Según la lógica setModo(this.modo) al final de confirmAction
        verifyThat("#txtISBN", isVisible()); // El campo debe estar listo para usarse de nuevo

        // 7. VOLVER A LA TIENDA Y VERIFICAR RESULTADO FINAL
        clickOn("#btnReturn"); 
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("Home"); 
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#txtSearch").eraseText(30).write(TITULO_MODIFICADO);
        sleep(1000); 
        
        verifyThat(TITULO_MODIFICADO, isVisible());
    }
}