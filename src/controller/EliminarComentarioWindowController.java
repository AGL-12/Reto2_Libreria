/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import model.Profile;

/**
 * FXML Controller class
 *
 * @author uazko
 */
public class EliminarComentarioWindowController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private Controller cont; // Controller to handle business logic
    private Profile profile; // Currently logged-in admin
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    public void setCont(Controller cont) {
        this.cont = cont;
    }

    // Set the current admin profile
    public void setProfile(Profile profile) {
        this.profile = profile;
    }
    
}
