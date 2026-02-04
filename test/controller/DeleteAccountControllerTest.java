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
 * Test de flujo completo para DeleteAccount (Auto-eliminación).
 * Flujo: Main -> SignUp -> MenuWindow -> DeleteAccount -> Verify Login Error.
 */
public class DeleteAccountControllerTest extends ApplicationTest {

    private final DBImplementation db = new DBImplementation();
    private final String USER_TEST = "auto_delete_user";
    private final String PASS_TEST = "1234";

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @After
    public void tearDown() {
        // Limpieza de seguridad en BD por si el test falla antes de auto-borrarse
        try {
            User u = db.getAllUsers().stream()
                    .filter(user -> user.getUsername().equals(USER_TEST))
                    .findFirst().orElse(null);
            if (u != null) db.dropOutUser(u);
        } catch (Exception e) {
            System.err.println("Limpieza: " + e.getMessage());
        }
    }

    @Test
    public void testAutoEliminacionDeCuenta() {
        // 1. SIGN UP (El usuario queda registrado y en sesión)
        clickOn("#btnLogIn");
        clickOn("#Button_SignUp");
        
        clickOn("#textFieldEmail").write("delete@me.com");
        clickOn("#textFieldUsername").write(USER_TEST);
        clickOn("#textFieldName").write("Auto");
        clickOn("#textFieldSurname").write("Delete");
        clickOn("#textFieldTelephone").write("123456789");
        clickOn("#textFieldCardN").write("1111222233334444");
        clickOn("#textFieldPassword").write(PASS_TEST);
        clickOn("#textFieldCPassword").write(PASS_TEST);
        clickOn("#rButtonO");
        clickOn("#buttonSignUp");
        
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.ENTER); // Cerrar alerta de éxito del registro

        // 2. NAVEGAR A ELIMINAR CUENTA (Desde MainBookStore -> MenuWindow -> DeleteAccount)
        // Como el registro nos deja logueados, accedemos directamente a opciones
        clickOn("#btnOption");
        WaitForAsyncUtils.waitForFxEvents();
        
        // Usamos el menú de acciones de la ventana MenuWindow
        clickOn("Acciones");
        clickOn("Eliminar Cuenta");
        WaitForAsyncUtils.waitForFxEvents();

        // 3. PROCESO DE ELIMINACIÓN
        // Escribimos la contraseña para confirmar
        clickOn("#TextFieldPassword").write(PASS_TEST);
        clickOn("#Button_Delete");
        
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.ENTER); // Confirmar alerta de "¿Estás seguro?"
        
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.ENTER); // Aceptar alerta de "Cuenta eliminada correctamente"
 
        verifyThat("#Button_LogIn", isVisible());

        clickOn("#TextField_Username").eraseText(30).write(USER_TEST);
        clickOn("#PasswordField_Password").eraseText(30).write(PASS_TEST);
        clickOn("#Button_LogIn");
        
        WaitForAsyncUtils.waitForFxEvents();
        
        // Verificamos que aparece el label de error por credenciales incorrectas
        // Según LogInWindow.fxml el ID es #labelIncorrecto
        verifyThat("#labelIncorrecto", isVisible());
    }
}