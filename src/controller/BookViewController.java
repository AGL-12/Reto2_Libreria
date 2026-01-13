/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import static com.mchange.v2.c3p0.impl.C3P0Defaults.user;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Book;
import model.ClassDAO;
import model.Commentate;
import model.DBImplementation;
import model.Profile;
import model.User;
import model.UserSession;
import model.Admin;

/**
 * FXML Controller class
 *
 * @author mikel
 */
public class BookViewController {

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
    @FXML
    public HeaderController headerController;

    private Book currentBook;

    private final ClassDAO dao = new DBImplementation();
    @FXML
    private HBox buttonBox;
    @FXML
    private Label lblUsuario;
    @FXML
    private Label lblFecha;
    @FXML
    private VBox cajaEscribir;
    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnPublicar;
    @FXML
    private TextArea txtNuevoComentario;
    @FXML
    private StarRateController estrellasController;

    private Profile currentUser = UserSession.getInstance().getUser();

    public void initialize() {
        initContextMenu();
    }

    /**
     * Configura el menú de clic derecho (Context Menu). Requerido para el 100%
     * en controles avanzados.
     */
    private void initContextMenu() {
        if (currentUser instanceof Admin) {
            btnAddComment.setVisible(false);
        }
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
            // 2. OBTENER USUARIO ACTUAL
            Profile currentUser = UserSession.getInstance().getUser();

            // 3. ORDENAR LA LISTA (LÓGICA NUEVA)
            if (currentUser != null) {
                comentarios.sort((c1, c2) -> {
                    int myId = currentUser.getUserCode();
                    boolean c1IsMine = c1.getUser().getUserCode() == myId;
                    boolean c2IsMine = c2.getUser().getUserCode() == myId;

                    // Si c1 es mío, va antes (-1)
                    if (c1IsMine && !c2IsMine) {
                        return -1;
                    }
                    // Si c2 es mío, c2 va antes (1)
                    if (!c1IsMine && c2IsMine) {
                        return 1;
                    }
                    // Si no, se quedan igual
                    return 0;
                });
            }
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
        // Validaciones
        if (currentUser == null) {
            showAlert("Debes iniciar sesión para comentar", Alert.AlertType.ERROR);
            return;
        }
        // Comprobar si ya comentó
        try {
            List<Commentate> comentariosExistentes = dao.getCommentsByBook(currentBook.getISBN());
            for (Commentate c : comentariosExistentes) {
                if (c.getUser().getUserCode() == currentUser.getUserCode()) {
                    showAlert("¡Ya has opinado sobre este libro!", Alert.AlertType.WARNING);
                    return;
                }
            }
        } catch (Exception e) {
            // Si falla la conexión, seguimos o mostramos error suave
        }

        // MOSTRAR LA CAJA (Usando el nombre nuevo)
        cajaEscribir.setVisible(true);
        cajaEscribir.setManaged(true);

        // Ocultar botón principal
        btnAddComment.setVisible(false);
        btnAddComment.setManaged(false);

        // MOSTRAR LA CAJA
        cajaEscribir.setVisible(true);
        cajaEscribir.setManaged(true);
        btnAddComment.setVisible(false);
        btnAddComment.setManaged(false);

        // --- AÑADE ESTO ---
        if (estrellasController != null) {
            estrellasController.setEditable(true); // ¡Habilitar clics!
            estrellasController.setValueStars(0);  // Resetear a 0 estrellas limpias
        }
        // ------------------

        txtNuevoComentario.requestFocus();
    }

    // --- ACCIÓN 2: CANCELAR ---
    @FXML
    private void handleCancelar(ActionEvent event) {
        txtNuevoComentario.clear();
        cajaEscribir.setVisible(false);
        cajaEscribir.setManaged(false);

        btnAddComment.setVisible(true);
        btnAddComment.setManaged(true);
    }

    // --- ACCIÓN 3: PUBLICAR ---
    @FXML
    private void handlePublicar(ActionEvent event) {
        String texto = txtNuevoComentario.getText().trim();

        if (texto.isEmpty()) {
            showAlert("El comentario no puede estar vacío", Alert.AlertType.WARNING);
            return;
        }

        try {
            Profile currentUser = UserSession.getInstance().getUser();
// Ahora cogemos el valor real:
            float puntuacion = 0;
            if (estrellasController != null) {
                puntuacion = (float) estrellasController.getValueUser(); // O .getRating(), según tu StarRateController
                // Si tu método se llama getRating() o getValueStars(), úsalo aquí.
            }

            // Creamos el comentario con la puntuación real
            Commentate newComment = new Commentate((User) currentUser, currentBook, texto, puntuacion);
            // -------------------
            dao.addComment(newComment);

            // 2. Crear tarjeta visual (AQUÍ SÍ usas CommentView.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CommentView.fxml"));
            Parent tarjeta = loader.load();
            CommentViewController controller = loader.getController();
            controller.setData(newComment);

            // Añadir arriba del todo
            commentsContainer.getChildren().add(0, tarjeta);

            // 3. Cerrar y limpiar
            handleCancelar(null);
            btnAddComment.setDisable(true); // Bloqueamos porque ya comentó

            showAlert("¡Comentario publicado!", Alert.AlertType.INFORMATION);

        } catch (Exception ex) {
            Logger.getLogger(BookViewController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert("Error al guardar: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    //Barra menu logica
    @FXML
    private void handleReportAction(ActionEvent event) {
        // Muestra un mensaje simulando la generación del reporte
        showAlert("Generando informe de comentarios...", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleHelpAction(ActionEvent event) {
        // Muestra la ventana de ayuda requerida
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ayuda del Sistema");
        alert.setHeaderText("Manual de Usuario");
        alert.setContentText("Guía rápida:\n"
                           + "1. Selecciona un libro para ver detalles.\n"
                           + "2. Escribe en el cuadro inferior y pulsa 'Publicar' para opinar.\n"
                           + "3. Si eres Admin, usa el botón 'Borrar' para moderar.\n"
                           + "4. Usa el menú Actions > Report para informes.");
        alert.showAndWait();
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
