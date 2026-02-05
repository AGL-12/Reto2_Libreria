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

public class MenuWindowControllerTest extends ApplicationTest {

    private final DBImplementation db = new DBImplementation();
    private final String USER_TEST = "user_menu_test";
    private final String PASS_TEST = "1234";

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
            System.err.println("Error en cleanup: " + e.getMessage());
        }
    }

    @Test
    public void testNavegacionMenuUsuarioCompleta() {
        clickOn("#btnLogIn");
        clickOn("#Button_SignUp");
        
        clickOn("#textFieldEmail").write("menu@test.com");
        clickOn("#textFieldUsername").write(USER_TEST);
        clickOn("#textFieldName").write("User");
        clickOn("#textFieldSurname").write("Test");
        clickOn("#textFieldTelephone").write("123456789");
        clickOn("#textFieldCardN").write("1111222233334444");
        clickOn("#textFieldPassword").write(PASS_TEST);
        clickOn("#textFieldCPassword").write(PASS_TEST);
        clickOn("#rButtonO");
        clickOn("#buttonSignUp");
        
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.ENTER); 

        clickOn("#btnOption");
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat("#rootPane", isVisible()); 

        clickOn("Acciones");
        clickOn("Modificar Perfil");
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat("#TextField_Name", isVisible()); 
        
        clickOn("#Button_Cancel");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Acciones");
        clickOn("Historial de Compras");
        WaitForAsyncUtils.waitForFxEvents();
        
        // CORRECCIÓN: Usamos el ID tableOrders que está definido en el FXML
        verifyThat("#tableOrders", isVisible()); 
        
        clickOn("#btnVolver");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Acciones");
        clickOn("Eliminar Cuenta");
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat("#TextFieldPassword", isVisible()); 
        
        clickOn("#Button_Cancel");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Acciones");
        clickOn("Volver a la Tienda");
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat("#tileBooks", isVisible());
    }
}