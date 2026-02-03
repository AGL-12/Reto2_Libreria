package controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
        txtSearch.setText(""); // Borra texto
        txtSearch.requestFocus(); // Mantiene el foco para seguir escribiendo
    }

    @FXML
    private void goToBuy(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/ShoppingCart.fxml"));
            Parent root = fxmlLoader.load();

            ShoppingCartController shopCont = fxmlLoader.getController();
            shopCont.headerController.setMode(UserSession.getInstance().getUser(), "buying");

            Stage stage = (Stage) rootHeader.getScene().getWindow();
            stage.setScene(new Scene(root));

            // 1. Calculamos cuánto mide la ventana nueva
            stage.sizeToScene();

            // 2. OPCIÓN A: Centrar en el medio del monitor (lo más fácil)
            stage.centerOnScreen();

            /* * 2. OPCIÓN B (MATEMÁTICA): Centrar relativa a la ventana anterior 
             * (Descomenta esto si quieres que salga encima de la vieja, no en medio de la pantalla)
             *
             * double centerX = oldStage.getX() + (oldStage.getWidth() / 2);
             * double centerY = oldStage.getY() + (oldStage.getHeight() / 2);
             * newStage.setX(centerX - (newStage.getWidth() / 2));
             * newStage.setY(centerY - (newStage.getHeight() / 2));
             */
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(HeaderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void seeAllPurchase(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/ShoppingHistory.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) rootHeader.getScene().getWindow();
            stage.setScene(new Scene(root));

            // 1. Calculamos cuánto mide la ventana nueva
            stage.sizeToScene();

            // 2. OPCIÓN A: Centrar en el medio del monitor (lo más fácil)
            stage.centerOnScreen();

            /* * 2. OPCIÓN B (MATEMÁTICA): Centrar relativa a la ventana anterior 
             * (Descomenta esto si quieres que salga encima de la vieja, no en medio de la pantalla)
             *
             * double centerX = oldStage.getX() + (oldStage.getWidth() / 2);
             * double centerY = oldStage.getY() + (oldStage.getHeight() / 2);
             * newStage.setX(centerX - (newStage.getWidth() / 2));
             * newStage.setY(centerY - (newStage.getHeight() / 2));
             */
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(HeaderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void backToMain(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MainBookStore.fxml"));
            Parent root = fxmlLoader.load();

            MainBookStoreController mainCont = fxmlLoader.getController();
            mainCont.headerController.setMode(UserSession.getInstance().getUser(), null);

            Stage stage = (Stage) rootHeader.getScene().getWindow();
            stage.setScene(new Scene(root));

            // 1. Calculamos cuánto mide la ventana nueva
            stage.sizeToScene();

            // 2. OPCIÓN A: Centrar en el medio del monitor (lo más fácil)
            stage.centerOnScreen();

            /* * 2. OPCIÓN B (MATEMÁTICA): Centrar relativa a la ventana anterior 
             * (Descomenta esto si quieres que salga encima de la vieja, no en medio de la pantalla)
             *
             * double centerX = oldStage.getX() + (oldStage.getWidth() / 2);
             * double centerY = oldStage.getY() + (oldStage.getHeight() / 2);
             * newStage.setX(centerX - (newStage.getWidth() / 2));
             * newStage.setY(centerY - (newStage.getHeight() / 2));
             */
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(HeaderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void logIn(ActionEvent event
    ) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/LoginWindow.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) rootHeader.getScene().getWindow();

            Stage oldStage = (Stage) rootHeader.getScene().getWindow();
            Stage newStage = new Stage();

            // Estilo sin bordes
            newStage.setScene(new Scene(root));

            // 1. Calculamos cuánto mide la ventana nueva
            newStage.sizeToScene();

            // 2. OPCIÓN A: Centrar en el medio del monitor (lo más fácil)
            newStage.centerOnScreen();

            /* * 2. OPCIÓN B (MATEMÁTICA): Centrar relativa a la ventana anterior 
             * (Descomenta esto si quieres que salga encima de la vieja, no en medio de la pantalla)
             *
             * double centerX = oldStage.getX() + (oldStage.getWidth() / 2);
             * double centerY = oldStage.getY() + (oldStage.getHeight() / 2);
             * newStage.setX(centerX - (newStage.getWidth() / 2));
             * newStage.setY(centerY - (newStage.getHeight() / 2));
             */
            newStage.show();
            oldStage.close();
        } catch (IOException ex) {
            Logger.getLogger(HeaderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void option(ActionEvent event
    ) {
        Profile pf = UserSession.getInstance().getUser();
        try {
            FXMLLoader fxmlLoader;
            if (pf instanceof User) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/view/MenuWindow.fxml"));
            } else if (pf instanceof Admin) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/view/OptionsAdmin.fxml"));
            } else {
                showAlert("No tiene una sesion iniciada", Alert.AlertType.WARNING);
                return;
            }
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) rootHeader.getScene().getWindow();
            stage.setScene(new Scene(root));

            // 1. Calculamos cuánto mide la ventana nueva
            stage.sizeToScene();

            // 2. OPCIÓN A: Centrar en el medio del monitor (lo más fácil)
            stage.centerOnScreen();

            /* * 2. OPCIÓN B (MATEMÁTICA): Centrar relativa a la ventana anterior 
             * (Descomenta esto si quieres que salga encima de la vieja, no en medio de la pantalla)
             *
             * double centerX = oldStage.getX() + (oldStage.getWidth() / 2);
             * double centerY = oldStage.getY() + (oldStage.getHeight() / 2);
             * newStage.setX(centerX - (newStage.getWidth() / 2));
             * newStage.setY(centerY - (newStage.getHeight() / 2));
             */
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(HeaderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void logOut(ActionEvent event
    ) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MainBookStore.fxml"));
            Parent root = fxmlLoader.load();

            UserSession.getInstance().setUser(null);

            MainBookStoreController main = fxmlLoader.getController();
            main.headerController.setMode(UserSession.getInstance().getUser(), null);

            Stage newStage = new Stage();

            newStage.setScene(new Scene(root));

            newStage.sizeToScene();

            newStage.centerOnScreen();

            newStage.show();
        } catch (IOException ex) {
            Logger.getLogger(HeaderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void configurarBotonBorrar() {
        // Si hay un icono mejor: btnSearch.setGraphic(new ImageView(...));
        btnSearch.visibleProperty().bind(txtSearch.textProperty().isNotEmpty());
    }

    public void setMode(Profile user, String filter) {
        if (user == null) {
            btnOption.setManaged(false);
            btnLogOut.setManaged(false);
            btnBuy.setManaged(false);
        } else if (user instanceof User) {
            btnLogIn.setManaged(false);
            lblUserName.setText(user.getName());
        } else if (user instanceof Admin) {
            btnLogIn.setManaged(false);
            lblUserName.setText(user.getName());
            btnBuy.setManaged(false);
        }
        if (filter == null) {
            btnBackMain.setManaged(false);
        } else {
            stackSearch.setVisible(false);
        }
        if ("buying".equals(filter)) {
            btnBuy.setManaged(false);
        }
        if ("book view".equals(filter)) {
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Gestión de librería");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
