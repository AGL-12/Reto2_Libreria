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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import model.Profile;

/**
 * FXML Controller class
 *
 * @author uazko
 */
public class BookOptionWindowController implements Initializable {

    @FXML
    private Button btnReturn;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnModify;
    @FXML
    private Button btnDelete;

    private Controller cont; // Controller to handle business logic
    private Profile profile; // Currently logged-in admin
    private String mod;

    /**
     * Initializes the controller class.
     */
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

    @FXML
    private void Return(ActionEvent event) {
    }

    @FXML
    private void createBook(ActionEvent event) {
        mod = "create";
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookCRUDWindow.fxml"));
        try {
            javafx.scene.Parent root = fxmlLoader.load();
            controller.BookCRUDWindowController controllerWindow = fxmlLoader.getController();
            controllerWindow.setProfile(profile);
            controllerWindow.setCont(this.cont);
            controllerWindow.setMod(mod);
        } catch (IOException ex) {
            Logger.getLogger(BookOptionWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void modifyBook(ActionEvent event) {
        mod = "modify";
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookCRUDWindow.fxml"));
        try {
            javafx.scene.Parent root = fxmlLoader.load();
            controller.BookCRUDWindowController controllerWindow = fxmlLoader.getController();
            controllerWindow.setProfile(profile);
            controllerWindow.setCont(this.cont);
            controllerWindow.setMod(mod);
        } catch (IOException ex) {
            Logger.getLogger(BookOptionWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void deleteBook(ActionEvent event) {
        mod = "modify";
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookCRUDWindow.fxml"));
        try {
            javafx.scene.Parent root = fxmlLoader.load();
            controller.BookCRUDWindowController controllerWindow = fxmlLoader.getController();
            controllerWindow.setProfile(profile);
            controllerWindow.setCont(this.cont);
            controllerWindow.setMod(mod);
        } catch (IOException ex) {
            Logger.getLogger(BookOptionWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
