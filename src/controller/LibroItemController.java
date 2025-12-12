package controller;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import model.Book;

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
    @FXML
    private ImageView estrallaFondo;
    @FXML
    private ImageView estrellaFrente;

    public void initialize() {
    }
    // Variable para saber cuánto miden tus estrellas de ancho (ajústalo a tu PNG)
    private final double ANCHO_TOTAL_ESTRELLAS = 120.0;
    private Book libro;

    public void setData(Book libro) {
        this.libro = libro;
        titulo.setText(libro.getTitulo());
        autor.setText(String.valueOf(libro.getIdAuthor()));
        Image imagenOriginal = new Image(getClass().getResourceAsStream("/images/" + libro.getCover()));

        // Definimos el tamaño objetivo: Ancho 140, Alto 210 (Ratio 2:3)
        recortarImagen(portada, imagenOriginal, 140, 210);
        // Llamamos a la función de las estrellas
        setValoracion(libro.getAvgValuation());
    }

    /**
     * 1. Calcula cuánto hay que escalar la imagen para llenar el hueco. 2.
     * Calcula el centro de la imagen (Viewport). 3. Aplica el recorte.
     */
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

    private void setValoracion(float avgValuation) {
        // Asumimos que la puntuación es sobre 5 (ej: 3.5 / 5)

        // 1. Calcular porcentaje (matemática simple)
        // Si tienes 5 estrellas, 3.5 puntos es el 70% del ancho
        double porcentaje = avgValuation / 5.0;

        // Protegemos por si viene un dato loco (negativo o mayor a 5)
        if (porcentaje > 1) {
            porcentaje = 1;
        }
        if (porcentaje < 0) {
            porcentaje = 0;
        }

        // 2. Calcular ancho visible
        double anchoVisible = ANCHO_TOTAL_ESTRELLAS * porcentaje;

        // 3. Crear el recorte (La tijera)
        // Rectangle(ancho, alto) -> El alto debe cubrir la imagen (ej. 24 o 30)
        Rectangle recorte = new Rectangle(anchoVisible, 30);

        // 4. Aplicar el recorte al panel de arriba
        estrellaFrente.setClip(recorte);
    }
}
