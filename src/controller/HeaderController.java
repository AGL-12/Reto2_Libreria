package controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class HeaderController {

    @FXML
    private Button login;
    @FXML
    private Button opcione;
    @FXML
    private ImageView logo;
    @FXML
    private HBox padreHeader;

    public void initialize() {
        logoResponsive();
    }

    @FXML
    private void abrirLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/LoginWindow.fxml"));

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(HeaderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void logoResponsive() {
        // 1. Vinculamos el ancho de la imagen al 25% (0.25) del ancho del contenedor
        logo.fitWidthProperty().bind(padreHeader.widthProperty().multiply(0.25));
    }

}
