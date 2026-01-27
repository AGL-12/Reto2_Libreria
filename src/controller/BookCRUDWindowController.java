package controller;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Author;
import model.Book;
import model.ClassDAO;
import model.DBImplementation;
import model.Profile;

public class BookCRUDWindowController implements Initializable {

    @FXML
    private TextField txtISBN;
    @FXML
    private TextField txtStock;
    @FXML
    private TextField txtSinopsis;
    @FXML
    private TextField txtEditorial;
    @FXML
    private Button btnConfirm;
    @FXML
    private Button btnReturn;
    @FXML
    private TextField txtTitle;
    @FXML
    private TextField txtIdAuthor;
    @FXML
    private TextField txtPages;
    @FXML
    private TextField txtPrice;
    @FXML
    private ImageView idFrontPage;
    @FXML
    private Button btnUploadFile;

    private File archivoPortada;
    private Profile profile;
    private String modo; // "create", "modify", "delete"
    private final ClassDAO dao = new DBImplementation();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configuramos el campo ISBN para que busque al pulsar ENTER
        txtISBN.setOnAction(event -> {
            if (!"create".equals(modo)) {
                buscarLibro();
            }
        });

        // También podemos buscar si el campo pierde el foco (opcional)
        txtISBN.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtISBN.getText().isEmpty() && !"create".equals(modo)) {
                buscarLibro();
            }
        });
    }

    /**
     * Configura la interfaz según la acción que vamos a realizar. Este método
     * se llama desde LibroOptionWindowController.
     */
    public void setModo(String modo) {
        this.modo = modo;

        switch (modo) {
            case "create":
                btnConfirm.setText("Añadir Libro");
                limpiarCampos();
                habilitarCampos(true); // Todo editable
                break;

            case "modify":
                btnConfirm.setText("Modificar Libro");
                limpiarCampos();
                habilitarCampos(false); // Bloqueado hasta que busque ISBN
                txtISBN.setDisable(false); // ISBN siempre editable para buscar
                txtISBN.setPromptText("Escribe ISBN y pulsa Enter");
                break;

            case "delete":
                btnConfirm.setText("Eliminar Libro");
                limpiarCampos();
                habilitarCampos(false); // Todo bloqueado (solo lectura)
                txtISBN.setDisable(false);
                txtISBN.setPromptText("Escribe ISBN y pulsa Enter");
                // En delete, el botón confirmar se deshabilita hasta encontrar el libro
                btnConfirm.setDisable(true);
                break;
        }
    }

    // --- LÓGICA DE BÚSQUEDA ---
    private void buscarLibro() {
        String isbn = txtISBN.getText().trim();
        if (isbn.isEmpty()) {
            return;
        }
    }

    private void rellenarDatos(Book libro) {
        txtTitle.setText(libro.getTitle());
        // Manejo seguro del autor por si es null
        //txtIdAutor.setText(String.valueOf(libro.getIdAuthor()));
        txtPages.setText(String.valueOf(libro.getSheets()));
        txtStock.setText(String.valueOf(libro.getStock()));
        txtSinopsis.setText(libro.getSypnosis());
        txtPrice.setText(String.valueOf(libro.getPrice()));
        txtEditorial.setText(libro.getEditorial());

        // Cargar imagen si existe
        if (libro.getCover() != null && !libro.getCover().isEmpty()) {
            try {
                // Intenta cargar desde recursos o ruta absoluta
                String ruta = "/images/" + libro.getCover();
                Image img = new Image(getClass().getResourceAsStream(ruta));
                idFrontPage.setImage(img);
            } catch (Exception e) {
                // Si falla, no rompemos la app, solo no mostramos imagen
                System.out.println("No se pudo cargar la imagen: " + libro.getCover());
            }
        }
    }

    // --- LÓGICA DE BOTONES ---
    private void confirmarAccion() {
        try {
            // Recoger datos (Validar que sean números)
            int isbn = Integer.parseInt(txtISBN.getText());

            if ("delete".equals(modo)) {
                dao.deleteBook(isbn);
                return; // Salimos, no hace falta leer el resto de campos para borrar
            }

            // Para Create y Modify leemos el resto de campos
            String titulo = txtTitle.getText();
            int idAutor = Integer.parseInt(txtIdAuthor.getText());
            int hojas = Integer.parseInt(txtPages.getText());
            int stock = Integer.parseInt(txtStock.getText());
            String sinopsis = txtSinopsis.getText();
            float precio = Float.parseFloat(txtPrice.getText());
            String editorial = txtEditorial.getText();
            String nombrePortada = (archivoPortada != null) ? archivoPortada.getName() : "default.png";

            // Crear objeto Autor dummy (solo necesitamos el ID para la BD)
            Author autor = new Author();
            autor.setIdAuthor(idAutor);

            // Crear objeto Book
        } catch (NumberFormatException e) {
            mostrarAlerta("Error de formato", "Por favor revisa que los campos numéricos (ISBN, Stock, Precio...) sean correctos.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Error", "Ocurrió un error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void volver() {
        cerrarVentana();
    }

    // --- UTILIDADES ---
    private void habilitarCampos(boolean habilitar) {
        txtTitle.setDisable(!habilitar);
        txtIdAuthor.setDisable(!habilitar);
        txtPages.setDisable(!habilitar);
        txtStock.setDisable(!habilitar);
        txtSinopsis.setDisable(!habilitar);
        txtPrice.setDisable(!habilitar);
        txtEditorial.setDisable(!habilitar);
        btnUploadFile.setDisable(!habilitar);
        // El ISBN se controla aparte
    }

    private void limpiarCampos() {
        txtISBN.setText("");
        txtTitle.setText("");
        txtIdAuthor.setText("");
        txtPages.setText("");
        txtStock.setText("");
        txtSinopsis.setText("");
        txtPrice.setText("");
        txtEditorial.setText("");
        idFrontPage.setImage(null);
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnReturn.getScene().getWindow();
        stage.close();
        // Opcional: Podrías reabrir la ventana anterior aquí si quisieras
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    // --- TUS MÉTODOS DE IMAGEN EXISTENTES ---
    private void arrastrarSobre(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void soltarImagen(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            File file = files.get(0);
            // Validar extensión simple
            if (file.getName().toLowerCase().endsWith(".png") || file.getName().toLowerCase().endsWith(".jpg")) {
                archivoPortada = file;
                idFrontPage.setImage(new Image(file.toURI().toString()));
                success = true;
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void subirPortada() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar portada");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        Stage stage = (Stage) btnUploadFile.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            archivoPortada = file;
            idFrontPage.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void dragOver(DragEvent event) {
    }

    @FXML
    private void dropImage(DragEvent event) {
    }

    @FXML
    private void uploadFrontPage(ActionEvent event) {
    }
}
