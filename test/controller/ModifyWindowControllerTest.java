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
 * Test de integración corregido con los IDs exactos de SignUpWindow.fxml y ModifyWindow.fxml.
 * Flujo: Sign Up -> Modificación Propia -> Logout -> Login Admin -> Modificación Admin.
 */
public class ModifyWindowControllerTest extends ApplicationTest {

    private final DBImplementation db = new DBImplementation();
    private final String USER_TEST = "estratega_test";
    private final String PASS_TEST = "1234";
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
            if (u != null) {
                db.dropOutUser(u);
                sleep(1000);
            }
        } catch (Exception e) {
            System.err.println("Error en limpieza: " + e.getMessage());
        }
    }

    @Test
    public void testFlujoCompletoModificacion() {
        // --- 1. SIGN UP (IDs exactos de tu FXML) ---
        clickOn("#btnLogIn"); 
        clickOn("#Button_SignUp"); // El botón que abre la ventana de registro
        
        clickOn("#textFieldEmail").write("test@modify.com");
        clickOn("#textFieldUsername").write(USER_TEST);
        clickOn("#textFieldName").write("NombreOriginal");
        clickOn("#textFieldSurname").write("ApellidoOriginal");
        clickOn("#textFieldTelephone").write("123456789");
        clickOn("#textFieldCardN").write("1234567890123456");
        clickOn("#textFieldPassword").write(PASS_TEST);
        clickOn("#textFieldCPassword").write(PASS_TEST);
        clickOn("#rButtonM"); // Seleccionar género
        
        clickOn("#buttonSignUp"); // Botón final de registro
        
        WaitForAsyncUtils.waitForFxEvents();
        sleep(2000); // Tiempo para que el hilo SessionHolderThread actúe

        clickOn("#btnOption");
        clickOn("#btnModifyProfile");
        WaitForAsyncUtils.waitForFxEvents();

        // Modificar datos propios
        clickOn("#TextField_Name").eraseText(20).write("MiNuevoNombre");
        clickOn("#TextField_Surname").eraseText(20).write("MiNuevoApellido");
        clickOn("#Button_SaveChanges"); 
        
        sleep(3500);
        push(KeyCode.ENTER); // Cerrar la alerta de éxito
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("Volver");
        // --- 3. LOGOUT Y LOGIN ADMIN ---
        clickOn("#btnLogOut");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#btnLogIn");
        clickOn("#TextField_Username").write(ADMIN_USER);
        clickOn("#PasswordField_Password").write(ADMIN_PASS);
        clickOn("#Button_LogIn");
        WaitForAsyncUtils.waitForFxEvents();

        // --- 4. MODIFICACIÓN DESDE ADMIN ---
        clickOn("#btnOption");
        WaitForAsyncUtils.waitForFxEvents();
        
        clickOn("Acciones"); // Menú superior
        clickOn("Modificar Usuario"); // Item del menú
        WaitForAsyncUtils.waitForFxEvents();

        // Seleccionar el usuario en el ComboBox
        clickOn("#comboUsers");
        clickOn(USER_TEST);
        WaitForAsyncUtils.waitForFxEvents();

        // El Admin edita el nombre
        clickOn("#TextField_Name").eraseText(20).write("validado por chayanne");
        clickOn("#Button_SaveChanges"); 
        
        sleep(500);
        push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();

 
        clickOn("Home");
        clickOn("#btnLogOut");
        WaitForAsyncUtils.waitForFxEvents();
        
        clickOn("#btnLogIn");
        clickOn("#TextField_Username").write(USER_TEST);
        clickOn("#PasswordField_Password").write(PASS_TEST);
        clickOn("#Button_LogIn");
        
        
        
    }
}