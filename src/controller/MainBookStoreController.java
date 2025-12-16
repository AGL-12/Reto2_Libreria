/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import model.Book;

/**
 *
 * @author Alexander
 */
public class MainBookStoreController {

    @FXML
    private BorderPane root;
    @FXML
    private TilePane tileBooks;

    List<Book> libros = new ArrayList<>();

    private List<Book> getDatos() {
        Book libro;

        for (int i = 0; i < 5; i++) {
            libro = new Book();
            libro.setCover("mood-heart.png");
            libro.setTitulo("carita wee");
            libro.setIdAuthor(1);
            libro.setAvgValuation(1.2f);
            libros.add(libro);
        }

        return libros;
    }

    public void initialize() {
        libros.addAll(getDatos());

        try {
            for (Book lib : libros) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookItem.fxml"));
                VBox libroBox = fxmlLoader.load();
                BookItemController libroItemController = fxmlLoader.getController();
                libroItemController.setData(lib);
                tileBooks.getChildren().add(libroBox);
            }
        } catch (IOException ex) {
            Logger.getLogger(MainBookStoreController.class.getName()).log(Level.SEVERE, "Error al cargar el FXML", ex);
        }

    }

}
