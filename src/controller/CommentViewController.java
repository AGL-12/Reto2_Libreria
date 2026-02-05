package controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import model.Admin;
import model.ClassDAO;
import model.Commentate;
import model.DBImplementation;
import model.Profile;
import model.UserSession;
import util.LogInfo;
import util.UtilGeneric;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Controlador para la vista individual de un comentario (CommentView.fxml).
 * <p>
 * Esta clase gestiona la visualización de cada comentario en la lista,
 * permitiendo ver la información del autor, la fecha y el contenido. También
 * controla la lógica para editar o eliminar el comentario si el usuario actual
 * es el propietario o un administrador.
 * </p>
 *
 * * @author mikel
 */
public class CommentViewController implements Initializable {

    private BookViewController parentController;
    @FXML
    private Label lblUsuario;
    @FXML
    private Label lblFecha;
    @FXML
    private TextArea txtComment;
    @FXML
    private HBox buttonBox;
    @FXML
    public StarRateController starRateController;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;

    private Commentate currentComment;
    private final ClassDAO dao = new DBImplementation();
    private boolean isEditing = false;

    /**
     * Establece el controlador padre (BookViewController) para poder
     * comunicarle cambios, como la eliminación de un comentario.
     *
     * @param parent El controlador de la vista de libro.
     */
    public void setParent(BookViewController parent) {
        this.parentController = parent;
    }

    /**
     * Inicializa la clase controladora. Se ejecuta automáticamente al cargar el
     * FXML. Configura el estado inicial de los componentes (deshabilita la
     * edición y oculta botones de gestión).
     *
     * * @param url La ubicación relativa del archivo FXML.
     * @param rb Los recursos específicos (idioma, etc.), puede ser null.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        ContextMenu cm = new ContextMenu();
        MenuItem miBorrar = new MenuItem("Borrar Comentario");
        miBorrar.setOnAction(this::handleDelete);
        cm.getItems().add(miBorrar);

        if (txtComment != null) {
            txtComment.setEditable(false);
        }

        if (buttonBox != null) {
            buttonBox.setVisible(false);
            buttonBox.setManaged(false);
        }

        if (starRateController != null) {
            starRateController.setEditable(false);
        }

    }

    /**
     * Método principal para cargar los datos del comentario en la vista. Recibe
     * un objeto {@link Commentate}, rellena los campos de texto y estrellas, y
     * decide qué botones mostrar según si el usuario logueado es el autor del
     * comentario o un administrador.
     *
     * @param comment El objeto comentario con toda la información a mostrar.
     */
    public void setData(Commentate comment) {
        this.currentComment = comment;

        // Rellenamos información visual
        if (lblUsuario != null && comment.getUser() != null) {
            lblUsuario.setText(comment.getUser().getName());
        }

        if (lblFecha != null && comment.getDateCreation() != null) {
            lblFecha.setText(comment.getDateCreation().toString());
        }

        if (txtComment != null) {
            txtComment.setText(comment.getCommentary());
            txtComment.getStyleClass().remove("comment-edit-mode");
        } else {
            LogInfo.getInstance().logWarning("ERROR CRÍTICO: txtComment es NULL en el controlador.");
        }

        if (starRateController != null) {
            starRateController.setValueStars(comment.getValuation());
        }

        // Declaramos el current user
        Profile currentUser = UserSession.getInstance().getUser();

        // Variables booleanas para saber si es owner o admin
        boolean isOwner = false;
        boolean isAdmin = false;

        if (currentUser != null) {
            if (comment.getUser() != null && currentUser.getUserCode() == comment.getUser().getUserCode()) {
                isOwner = true;
            }
            if (currentUser instanceof Admin) {
                isAdmin = true;
            }
        }

        // Reglas de visibilidad
        if (buttonBox != null) {
            if (isOwner) {
                // Si es el dueño ve todo
                buttonBox.setVisible(true);
                buttonBox.setManaged(true);

                btnEdit.setVisible(true);
                btnEdit.setManaged(true);

                btnDelete.setVisible(true);
                btnDelete.setManaged(true);

            } else if (isAdmin) {
                // Si es admin solo ve borrar
                buttonBox.setVisible(true);
                buttonBox.setManaged(true);

                // Ocultamos editar
                btnEdit.setVisible(false);
                btnEdit.setManaged(false);

                // Mostramos borrar
                btnDelete.setVisible(true);
                btnDelete.setManaged(true);

            } else {
                // si no es ni dueño ni admin no ve nada
                buttonBox.setVisible(false);
                buttonBox.setManaged(false);
            }
        }
    }

    /**
     * Habilita manualmente el modo de edición desde fuera del controlador. Útil
     * si queremos activar la edición programáticamente.
     */
    public void activeEditable() {
        LogInfo.getInstance().logInfo("Activando modo edición manualmente (activeEditable).");
        // Mostramos los botones
        buttonBox.setVisible(true);
        buttonBox.setManaged(true);

        // Habilitamos edición
        txtComment.setEditable(true);
        txtComment.requestFocus();
    }

