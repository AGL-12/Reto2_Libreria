package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty; 
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
import model.UserSession;

/**
 * Controlador de la ventana de eliminar comentarios
 * Es una ventana que solo tiene acceso el adminsitrador
 * filtra por usuario sus comentarios
 * @author unai azkorra
 * @version 1.0
 */
public class DeleteComentWindowController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DeleteComentWindowController.class.getName());
    private DBImplementation db = new DBImplementation();

    @FXML private TableView<Commentate> tableComments;
    @FXML private TableColumn<Commentate, String> colBook;    
    @FXML private TableColumn<Commentate, String> colDate;    
    @FXML private TableColumn<Commentate, String> colComment; 
    @FXML private ComboBox<User> comboUsers;

    /**
     * metodo que se usa para inicializar los parametros y establecer los valores iniciales
     * @param location
     * @param resources 
     */
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colBook.setCellValueFactory(cellData -> {
            if (cellData.getValue().getBook() != null) {
                return new SimpleStringProperty(cellData.getValue().getBook().getTitle());
            } else {
                return new SimpleStringProperty("Desconocido");
            }
        });

        colDate.setCellValueFactory(cellData ->  new SimpleStringProperty(cellData.getValue().getFormattedDate()));

        colComment.setCellValueFactory(cellData ->  new SimpleStringProperty(cellData.getValue().getCommentary()));

        try {
            ObservableList<User> users = FXCollections.observableArrayList(db.getAllUsers());
            comboUsers.setItems(users);
            comboUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    cargarComentarios(newVal.getUsername());
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar usuarios", e);
        }
    }

    /**
     *recibe por parametro el username para buscar los comentarios 
     * @param username 
     */
    private void cargarComentarios(String username) {
        try {
            ObservableList<Commentate> lista = FXCollections.observableArrayList(db.getCommentsByUser(username));
            tableComments.setItems(lista);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar comentarios", e);
        }
    }

    /**
     * Metodo que se ejecuta al presionar eliminar el libro. 
     * En caso de no haber elegido un comentario saltara un error avisando al usuario que debe 
     * seleccionar un comentario.
     * @param event 
     */
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

    /**
     * el metodo entra en accion al pulsar volver y aber el menu de operacion del administrador
     * @param event 
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OptionsAdmin.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al volver", ex);
        }
    }
}