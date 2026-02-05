/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
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
import java.util.logging.Logger;

/**
 * Controlador para la vista individual de un comentario (CommentView.fxml).
 * Esta clase gestiona la visualización de cada comentario en la lista,
 * permitiendo ver la información del autor, la fecha y el contenido. También
 * controla la lógica para editar o eliminar el comentario si el usuario actual
 * es el propietario o un administrador.
 *
 * * @author mikel
 */
public class CommentViewController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(CommentViewController.class.getName());
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
     * * @param comment El objeto comentario con toda la información a mostrar.
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
            LOGGER.severe("ERROR CRÍTICO: txtComment es NULL en el controlador.");
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
        LOGGER.info("Activando modo edición manualmente (activeEditable).");
        // Mostramos los botones
        buttonBox.setVisible(true);
        buttonBox.setManaged(true);

        // Habilitamos edición
        txtComment.setEditable(true);
        txtComment.requestFocus();
    }

    /**
     * Maneja el evento del botón "Editar" (o "Guardar"). Si no se está
     * editando, activa el modo edición (habilita texto y estrellas). Si ya se
     * está editando, guarda los cambios en la base de datos y actualiza la
     * vista.
     *
     * * @param event El evento de acción generado por el botón.
     */
    @FXML
    private void handleEdit(ActionEvent event) {
        if (!isEditing) {
            // Modo para empezar a editar
            LOGGER.info("Usuario inició edición del comentario.");
            isEditing = true;

            // Habilitamos la escritura para que se pueda editar
            txtComment.setEditable(true);
            txtComment.requestFocus();
            if (!txtComment.getStyleClass().contains("comment-edit-mode")) {
                txtComment.getStyleClass().add("comment-edit-mode");
            }
            // Habilitamos edición de estrellas
            if (starRateController != null) {
                starRateController.setEditable(true);
            }

            // Cambiamos los botones
            btnEdit.setText("Guardar");
            btnDelete.setText("Cancelar");

        } else {
            // Guardamos cambios
            LOGGER.info("Intentando guardar cambios del comentario...");
            String nuevoTexto = txtComment.getText();

            if (nuevoTexto.trim().isEmpty()) {
                showAlert("El comentario no puede estar vacío.", Alert.AlertType.WARNING);
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Actualizar base de datos y el objeto
                        currentComment.setCommentary(nuevoTexto);
                        dao.updateComment(currentComment);

                        // Vuelve al estado normal
                        Platform.runLater(() -> finalizarEdicion());

                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error al guardar el comentario en BD", e);
                        Platform.runLater(() -> showAlert("Error al guardar: " + e.getMessage(), Alert.AlertType.ERROR));
                    }
                }
            }).start();
        }
    }

    /**
     * Maneja el evento del botón "Borrar" (o "Cancelar"). Si se está en modo
     * edición, funciona como botón "Cancelar" y revierte los cambios. Si está
     * en modo normal, pide confirmación para eliminar el comentario de la BD.
     *
     * * @param event El evento de acción generado por el botón.
     */
    @FXML
    private void handleDelete(ActionEvent event) {
        if (isEditing) {
            // Modo para cancelar la edición
            LOGGER.info("Edición cancelada por el usuario.");
            txtComment.setText(currentComment.getCommentary());
            finalizarEdicion();

        } else {
// Lógica de borrar comentario
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Borrar");
            alert.setHeaderText("¿Seguro que quieres borrarlo?");
            alert.setContentText("No se puede deshacer.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // 1. Borrar de la base de datos
                    dao.deleteComment(currentComment);

                    // 2. Borrar visualmente (Método seguro)
                    Node source = (Node) event.getSource();
                    Node fichaComentario = source.getParent().getParent();
                    if (fichaComentario != null && fichaComentario.getParent() != null) {
                        ((javafx.scene.layout.Pane) fichaComentario.getParent()).getChildren().remove(fichaComentario);
                    }

                    // 3. AVISAR AL PADRE (BookViewController)
                    if (parentController != null) {
                        System.out.println("Enviando aviso al padre para habilitar botón...");
                        parentController.onCommentDeleted();
                    } else {
                        System.err.println("Error: parentController es NULL. No se puede avisar.");
                    }

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error crítico al borrar comentario", e);
                    showAlert("Error al borrar: " + e.getMessage(), Alert.AlertType.ERROR);
                }
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
     * Muestra una alerta simple al usuario.
     *
     * * @param message El mensaje a mostrar.
     * @param type El tipo de alerta (ERROR, WARNING, INFORMATION).
     */
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.show();
    }

}
