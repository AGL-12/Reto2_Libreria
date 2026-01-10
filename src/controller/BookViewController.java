/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import static com.mchange.v2.c3p0.impl.C3P0Defaults.user;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import model.Book;
import model.ClassDAO;
import model.Commentate;
import model.DBImplementation;
import model.Profile;
import model.User;
import model.UserSession;
import org.hibernate.Session;
import utilities.HibernateUtil;

/**
 * FXML Controller class
 *
 * @author mikel
 */
public class BookViewController implements Initializable {

    @FXML
    private ImageView coverBook;
    @FXML
    private Label titleBook;
    @FXML
    private Label authorName;
    @FXML
    private Label priceBook;
    @FXML
    private Label sypnosis;
    @FXML
    private Label stockBook;
    @FXML
    private Button btnSaveComment;
    @FXML
    private Button btnAddComment;
    @FXML
    private VBox commentsContainer;

    private Book currentBook;

    private final ClassDAO dao = new DBImplementation();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initContextMenu();
    }

    /**
     * Configura el menú de clic derecho (Context Menu). Requerido para el 100%
     * en controles avanzados.
     */
    private void initContextMenu() {
        /*
        ContextMenu contextMenu = new ContextMenu();

        MenuItem itemEditar = new MenuItem("Modificar Comentario");
        itemEditar.setOnAction(this::handleModify);

        MenuItem itemBorrar = new MenuItem("Eliminar Comentario");
        itemBorrar.setOnAction(this::handleDelete);

        contextMenu.getItems().addAll(itemEditar, itemBorrar);
        listViewComments.setContextMenu(contextMenu);
         */
    }

    /**
     * Refresca la lista visual pidiendo los datos actualizados al modelo.
     */
    private void refreshList() {
        commentsContainer.getChildren().clear();
        try {
            // currentBook es el libro que estás visualizando
            List<Commentate> comentarios = dao.getCommentsByBook(currentBook.getISBN());

            for (Commentate coment : comentarios) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/CommentView.fxml"));
                Parent commentBox = fxmlLoader.load();

                CommentViewController con = fxmlLoader.getController();
                con.setData(coment);

                commentsContainer.getChildren().add(commentBox);
            }
        } catch (IOException ex) {
            Logger.getLogger(BookViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Gestión de librería");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    void setData(Book book) {
        this.currentBook = book;
        Image originalImage = new Image(getClass().getResourceAsStream("/images/" + book.getCover()));

        // Definimos el tamaño objetivo: Ancho 140, Alto 210 (Ratio 2:3)
        cutOutImage(coverBook, originalImage, 140, 210);

        titleBook.setText(book.getTitle());
        authorName.setText(book.getAuthor().toString());
        priceBook.setText("precio: " + book.getPrice());
        sypnosis.setText(book.getSypnosis());
        stockBook.setText("Stock: " + book.getStock());

        refreshList();
    }

    @FXML
    private void handleNewComment(ActionEvent event) {

        Profile currentUser = UserSession.getInstance().getUser();

        if (currentUser == null) {
            showAlert("Debes iniciar sesión para comentar", Alert.AlertType.ERROR);
            return;
        } else if (!(currentUser instanceof User)) {
            showAlert("Los administradores no pueden publicar opiniones.", Alert.AlertType.ERROR);
            return;
        }
        
        
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Nuevo comentario");
        dialog.setHeaderText("Escribe tu opinión sobre " + currentBook.getTitle());
        dialog.setContentText("Comentario");
        
        // Esperamos a que el usuario escriba y pulse Aceptar
        java.util.Optional<String> result = dialog.showAndWait();

        // 3. Si hay texto, procedemos
        result.ifPresent(textoComentario -> {
            if (textoComentario.trim().isEmpty()) return;

            try {
                // A. Crear el objeto Comentario (Asumimos 5 estrellas por defecto)
                Commentate newComment = new Commentate((User) currentUser, currentBook, textoComentario, 5.0f);

                // B. Guardar en la Base de Datos
                dao.addComment(newComment);

                // C. ACTUALIZAR LA VISTA AL INSTANTE (Sin recargar)
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/CommentView.fxml"));
                Parent commentBox = fxmlLoader.load();

                // Pasamos los datos a la tarjeta del comentario
                CommentViewController controller = fxmlLoader.getController();
                controller.setData(newComment);

                // D. EL TRUCO: Añadir en la posición 0 (arriba del todo)
                commentsContainer.getChildren().add(0, commentBox);

                showAlert("¡Tu opinión se ha publicado!", Alert.AlertType.INFORMATION);

            } catch (Exception ex) {
                Logger.getLogger(BookViewController.class.getName()).log(Level.SEVERE, null, ex);
                showAlert("Error al guardar el comentario.", Alert.AlertType.ERROR);
            }
        });
    }

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