package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import model.DBImplementation;
import model.User;
import model.Admin;
import model.UserSession;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.base.NodeMatchers.isEnabled;
import javafx.scene.input.KeyCode;
import java.util.Set;
import javafx.scene.Node;
import javafx.collections.ObservableList;

/**
 * Test de integración para el borrado de usuarios por el Administrador.
 */
public class DeleteAccountAdminControllerTest extends ApplicationTest {

    private final DBImplementation db = new DBImplementation();
    private final String LOGIN_TEST = "user_test_delete";
    private final String ADMIN_PASS = "1234";

    @Override
    public void start(Stage stage) throws Exception {
        // 1. COMPROBAR Y CREAR USUARIO A ELIMINAR
        // Buscamos en la lista si existe el usuario de prueba
        boolean existe = false;
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

        // 2. CONFIGURAR ADMIN EN SESIÓN
        // Como ahora pides la contraseña del ADMIN, necesitamos que haya uno en la sesión
        Admin admin = new Admin();
        admin.setUsername("adminTest");
        admin.setPassword(ADMIN_PASS);
        UserSession.getInstance().setUser(admin);

        // 3. LANZAR VENTANA
        Parent root = FXMLLoader.load(getClass().getResource("/view/DeleteAccountAdmin.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    public void testBorrarUsuarioAdmin() {
        // 1. Buscar y seleccionar el usuario específico en el ComboBox
        ComboBox<User> combo = lookup("#ComboBoxUser").queryAs(ComboBox.class);
        ObservableList<User> items = combo.getItems();
        User usuarioEncontrado = null;

        for (User u : items) {
            if (u.getUsername().equals(LOGIN_TEST)) {
                usuarioEncontrado = u;
                break;
            }
        }

        final User finalUser = usuarioEncontrado;
        interact(new Runnable() {
            @Override
            public void run() {
                combo.getSelectionModel().select(finalUser);
            }
        });
        
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#TextFieldPassword").write(ADMIN_PASS);
        
        WaitForAsyncUtils.waitForFxEvents();

        // 3. Verificar que el botón 'Borrar' esté habilitado y pulsar
        verifyThat("#Button_Delete", isEnabled());
        clickOn("#Button_Delete");

        // 4. Gestión de la alerta de confirmación
        WaitForAsyncUtils.waitForFxEvents();
        Set<Node> botonesConfirmacion = lookup(".dialog-pane .button").queryAll();
        for (Node nodo : botonesConfirmacion) {
            Button boton = (Button) nodo;
            String texto = boton.getText().toLowerCase();
            if (texto.equals("sí") || texto.equals("yes") || texto.equals("aceptar")) {
                clickOn(boton);
                break;
            }
        }
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat("Usuario eliminado correctamente.", isVisible());
        
        Set<Node> botonesExito = lookup(".dialog-pane .button").queryAll();
        for (Node nodo : botonesExito) {
            clickOn(nodo); 
            break;
        }
    }
}