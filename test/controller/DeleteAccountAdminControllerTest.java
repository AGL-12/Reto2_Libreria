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
import org.testfx.matcher.control.LabeledMatchers;

public class DeleteAccountAdminControllerTest extends ApplicationTest {

    private final DBImplementation db = new DBImplementation();
    private final String USER_TEST = "user_temp_delete";
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
            if (u != null) db.dropOutUser(u);
        } catch (Exception e) {}
    }

    @Test
    public void testFlujoAdminBorraEIntentaLogin() {
        // 1. REGISTRO
        clickOn("#btnLogIn");
        clickOn("#Button_SignUp");
        clickOn("#textFieldEmail").write("temp@test.com");
        clickOn("#textFieldUsername").write(USER_TEST);
        clickOn("#textFieldName").write("Temp");
        clickOn("#textFieldSurname").write("User");
        clickOn("#textFieldTelephone").write("123456789");
        clickOn("#textFieldCardN").write("0000111122223333");
        clickOn("#textFieldPassword").write(PASS_TEST);
        clickOn("#textFieldCPassword").write(PASS_TEST);
        clickOn("#rButtonO");
        clickOn("#buttonSignUp");
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.ENTER); // Cerrar alerta éxito

        // 2. SALIR DEL USUARIO RECIÉN CREADO (Si loguea automático)
        try { clickOn("#btnLogOut"); } catch (Exception e) {}

         clickOn("#btnLogIn");
        // 3. LOGIN ADMIN
        clickOn("#TextField_Username").eraseText(30).write(ADMIN_USER);
        clickOn("#PasswordField_Password").eraseText(30).write(ADMIN_PASS);
        clickOn("#Button_LogIn");
        WaitForAsyncUtils.waitForFxEvents();

        // 4. IR A BORRADO
        clickOn("#btnOption");
        clickOn("#btnDeleteUser");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#ComboBoxUser");
        clickOn(USER_TEST);
        clickOn("#TextFieldPassword").write(ADMIN_PASS);
        clickOn("#Button_Delete");
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.ENTER); // Confirmar
        push(KeyCode.ENTER);
        // 5. NAVEGACIÓN ATRÁS (No hay logout en este menú)
        clickOn("#Button_Cancel"); // Vuelve a OptionsAdmin
        clickOn("Home"); // Vuelve a MainBookStore (Header)
        WaitForAsyncUtils.waitForFxEvents();
        sleep(2000);
        clickOn("#btnLogOut");
        verifyThat("#btnLogIn", isVisible());

        // 6. VERIFICAR QUE EL USUARIO NO EXISTE
        clickOn("#btnLogIn");
        clickOn("#TextField_Username").eraseText(30).write(USER_TEST);
        clickOn("#PasswordField_Password").eraseText(30).write(PASS_TEST);
        clickOn("#Button_LogIn");
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat("#labelIncorrecto", isVisible());
        
    }
}