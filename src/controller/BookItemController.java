package controller;

import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import model.Book;

public class BookItemController {

    @FXML
    private VBox rootBookItem;
    @FXML
    private ImageView cover;
    @FXML
    private Label title;
    @FXML
    private Label author;
    @FXML
    private StarRateController starsController;

    private Book book;

    public void setData(Book book) {
        //this.book = book;
        Tooltip cov = new Tooltip(book.getTitle());
        Tooltip.install(cover, cov);

        title.setText(book.getTitle());
        title.setTooltip(new Tooltip(book.getTitle()));
        // 1. Verificamos que la lista no sea null ni esté vacía para evitar errores
        if (book.getAuthor() != null) {

            String textoAutor = book.getAuthor().toString();

            author.setText(textoAutor);

            // 3. Crear y asignar el Tooltip con el texto COMPLETO
            Tooltip tooltip = new Tooltip(textoAutor);

            author.setTooltip(tooltip);

        } else {
            author.setText("Anónimo"); // O déjalo vacío ""
        }
        Image originalImage = new Image(getClass().getResourceAsStream("/images/" + book.getCover()));

        // Definimos el tamaño objetivo: Ancho 140, Alto 210 (Ratio 2:3)
        cutOutImage(cover, originalImage, 140, 210);
        // CONFIGURACIÓN:
        starsController.setEditable(false); // BLOQUEADO
        starsController.setValueStars(book.getAvgValuation()); // PINTAR NOTA
    }

    /**
     * 1. Calcula cuánto hay que escalar la imagen para llenar el hueco. 2.
     * Calcula el centro de la imagen (Viewport). 3. Aplica el recorte.
     */
    private void cutOutImage(ImageView imageView, Image image, double targetWidth, double targetHeight) {
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

}
