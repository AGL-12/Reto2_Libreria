package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import model.Author;
import model.Book;
import model.DBImplementation;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.Button;
import java.util.concurrent.TimeUnit;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Test de integración para la modificación de libros.
 */
public class BookCRUDWindowControllerTest extends ApplicationTest {

    private final DBImplementation db = new DBImplementation();
    private final long ISBN_TEST = 8881112223L; 

    @Override
    public void start(Stage stage) throws Exception {
        db.deleteBook(ISBN_TEST);
 
        Author testAuthor = db.getOrCreateAuthor("Test", "Writer");
        
        Book tempBook = new Book();
        tempBook.setISBN(ISBN_TEST);
        tempBook.setTitle("Título Original");
        tempBook.setAuthor(testAuthor);
        tempBook.setSheets(100);
        tempBook.setStock(5);
        tempBook.setPrice(10.0f);
        tempBook.setSypnosis("Test");
        tempBook.setEditorial("Editorial Test");
        tempBook.setCover("default.png");

        try {
            db.createBook(tempBook);
        } catch (Exception e) {
            // Si ya existe, no hay problema
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BookCRUDWindow.fxml"));
        Parent root = loader.load();
        BookCRUDWindowController controller = loader.getController();
        
        // Modo modificación para no borrar registros
        controller.setModo("modify");
        
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    public void testModificarLibro() {
        // Buscar el libro por ISBN
        clickOn("#txtISBN").write(String.valueOf(ISBN_TEST)).push(KeyCode.ENTER);
        
        // Pausa breve para asegurar que los datos se cargan en los campos
        WaitForAsyncUtils.waitForFxEvents();

        // Limpiar el título: Ctrl+A y Backspace
        clickOn("#txtTitle").push(KeyCode.CONTROL, KeyCode.A).push(KeyCode.BACK_SPACE);
        write("Título Modificado");
        
        // Confirmar cambios
        clickOn("#btnConfirm");

        // Esperar a que la alerta de éxito sea visible
        verifyThat("Libro modificado.", isVisible());

        // CERRAMOS LA ALERTA DE FORMA SEGURA:
        // Buscamos cualquier botón dentro de la alerta (que suele ser el de Aceptar/OK)
        // Esto evita errores si el texto cambia según el idioma del sistema
        lookup(".dialog-pane .button").queryAllAs(Button.class).stream()
            .findFirst()
            .ifPresent(this::clickOn);
    }
}