package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import model.DBImplementation;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.Button;
import java.util.Set;
import javafx.scene.Node;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Test de flujo completo: 
 * MainBookStore -> Login -> OptionsAdmin -> BookOption (Create) -> BookOption (Modify) -> MainBookStore (Verify)
 */
public class BookCRUDWindowControllerTest extends ApplicationTest {

    private final DBImplementation db = new DBImplementation();
    private final long ISBN_NUEVO = 7771112223L; 
    private final String ADMIN_USER = "admin";
    private final String ADMIN_PASS = "1234";
    // Título que esperamos encontrar al final en la tienda
    private final String TITULO_MODIFICADO = "Título Modificado en el Flow";

    @Override
    public void start(Stage stage) throws Exception {
        // Limpiamos el libro por si acaso existía de pruebas anteriores
        try {
            db.deleteBook(ISBN_NUEVO);
        } catch (Exception e) {}

        // Iniciamos en la ventana principal de la tienda
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainBookStore.fxml"));
        Parent root = loader.load();
        // Nota: Asegúrate de que MainBookStoreController no falla si headerController es null o manéjalo aquí
        // MainBookStoreController main = loader.getController();
        // main.headerController.setMode(null, null); // Si es necesario inicializar el header
        
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    public void testFlujoCompletoCrearYModificar() {
        // 1. LOGIN
        clickOn("#btnLogIn");
        WaitForAsyncUtils.waitForFxEvents();
        
        clickOn("#TextField_Username").write(ADMIN_USER);
        clickOn("#PasswordField_Password").write(ADMIN_PASS);
        clickOn("#Button_LogIn");
        WaitForAsyncUtils.waitForFxEvents();

        // 2. NAVEGAR A OPCIONES
        clickOn("#btnOption"); // O #btnPerfil según tu header
        WaitForAsyncUtils.waitForFxEvents();

        // 3. NAVEGAR A GESTIÓN LIBROS
        verifyThat("#btnLibro", isVisible());
        clickOn("#btnLibro");
        WaitForAsyncUtils.waitForFxEvents();

        // 4. MODO CREAR
        clickOn("#btnAdd");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#txtISBN").write(String.valueOf(ISBN_NUEVO));
        clickOn("#txtTitle").write("Libro de Prueba Flow");
        clickOn("#txtNombreAutor").write("Autor");
        clickOn("#txtApellidoAutor").write("test");
        clickOn("#txtEditorial").write("Editorial Test");
        clickOn("#txtPrice").write("25.50");
        clickOn("#txtStock").write("10");
        clickOn("#txtPages").write("200");
        clickOn("#txtSinopsis").write("test");
        
        clickOn("#btnConfirm");
        WaitForAsyncUtils.waitForFxEvents();
        cerrarAlerta();

        // Nota: Al confirmar creación, ¿tu ventana se cierra o vuelves manualmente?
        // Asumo que vuelves manualmente con btnReturn si la ventana no se cierra sola.

        clickOn("#btnModify");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#txtISBN").write(String.valueOf(ISBN_NUEVO)).push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();

        // Modificar el título
        clickOn("#txtTitle").push(KeyCode.CONTROL, KeyCode.A).push(KeyCode.BACK_SPACE);
        write(TITULO_MODIFICADO);

        clickOn("#btnConfirm");
        WaitForAsyncUtils.waitForFxEvents();
        cerrarAlerta();
        
        // Salir del CRUD
        clickOn("#btnReturn");
        WaitForAsyncUtils.waitForFxEvents();

        // 6. VOLVER AL HOME (MAIN BOOK STORE)
        // Desde OptionsAdmin/BookOptionWindow pulsamos Home
        // Si "Home" es texto en un botón:
        clickOn("Home"); 
        WaitForAsyncUtils.waitForFxEvents();

        // 7. VERIFICACIÓN FINAL EN LA TIENDA
        // OPCIONAL: Si tienes barra de búsqueda, úsala para filtrar y asegurar que el libro es visible.
        // Si no usas búsqueda y el libro está muy abajo (scroll), el test fallará.
        try {
            // Intenta escribir en la barra de búsqueda si existe (Pon aquí el ID correcto, ej: #txtSearch)
            clickOn("#txtSearch").write(TITULO_MODIFICADO);
            WaitForAsyncUtils.waitForFxEvents();
        } catch (Exception e) {
            // Si no hay barra de búsqueda o falla, intentamos verificar directamente
            System.out.println("No se usó barra de búsqueda, verificando visibilidad directa...");
        }

        // Verificamos que el título modificado es visible en la escena (MainBookStore)
        verifyThat(TITULO_MODIFICADO, isVisible());
        sleep(4000);
    }

    private void cerrarAlerta() {
        Set<Node> botones = lookup(".dialog-pane .button").queryAll();
        for (Node nodo : botones) {
            if (nodo instanceof Button) {
                clickOn(nodo);
                break;
            }
        }
    }
}