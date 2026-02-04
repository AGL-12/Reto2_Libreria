package controller;

import javafx.application.Platform;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import main.Main;
import model.Commentate;
import model.DBImplementation;
import model.User;
import org.junit.After;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import org.testfx.matcher.control.LabeledMatchers;

public class DeleteComentWindowControllerTest extends ApplicationTest {

    private final DBImplementation db = new DBImplementation();
    private final String USER_TEST = "test_coment_user";
    private final String ADMIN_USER = "admin";
    private final String ADMIN_PASS = "1234";

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @After
    public void tearDown() {
        try {
            User u = db.getAllUsers().stream()
                    .filter(user -> user.getUsername().equals(USER_TEST))
                    .findFirst().orElse(null);
            if (u != null) db.dropOutUser(u);
        } catch (Exception e) {
            System.err.println("Error en limpieza: " + e.getMessage());
        }
    }

    @Test
    public void testFlujoCompletoBorrarComentario() {
        // 1. REGISTRO Y LOGIN AUTOMÁTICO
        clickOn("#btnLogIn");
        clickOn("#Button_SignUp");
        clickOn("#textFieldEmail").write("test@test.com");
        clickOn("#textFieldUsername").write(USER_TEST);
        clickOn("#textFieldName").write("Test");
        clickOn("#textFieldSurname").write("User");
        clickOn("#textFieldTelephone").write("123456789");
        clickOn("#textFieldCardN").write("1234123412341234");
        clickOn("#textFieldPassword").write("1234");
        clickOn("#textFieldCPassword").write("1234");
        clickOn("#rButtonO");
        clickOn("#buttonSignUp");
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.ENTER); 

        // 2. SELECCIONAR LIBRO Y COMENTAR
        clickOn("1984"); 
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnAddComment"); 
        clickOn("#txtNuevoComentario").write("Comentario JUnit");
        clickOn("#btnPublicar");
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.ENTER); 

        // 3. LOGOUT (Menú Contextual)
        rightClickOn("#rootPane");
        clickOn("Cerrar Sesión");
        WaitForAsyncUtils.waitForFxEvents();

        // 4. LOGIN ADMIN
        clickOn("#btnLogIn");
        clickOn("#TextField_Username").write(ADMIN_USER);
        clickOn("#PasswordField_Password").write(ADMIN_PASS);
        clickOn("#Button_LogIn");
        WaitForAsyncUtils.waitForFxEvents();

        // 5. NAVEGAR A MODERACIÓN
        clickOn("#btnOption");
        clickOn("#btnEliminarComentario");
        WaitForAsyncUtils.waitForFxEvents();

        // 6. SELECCIÓN DE USUARIO
        clickOn("#comboUsers");
        clickOn(USER_TEST);
        
        // Espera para carga asíncrona de la tabla
        sleep(1500); 

        // SELECCIÓN FORZADA DE LA FILA
        TableView<Commentate> table = lookup("#tableComments").queryTableView();
        interact(() -> {
            if (!table.getItems().isEmpty()) {
                table.getSelectionModel().select(0);
                table.requestFocus();
            }
        });
        WaitForAsyncUtils.waitForFxEvents();

        // 7. BORRAR
        clickOn("Eliminar Comentario"); 
        WaitForAsyncUtils.waitForFxEvents();
         sleep(3500);
        // Confirmar diálogo "¿Desea eliminar?"
        push(KeyCode.ENTER);
        // Tiempo para que procese el borrado y salte el aviso de éxito
    }
}