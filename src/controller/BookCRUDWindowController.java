/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Profile;

/**
 * FXML Controller class
 *
 * @author uazko
 */
public class BookCRUDWindowController implements Initializable {

    @FXML
    private Button btnConfirm;
    @FXML
    private Button btnReturn;
    @FXML
    private TextField txtISBN;
    @FXML
    private TextField txtTitle;
    @FXML
    private TextField txtIdAuthor;
    @FXML
    private TextField txtPages;
    @FXML
    private TextField txtStock;
    @FXML
    private TextField txtSinopsis;
    @FXML
    private TextField txtPrice;
    @FXML
    private TextField txtEditorial;
    @FXML
    private ImageView idFrontPage;
    @FXML
    private Button btnUploadFile;

    private File frontPageFile;
    private Controller cont; // Controller to handle business logic
    private Profile profile;
    private String mod;

    /**
     * Initializes the controller class.
     */
    public void setMod(String option) {
        this.mod = option;
    }
    public void setCont(Controller cont) {
        this.cont = cont;
    }

    // Set the current admin profile
    public void setProfile(Profile profile) {
        this.profile = profile;
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        frontPageFile = null;
        btnConfirm.setText(mod);
    }

    @FXML
    private void dragOver(DragEvent event) {
        Dragboard db = event.getDragboard();

        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }

        event.consume();
    }

    @FXML
    private void dropImage(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            File file = files.get(0); // Solo una imagen

            // Validar que sea imagen
            if (file.getName().matches(".*\\.(png|jpg|jpeg)$")) {
                frontPageFile = file;
                Image imagen = new Image(file.toURI().toString());
                idFrontPage.setImage(imagen);
                success = true;
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    @FXML

    private void uploadFrontPage() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar portada del libro");

        // Filtros solo para imágenes
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) btnUploadFile.getScene().getWindow();

        frontPageFile = fileChooser.showOpenDialog(stage);

        if (frontPageFile != null) {
            Image imagen = new Image(frontPageFile.toURI().toString());
            idFrontPage.setImage(imagen);
        }
    }

}
