package controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Commentate;
import model.DBImplementation;
import model.Profile;

public class DeleteComentWindowController implements Initializable {

    @FXML private Button btnVolver;
    @FXML private TextField txtNameUsu;
    
    // Asegúrate de tener fx:id="tableComments" en el FXML
    @FXML private TableView<Commentate> tableComments; 
    
    @FXML private TableColumn<Commentate, String> columnTitle;
    @FXML private TableColumn<Commentate, String> columnComent;
    @FXML private TableColumn<Commentate, String> columnDate;
    
    @FXML private Button btnDeleteComent;

    private final DBImplementation dao = new DBImplementation();
    private final ObservableList<Commentate> commentsData = FXCollections.observableArrayList();
    private Profile adminProfile; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        columnTitle.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getBook().getTitle()));
        
        columnComent.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCommentary()));
            
        columnDate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDateCreation().toString()));
        
        if (tableComments != null) {
            tableComments.setItems(commentsData);
        }

        txtNameUsu.setOnAction(event -> buscarComentarios());
        btnDeleteComent.setOnAction(event -> borrarComentario());
        btnVolver.setOnAction(event -> volver());
    }
    
    public void setProfile(Profile profile){
        this.adminProfile = profile;
    }

    private void buscarComentarios() {
        String username = txtNameUsu.getText().trim();
        if (!username.isEmpty()) {
            List<Commentate> encontrados = dao.getCommentsByUser(username);
            commentsData.clear();
            commentsData.addAll(encontrados);
            
            if(encontrados.isEmpty()){
                mostrarAlerta("Info", "Sin comentarios para este usuario.", Alert.AlertType.INFORMATION);
            }
        }
    }

    private void borrarComentario() {
        if (tableComments == null) return;
        Commentate selected = tableComments.getSelectionModel().getSelectedItem();
        
        if (selected != null) {
            try {
                dao.deleteComment(selected);
                commentsData.remove(selected);
                mostrarAlerta("Éxito", "Comentario eliminado.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al borrar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Aviso", "Selecciona un comentario primero.", Alert.AlertType.WARNING);
        }
    }

    private void volver() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/OptionsAdmin.fxml"));
            Parent root = fxmlLoader.load();
            
            OptionsAdminController controller = fxmlLoader.getController();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            ((Stage) btnVolver.getScene().getWindow()).close();

        } catch (IOException ex) {
            Logger.getLogger(DeleteComentWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}