package controller;

import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

import java.io.IOException;
import javafx.application.Platform;

//Imports para click derecho
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import util.LogInfo;
import util.UtilGeneric;

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
        LogInfo.getInstance().logInfo("Inicializando BookViewController...");
        initGlobalContextMenu();
    }

    /**
     * Configura reglas específicas de la interfaz según el tipo de usuario. Por
     * ejemplo, oculta el botón de "Añadir Comentario" si el usuario es Admin.
     */
    private void initContextMenu() {
        if (currentUser instanceof Admin) {
            btnAddComment.setVisible(false);
            LogInfo.getInstance().logInfo("Usuario es Admin: Botón de comentar oculto.");
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
        LogInfo.getInstance().logInfo("Refrescando lista de comentarios...");
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

            // 2. Ordenación
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
            LogInfo.getInstance().logSevere("Error al cargar la vista dinámica de comentarios (FXML)", ex);
        }
    }

    /**
     * Muestra una ventana de alerta emergente utilizando UtilGeneric.
     */
    private void showAlert(String message, Alert.AlertType type) {
        UtilGeneric.getInstance().showAlert(message, type, "Book&Bugs");
    }

    /**
     * Carga los datos de un libro específico en la vista. Rellena todos los
     * campos visuales (título, precio, sinopsis), procesa la imagen de portada
     * y determina si el botón "Añadir al Carrito" debe mostrarse.
     *
     * * @param book El objeto libro con la información a mostrar.
     */
    void setData(Book book) {
        this.currentBook = book;
        LogInfo.getInstance().logInfo("Cargando datos del libro: " + (book != null ? book.getTitle() : "NULL"));

        // 1. Cargar la imagen
        try {
            if (book.getCover() != null && !book.getCover().isEmpty()) {
                Image originalImage = new Image(getClass().getResourceAsStream("/images/" + book.getCover()));
                // Usamos el método local cutOutImage para asegurar el recorte correcto
                UtilGeneric.getInstance().cutOutImage(coverBook, originalImage, 140, 210);
            }
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al procesar imagen del libro: " + book.getCover(), e);
        }

        // Rellenar textos
        titleBook.setText(book.getTitle());
        authorName.setText(book.getAuthor().toString());
        priceBook.setText("precio: " + book.getPrice());
        sypnosis.setText(book.getSypnosis());
        stockBook.setText("Stock: " + book.getStock());

        // Cargar comentarios
        refreshList();

        // Lógica botón comprar
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
                LogInfo.getInstance().logInfo("Libro sin stock, ocultando botón de compra.");
                btnAddToCart.setVisible(false);
                btnAddToCart.setManaged(false);
            }
        }
    }

    /**
     * Maneja la acción de pulsar el botón "+ Escribir opinión".
     */
    @FXML
    private void handleNewComment(ActionEvent event) {
        LogInfo.getInstance().logInfo("Intento de añadir nuevo comentario.");
        // Validaciones
        if (currentUser == null) {
            LogInfo.getInstance().logWarning("Intento de comentar sin sesión iniciada.");
            showAlert("Debes iniciar sesión para comentar", Alert.AlertType.ERROR);
            return;
        }
        // Comprobar si el usuario ya ha comentado (redundancia de seguridad)
        try {
            List<Commentate> comentariosExistentes = dao.getCommentsByBook(currentBook.getISBN());
            for (Commentate c : comentariosExistentes) {
                if (c.getUser().getUserCode() == currentUser.getUserCode()) {
                    LogInfo.getInstance().logWarning("El usuario ya ha comentado este libro.");
                    showAlert("¡Ya has opinado sobre este libro!", Alert.AlertType.WARNING);
                    return;
                }
            }
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error comprobando comentarios existentes: " + e.getMessage(), e);
        }

        // Muestra la caja de escritura
        cajaEscribir.setVisible(true);
        cajaEscribir.setManaged(true);

        // Ocultar botón principal
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
     */
    @FXML
    private void handleCancelar(ActionEvent event) {
        LogInfo.getInstance().logInfo("Cancelando escritura de comentario.");
        txtNuevoComentario.clear();
        cajaEscribir.setVisible(false);
        cajaEscribir.setManaged(false);

        btnAddComment.setVisible(true);
        btnAddComment.setManaged(true);
    }

    /**
     * Guarda el nuevo comentario en la base de datos usando un Hilo secundario.
     */
    @FXML
    private void handlePublicar(ActionEvent event) {
        String texto = txtNuevoComentario.getText().trim();
        if (texto.isEmpty()) {
            showAlert("El comentario no puede estar vacío", Alert.AlertType.WARNING);
            return;
        }
        if (texto.length() > 500) {
            LogInfo.getInstance().logWarning("Intento de publicar comentario demasiado largo: " + texto.length() + " caracteres.");
            showAlert("El comentario es demasiado largo (máximo 500 caracteres).", Alert.AlertType.WARNING);
            return;
        }
        // Bloqueamos el botón para que el usuario no haga doble clic
        btnPublicar.setDisable(true);

        // Creamos el hilo
        new Thread(new Runnable() {
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
                                LogInfo.getInstance().logSevere("Error al cargar la tarjeta visual del comentario", ex);
                            }
                        }
                    });
                } catch (final Exception ex) {
                    // Si hay error en la BD, avisamos al usuario
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            LogInfo.getInstance().logSevere("Error crítico al publicar en la base de datos", ex);
                            btnPublicar.setDisable(false);
                            showAlert("Error: " + ex.getMessage(), Alert.AlertType.ERROR);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Maneja la acción del menú "Informe Técnico" usando UtilGeneric.
     */
    @FXML
    private void handleInformeTecnico(ActionEvent event) {
        UtilGeneric.getInstance().getJasperReport();
    }

    /**
     * Maneja la acción del menú "Manual de Usuario" usando UtilGeneric.
     */
    @FXML
    private void handleHelpAction(ActionEvent event) {
        UtilGeneric.getInstance().helpAction();
    }

    /**
     * Maneja la acción del menú "Acerca de..." usando UtilGeneric.
     */
    @FXML
    private void handleAboutAction(ActionEvent event) {
        UtilGeneric.getInstance().aboutAction();
    }

    /**
     * Maneja la acción del menú "Salir" usando UtilGeneric.
     */
    @FXML
    private void handleExit(ActionEvent event) {
        UtilGeneric.getInstance().exit();
    }

    // Método repetido en tu XML original, lo dejamos mapeado al mismo de ayuda
    @FXML
    private void handleReportAction(ActionEvent event) {
        UtilGeneric.getInstance().helpAction();
    }

    /**
     * Añade el libro actual al carrito de compras de la sesión.
     */
    @FXML
    private void handleAddToCart(ActionEvent event) {
        if (currentBook == null) {
            LogInfo.getInstance().logWarning("Error: currentBook es NULL.");
            showAlert("Error: No se ha cargado ningún libro.", Alert.AlertType.ERROR);
            return;
        }

        if (!UserSession.getInstance().isLoggedIn()) {
            LogInfo.getInstance().logWarning("Intento de compra sin login.");
            showAlert("Debes iniciar sesión para comprar.", Alert.AlertType.WARNING);
            return;
        }

        try {
            UserSession.getInstance().addToCart(currentBook);
            LogInfo.getInstance().logInfo("Libro añadido al carrito exitosamente.");
            showAlert("¡Libro añadido al carrito!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error crítico al añadir al carrito", e);
            showAlert("Error al añadir al carrito: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Configura el menú global de clic derecho (Context Menu).
     */
    private void initGlobalContextMenu() {
        globalMenu = new ContextMenu();
        globalMenu.setAutoHide(true);

        MenuItem itemAddCart = new MenuItem("Añadir al Carrito");
        itemAddCart.setOnAction(event -> handleAddToCart(null));

        if (UserSession.getInstance().getUser() instanceof Admin) {
            itemAddCart.setDisable(true);
        }

        MenuItem itemInforme = new MenuItem("Generar Informe Técnico");
        itemInforme.setOnAction(event -> handleInformeTecnico(event));

        MenuItem itemExit = new MenuItem("Salir");
        itemExit.setOnAction(event -> handleExit(event));

        MenuItem itemManual = new MenuItem("Manual de Usuario");
        itemManual.setOnAction(event -> handleHelpAction(event));

        MenuItem itemAbout = new MenuItem("Acerca de...");
        itemAbout.setOnAction(event -> handleAboutAction(event));

        globalMenu.getItems().addAll(
                itemAddCart,
                itemInforme,
                new SeparatorMenuItem(),
                itemExit,
                new SeparatorMenuItem(),
                itemManual,
                itemAbout
        );

        if (rootPane != null) {
            rootPane.setOnContextMenuRequested(event -> {
                globalMenu.show(rootPane, event.getScreenX(), event.getScreenY());
                event.consume();
            });

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
        LogInfo.getInstance().logInfo("Notificación recibida: Comentario borrado. Reactivando botón de opinar.");

        if (btnAddComment != null) {
            btnAddComment.setVisible(true);
            btnAddComment.setManaged(true);
            btnAddComment.setDisable(false);
            btnAddComment.requestFocus();
        }
    }
}
