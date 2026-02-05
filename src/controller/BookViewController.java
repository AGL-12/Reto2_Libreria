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

//Imports para el informe
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.application.Platform;
import javafx.event.EventHandler;

//Imports para click derecho
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;
import static org.hsqldb.HsqlDateTime.e;

import util.LogInfo;

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

    private final LogInfo LOGGER = LogInfo.getInstance();
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
        LOGGER.logInfo("Inicializando BookViewController...");
        initContextMenu();
    }

    /**
     * Configura reglas específicas de la interfaz según el tipo de usuario. Por
     * ejemplo, oculta el botón de "Añadir Comentario" si el usuario es Admin.
     */
    private void initContextMenu() {
        if (currentUser instanceof Admin) {
            btnAddComment.setVisible(false);
            LOGGER.logInfo("Usuario es Admin: Botón de comentar oculto.");
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
        LOGGER.logInfo("Refrescando lista de comentarios...");
        commentsContainer.getChildren().clear();
        try {
            List<Commentate> comentarios = dao.getCommentsByBook(currentBook.getISBN());

            // 1. COMPROBACIÓN: ¿El usuario ya ha comentado?
            Profile userActual = UserSession.getInstance().getUser();
            boolean yaComento = false;

            if (userActual != null) {
                for (Commentate c : comentarios) {
                    if (c.getUser().getUserCode() == userActual.getUserCode()) {
                        yaComento = true;
                        break;
                    }
                }
            }
            // Bloqueamos el botón si ya existe un comentario del usuario
            btnAddComment.setDisable(yaComento);

            // 2. Ordenación (tu lógica actual)
            if (userActual != null) {
                comentarios.sort((c1, c2) -> {
                    int myId = userActual.getUserCode();
                    boolean c1IsMine = c1.getUser().getUserCode() == myId;
                    boolean c2IsMine = c2.getUser().getUserCode() == myId;
                    if (c1IsMine && !c2IsMine) {
                        return -1;
                    }
                    if (!c1IsMine && c2IsMine) {
                        return 1;
                    }
                    return 0;
                });
            }

            // 3. Carga visual
            for (Commentate coment : comentarios) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/CommentView.fxml"));
                Parent commentBox = fxmlLoader.load();
                CommentViewController con = fxmlLoader.getController();
                con.setData(coment);
                con.setParent(this);
                commentsContainer.getChildren().add(commentBox);
            }
        } catch (IOException ex) {
            LOGGER.logSevere("Error al cargar la vista dinámica de comentarios (FXML)", ex);
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
        alert.setTitle("Book&Bugs - Gestión de Librería");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // --- AÑADIR LOGO A LA ALERTA ---
        try {
            String imagePath = "/images/Book&Bugs_Logo.png";
            InputStream imageStream = getClass().getResourceAsStream(imagePath);
            if (imageStream != null) {
                Image logo = new Image(imageStream);
                ImageView imageView = new ImageView(logo);
                imageView.setFitHeight(50); // Tamaño adecuado para la alerta
                imageView.setPreserveRatio(true);
                alert.setGraphic(imageView);

                // También ponemos el icono en la barra de la ventana
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(logo);
            }
        } catch (Exception e) {
            LOGGER.logWarning("No se pudo cargar el logo en la alerta: " + e.getMessage());
        }

        alert.showAndWait();
    }

    /**
     * Carga los datos de un libro específico en la vista. Rellena todos los
     * campos visuales (título, precio, sinopsis), procesa la imagen de portada
     * y determina si el botón "Añadir al Carrito" debe mostrarse (oculto para
     * Admins).
     *
     * * @param book El objeto libro con la información a mostrar.
     */
    void setData(Book book) {
        this.currentBook = book;
        LOGGER.logInfo("Cargando datos del libro: " + (book != null ? book.getTitle() : "NULL"));
        // 1. Cargar la imagen (con protección por si falla el archivo)
        try {
            if (book.getCover() != null && !book.getCover().isEmpty()) {
                Image originalImage = new Image(getClass().getResourceAsStream("/images/" + book.getCover()));
                cutOutImage(coverBook, originalImage, 140, 210);
            }
        } catch (Exception e) {
            LOGGER.logSevere("Error al procesar imagen del libro: " + book.getCover(), e);
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
            btnAddComment.setVisible(false);
            btnAddComment.setManaged(false);

        } else {
            // Es Usuario Normal (o invitado) -> Depende del Stock
            btnAddComment.setVisible(true);
            btnAddComment.setManaged(true);
            if (book.getStock() > 0) {
                // Hay stock -> Botón VISIBLE
                btnAddToCart.setVisible(true);
                btnAddToCart.setManaged(true);
            } else {
                // No hay stock (0) -> Botón OCULTO (Desaparece)
                LOGGER.logInfo("Libro sin stock, ocultando botón de compra.");
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
        LOGGER.logInfo("Intento de añadir nuevo comentario.");
        // Validaciones
        if (currentUser == null) {
            LOGGER.logWarning("Intento de comentar sin sesión iniciada.");
            showAlert("Debes iniciar sesión para comentar", Alert.AlertType.ERROR);
            return;
        }
        // Comprobar si el usuario ya ha comentado
        try {
            List<Commentate> comentariosExistentes = dao.getCommentsByBook(currentBook.getISBN());
            for (Commentate c : comentariosExistentes) {
                if (c.getUser().getUserCode() == currentUser.getUserCode()) {
                    LOGGER.logWarning("El usuario ya ha comentado este libro.");
                    showAlert("¡Ya has opinado sobre este libro!", Alert.AlertType.WARNING);
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.logSevere("Error comprobando comentarios existentes: " + e.getMessage(), e);
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
        LOGGER.logInfo("Cancelando escritura de comentario.");
        txtNuevoComentario.clear();
        cajaEscribir.setVisible(false);
        cajaEscribir.setManaged(false);

        btnAddComment.setVisible(true);
        btnAddComment.setManaged(true);
    }

    /**
     * Guarda el nuevo comentario en la base de datos. Recoge el texto y la
     * valoración (estrellas), valida que no esté vacío, guarda el objeto
     * {@link Commentate} mediante el DAO y actualiza la lista visual.
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
        if (texto.length() > 500) {
            LOGGER.logWarning("Intento de publicar comentario demasiado largo: " + texto.length() + " caracteres.");
            showAlert("El comentario es demasiado largo (máximo 500 caracteres).", Alert.AlertType.WARNING);
            return;
        }
        // Bloqueamos el botón para que el usuario no haga doble clic
        btnPublicar.setDisable(true);

        // Creamos el hilo de forma clásica (Clase anónima)
        Thread hiloPublicar = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 1. Preparamos el objeto
                    Profile user = UserSession.getInstance().getUser();
                    float puntuacion = (estrellasController != null) ? (float) estrellasController.getValueUser() : 0;
                    final Commentate newComment = new Commentate((User) user, currentBook, texto, puntuacion);

                    // 2. Guardamos en la base de datos (Operación pesada)
                    dao.addComment(newComment);

                    // 3. Volvemos al hilo de la interfaz para actualizar la pantalla
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CommentView.fxml"));
                                Parent tarjeta = loader.load();
                                CommentViewController conHijo = loader.getController();

                                conHijo.setData(newComment);
                                conHijo.setParent(BookViewController.this); // IMPORTANTE: Pasamos el padre

                                commentsContainer.getChildren().add(0, tarjeta);
                                handleCancelar(null); // Limpia los campos

                                btnAddComment.setDisable(true); // Ya ha comentado, deshabilitamos
                                btnPublicar.setDisable(false);
                                showAlert("¡Comentario publicado!", Alert.AlertType.INFORMATION);
                            } catch (IOException ex) {
                                LOGGER.logSevere("Error al cargar la tarjeta visual del comentario", ex);
                            }
                        }
                    });
                } catch (final Exception ex) {
                    // Si hay error en la BD, avisamos al usuario
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            LOGGER.logSevere("Error crítico al publicar en la base de datos", ex);
                            btnPublicar.setDisable(false);
                            showAlert("Error: " + ex.getMessage(), Alert.AlertType.ERROR);
                        }
                    });
                }
            }
        });
        hiloPublicar.start();
    }

    /**
     * Abre el Manual de Usuario en formato PDF. Extrae el recurso del JAR a un
     * archivo temporal para que el sistema operativo pueda abrirlo.
     *
     * * @param event Evento del menú de ayuda.
     */
    @FXML
    private void handleReportAction(ActionEvent event) {
        LOGGER.logInfo("Abriendo Manual de Usuario (PDF)...");

        try {
            String resourcePath = "/documents/Manual_Usuario.pdf";

            InputStream pdfStream = getClass().getResourceAsStream(resourcePath);

            if (pdfStream == null) {
                LOGGER.logSevere("No se encontró el archivo del manual en la ruta: " + resourcePath, null);
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
            LOGGER.logSevere("Excepción de E/S al intentar abrir el manual PDF", e);
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
            LOGGER.logSevere("Error: currentBook es NULL al intentar añadir al carrito.", null);
            showAlert("Error: No se ha cargado ningún libro.", Alert.AlertType.ERROR);
            return;
        }

        // Validar que el usuario puede comprar
        if (!UserSession.getInstance().isLoggedIn()) {
            LOGGER.logWarning("Intento de compra sin login.");
            showAlert("Debes iniciar sesión para comprar.", Alert.AlertType.WARNING);
            return;
        }

        try {
            UserSession.getInstance().addToCart(currentBook);
            LOGGER.logInfo("Libro añadido al carrito exitosamente.");
            showAlert("¡Libro añadido al carrito!", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            LOGGER.logSevere("Error crítico al añadir al carrito", e);
            showAlert("Error al añadir al carrito: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        LOGGER.logInfo("Cerrando aplicación desde el menú.");
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
        LOGGER.logInfo("Generando informe técnico JasperReports...");
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
                LOGGER.logSevere("No se encuentra el archivo .jrxml en la ruta especificada", null);
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
            LOGGER.logWarning("No se pudo cerrar la conexión de Jasper: " + e.getMessage());
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
            LOGGER.logSevere("Excepción al intentar abrir el Manual de Usuario", e);
            showAlert("Error al abrir el manual: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAboutAction(ActionEvent event) {
        LOGGER.logInfo("Mostrando ventana 'Acerca de...'."); // Log de inicio

        String mensaje = "Book&Bugs - Gestión de Librería v1.0\n\n"
                + "Desarrollado por el equipo de desarrollo:\n"
                + "• Alex\n"
                + "• Unai\n"
                + "• Ander\n"
                + "• Mikel\n\n"
                + "Proyecto Reto 2 - 2025";

        // Creamos la alerta
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de...");
        alert.setHeaderText("Información del Proyecto");
        alert.setContentText(mensaje);

        // --- AÑADIR LOGO ---
        try {
            String imagePath = "/images/Book&Bugs_Logo.png";
            // Usar getResourceAsStream es más seguro para comprobar nulos antes de crear la Image
            java.io.InputStream imageStream = getClass().getResourceAsStream(imagePath);

            if (imageStream != null) {
                Image logo = new Image(imageStream);
                ImageView imageView = new ImageView(logo);

                // Ajustar tamaño para que no salga gigante
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);

                // Poner la imagen a la izquierda del texto
                alert.setGraphic(imageView);

                // Opcional: Poner el logo también en el icono de la ventana de la alerta
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(logo);
            } else {
                LOGGER.logWarning("No se encontró la imagen del logo en la ruta: " + imagePath);
            }

        } catch (Exception e) {
            LOGGER.logSevere("Error no crítico al cargar el logo en About: " + e.getMessage(), e);
        }

        alert.showAndWait();
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

    /**
     * Método público que llamará el hijo (comentario) cuando se borre. Vuelve a
     * mostrar y habilitar el botón de escribir opinión.
     */
    public void onCommentDeleted() {
        LOGGER.logInfo("Notificación recibida: Comentario borrado. Reactivando botón de opinar.");

        if (btnAddComment != null) {
            // 1. Lo hacemos visible de nuevo
            btnAddComment.setVisible(true);
            btnAddComment.setManaged(true);

            // 2. IMPORTANTE: Lo rehabilitamos (estaba en setDisable(true) tras publicar)
            btnAddComment.setDisable(false);

            // 3. Opcional: Solicitar foco para que el usuario vea que ha vuelto
            btnAddComment.requestFocus();
        }
    }

}
