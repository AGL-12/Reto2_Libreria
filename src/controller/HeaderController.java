package controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class HeaderController {

    @FXML
    private HBox rootHeader;
    @FXML
    private ImageView logo;
    @FXML
    private Label lblUserName;
    @FXML
    private Button logIn;
    @FXML
    private Button option;
    @FXML
    private Button logOut;
    @FXML
    private Button btnBuy;
    @FXML
    private Button btnAllPurchase;
    @FXML
    private Button btnBackMain;
    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnSearch;

    public void initialize() {
        logoResponsive();
        configurarBotonBorrar();
    }

    @FXML
    private void clearSearch(ActionEvent event) {
        txtSearch.setText(""); // Borra texto
        txtSearch.requestFocus(); // Mantiene el foco para seguir escribiendo
    }

    @FXML
    private void abrirLogin() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/LoginWindow.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) rootHeader.getScene().getWindow();

            Stage oldStage = (Stage) rootHeader.getScene().getWindow();
            Stage newStage = new Stage();

            // Estilo sin bordes
            newStage.initStyle(StageStyle.UNDECORATED);
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

    private void logoResponsive() {
        // 1. Vinculamos el ancho de la imagen al 25% (0.25) del ancho del contenedor
        logo.fitWidthProperty().bind(rootHeader.widthProperty().multiply(0.25));
    }

    void setForNoUser() {
        option.setManaged(false);
        logOut.setManaged(false);
        btnAllPurchase.setManaged(false);
        btnBackMain.setManaged(false);
    }

    private void configurarBotonBorrar() {
        // Si tienes un icono mejor: btnSearch.setGraphic(new ImageView(...));
        btnSearch.visibleProperty().bind(txtSearch.textProperty().isNotEmpty());
        
        // --- O ---
        
        // Opción 2: Si quieres que TAMBIÉN se esconda si solo hay espacios en blanco ("   ")
        /*
        btnSearch.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            String texto = txtSearch.getText();
            return texto != null && !texto.trim().isEmpty();
        }, txtSearch.textProperty()));
        */
    }

    // --- NUEVO GETTER ---
    // Esto permite que el MainBookStoreController escuche lo que escribes aquí
    public TextField getSearchTextField() {
        return txtSearch;
    }
}
