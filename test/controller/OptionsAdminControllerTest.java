package controller;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import main.Main;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

public class OptionsAdminControllerTest extends ApplicationTest {

    private final String ADMIN_USER = "admin";
    private final String ADMIN_PASS = "1234";

    @Override
    public void start(Stage stage) throws Exception {
        new Main().start(stage);
    }

    @Test
    public void testNavegacionAdminCompleta() {
        // 1. LOGIN ADMIN
        clickOn("#btnLogIn");
        clickOn("#TextField_Username").write(ADMIN_USER);
        clickOn("#PasswordField_Password").write(ADMIN_PASS);
        clickOn("#Button_LogIn");
        WaitForAsyncUtils.waitForFxEvents();

        // 2. ACCEDER A OPCIONES
        clickOn("#btnOption");
        WaitForAsyncUtils.waitForFxEvents();

        // 3. NAVEGAR A GESTIÓN LIBROS (vía Menú superior)
        clickOn("Acciones");
        clickOn("Gestión de Libros");
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat("#txtISBN", isVisible());
        
        // Volver (Usamos botón físico para estabilidad)
        clickOn("#btnReturn");
        WaitForAsyncUtils.waitForFxEvents();

        // 4. NAVEGAR A ELIMINAR COMENTARIO (vía Menú superior)
        clickOn("Acciones");
        clickOn("Eliminar Comentario");
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat("#tableComments", isVisible());
        
        clickOn("Volver");
        WaitForAsyncUtils.waitForFxEvents();

        // 5. NAVEGAR A BORRAR USUARIO (vía Menú superior)
        clickOn("Acciones");
        clickOn("Borrar Usuario");
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat("#ComboBoxUser", isVisible());
        
        clickOn("#Button_Cancel");
        WaitForAsyncUtils.waitForFxEvents();

        // 6. NAVEGAR A MODIFICAR USUARIO (vía Menú superior)
        clickOn("Acciones");
        clickOn("Modificar Usuario");
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat("#comboUsers", isVisible());
        
        clickOn("#Button_Cancel");
        WaitForAsyncUtils.waitForFxEvents();

        // 7. REGRESAR AL HOME
        clickOn("Home");
        WaitForAsyncUtils.waitForFxEvents();

        // 8. VERIFICAR TIENDA
        verifyThat("#tileBooks", isVisible());
    }
}