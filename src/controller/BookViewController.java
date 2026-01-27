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


// --- IMPORTS NUEVOS PARA EL INFORME ---
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
// --------------------------------------

//Imports para informe
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


//Imports para click derecho

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;


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
    private Button btnAddComment;
    @FXML
    private VBox commentsContainer;
    @FXML
    private Button btnAddToCart;
    @FXML
    public HeaderController headerController;

    @FXML
    private javafx.scene.control.MenuItem menuItemReport;
    @FXML
    private VBox rootPane;

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
    
    private ContextMenu globalMenu;

    private Profile currentUser = UserSession.getInstance().getUser();

    public void initialize() {
        initContextMenu();
        initGlobalContextMenu();
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

        Profile user = UserSession.getInstance().getUser();

        if (user instanceof Admin) {
            // El Admin NO compra
            btnAddToCart.setVisible(false);
            btnAddToCart.setManaged(false);
        } else {
            // El Usuario SÍ compra
            btnAddToCart.setVisible(true);
            btnAddToCart.setManaged(true);
        }
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

    @FXML
    private void handleReportAction(ActionEvent event) {

        // --- HEMOS BORRADO EL BLOQUE IF DE SEGURIDAD ---
        // Ahora entra cualquier usuario (Admin o Normal)
        try {
            // CAMBIO: Ahora apuntamos al Manual de Usuario en vez de al Informe de Stock
            String resourcePath = "/documents/Manual_Usuario.pdf";

            InputStream pdfStream = getClass().getResourceAsStream(resourcePath);

            if (pdfStream == null) {
                showAlert("Error: No se encuentra el archivo en: " + resourcePath, Alert.AlertType.ERROR);
                return;
            }

            File tempFile = File.createTempFile("Manual_Usuario", ".pdf");
            tempFile.deleteOnExit();

            Files.copy(pdfStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            } else {
                showAlert("Error: No se puede abrir el visor de PDF.", Alert.AlertType.ERROR);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error al abrir el manual: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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

    @FXML
    private void handleAddToCart(ActionEvent event) {
        // 1. Validar que hay un libro seleccionado
        if (currentBook == null) {
            showAlert("Error: No se ha cargado ningún libro.", Alert.AlertType.ERROR);
            return;
        }

        // 2. Validar que el usuario puede comprar
        // (Aunque ya ocultamos el botón al Admin, doble seguridad no sobra)
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert("Debes iniciar sesión para comprar.", Alert.AlertType.WARNING);
            return;
        }

        try {
            UserSession.getInstance().addToCart(currentBook);
            // 4. Feedback visual
            showAlert("¡Libro añadido al carrito!", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error al añadir al carrito: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        // Cierra la aplicación completamente
        javafx.application.Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleLogOut(ActionEvent event) {
        // 1. Limpiar sesión
        UserSession.getInstance().cleanUserSession();

        // 2. Navegar al Login (necesitas tu lógica de navegación aquí)
        // Ejemplo rápido:
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LogInWindow.fxml"));
            Parent root = loader.load();
            btnAddToCart.getScene().setRoot(root); // Usamos cualquier nodo para pillar la escena
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    // --- MÉTODO NUEVO: GENERAR INFORME JASPER ---
    private void handleInformeTecnico(ActionEvent event) {
        Connection con = null;
        try {
            // 1. CONEXIÓN A BASE DE DATOS
            // Ajusta el usuario y contraseña a los tuyos de MySQL
            String url = "jdbc:mysql://localhost:3306/bookstore?useSSL=false&serverTimezone=UTC";
            String user = "root"; 
            String pass = "abcd*1234"; // <--- ¡PON TU CONTRASEÑA AQUÍ!

            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, user, pass);

            // 2. CARGAR EL ARCHIVO .JRXML
            // Busca en el paquete 'reports' que creamos anteriormente
            InputStream reportStream = getClass().getResourceAsStream("/reports/InformeTecnico.jrxml");
            
            if (reportStream == null) {
                showAlert("Error: No se encuentra /reports/InformeTecnicoDB.jrxml", Alert.AlertType.ERROR);
                return;
            }

            // 3. COMPILAR Y LLENAR EL INFORME
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            
            // Llenamos el informe pasando la conexión 'con' para que ejecute la Query SQL
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, con);

            // 4. MOSTRAR VISOR
            JasperViewer.viewReport(jasperPrint, false); // false = no cerrar la app al salir

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error al generar informe: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) {}
        }
    }

    @FXML
    private void handleHelpAction(ActionEvent event) {
        try {
            // 1. Ruta al PDF del Manual (Asegúrate de que el archivo se llame así en src/documents)
            String resourcePath = "/documents/Manual_Usuario.pdf";

            // 2. Cargar archivo
            InputStream pdfStream = getClass().getResourceAsStream(resourcePath);

            if (pdfStream == null) {
                showAlert("Error: No se encuentra el manual en: " + resourcePath, Alert.AlertType.ERROR);
                return;
            }

            // 3. Crear temporal y abrir
            File tempFile = File.createTempFile("Manual_Usuario", ".pdf");
            tempFile.deleteOnExit();
            Files.copy(pdfStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            } else {
                showAlert("No se puede abrir el PDF automáticamente.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error al abrir el manual: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAboutAction(ActionEvent event) {
        showAlert("BookStore App v1.0\nDesarrollado por Mikel\nProyecto Reto 2", Alert.AlertType.INFORMATION);
    }

/**
     * Configura el menú global de clic derecho para toda la ventana.
     */
    private void initGlobalContextMenu() {
        // 1. Inicializamos el menú
        globalMenu = new ContextMenu();
        globalMenu.setAutoHide(true);

        // --- Opción 1: Añadir al Carrito ---
        MenuItem itemAddCart = new MenuItem("Añadir al Carrito");
        itemAddCart.setOnAction(event -> handleAddToCart(null));

        // Si es Admin, deshabilitamos esta opción
        if (UserSession.getInstance().getUser() instanceof Admin) {
            itemAddCart.setDisable(true); 
        }

        // =========================================================
        // --- NUEVO: OPCIÓN INFORME TÉCNICO (JASPER) ---
        // =========================================================
        MenuItem itemInforme = new MenuItem("Generar Informe Técnico");
        itemInforme.setOnAction(event -> handleInformeTecnico(event));
        // =========================================================

        // --- Resto de Opciones ---
        MenuItem itemLogOut = new MenuItem("Cerrar Sesión");
        itemLogOut.setOnAction(event -> handleLogOut(event));

        MenuItem itemExit = new MenuItem("Salir");
        itemExit.setOnAction(event -> handleExit(event));

        SeparatorMenuItem separator = new SeparatorMenuItem();

        MenuItem itemManual = new MenuItem("Manual de Usuario");
        itemManual.setOnAction(event -> handleHelpAction(event));

        MenuItem itemAbout = new MenuItem("Acerca de...");
        itemAbout.setOnAction(event -> handleAboutAction(event));

        // 2. AÑADIR TODO AL MENÚ EN ORDEN
        globalMenu.getItems().addAll(
                itemAddCart,      // 1. Comprar
                itemInforme,      // 2. Informe Técnico (NUEVO)
                new SeparatorMenuItem(), // Línea separadora
                itemLogOut,       // 3. Cerrar Sesión
                itemExit,         // 4. Salir
                separator,        // Línea separadora
                itemManual,       // 5. Ayuda
                itemAbout         // 6. About
        );

        // 3. Asignar eventos al panel principal (rootPane)
        if (rootPane != null) {
            rootPane.setOnContextMenuRequested(event -> {
                // Mostrar el menú donde se hizo clic
                globalMenu.show(rootPane, event.getScreenX(), event.getScreenY());
                event.consume(); 
            });

            // Ocultar el menú si se hace clic izquierdo fuera
            rootPane.setOnMousePressed(event -> {
                if (event.isPrimaryButtonDown() && globalMenu.isShowing()) {
                    globalMenu.hide();
                }
            });
        }
    }
}
