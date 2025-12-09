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
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import model.Libro;

/**
 *
 * @author Alexander
 */
public class MainLibreriaController {

    @FXML
    private GridPane gridItemLibros;

    @FXML
    private BorderPane Padre;

    List<Libro> libros = new ArrayList<>();

    private List<Libro> getDatos() {
        Libro libro;

        for (int i = 0; i < 5; i++) {
            libro = new Libro();
            libro.setTitulo("carita wee");
            libro.setAutor("tu papi chulo");
            libro.setPuntuacion(1.2f);
            libros.add(libro);
        }

        return libros;
    }

    public void initialize() {
        libros.addAll(getDatos());
        int column = 0;
        int row = 1;

        try {
            for (Libro lib : libros) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/LibroItem.fxml"));
                VBox libroBox = fxmlLoader.load();
                LibroItemController libroItemController = fxmlLoader.getController();
                libroItemController.setData(lib);

                if (column == 6) {
                    column = 0;
                    ++row;
                }

                gridItemLibros.add(libroBox, column++, row);
                GridPane.setMargin(libroBox, new Insets(10));
            }
        } catch (IOException ex) {
            Logger.getLogger(MainLibreriaController.class.getName()).log(Level.SEVERE, "Error al cargar el FXML", ex);
        }

    }

}
