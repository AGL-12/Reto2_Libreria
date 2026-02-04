/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

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

// Imports necesarios para jasper
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
// --------------------------------------

//Imports para el informe
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.event.EventHandler;

//Imports para click derecho
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * Controlador principal para la vista detallada de un libro (BookView.fxml).
 * Esta clase gestiona toda la interacción en la pantalla de "Ficha de Libro":
 * muestra la información (título, precio, stock), permite añadir el libro al
 * carrito, listar comentarios existentes y añadir nuevos. También incluye
 * funcionalidades avanzadas como la generación de informes técnicos con
 * JasperReports y menús contextuales (clic derecho).
 *
 * * @author mikel
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
    private MenuItem menuItemReport;
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

    /**
     * Método de inicialización del controlador. Se llama automáticamente al
     * cargar la vista. Configura los menús contextuales.
     */
    public void initialize() {
        initGlobalContextMenu();
    }

    /**
     * Configura reglas específicas de la interfaz según el tipo de usuario. Por
     * ejemplo, oculta el botón de "Añadir Comentario" si el usuario es Admin.
     */
    private void initContextMenu() {
        if (currentUser instanceof Admin) {
            btnAddComment.setVisible(false);
        }
        initGlobalContextMenu();
    }

    /**
     * Carga y muestra la lista de comentarios asociados al libro actual.
     * Obtiene los datos de la base de datos, ordena la lista para que los
     * comentarios del propio usuario aparezcan primero, y genera dinámicamente
     * las tarjetas de comentario.
     */
    private void refreshList() {
        commentsContainer.getChildren().clear();
        try {
            // currentBook es el libro que estás visualizando
            List<Commentate> comentarios = dao.getCommentsByBook(currentBook.getISBN());
            // Obtiene el usuario actual
            Profile currentUser = UserSession.getInstance().getUser();

            // Ordena la lista de comentarios
            if (currentUser != null) {
                comentarios.sort((c1, c2) -> {
                    int myId = currentUser.getUserCode();
                    boolean c1IsMine = c1.getUser().getUserCode() == myId;
                    boolean c2IsMine = c2.getUser().getUserCode() == myId;

                    // Si c1 es mío, va antes
                    if (c1IsMine && !c2IsMine) {
                        return -1;
                    }
                    // Si c2 es mío, c2 va antes
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

    /**
     * Muestra una ventana de alerta emergente.
     *
     * * @param message El mensaje a mostrar.
     * @param type El tipo de alerta (ERROR, WARNING, INFORMATION).
     */
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Gestión de librería");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Carga los datos de un libro específico en la vista.
     * Rellena todos los campos visuales (título, precio, sinopsis), procesa la
     * imagen de portada y determina si el botón "Añadir al Carrito" debe
     * mostrarse (oculto para Admins).
     *
     * * @param book El objeto libro con la información a mostrar.
     */
void setData(Book book) {
        this.currentBook = book;

        // 1. Cargar la imagen (con protección por si falla el archivo)
        try {
            if (book.getCover() != null && !book.getCover().isEmpty()) {
                Image originalImage = new Image(getClass().getResourceAsStream("/images/" + book.getCover()));
                cutOutImage(coverBook, originalImage, 140, 210);
            }
        } catch (Exception e) {
            System.out.println("Aviso: No se pudo cargar la imagen del libro " + book.getTitle());
        }

        // Rellenar textos
        titleBook.setText(book.getTitle());
        authorName.setText(book.getAuthor().toString());
        priceBook.setText("precio: " + book.getPrice());
        sypnosis.setText(book.getSypnosis());
        stockBook.setText("Stock: " + book.getStock());

        // Cargar comentarios
        refreshList();

        // Logica boton comprar
        Profile user = UserSession.getInstance().getUser();

        if (user instanceof Admin) {
            // Es Administrador -> NUNCA puede comprar
            btnAddToCart.setVisible(false);
            btnAddToCart.setManaged(false);
            
        } else {
            // Es Usuario Normal (o invitado) -> Depende del Stock
            if (book.getStock() > 0) {
                // Hay stock -> Botón VISIBLE
                btnAddToCart.setVisible(true);
                btnAddToCart.setManaged(true);
            } else {
                // No hay stock (0) -> Botón OCULTO (Desaparece)
                btnAddToCart.setVisible(false);
                btnAddToCart.setManaged(false);
            }
        }
    }
    /**
     * Maneja la acción de pulsar el botón "+ Escribir opinión".
     * <p>
     * Verifica que el usuario esté logueado y no haya comentado antes. Si todo
     * es correcto, muestra el formulario de escritura.
     * </p>
     *
     * * @param event Evento del botón.
     */
    @FXML
    private void handleNewComment(ActionEvent event) {
        // Validaciones
        if (currentUser == null) {
            showAlert("Debes iniciar sesión para comentar", Alert.AlertType.ERROR);
            return;
        }
        // Comprobar si el usuario ya ha comentado
        try {
            List<Commentate> comentariosExistentes = dao.getCommentsByBook(currentBook.getISBN());
            for (Commentate c : comentariosExistentes) {
                if (c.getUser().getUserCode() == currentUser.getUserCode()) {
                    showAlert("¡Ya has opinado sobre este libro!", Alert.AlertType.WARNING);
                    return;
                }
            }
        } catch (Exception e) {
            // Falla la conexion
        }

        // Muestra la caja mostrando el nombre
        cajaEscribir.setVisible(true);
        cajaEscribir.setManaged(true);

        // Ocultar botón principal
        btnAddComment.setVisible(false);
        btnAddComment.setManaged(false);

        //  Muestra la caja entera
        cajaEscribir.setVisible(true);
        cajaEscribir.setManaged(true);
        btnAddComment.setVisible(false);
        btnAddComment.setManaged(false);

        if (estrellasController != null) {
            estrellasController.setEditable(true);
            estrellasController.setValueStars(0);
        }

        txtNuevoComentario.requestFocus();
    }

    /**
     * Cancela la creación de un comentario, limpiando el formulario y
     * ocultándolo.
     *
     * * @param event Evento del botón Cancelar.
     */
    @FXML
    private void handleCancelar(ActionEvent event) {
        txtNuevoComentario.clear();
        cajaEscribir.setVisible(false);
        cajaEscribir.setManaged(false);

        btnAddComment.setVisible(true);
        btnAddComment.setManaged(true);
    }

    /**
     * Guarda el nuevo comentario en la base de datos.
     * Recoge el texto y la valoración (estrellas), valida que no esté vacío,
     * guarda el objeto {@link Commentate} mediante el DAO y actualiza la lista
     * visual.
     *
     * * @param event Evento del botón Publicar.
     */
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
                puntuacion = (float) estrellasController.getValueUser();
            }

            // Creamos el comentario con la puntuación real
            Commentate newComment = new Commentate((User) currentUser, currentBook, texto, puntuacion);
            dao.addComment(newComment);

            // Crear tarjeta visual
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CommentView.fxml"));
            Parent tarjeta = loader.load();
            CommentViewController controller = loader.getController();
            controller.setData(newComment);

            // Añadir arriba del todo
            commentsContainer.getChildren().add(0, tarjeta);

            // Cerrar y limpiar
            handleCancelar(null);
            btnAddComment.setDisable(true); 

            showAlert("¡Comentario publicado!", Alert.AlertType.INFORMATION);

        } catch (Exception ex) {
            Logger.getLogger(BookViewController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert("Error al guardar: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Abre el Manual de Usuario en formato PDF. Extrae el recurso del JAR a un
     * archivo temporal para que el sistema operativo pueda abrirlo.
     *
     * * @param event Evento del menú de ayuda.
     */
    @FXML
    private void handleReportAction(ActionEvent event) {

        try {
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

    /**
     * Método auxiliar para recortar y escalar la imagen de portada. Aplica un
     * recorte "Center Crop" para que la imagen llene el espacio sin deformarse.
     *
     * * @param imageView El componente visual donde irá la imagen.
     * @param image La imagen original.
     * @param targetWidth Ancho deseado.
     * @param targetHeight Alto deseado.
     */
    private void cutOutImage(ImageView imageView, Image image, double targetWidth, double targetHeight) {
        // Establecemos el tamaño final que tendrá el ImageView
        imageView.setFitWidth(targetWidth);
        imageView.setFitHeight(targetHeight);

        double originalWidth = image.getWidth();
        double originalHeight = image.getHeight();

        double scaleX = targetWidth / originalWidth;
        double scaleY = targetHeight / originalHeight;

        // Elegimos el factor de escala mayor asi se rellenara todo el huecos
        double scale = Math.max(scaleX, scaleY);

        // Calculamos el tamaño que tendría la imagen
        double scaledWidth = originalWidth * scale;
        double scaledHeight = originalHeight * scale;

        // Calculamos el la ventana de recorte sobre la imagen original
        double viewportWidth = targetWidth / scale;
        double viewportHeight = targetHeight / scale;

        // Centramos el recorte
        double viewportX = (originalWidth - viewportWidth) / 2;
        double viewportY = (originalHeight - viewportHeight) / 2;

        // Aplicamos la imagen y el recorte
        imageView.setImage(image);
        imageView.setViewport(new Rectangle2D(viewportX, viewportY, viewportWidth, viewportHeight));
        imageView.setSmooth(true); 
        imageView.setPreserveRatio(false); // Se desactiva para que haga caso a viewport
    }

    /**
     * Añade el libro actual al carrito de compras de la sesión. Valida que haya
     * un libro seleccionado y que el usuario esté logueado.
     *
     * * @param event Evento del botón de añadir.
     */
    @FXML
    private void handleAddToCart(ActionEvent event) {
        //Verifica que hay un libro seleccionado
        if (currentBook == null) {
            showAlert("Error: No se ha cargado ningún libro.", Alert.AlertType.ERROR);
            return;
        }

        // Validar que el usuario puede comprar
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert("Debes iniciar sesión para comprar.", Alert.AlertType.WARNING);
            return;
        }

        try {
            UserSession.getInstance().addToCart(currentBook);
            showAlert("¡Libro añadido al carrito!", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error al añadir al carrito: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        javafx.application.Platform.exit();
        System.exit(0);
    }

    /**
     * Genera un informe técnico de stock utilizando JasperReports. Conecta
     * directamente a la base de datos y lanza el visor de informes.
     *
     * * @param event Evento del menú.
     */
    @FXML
    private void handleInformeTecnico(ActionEvent event) {
        Connection con = null;
        try {
            // Conecta a la base de datos
            String url = "jdbc:mysql://localhost:3306/bookstore?useSSL=false&serverTimezone=UTC";
            String user = "root";
            String pass = "abcd*1234"; 

            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, user, pass);

            //Cargamos el archivo.JRXML
            InputStream reportStream = getClass().getResourceAsStream("/reports/InformeTecnico.jrxml");

            if (reportStream == null) {
                showAlert("Error: No se encuentra /reports/InformeTecnicoDB.jrxml", Alert.AlertType.ERROR);
                return;
            }

            // Compilamos
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // Llenamos el informe pasando la conexión 'con' para que ejecute la Query que hemos puesto
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, con);

            //Lo mostramos
            JasperViewer.viewReport(jasperPrint, false); 
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error al generar informe: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
            }
        }
    }

    @FXML
    private void handleHelpAction(ActionEvent event) {
        try {
            //Ruta para el pdf del manual
            String resourcePath = "/documents/Manual_Usuario.pdf";

            // Cargamos el archivo
            InputStream pdfStream = getClass().getResourceAsStream(resourcePath);

            if (pdfStream == null) {
                showAlert("Error: No se encuentra el manual en: " + resourcePath, Alert.AlertType.ERROR);
                return;
            }

            // Creamos archivo temporal y abrimos
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
     * Configura el menú global de clic derecho (Context Menu) para toda la
     * ventana. Ofrece accesos directos a Comprar, Informes, Cerrar Sesión y
     * Ayuda.
     */
    private void initGlobalContextMenu() {
        // Inicializamos el menú
        globalMenu = new ContextMenu();
        globalMenu.setAutoHide(true);

        //Opción de añadir al carrito
        MenuItem itemAddCart = new MenuItem("Añadir al Carrito");
        itemAddCart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleAddToCart(null);
            }
        });

        // Si es Admin, deshabilitamos esta opción ya que no puede comprar
        if (UserSession.getInstance().getUser() instanceof Admin) {
            itemAddCart.setDisable(true);
        }

        // Opción para el informe tecnico JASPER
        MenuItem itemInforme = new MenuItem("Generar Informe Técnico");
        itemInforme.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleInformeTecnico(event);
            }
        });

        // El resto de opciones "sencillas"
        MenuItem itemExit = new MenuItem("Salir");
        itemExit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleExit(event);
            }
        });

        MenuItem itemManual = new MenuItem("Manual de Usuario");
        itemManual.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleHelpAction(event);
            }
        });

        MenuItem itemAbout = new MenuItem("Acerca de...");
        itemAbout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleAboutAction(event);
            }
        });

        // Añadimos el menú
        globalMenu.getItems().addAll(
                itemAddCart, 
                itemInforme, 
                new SeparatorMenuItem(), 
                itemExit, 
                new SeparatorMenuItem(),
                itemManual, 
                itemAbout 
        );

        // Asignamos los eventos al panel
        if (rootPane != null) {
            rootPane.setOnContextMenuRequested(event -> {
                // Mostrar el menú donde se ha hecho clic
                globalMenu.show(rootPane, event.getScreenX(), event.getScreenY());
                event.consume();
            });

            // Con esto ocultamos el menú si se hace click izquierdo fuera
            rootPane.setOnMousePressed(event -> {
                if (event.isPrimaryButtonDown() && globalMenu.isShowing()) {
                    globalMenu.hide();
                }
            });
        }
    }
}
