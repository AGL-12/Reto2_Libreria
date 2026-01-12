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
import java.util.stream.Collectors;
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
import model.Profile;
import model.User;

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

    private List<Book> allBooks = new ArrayList<>();
    private final ClassDAO dao = new DBImplementation();
    // El temporizador para el delay
    private PauseTransition pause;

    @FXML
    public void initialize() {
        allBooks = dao.getAllBooks();

        showBooks(allBooks);
        // 2. CONFIGURAR EL DELAY (Por ejemplo, 0.5 segundos)
        // Esto crea un timer que espera 500ms antes de disparar su acción.
        pause = new PauseTransition(Duration.seconds(0.5));

        // Qué pasa cuando el timer termina (se acabó el tiempo de espera)
        pause.setOnFinished(event -> {
            // Obtenemos el texto actual del header y buscamos
            String textoABuscar = headerController.getSearchTextField().getText().trim();
            System.out.println("Buscando en BD: " + textoABuscar); // Log para que veas el delay
            searchBooks(textoABuscar);
        });

        // 3. CONECTAR EL LISTENER
        if (headerController != null) {
            headerController.getSearchTextField().textProperty().addListener((obs, oldVal, newVal) -> {
                // MAGIA: Cada vez que escribes una letra...

                // A. Reiniciamos el timer desde cero (si estaba contando, se para y vuelve a empezar)
                pause.playFromStart();

                // Resultado: Si escribes rápido "Harry", el timer se reinicia 5 veces
            });
        }
    }

    private void searchBooks(String text) {
        if (text == null || text.trim().isEmpty()) {
            // Si borran el texto, mostramos la lista maestra entera
            showBooks(allBooks);
            return;
        }

        String busqueda = text.toLowerCase();

        // FILTRO PROFESIONAL CON STREAMS
        List<Book> filtrados = allBooks.stream()
                .filter(b -> {
                    // Condiciones de búsqueda (Título OR Autor OR ISBN)
                    boolean coincideTitulo = b.getTitle().toLowerCase().contains(busqueda);
                    // Ojo con los nulos en autor
                    boolean coincideAutor = b.getAuthor() != null && b.getAuthor().toString().toLowerCase().contains(busqueda);
                    boolean coincideISBN = String.valueOf(b.getISBN()).contains(busqueda);

                    return coincideTitulo || coincideAutor || coincideISBN;
                })
                .collect(Collectors.toList());

        showBooks(filtrados);
    }

    private void showBooks(List<Book> allBooks) {
        tileBooks.getChildren().clear();

        try {
            for (Book lib : allBooks) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookItem.fxml"));
                VBox libroBox = fxmlLoader.load();

                BookItemController itemController = fxmlLoader.getController();

                // Aquí 'lib' ya viene con el avgValuation calculado desde el DAO
                itemController.setData(lib);

                tileBooks.getChildren().add(libroBox);
            }
        } catch (IOException ex) {
            Logger.getLogger(MainBookStoreController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
