package controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.Admin;
import model.Profile;
import model.User;
import model.UserSession;

public class HeaderController {

    @FXML
    private HBox rootHeader;
    @FXML
    private ImageView logo;
    @FXML
    private Label lblUserName;
    @FXML
    private Button btnBuy;
    @FXML
    private Button btnBackMain;
    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnLogIn;
    @FXML
    private Button btnOption;
    @FXML
    private Button btnLogOut;
    @FXML
    private StackPane stackSearch;

    // --- NUEVO GETTER ---
    // Esto permite que el MainBookStoreController escuche lo que escribes aquí
    public TextField getSearchTextField() {
        return txtSearch;
    }

    public void initialize() {
        configurarBotonBorrar();
    }

    @FXML
    public void clearSearch() {
        txtSearch.setText("");
        txtSearch.requestFocus();
    }

    @FXML
    private void goToBuy(ActionEvent event) {
        navigate(event, "/view/ShoppingCart.fxml", "Carrito de Compra");
    }

    @FXML
    private void backToMain(ActionEvent event) {
        navigate(event, "/view/MainBookStore.fxml", "Librería");
    }

    @FXML
    private void logIn(ActionEvent event) {
        // Al ir al login, mantenemos la ventana.
        navigate(event, "/view/LoginWindow.fxml", "Book&Bugs - Login");
    }

    @FXML
    private void logOut(ActionEvent event) {
        // 1. Limpiamos la sesión
        UserSession.getInstance().cleanUserSession();

        // 2. Navegamos al Main (ahora se cargará como anónimo)
        navigate(event, "/view/MainBookStore.fxml", "Librería");
    }

    @FXML
    private void option(ActionEvent event) {
        Profile pf = UserSession.getInstance().getUser();

        if (pf instanceof User) {
            navigate(event, "/view/MenuWindow.fxml", "Opciones de Usuario");
        } else if (pf instanceof Admin) {
            navigate(event, "/view/OptionsAdmin.fxml", "Panel de Administración");
        }
    }

    private void configurarBotonBorrar() {
        if (btnSearch != null && txtSearch != null) {
            btnSearch.visibleProperty().bind(txtSearch.textProperty().isNotEmpty());
        }
    }

    public void setMode(Profile user, String filter) {
        boolean loggedIn = (user != null);
        boolean isAdmin = (user instanceof Admin);

        // Gestión de botones de usuario
        btnOption.setVisible(loggedIn);
        btnOption.setManaged(loggedIn);

        btnLogOut.setVisible(loggedIn);
        btnLogOut.setManaged(loggedIn);

        // Botón Login (solo visible si NO estás logueado)
        btnLogIn.setVisible(!loggedIn);
        btnLogIn.setManaged(!loggedIn);

        // Botón Comprar (solo visible si logueado y NO es admin)
        boolean canBuy = loggedIn && !isAdmin;
        btnBuy.setVisible(canBuy);
        btnBuy.setManaged(canBuy);

        // Texto de bienvenida
        if (loggedIn) {
            lblUserName.setText(user.getName());
        } else {
            lblUserName.setText("Bienvenido");
        }

        // Gestión de filtros de vista (para ocultar buscador o botón volver)
        boolean isMainView = (filter == null);

        // Botón Volver (Solo visible si NO estamos en la vista principal)
        btnBackMain.setVisible(!isMainView);
        btnBackMain.setManaged(!isMainView);

        // Buscador (Solo visible en la vista principal)
        stackSearch.setVisible(isMainView);

        // Caso especial: En el carrito no mostramos el botón de comprar (obvio)
        if ("buying".equals(filter)) {
            btnBuy.setVisible(false);
            btnBuy.setManaged(false);
        }
    }

    /**
     * metodo reutilizable para cada oopcion del header.
     */
    private void navigate(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            configurarControladorDestino(loader.getController());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));

            if (title != null) {
                stage.setTitle(title);
            }
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(HeaderController.class.getName()).log(Level.SEVERE, "Error navegando a " + fxmlPath, ex);
        }
    }

    /**
     * Configura los controladores que requieren pasar datos de sesión o modo.
     */
    private void configurarControladorDestino(Object controller) {
        Profile user = UserSession.getInstance().getUser();

        if (controller instanceof MainBookStoreController) {
            ((MainBookStoreController) controller).headerController.setMode(user, null);
        } else if (controller instanceof ShoppingCartController) {
            ((ShoppingCartController) controller).headerController.setMode(user, "buying");
        }
        // Puedes añadir más 'else if' si otros controladores necesitan datos iniciales
    }
}
