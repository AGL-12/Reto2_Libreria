package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Test de integración para la ventana de opciones de libros.
 * Verifica la navegación hacia el CRUD y el retorno al menú principal.
 */
public class BookOptionWindowControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        // Cargamos la ventana de opciones de libros
        Parent root = FXMLLoader.load(getClass().getResource("/view/BookOptionWindow.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    public void testNavegarAAñadirLibro() {
        // Hacemos clic en el botón de añadir
        clickOn("#btnAdd");

        // Verificamos que se abra la ventana CRUD. 
        // Buscamos un elemento único de esa ventana, como el campo ISBN
        verifyThat("#txtISBN", isVisible());
        
        // Verificamos que el botón de confirmación tenga el texto de creación
        verifyThat("Añadir Libro", isVisible());
    }

    @Test
    public void testNavegarAModificarLibro() {
        // Hacemos clic en el botón de modificar
        clickOn("#btnModify");

        // Verificamos que se abra la ventana CRUD
        verifyThat("#txtISBN", isVisible());
        
        // Verificamos que el botón de confirmación tenga el texto de modificación
        verifyThat("Modificar Libro", isVisible());
    }

    @Test
    public void testBotonVolver() {
        // Hacemos clic en el botón de volver
        clickOn("#btnReturn");

        // Verificamos que regresamos a la ventana de opciones del administrador
        // Buscamos un botón que solo esté en OptionsAdmin, por ejemplo btnLibro
        verifyThat("#btnLibro", isVisible());
    }
}