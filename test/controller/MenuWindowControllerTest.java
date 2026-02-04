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
 * Test de navegación para el panel de Menú de Usuario (MenuWindow).
 * Flujo: Main -> SignUp -> MenuWindow -> Subventanas (vía Menú) -> Home.
 */
public class MenuWindowControllerTest extends ApplicationTest {

    private final DBImplementation db = new DBImplementation();
    private final String USER_TEST = "user_menu_test";
    private final String PASS_TEST = "1234";

    @Override
    public void start(Stage stage) throws Exception {
        // Iniciamos la aplicación desde el punto de entrada principal
        new Main().start(stage); 
    }

    @After
    public void tearDown() {
        // Limpieza del usuario de prueba para mantener la BD limpia
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
        // --- 1. SIGN UP (El usuario queda registrado y logueado) ---
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
        push(KeyCode.ENTER); // Cerrar alerta de éxito del registro

        // --- 2. ENTRAR AL MENÚ DE USUARIO (Desde el Header) ---
        clickOn("#btnOption");
        WaitForAsyncUtils.waitForFxEvents();
        // Validamos que estamos en la ventana correcta buscando el nodo raíz
        verifyThat("#rootPane", isVisible()); 

        // --- 3. NAVEGAR A MODIFICAR PERFIL (Vía Menú Superior "Acciones") ---
        clickOn("Acciones");
        clickOn("Modificar Perfil");
        WaitForAsyncUtils.waitForFxEvents();
        // Verificamos presencia de un campo único de ModifyWindow.fxml
        verifyThat("#TextField_Name", isVisible()); 
        
        // Volver (Usamos botón físico para evitar errores de casting en vuestro controlador)
        clickOn("#Button_Cancel");
        WaitForAsyncUtils.waitForFxEvents();

        // --- 4. NAVEGAR A HISTORIAL DE COMPRAS (Vía Menú Superior "Acciones") ---
        clickOn("Acciones");
        clickOn("Historial de Compras");
        WaitForAsyncUtils.waitForFxEvents();
        // Verificamos presencia de la tabla de ShoppingHistory.fxml
        verifyThat("#tblHistory", isVisible()); 
        
        // Volver
        clickOn("#btnVolver");
        WaitForAsyncUtils.waitForFxEvents();

        // --- 5. NAVEGAR A ELIMINAR CUENTA (Vía Menú Superior "Acciones") ---
        clickOn("Acciones");
        clickOn("Eliminar Cuenta");
        WaitForAsyncUtils.waitForFxEvents();
        // Verificamos presencia de campo de DeleteAccount.fxml
        verifyThat("#TextFieldPassword", isVisible()); 
        
        // Volver
        clickOn("#Button_Cancel");
        WaitForAsyncUtils.waitForFxEvents();

        // --- 6. REGRESAR A LA TIENDA (Vía Menú Superior "Acciones") ---
        clickOn("Acciones");
        clickOn("Volver a la Tienda");
        WaitForAsyncUtils.waitForFxEvents();

        // --- 7. VERIFICACIÓN FINAL ---
        // Confirmar que estamos de vuelta en MainBookStore viendo el catálogo
        verifyThat("#tileBooks", isVisible());
    }
}