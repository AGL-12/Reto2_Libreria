package controller;

import java.net.URL;
import java.sql.Date; // O java.util.Date según tu modelo
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Book;
import model.ClassDAO;
import model.Commentate;
import model.DBImplementation;

public class DeleteComentWindowController implements Initializable {

    // --- ELEMENTOS FXML ---
    @FXML
    private Button btnReturn;
    @FXML
    private TextField txtNameUsu; // Campo de búsqueda
    
    // IMPORTANTE: Debes añadir fx:id="tblComentarios" a tu TableView en el FXML
    @FXML
    private TableView<Commentate> tblComentarios; 
    
    @FXML
    private TableColumn<Commentate, String> columnTitle; // Título del libro
    @FXML
    private TableColumn<Commentate, String> columnComent; // Texto del comentario
    @FXML
    private TableColumn<Commentate, Date> columnDate;     // Fecha
    
    @FXML
    private Button btnDeleteComent;

    // --- VARIABLES ---
    private final ClassDAO dao = new DBImplementation();
    private ObservableList<Commentate> commentsData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        cargarDatos(""); // Cargar todo al principio

        // Listener para buscar mientras escribes (opcional)
        txtNameUsu.textProperty().addListener((observable, oldValue, newValue) -> {
            cargarDatos(newValue);
        });
        
        // Configurar botones
        btnDeleteComent.setOnAction(event -> eliminarComentario());
        btnReturn.setOnAction(event -> cerrarVentana());
    }

    private void configurarTabla() {
        // 1. Columna Título del Libro
        // Como 'title' está dentro del objeto 'Book', usamos una lambda para extraerlo
        columnTitle.setCellValueFactory(cellData -> {
            if (cellData.getValue().getBook() != null) {
                return new SimpleStringProperty(cellData.getValue().getBook().getTitle());
            } else {
                return new SimpleStringProperty("Sin título");
            }
        });

        // 2. Columna Texto del Comentario
        // Asumo que tu atributo en Commentate se llama 'text' o similar.
        // Si usas PropertyValueFactory, el nombre debe coincidir con el atributo de la clase.
        // Ejemplo: new PropertyValueFactory<>("comentaryText");
        // Aquí uso lambda para ser más flexible:
        columnComent.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCommentary()) // ¡Ajusta este getter!
        );

        // 3. Columna Fecha
        columnDate.setCellValueFactory(new PropertyValueFactory<>("commentDate")); // ¡Ajusta este nombre de atributo!

        // Asignar la lista a la tabla
        tblComentarios.setItems(commentsData);
    }

    private void cargarDatos(String username) {
        try {
            commentsData.clear();
            List<Commentate> lista = dao.getCommentsByUser(username);
            commentsData.addAll(lista);
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al cargar comentarios: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void eliminarComentario() {
        Commentate selectedComment = tblComentarios.getSelectionModel().getSelectedItem();
        
        if (selectedComment == null) {
            mostrarAlerta("Selección vacía", "Por favor, selecciona un comentario para eliminar.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Llamada al DAO para borrar (ya implementado en tu DBImplementation)
            dao.deleteComment(selectedComment);
            
            mostrarAlerta("Éxito", "Comentario eliminado correctamente.", Alert.AlertType.INFORMATION);
            
            // Recargar la tabla para ver que desaparece
            cargarDatos(txtNameUsu.getText());
            
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo eliminar el comentario: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cerrarVentana() {
        try {
            // Aquí puedes reabrir el menú de Admin si quieres
            /*
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OptionsAdmin.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            */
            Stage currentStage = (Stage) btnReturn.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}