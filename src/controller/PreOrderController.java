/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private VBox padre;
    @FXML
    private ImageView portada;
    @FXML
    private Label titulo;
    @FXML
    private Label precio;
    @FXML
    private Label cantidadLabel;

    private Book libro;
    private ShoppingCartController shoppingCartController;

    public void setData(Book libro, ShoppingCartController cartController) {
        this.libro = libro;
        this.shoppingCartController = cartController;

        titulo.setText(libro.getTitle());
        precio.setText(String.valueOf(libro.getPrice()));
        Image imagenOriginal = new Image(getClass().getResourceAsStream("/images/" + libro.getCover()));

        // Definimos el tamaño objetivo: Ancho 140, Alto 210 (Ratio 2:3)
        recortarImagen(portada, imagenOriginal, 80, 80);

        SpinnerValueFactory<Integer> valueFactory
                = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, libro.getStock(), 1);
        spinnerCantidad.setValueFactory(valueFactory);

        // Escuchador sin lambdas para actualizar el total al cambiar cantidad
        spinnerCantidad.valueProperty().addListener(new javafx.beans.value.ChangeListener<Integer>() {
            @Override
            public void changed(javafx.beans.value.ObservableValue<? extends Integer> obs, Integer oldVal, Integer newVal) {
                if (shoppingCartController != null) {
                    shoppingCartController.actualizarPrecioTotal();
                }
            }
        });

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

}
