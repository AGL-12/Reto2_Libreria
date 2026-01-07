/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Alexander
 */
public class BookController {

    @FXML
    private Button btnAddComment;
    @FXML
    private VBox commentsContainer;
    @FXML
    private Button btnSaveComment;

    @FXML
    public void initialize() {
        // Cargar comentarios existentes (simulado)
        cargarComentarioExistente("Juan", "Muy buen libro", false);
        cargarComentarioExistente("Maria", "No me gustó el final", false);
    }

    @FXML
    private void handleNewComment() {
        try {
            // 1. Cargamos el FXML del comentario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/ComentViewController.fxml"));
            Parent newCommentNode = loader.load();

            // 2. Obtenemos el controlador de ese comentario específico
            ComentViewController controller = loader.getController();

            // 3. Configuramos para "CREAR" (Modo Edición)
            // Aquí usas el método que creamos antes
            controller.activeEditable();

            // Opcional: Configurar datos iniciales del usuario actual
            // controller.setUsuario("Yo (Usuario Actual)");
            // 4. Añadimos el nodo al PRINCIPIO de la lista (índice 0)
            // Así aparece justo debajo del título "Comentarios"
            commentsContainer.getChildren().add(0, newCommentNode);

            // 5. (Opcional) Enfocar el editor o hacer scroll hacia él si es necesario
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método auxiliar para cargar comentarios de la base de datos (solo lectura)
    private void cargarComentarioExistente(String user, String texto, boolean esMio) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/ComentViewController.fxml"));
            Node node = loader.load();
            ComentViewController controller = loader.getController();

            // Configurar datos
            // controller.setDatos(user, texto, ...);
            // Modo lectura por defecto
            // (El initialize del ComentViewController ya lo hace, pero por seguridad...)
            // controller.activarModoLectura(); 
            commentsContainer.getChildren().add(node);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
