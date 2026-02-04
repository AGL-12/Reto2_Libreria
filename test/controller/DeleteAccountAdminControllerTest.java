package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import model.DBImplementation;
import model.User;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import javafx.scene.Node;
import javafx.collections.ObservableList;
import java.util.Set;
import org.junit.Assert;

/**
 * Test de flujo completo para DeleteAccountAdminController.
 * Selección VISUAL (Clicks) y sin Lambdas.
 */
public class DeleteAccountAdminControllerTest extends ApplicationTest {

    private final DBImplementation db = new DBImplementation();
    private final String LOGIN_TEST = "user_test_delete";
    private final String ADMIN_USER = "admin"; 
    private final String ADMIN_PASS = "1234"; 

    @Override
    public void start(Stage stage) throws Exception {
        // 1. PREPARACIÓN: Asegurar que el usuario a borrar existe en la BD
        boolean existe = false;
        try {
            for (User u : db.getAllUsers()) {
                if (u.getUsername().equals(LOGIN_TEST)) {
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                User u = new User();
                u.setUsername(LOGIN_TEST);
                u.setPassword("test1234");
                u.setName("Borrar");
                u.setSurname("Test");
                u.setEmail("test@delete.com");
                u.setTelephone("690014712");
                db.signUp(u);
            }
        } catch (Exception e) {
            System.out.println("Aviso en start: " + e.getMessage());
        }

        // Iniciamos en la ventana principal
        Parent root = FXMLLoader.load(getClass().getResource("/view/MainBookStore.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    public void testFlujoCompletoBorrarUsuario() {
        // --- PASO 1: LOGIN ---
        clickOn("#btnLogIn");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#TextField_Username").write(ADMIN_USER);
        clickOn("#PasswordField_Password").write(ADMIN_PASS);
        clickOn("#Button_LogIn");
        WaitForAsyncUtils.waitForFxEvents();

        // --- PASO 2: IR A OPCIONES DE ADMIN ---
        clickOn("#btnOption"); 
        WaitForAsyncUtils.waitForFxEvents();

        // --- PASO 3: IR A BORRAR USUARIO ---
        clickOn("#btnDeleteUser"); 
        WaitForAsyncUtils.waitForFxEvents();

        // --- PASO 4: SELECCIÓN VISUAL DEL USUARIO (HACIENDO CLICK) ---
        // Primero verificamos que el usuario esté cargado en el combo (para evitar clicks ciegos)
        ComboBox<User> combo = lookup("#ComboBoxUser").queryAs(ComboBox.class);
        boolean usuarioEnLista = false;
        for (User u : combo.getItems()) {
            if (u.getUsername().equals(LOGIN_TEST)) {
                usuarioEnLista = true;
                break;
            }
        }
        
        if (!usuarioEnLista) {
            Assert.fail("El usuario '" + LOGIN_TEST + "' no aparece en la lista del ComboBox.");
        }

        // 1. Hacemos CLICK en el ComboBox para desplegarlo
        clickOn("#ComboBoxUser");
        WaitForAsyncUtils.waitForFxEvents();

        // 2. Hacemos CLICK en el texto del usuario dentro de la lista desplegada
        // Como User.toString() devuelve el username, buscamos por ese texto
        clickOn(LOGIN_TEST);
        WaitForAsyncUtils.waitForFxEvents();

        // --- PASO 5: VALIDACIÓN Y BORRADO ---
        // Escribimos la contraseña del ADMINISTRADOR
        clickOn("#TextFieldPassword").write(ADMIN_PASS);
        WaitForAsyncUtils.waitForFxEvents();

        // Hacemos clic en borrar
        clickOn("#Button_Delete");
        WaitForAsyncUtils.waitForFxEvents();

        // --- PASO 6: CONFIRMACIÓN (Alerta "¿Estás seguro?") ---
        Set<Node> botonesConfirmacion = lookup(".dialog-pane .button").queryAll();
        for (Node nodo : botonesConfirmacion) {
            if (nodo instanceof Button) {
                Button boton = (Button) nodo;
                String texto = boton.getText().toLowerCase();
                if (texto.contains("s") || texto.contains("yes") || texto.contains("aceptar")) {
                    clickOn(boton);
                    break;
                }
            }
        }
        WaitForAsyncUtils.waitForFxEvents();

        // --- PASO 7: MENSAJE DE ÉXITO ---
        verifyThat("Usuario eliminado correctamente.", isVisible());
        
        // Cerrar la alerta de éxito
        Set<Node> botonesExito = lookup(".dialog-pane .button").queryAll();
        for (Node nodo : botonesExito) {
            clickOn(nodo);
            break;
        }
        
        clickOn("#ComboBoxUser");
        sleep(2000);
    }
}