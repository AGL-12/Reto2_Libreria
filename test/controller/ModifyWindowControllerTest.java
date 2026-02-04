package controller;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import main.Main;
import model.DBImplementation;
import model.User;
import org.junit.After;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Test de integración para la ventana de modificación con flujo cruzado.
 * Flujo: Sign Up -> Modificación Propia -> Logout -> Login Admin -> Modificación Admin -> Home.
 */
public class ModifyWindowControllerTest extends ApplicationTest {

    private final DBImplementation db = new DBImplementation();
    private final String USER_TEST = "estratega_test";
    private final String PASS_TEST = "1234";
    private final String ADMIN_USER = "admin";
    private final String ADMIN_PASS = "1234";

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage); // Inicia desde la ventana principal
    }

    @After
    public void tearDown() {
        // Limpieza del usuario de prueba al finalizar
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
    public void testFlujoEstrategiaModificacion() {
        // --- 1. SIGN UP (El usuario queda registrado y en sesión) ---
        clickOn("#btnLogIn"); //
        clickOn("#Button_SignUp"); //
        
        clickOn("#textFieldEmail").write("estrategia@test.com"); //
        clickOn("#textFieldUsername").write(USER_TEST); //
        clickOn("#textFieldName").write("NombreOriginal"); //
        clickOn("#textFieldSurname").write("Apellido"); //
        clickOn("#textFieldTelephone").write("123456789"); //
        clickOn("#textFieldCardN").write("1234123412341234"); //
        clickOn("#textFieldPassword").write(PASS_TEST); //
        clickOn("#textFieldCPassword").write(PASS_TEST); //
        clickOn("#rButtonO"); //
        clickOn("#buttonSignUp"); //
        
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.ENTER); // Aceptar alerta de éxito

        // --- 2. MODIFICACIÓN PROPIA (Como ya estamos registrados/logueados) ---
        clickOn("#btnOption"); // Entrar al menú de usuario
        WaitForAsyncUtils.waitForFxEvents();
        
        // Navegación por menú superior
        clickOn("Acciones"); //
        clickOn("Modificar Perfil"); //
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#TextField_Name").eraseText(30).write("ModificadoPorMi"); //
        clickOn("#Button_SaveChanges"); //
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.ENTER); // Cerrar alerta éxito

        // Volver al Home para cerrar sesión
        if (lookup("#btnBack").tryQuery().isPresent()) {
            clickOn("#btnBack"); //
        } else {
            clickOn("#Button_Cancel"); //
            WaitForAsyncUtils.waitForFxEvents();
            clickOn("#btnBack"); //
        }

        // --- 3. LOGOUT Y LOGIN ADMIN ---
        clickOn("#btnLogOut"); //
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#btnLogIn"); //
        clickOn("#TextField_Username").write(ADMIN_USER); //
        clickOn("#PasswordField_Password").write(ADMIN_PASS); //
        clickOn("#Button_LogIn"); //
        WaitForAsyncUtils.waitForFxEvents();

        // --- 4. MODIFICACIÓN POR ADMINISTRADOR ---
        clickOn("#btnOption"); // Ir a opciones admin
        WaitForAsyncUtils.waitForFxEvents();
        
        clickOn("Acciones"); // [cite: 51, 192]
        clickOn("Modificar Usuario"); // [cite: 51, 192]
        WaitForAsyncUtils.waitForFxEvents();

        // Seleccionar el usuario que acabamos de crear en el combo
        clickOn("#comboUsers"); //
        clickOn(USER_TEST);
        WaitForAsyncUtils.waitForFxEvents();

        // Modificar el nombre que el usuario se puso a sí mismo
        clickOn("#TextField_Name").eraseText(30).write("CambiadoPorAdmin"); //
        clickOn("#Button_SaveChanges"); //
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.ENTER);
        
        // Volver a la tienda desde el panel de opciones admin
        clickOn("Home"); // Hyperlink 
        
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat("#tileBooks", isVisible()); // Confirmar regreso al inicio
    }
}