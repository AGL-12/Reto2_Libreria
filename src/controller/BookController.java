/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author mikel
 */
public class BookController implements Initializable {

    @FXML
    private VBox vboxComentarios;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        cargarComentarioPrueba();
    }    

    private void cargarComentarioPrueba() {
        try {
            for (int i = 0; i < 5; i++) {
                 FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ComentView.fxml"));
            Parent nodoComentario = loader.load();
            ComentViewController controller = loader.getController();
            
            controller.setDatos(
                    "Mikel (Tutor)",
                    "16/12/2025",
                    "¡Funciona! PRobando",
                    5
            );
            
            vboxComentarios.getChildren().add(nodoComentario);
            }
           
        } catch (IOException ex) {
            Logger.getLogger(BookController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
