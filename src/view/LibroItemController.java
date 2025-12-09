package view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.Libro;

public class LibroItemController {

    @FXML
    private Label autor;

    @FXML
    private ImageView portada;

    @FXML
    private Label titulo;

    @FXML
    private StackPane valoracion;
    @FXML
    private VBox padre;

    private Libro libro;
    
    public void setData(Libro libro) {
        this.libro = libro;
        titulo.setText(libro.getTitulo());
        autor.setText(libro.getAutor());
    }

    public void initialize() {
        
    }
}
