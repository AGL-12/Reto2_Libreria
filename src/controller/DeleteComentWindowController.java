/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.URL;
import java.sql.Date;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import model.Book;
import model.Commentate;
import model.Profile;

/**
 * FXML Controller class
 *
 * @author uazko
 */
public class DeleteComentWindowController implements Initializable {

    @FXML
    private Button btnReturn;
    @FXML
    private TextField txtNameUsu;
    @FXML
    private TableColumn<Book, String> columnTitle;
    @FXML
    private TableColumn<Commentate, String> columnComent;
    @FXML
    private TableColumn<Commentate, Date> columnDate;
    @FXML
    private Button btnDeleteComent;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
}
