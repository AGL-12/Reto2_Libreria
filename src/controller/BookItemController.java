package controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Book;
import model.UserSession;

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
    @FXML
    private Label contador;
    @FXML
    private StackPane stackImg;
    @FXML
    private ImageView soldOut;

    private Book book;

    public void setData(Book book) {
        this.book = book;
        setComponents();
    }

    @FXML
    private void openBookView(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookView.fxml"));
            Parent root = fxmlLoader.load();

            BookViewController cont = fxmlLoader.getController();
            cont.setData(book);
            cont.headerController.setMode(UserSession.getInstance().getUser(), "book view");

            // Referencia a la ventana vieja (para cerrarla luego)
            Stage oldStage = (Stage) rootBookItem.getScene().getWindow();
            
            // Crear la ventana NUEVA
            Stage newStage = new Stage();
            

            newStage.setTitle("Book&Bugs - " + book.getTitle()); 
            newStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/Book&Bugs_Logo.png")));

            newStage.setScene(new Scene(root));
            
            // Ajustes de tamaño y posición
            newStage.sizeToScene();
            newStage.centerOnScreen();

            // Mostrar la nueva y cerrar la vieja
            newStage.show();
            oldStage.close();
        } catch (IOException ex) {
            Logger.getLogger(BookItemController.class.getName()).log(Level.SEVERE, null, ex);
        }
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

   private void setComponents() {
        // Tooltips y Textos
        Tooltip cov = new Tooltip(book.getTitle());
        Tooltip.install(stackImg, cov);

        title.setText(book.getTitle());
        title.setTooltip(new Tooltip(book.getTitle()));

        contador.setText("(" + book.getComments().size() + ")");
        
        if (book.getAuthor() != null) {
            String textoAutor = book.getAuthor().toString();
            author.setText(textoAutor);
            Tooltip tooltip = new Tooltip(textoAutor);
            author.setTooltip(tooltip);
        } else {
            author.setText("Anónimo");
        }

        // --- CORRECCIÓN DE IMÁGENES (Anti-Caídas) ---
        
        // 1. Determinar nombre de la imagen (evitar nulos)
        String imageName = book.getCover();
        if (imageName == null || imageName.isEmpty()) {
            imageName = "default.png"; // Nombre por defecto si viene null de la BD
        }
        
        // 2. Intentar cargar la imagen del libro
        java.io.InputStream imageStream = getClass().getResourceAsStream("/images/" + imageName);
        
        // 3. Si no existe, cargar una imagen de respaldo QUE SEPAS QUE EXISTE (ej. el logo)
        if (imageStream == null) {
            System.out.println("⚠️ Imagen no encontrada: " + imageName + ". Usando respaldo.");
            // Asegúrate de usar una imagen que SÍ tengas subida, vi que tienes 'Book&Bugs_Logo.png'
            imageStream = getClass().getResourceAsStream("/images/Book&Bugs_Logo.png");
        }

        // 4. Cargar la imagen solo si tenemos un stream válido
        if (imageStream != null) {
            Image originalImage = new Image(imageStream);
            cutOutImage(cover, originalImage, 140, 210);
        }

        // 5. Cargar imagen SoldOut con seguridad
        java.io.InputStream soldOutStream = getClass().getResourceAsStream("/images/soldOut.png");
        if (soldOutStream != null) {
            Image soldOugImage = new Image(soldOutStream);
            cutOutImage(soldOut, soldOugImage, 140, 210);
        } else {
            System.out.println("⚠️ No se encontró la imagen soldOut.png");
        }

        // --- FIN CORRECCIÓN ---

        if (book.getStock() <= 0) {
            // --- NO HAY STOCK ---
            cover.setOpacity(0.5);      
        } else {
            // --- HAY STOCK ---
            soldOut.setVisible(false);  
        }
        
        starsController.setEditable(false); 
        starsController.setValueStars(book.getAvgValuation()); 
    }

}
