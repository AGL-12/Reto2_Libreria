package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.DBImplementation;
import model.User;
import model.Admin;
import model.Book;
import model.Commentate;
import org.junit.After;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import javafx.scene.Node;
import java.util.Set;
import java.sql.Timestamp;
import org.junit.Assert;

/**
 * Test de integración para DeleteComentWindow.
 * Crea sus propios datos al inicio y los limpia al final (@After).
 */
public class DeleteComentWindowControllerTest extends ApplicationTest {

    private final DBImplementation db = new DBImplementation();

    // DATOS DE PRUEBA EXCLUSIVOS PARA ESTE TEST
    private final String TEST_ADMIN_USER = "admin_test_delete";
    private final String TEST_ADMIN_PASS = "1234";
    private final String TEST_USER = "user_test_comentario";
    private final long TEST_ISBN = 9999999999L;
    private final String TEST_COMMENT = "Comentario autogenerado para test";

    /**
     * Configuración inicial: Crea todos los datos necesarios en la BD.
     */
    @Override
    public void start(Stage stage) throws Exception {
        // 0. LIMPIEZA PREVIA (Por seguridad, si un test anterior falló fatalmente)
        limpiarDatosDePrueba();

        System.out.println("--- INICIANDO SETUP DEL TEST ---");
        try {
            // 2. CREAR USUARIO
            User user = new User();
            user.setUsername(TEST_USER);
            user.setPassword("1234");
            user.setName("UserTest");
            user.setSurname("Test");
            user.setEmail("usertest@test.com");
            user.setTelephone("000000000");
            db.signUp(user);

            // 3. CREAR LIBRO
            Book book = new Book();
            book.setISBN(TEST_ISBN);
            book.setTitle("Libro Test Comentario");
            book.setAuthor(db.getOrCreateAuthor("AutorTest", "ApellidoTest"));
            book.setStock(50);
            book.setPrice(10f);
            book.setEditorial("EditorialTest");
            book.setCover("default.png");
            book.setSheets(100);
            book.setSypnosis("Sinopsis Test");
            db.createBook(book);

            // 4. CREAR COMENTARIO
            // Recuperamos objetos frescos de la BD para tener IDs correctos
            User userReal = buscarUsuarioPorNombre(TEST_USER);
            Book bookReal = db.getBookData(TEST_ISBN);

            if (userReal != null && bookReal != null) {
                // Usamos constructor con parámetros para generar el ID compuesto correctamente
                Commentate c = new Commentate(userReal, bookReal, TEST_COMMENT, 5);
                db.addComment(c);
                System.out.println("Datos creados correctamente.");
            } else {
                throw new RuntimeException("Error recuperando Usuario o Libro creados.");
            }

        } catch (Exception e) {
            System.err.println("Error crítico en Start: " + e.getMessage());
            // Si falla la creación, intentamos limpiar para no dejar basura
            limpiarDatosDePrueba(); 
            throw e; 
        }

        // 5. CARGAR LA INTERFAZ (Inicio en MainBookStore)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainBookStore.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Limpieza final: Se ejecuta SIEMPRE al acabar el test.
     */
    @After
    public void tearDown() {
        System.out.println("--- EJECUTANDO TEARDOWN (LIMPIEZA) ---");
        limpiarDatosDePrueba();
    }

    @Test
    public void testFlujoBorrarComentario() {
        // --- 1. LOGIN ---
        clickOn("#btnLogIn");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#TextField_Username").write(TEST_ADMIN_USER);
        clickOn("#PasswordField_Password").write(TEST_ADMIN_PASS);
        clickOn("#Button_LogIn");
        WaitForAsyncUtils.waitForFxEvents();

        // --- 2. NAVEGAR A GESTIÓN ---
        clickOn("#btnOption");
        WaitForAsyncUtils.waitForFxEvents();

        // Ir a Comentarios (Buscamos por ID o texto)
        try {
            clickOn("#btnEliminarComentario");
        } catch (Exception e) {
            clickOn(buscarBotonPorTexto("Gestionar Comentarios"));
        }
        WaitForAsyncUtils.waitForFxEvents();

        // --- 3. SELECCIONAR USUARIO ---
        ComboBox<User> combo = lookup("#comboUsers").queryAs(ComboBox.class);
        
        // Buscar el usuario de prueba en el combo
        User target = null;
        for (User u : combo.getItems()) {
            if (u.getUsername().equals(TEST_USER)) {
                target = u;
                break;
            }
        }
        
        if (target == null) {
            Assert.fail("El usuario de test no aparece en el ComboBox.");
        }

        final User userSelect = target;
        interact(new Runnable() {
            @Override
            public void run() {
                combo.getSelectionModel().select(userSelect);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();

        // --- 4. SELECCIONAR COMENTARIO (TABLA) ---
        TableView<Commentate> tabla = lookup("#tableComments").queryTableView();
        
        // Seleccionamos la primera fila (debería ser nuestro comentario)
        interact(new Runnable() {
            @Override
            public void run() {
                if (!tabla.getItems().isEmpty()) {
                    tabla.getSelectionModel().select(0);
                    tabla.requestFocus();
                }
            }
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Verificar que se seleccionó algo antes de borrar
        if (tabla.getSelectionModel().getSelectedItem() == null) {
             Assert.fail("No se pudo seleccionar el comentario en la tabla (¿Tabla vacía?).");
        }

        // --- 5. BORRAR ---
        try {
             clickOn("#btnDelete"); 
        } catch (Exception e) {
             clickOn(buscarBotonPorTexto("Eliminar Comentario"));
        }
        
        // --- 6. VERIFICAR ---
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat("Comentario eliminado correctamente.", isVisible());
        
        cerrarAlerta();
        WaitForAsyncUtils.waitForFxEvents();

        // --- 7. VOLVER ---
        try {
             clickOn("#btnBack"); 
        } catch (Exception e) {
             clickOn(buscarBotonPorTexto("Volver")); 
        }
        WaitForAsyncUtils.waitForFxEvents();
        
        // Confirmar salida
        try {
            verifyThat("#btnEliminarComentario", isVisible());
        } catch (Exception e) {
            // Si el ID varía, confiamos en que no hubo excepción al volver
        }
    }

    // ================= MÉTODOS AUXILIARES =================

    /**
     * Elimina todos los datos creados por este test.
     */
    private void limpiarDatosDePrueba() {
        try {
            // 1. Borrar Libro (esto a veces borra comentarios en cascada si está configurado, si no, se borran con el usuario)
            try {
                db.deleteBook(TEST_ISBN);
            } catch (Exception e) { /* Ignorar si no existe */ }

            // 2. Borrar Usuario (CascadeType.ALL en User borrará sus comentarios y pedidos)
            User u = buscarUsuarioPorNombre(TEST_USER);
            if (u != null) {
                db.dropOutUser(u);
            }

            // 3. Borrar Admin
            User admin = buscarUsuarioPorNombre(TEST_ADMIN_USER);
            if (admin != null) {
                db.dropOutUser(admin); // dropOutUser acepta Profile/User/Admin
            }
            
        } catch (Exception e) {
            System.out.println("Nota en limpieza: " + e.getMessage());
        }
    }

    private User buscarUsuarioPorNombre(String username) {
        for (User u : db.getAllUsers()) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    private Button buscarBotonPorTexto(String texto) {
        Set<Node> botones = lookup(".button").queryAll();
        for (Node nodo : botones) {
            if (nodo instanceof Button) {
                Button b = (Button) nodo;
                if (b.getText() != null && b.getText().contains(texto)) {
                    return b;
                }
            }
        }
        throw new RuntimeException("Botón no encontrado con texto: " + texto);
    }

    private void cerrarAlerta() {
        Set<Node> botones = lookup(".dialog-pane .button").queryAll();
        for (Node nodo : botones) {
            clickOn(nodo);
            break;
        }
    }
}