/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import model.ClassDAO;
import model.Commentate;
import model.DBImplementation;
import model.Profile;
import model.UserSession;

/**
 * FXML Controller class
 *
 * @author mikel
 */
public class CommentViewController implements Initializable {

    @FXML
    private Label lblUsuario;
    @FXML
    private Label lblFecha;
    @FXML
    private TextArea txtComment;
    @FXML
    private HBox buttonBox;
    @FXML
    private StarRateController estrellasController;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;

    private Commentate currentComment;
    private final ClassDAO dao = new DBImplementation();
    private boolean isEditing = false;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (txtComment != null) {
            txtComment.setEditable(false);
        }

        if (buttonBox != null) {
            buttonBox.setVisible(false);
            buttonBox.setManaged(false);
        }

        if (estrellasController != null) {
            estrellasController.setEditable(false);
        }

    }

   /* public void setDatos(String usuario, String fecha, String texto, float puntuacion) {
        this.lblUsuario.setText(usuario);
        this.lblFecha.setText(fecha);
        this.txtComment.setText(texto);

        if (estrellasController != null) {
            estrellasController.setValueStars(puntuacion);
        }
    }*/
    
    
  public void setData(Commentate comment) {
        this.currentComment = comment;

      
        // 1. Rellenar la información visual (con seguridad anti-null)
        if (lblUsuario != null && comment.getUser() != null) {
            lblUsuario.setText(comment.getUser().getName());
        }
        
        if (lblFecha != null && comment.getDateCreation() != null) {
            lblFecha.setText(comment.getDateCreation().toString());
        }
        
        if (txtComment != null) {
            txtComment.setText(comment.getCommentary());
            // Forzamos estilo por si acaso
            txtComment.setStyle("-fx-text-fill: black; -fx-control-inner-background: white; -fx-opacity: 1;");
        } else {
            System.err.println("¡ERROR CRÍTICO! txtComment es NULL en el controlador.");
        }
        
        if (estrellasController != null) {
            estrellasController.setValueStars(comment.getValuation());
        }

        // 2. LÓGICA DE SEGURIDAD: ¿Muestro los botones?
        Profile currentUser = UserSession.getInstance().getUser();

        // Verificamos que todo exista antes de comparar
        if (currentUser != null && comment.getUser() != null && 
            currentUser.getUserCode() == comment.getUser().getUserCode()) {
            
            // ¡Es mío!
            if (buttonBox != null) {
                buttonBox.setVisible(true);
                buttonBox.setManaged(true);
            }
        } else {
            // No es mío
            if (buttonBox != null) {
                buttonBox.setVisible(false);
                buttonBox.setManaged(false);
            }
        }
    }
    public void activeEditable() {
        // 1. Mostrar los botones
        buttonBox.setVisible(true);
        buttonBox.setManaged(true);

        // 2. Habilitar edición
        txtComment.setEditable(true);
        txtComment.requestFocus();
    }

    /*void setData(Commentate coment) {
        lblUsuario.setText(coment.getUser().getName());
        lblFecha.setText(coment.getDateCreation().toString());
        txtComment.setText(coment.getCommentary());
        estrellasController.setValueStars(coment.getValuation());
    }*/
@FXML
    private void handleEdit(ActionEvent event) {
        if (!isEditing) {
            // --- MODO: EMPEZAR A EDITAR ---
            isEditing = true;
            
            // 1. Habilitar escritura
            txtComment.setEditable(true);
            txtComment.requestFocus(); // Poner el cursor ahí
           // txtComment.setStyle("-fx-text-fill: black; -fx-control-inner-background: #ffffff; -fx-border-color: #2E86C1;"); // Borde azul para notar que se edita
            
            // 2. Habilitar Estrellas (NUEVO)
            if (estrellasController != null) {
                estrellasController.setEditable(true);
            }

            // 2. Cambiar botones
            btnEdit.setText("💾 Guardar");
            btnDelete.setText("❌ Cancelar");
            
        } else {
            // --- MODO: GUARDAR CAMBIOS ---
            String nuevoTexto = txtComment.getText();
            
            if (nuevoTexto.trim().isEmpty()) {
                showAlert("El comentario no puede estar vacío.", Alert.AlertType.WARNING);
                return;
            }

            try {
                // Actualizar DB y Objeto
                currentComment.setCommentary(nuevoTexto);
                dao.updateComment(currentComment);
                
                // Volver a estado normal
                finalizarEdicion();
                
                // Mensaje opcional (puedes quitarlo si te molesta)
                // showAlert("Comentario guardado", Alert.AlertType.INFORMATION); 

            } catch (Exception e) {
                showAlert("Error al guardar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (isEditing) {
            // --- MODO: CANCELAR EDICIÓN ---
            // Restauramos el texto original
            txtComment.setText(currentComment.getCommentary());
            finalizarEdicion();
            
        } else {
            // --- MODO: BORRAR COMENTARIO ---
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Borrar");
            alert.setHeaderText("¿Seguro que quieres borrarlo?");
            alert.setContentText("No se puede deshacer.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    dao.deleteComment(currentComment);
                    
                    // Borrar visualmente
                    if (buttonBox != null && buttonBox.getParent() != null) {
                        Node tarjeta = buttonBox.getParent();
                        tarjeta.setVisible(false);
                        tarjeta.setManaged(false);
                    }
                } catch (Exception e) {
                    showAlert("Error al borrar: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        }
    }
    
    // Método auxiliar para volver al estado normal
    private void finalizarEdicion() {
        isEditing = false;
        txtComment.setEditable(false);
        txtComment.setStyle("-fx-text-fill: black; -fx-control-inner-background: white;"); // Quitar borde azul
        
        btnEdit.setText("Editar"); // O pon tu icono "✏️"
        btnDelete.setText("Borrar"); // O pon tu icono "🗑️"
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.show();
    }
    
}