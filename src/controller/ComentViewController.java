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
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

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
    private TextArea comentario;
    @FXML
    private EstrellaPuntuacionController estrellasController;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        estrellasController.setEditable(true);
        comentario.setEditable(false);
        comentario.setWrapText(true);
        
        
        if(estrellasController != null){
            estrellasController.setEditable(false);
        }
        
    } 
    
    public void setDatos(String usuario, String fecha, String texto, float puntuacion){
        this.lblUsuario.setText(usuario);
        this.lblFecha.setText(fecha);
        this.comentario.setText(texto);
        
        if(estrellasController != null){
            estrellasController.setNota(puntuacion);
        }
    }
    
}
