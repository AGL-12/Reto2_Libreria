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
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Book;
import model.ClassDAO;
import model.DBImplementation;

/**
 *
 * @author Alexander
 */
public class MainBookStoreController {

    @FXML
    private BorderPane root;
    @FXML
    private TilePane tileBooks;
    @FXML
    public HeaderController headerController;

    List<Book> libros = new ArrayList<>();

    private final ClassDAO dao = new DBImplementation();
    // El temporizador para el delay
    private PauseTransition pause;

    public void initialize() {
        cargarLibros("");
        // 2. CONFIGURAR EL DELAY (Por ejemplo, 0.5 segundos)
        // Esto crea un timer que espera 500ms antes de disparar su acción.
        pause = new PauseTransition(Duration.seconds(0.5));

        // Qué pasa cuando el timer termina (se acabó el tiempo de espera)
        pause.setOnFinished(event -> {
            // Obtenemos el texto actual del header y buscamos
            String textoABuscar = headerController.getSearchTextField().getText();
            System.out.println("Buscando en BD: " + textoABuscar); // Log para que veas el delay
            cargarLibros(textoABuscar);
        });

        // 3. CONECTAR EL LISTENER
        if (headerController != null) {
            headerController.getSearchTextField().textProperty().addListener((obs, oldVal, newVal) -> {
                // MAGIA: Cada vez que escribes una letra...

                // A. Reiniciamos el timer desde cero (si estaba contando, se para y vuelve a empezar)
                pause.playFromStart();

                // Resultado: Si escribes rápido "Harry", el timer se reinicia 5 veces
                // y solo se ejecuta 'cargarLibros' una vez al final.
            });
        }
    }

    public void headerMode(String mode) {
        switch (mode) {
            case "NO_USER":
                headerController.setForNoUser();
                break;
        }
    }

    private void cargarLibros(String string) {
        // Llamamos al método de búsqueda del DAO
        List<Book> librosEncontrados = dao.buscarLibros(string);
        // Limpiamos el panel visual
        tileBooks.getChildren().clear();

        // Rellenamos con los resultados
        for (Book lib : librosEncontrados) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookItem.fxml"));
                VBox libroBox = fxmlLoader.load();

                BookItemController libroItemController = fxmlLoader.getController();
                libroItemController.setData(lib);

                tileBooks.getChildren().add(libroBox);
            } catch (IOException ex) {
                Logger.getLogger(MainBookStoreController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
