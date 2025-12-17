/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import model.Author;
import model.Book;

/**
 * FXML Controller class
 *
 * @author ander
 */
public class ShoppingCartController implements Initializable {

    
    private TilePane tileLibros;
    
    List<Book> libros = new ArrayList<>();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        libros.addAll(cargarLibros());

        try {
            for (Book lib : libros) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/PreOrder.fxml"));
                VBox libroBox = fxmlLoader.load();
                PreOrderController preOrderController = fxmlLoader.getController();
                preOrderController.setData(lib);
                tileLibros.getChildren().add(libroBox);
            }
        } catch (IOException ex) {
            Logger.getLogger(MainBookStoreController.class.getName()).log(Level.SEVERE, "Error al cargar el FXML", ex);
        }
    }

    /*Metodo prueba para tener libros cargados*/
    private List<Book> cargarLibros() {
        Book libro;
        List<Author> listAuthors = new ArrayList<>();
        Author a = new Author(1, "Alex", "Boss");
        listAuthors.add(a);

        for (int i = 0; i < 5; i++) {
            libro = new Book();
            libro.setCover("mood-heart.png");
            libro.setTitulo("carita wee");
            libro.setListAuthors(listAuthors);
            libro.setAvgValuation(1.2f);
            libros.add(libro);
        }

        return libros;
        

    }

}
