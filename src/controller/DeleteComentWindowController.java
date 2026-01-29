package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty; // IMPORTANTE
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Commentate;
import model.DBImplementation;
import model.User;
import model.UserSession; // Asegúrate de importar esto si usas validación de sesión

public class DeleteComentWindowController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DeleteComentWindowController.class.getName());
    private DBImplementation db = new DBImplementation();

    @FXML private TableView<Commentate> tableComments;
    // Definimos todas como String porque vamos a mostrar texto formateado
    @FXML private TableColumn<Commentate, String> colBook;    
    @FXML private TableColumn<Commentate, String> colDate;    
    @FXML private TableColumn<Commentate, String> colComment; 
    @FXML private ComboBox<User> comboUsers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. TÍTULO DEL LIBRO
        // Accedemos al objeto Book y luego a su título. Protegemos contra nulos.
        colBook.setCellValueFactory(cellData -> {
            if (cellData.getValue().getBook() != null) {
                return new SimpleStringProperty(cellData.getValue().getBook().getTitle());
            } else {
                return new SimpleStringProperty("Desconocido");
            }
        });

        // 2. FECHA FORMATEADA
        // Usamos tu método getFormattedDate() de Commentate.java
        colDate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFormattedDate()));

        // 3. CONTENIDO DEL COMENTARIO
        // Usamos el getter del campo 'commentary'
        colComment.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCommentary()));

        // Carga inicial de usuarios
        try {
            ObservableList<User> users = FXCollections.observableArrayList(db.getAllUsers());
            comboUsers.setItems(users);

            // Listener para cargar comentarios al seleccionar usuario
            comboUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    cargarComentarios(newVal.getUsername());
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar usuarios", e);
        }
    }

    private void cargarComentarios(String username) {
        try {
            ObservableList<Commentate> lista = FXCollections.observableArrayList(db.getCommentsByUser(username));
            tableComments.setItems(lista);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar comentarios", e);
        }
    }

    @FXML
    private void handleDeleteComment(ActionEvent event) {
        Commentate selected = tableComments.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                db.deleteComment(selected);
                tableComments.getItems().remove(selected);
                new Alert(Alert.AlertType.INFORMATION, "Comentario eliminado correctamente.").show();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error al eliminar el comentario.").show();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Debes seleccionar un comentario de la tabla.").show();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OptionsAdmin.fxml"));
            Parent root = loader.load();
            
            // Obtener Stage desde el evento (seguro)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al volver", ex);
        }
    }
}