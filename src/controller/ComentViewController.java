/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;

/**
 * FXML Controller class
 *
 * @author mikel
 */
public class ComentViewController implements Initializable {

    @FXML
    private Label lblUsuario;
    @FXML
    private Label lblFecha;
    @FXML
    private TextArea txtComentario;
    @FXML
    private HBox buttonBox;
    @FXML
    private StarRateController estrellasController;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnBorrar;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        buttonBox.setVisible(false);
        buttonBox.setManaged(false);
        if (estrellasController != null) {
            estrellasController.setEditable(false);
        }

    }

    public void setDatos(String usuario, String fecha, String texto, float puntuacion) {
        this.lblUsuario.setText(usuario);
        this.lblFecha.setText(fecha);
        this.txtComentario.setText(texto);

        if (estrellasController != null) {
            estrellasController.setValueStars(puntuacion);
        }
    }

    public void activeEditable() {
        // 1. Mostrar los botones
        buttonBox.setVisible(true);
        buttonBox.setManaged(true);

        // 2. Habilitar edición
        txtComentario.setEditable(true);
        txtComentario.requestFocus();
    }
}