    /**
     * Maneja el evento del botón "Editar" (o "Guardar").
     * <p>
     * Si no se está editando, activa el modo edición (habilita texto y
     * estrellas). Si ya se está editando, guarda los cambios en la base de
     * datos y actualiza la vista.
     * </p>
     *
     * * @param event El evento de acción generado por el botón.
     */
    @FXML
    private void handleEdit(ActionEvent event) {
        if (!isEditing) {
            // --- ACTIVAR MODO EDICIÓN ---
            LogInfo.getInstance().logInfo("Usuario inició edición del comentario.");
            isEditing = true;
            txtComment.setEditable(true);
            txtComment.requestFocus();

            if (!txtComment.getStyleClass().contains("comment-edit-mode")) {
                txtComment.getStyleClass().add("comment-edit-mode");
            }

            if (starRateController != null) {
                starRateController.setEditable(true);
            }

            btnEdit.setText("Guardar");
            btnDelete.setText("Cancelar");

        } else {
            // --- GUARDAR CAMBIOS ---
            LogInfo.getInstance().logInfo("Intentando guardar cambios del comentario...");
            String nuevoTexto = txtComment.getText();

            if (nuevoTexto.trim().isEmpty()) {
                UtilGeneric.getInstance().showAlert("El comentario no puede estar vacío.", Alert.AlertType.WARNING, "Error");
                return;
            }
            if (nuevoTexto.length() > 500) {
                LogInfo.getInstance().logWarning("Intento de editar comentario con demasiados caracteres: " + nuevoTexto.length());
                showAlert("El comentario no puede superar los 500 caracteres.", Alert.AlertType.WARNING);
                return;
            }
            // Bloqueamos botón para evitar doble clic
            btnEdit.setDisable(true);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 1. Actualizamos el objeto con los datos de la interfaz
                        currentComment.setCommentary(nuevoTexto);

                        // IMPORTANTE: Recogemos la valoración de las estrellas
                        if (starRateController != null) {
                            float nuevaNota = (float) starRateController.getValueUser();
                            currentComment.setValuation(nuevaNota);
                        }

                        // 2. Guardamos en Base de Datos
                        dao.updateComment(currentComment);

                        // 3. Volvemos al hilo de UI para cerrar la edición
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                btnEdit.setDisable(false);
                                finalizarEdicion();
                                showAlert("Cambios guardados correctamente.", Alert.AlertType.INFORMATION);
                            }
                        });

                    } catch (final Exception e) {
                        LogInfo.getInstance().logSevere("Error al guardar el comentario en BD", e);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                UtilGeneric.getInstance().showAlert("Error al guardar: " + e.getMessage(), Alert.AlertType.ERROR, "Error");
                                btnEdit.setDisable(false);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    /**
     * Maneja el evento del botón "Borrar" (o "Cancelar").
     * <p>
     * Si se está en modo edición, funciona como botón "Cancelar" y revierte los
     * cambios. Si está en modo normal, pide confirmación para eliminar el
     * comentario de la BD.
     * </p>
     *
     * * @param event El evento de acción generado por el botón.
     */
    @FXML
    private void handleDelete(final ActionEvent event) {
        if (isEditing) {
            // Modo para cancelar la edición
            LogInfo.getInstance().logInfo("Edición cancelada por el usuario.");
            txtComment.setText(currentComment.getCommentary());
            finalizarEdicion();
        } else {
            LogInfo.getInstance().logInfo("Iniciando proceso de borrado de comentario.");
            // 1. Alerta de confirmación personalizada con logo
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
            conf.setTitle("Borrar");
            conf.setHeaderText(null);
            conf.setContentText("¿Seguro que quieres borrarlo? No se puede deshacer.");

            try {
                Image logo = new Image(getClass().getResourceAsStream("/images/Book&Bugs_Logo.png"));
                ImageView view = new ImageView(logo);
                view.setFitHeight(50);
                view.setPreserveRatio(true);
                conf.setGraphic(view);
                ((Stage) conf.getDialogPane().getScene().getWindow()).getIcons().add(logo);
            } catch (Exception e) {
                LogInfo.getInstance().logWarning("No se pudo cargar el logo en la alerta de confirmación.");
            }

            Optional<ButtonType> result = conf.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Operación de Base de Datos
                            dao.deleteComment(currentComment);

                            // Actualización de la Interfaz
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    Node source = (Node) event.getSource();
                                    Node tarjetaEntera = source.getParent().getParent();

                                    if (tarjetaEntera.getParent() != null) {
                                        ((javafx.scene.layout.Pane) tarjetaEntera.getParent()).getChildren().remove(tarjetaEntera);
                                    }

                                    // Avisar al padre si existe
                                    if (parentController != null) {
                                        parentController.onCommentDeleted();
                                    }

                                    UtilGeneric.getInstance().showAlert("Comentario borrado correctamente.", Alert.AlertType.INFORMATION, "Exito");
                                }
                            });
                        } catch (final Exception e) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    LogInfo.getInstance().logSevere("Error crítico al intentar borrar el comentario", e);
                                    UtilGeneric.getInstance().showAlert("Error al borrar: " + e.getMessage(), Alert.AlertType.ERROR, "Error");
                                }
                            });
                        }
                    }
                }).start();
            }
        }
    }

    /**
     * Método auxiliar para restaurar la vista al estado de "Solo Lectura".
     * Deshabilita la edición de texto y restaura los botones originales.
     */
    private void finalizarEdicion() {
        isEditing = false;
        txtComment.setEditable(false);
        txtComment.getStyleClass().remove("comment-edit-mode");
        btnEdit.setText("Editar"); // O pon tu icono "✏️"
        btnDelete.setText("Borrar"); // O pon tu icono "🗑️"
    }

    /**
     * Muestra una alerta simple al usuario, intentando cargar el logo
     * corporativo.
     *
     * * @param message El mensaje a mostrar.
     * @param type El tipo de alerta (ERROR, WARNING, INFORMATION).
     */
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setTitle("Book&Bugs");

        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/Book&Bugs_Logo.png"));
            ImageView view = new ImageView(logo);
            view.setFitHeight(50);
            view.setPreserveRatio(true);
            alert.setGraphic(view);
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(logo);
        } catch (Exception e) {
            LogInfo.getInstance().logWarning("Error no crítico al cargar la imagen en showAlert: " + e.getMessage());
        }

        alert.showAndWait();
    }
}
