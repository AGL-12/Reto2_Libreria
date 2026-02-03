package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import model.ClassDAO;
import model.DBImplementation;
import model.UserSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.framework.junit.ApplicationTest;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 *
 * @author Alexander
 */
public class MainBookStoreControllerTest extends ApplicationTest {

    ClassDAO dao = new DBImplementation();

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainBookStore.fxml"));
        Parent root = loader.load();
        MainBookStoreController main = loader.getController();
        main.headerController.setMode(UserSession.getInstance().getUser(), null);

        stage.setScene(new Scene(root));
        stage.show();
        stage.toFront();
    }

    @Test
    public void testHeaderEstado() {
        verifyThat("#btnLogIn", isVisible());

        Label name = lookup("#lblUserName").query();
        Assert.assertEquals("Bienvenido", name.getText());

        verifyThat("#txtSearch", isVisible());

        System.out.println("Test Header: OK. Estado inicial correcto (LogIn visible).");
    }

    @Test
    public void testCargaLibros() {
        int total = dao.getAllBooks().size();
        // 1. Verificamos que el contenedor de libros es visible
        verifyThat("#tileBooks", isVisible());

        // 2. Comprobamos que hay libros dentro
        TilePane tileBooks = lookup("#tileBooks").query();
        Assert.assertEquals("no muestra todos los libros", total, tileBooks.getChildren().size());
    }

    @Test
    public void testHeaderSearch() {
        TilePane tileBooks = lookup("#tileBooks").query();
        int totalLibros = tileBooks.getChildren().size();

        Assert.assertTrue("Se necesitan libros en la BD para probar el buscador", totalLibros > 0);

        clickOn("#txtSearch");

        write("Harry");

        // ESPERAR AL THREAD (PauseTransition de 0.5s)
        sleep(1000);

        int librosFiltrados = tileBooks.getChildren().size();
        System.out.println("Libros tras buscar 'Harry': " + librosFiltrados);

        Assert.assertTrue("El filtro debería mantener o reducir la cantidad, nunca aumentar", librosFiltrados <= totalLibros);

        // Caso: Borrar búsqueda
        clickOn("#btnSearch");
        sleep(1000); // Esperar a que se restauren

        Assert.assertEquals("Al borrar, deben volver todos los libros",
                totalLibros,
                tileBooks.getChildren().size());

        System.out.println("Test Buscador: OK.");
    }
    @Test
    public void testClickBook() {
        clickOn("1984");
        sleep(1500);
        clickOn("#btnBackMain");
    }

    @Test
    public void testLogIn() {
        clickOn("#btnLogIn");
        clickOn("#Button_SignUp");
        clickOn("#textFieldEmail").write("test@test.test");
        clickOn("#textFieldUsername").write("test");
        clickOn("#textFieldName").write("testname");
        clickOn("#textFieldSurname").write("testsurname");
        clickOn("#textFieldTelephone").write("101010101");
        clickOn("#textFieldCardN").write("0101010101010101");
        clickOn("#textFieldPassword").write("1234");
        clickOn("#textFieldCPassword").write("1234");
        clickOn("#rButtonO");
        clickOn("#buttonSignUp");
        sleep(1000);
        type(KeyCode.ENTER);
        //header estado
        sleep(2000);
        verifyThat("#btnOption", isVisible());
        verifyThat("#btnLogOut", isVisible());
        verifyThat("#btnBuy", isVisible());

        Label name = lookup("#lblUserName").query();
        Assert.assertEquals("testname", name.getText());

        verifyThat("#txtSearch", isVisible());

        System.out.println("Test Header: OK. Estado Logged correcto (LogOut visible).");
    }

}
