/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Book;

/**
 * FXML Controller class
 *
 * @author ander
 */
public class PreOrderController {

    @FXML
    private Spinner<Integer> spinnerCantidad;
    @FXML
    private ImageView imgBook;
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblPrice;

    private Book libro;
    private ShoppingCartController shoppingCartController;
    @FXML
    private HBox rootPreOrder;
    @FXML
    private Button btnDelete;

    public void setData(Book libro, ShoppingCartController cartController) {
        this.libro = libro;
        this.shoppingCartController = cartController;

        lblTitle.setText(libro.getTitle());
        lblPrice.setText(String.format("%.2f €", libro.getPrice()));

        // Esta es la parte que fallaba (Línea 57 aprox)
        // Ahora funcionará porque el FXML está bien importado
        SpinnerValueFactory<Integer> valueFactory
                = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, libro.getStock(), 1);
        spinnerCantidad.setValueFactory(valueFactory);

        spinnerCantidad.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (shoppingCartController != null) {
                shoppingCartController.actualizarPrecioTotal();
            }
        });

        if (libro.getCover() != null) {
            Image img = new Image(getClass().getResourceAsStream("/images/" + libro.getCover()));
            recortarImagen(imgBook, img, 80, 80);
        }
    }

    private void recortarImagen(ImageView imageView, Image image, double targetWidth, double targetHeight) {
        // Establecemos el tamaño final que tendrá el ImageView
        imageView.setFitWidth(targetWidth);
        imageView.setFitHeight(targetHeight);

        // Algoritmo "Center Crop"
        double originalWidth = image.getWidth();
        double originalHeight = image.getHeight();

        double scaleX = targetWidth / originalWidth;
        double scaleY = targetHeight / originalHeight;

        // Elegimos el factor de escala mayor para asegurar que llenamos todo el hueco
        double scale = Math.max(scaleX, scaleY);

        // Calculamos el tamaño que tendría la imagen escalada
        double scaledWidth = originalWidth * scale;
        double scaledHeight = originalHeight * scale;

        // Calculamos el Viewport (la ventana de recorte sobre la imagen original)
        double viewportWidth = targetWidth / scale;
        double viewportHeight = targetHeight / scale;

        // Centramos el recorte (x, y)
        double viewportX = (originalWidth - viewportWidth) / 2;
        double viewportY = (originalHeight - viewportHeight) / 2;

        // Aplicamos la imagen y el recorte
        imageView.setImage(image);
        imageView.setViewport(new Rectangle2D(viewportX, viewportY, viewportWidth, viewportHeight));
        imageView.setSmooth(true); // Suavizado para mejor calidad
        imageView.setPreserveRatio(false); // Importante: desactivar para que obedezca al viewport
    }

    public Book getBook() {
        return this.libro;
    }

    public int getCantidadSeleccionada() {
        // Devolvemos el número que el usuario ha elegido en el selector
        return spinnerCantidad.getValue();
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (shoppingCartController != null) {
            // Llamamos a un nuevo método que crearemos en el ShoppingCartController
            shoppingCartController.eliminarLibroDelCarrito(libro);
        }
    }

}
