package controller;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.UUID;
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

public class BookCRUDWindowController implements Initializable {

    @FXML private Button btnConfirmar;
    @FXML private Button btnReturn;
    @FXML private TextField txtISBN;
    @FXML private TextField txtTitle;
    @FXML private TextField txtAuthorName;
    @FXML private TextField txtAuthorLastname;
    @FXML private TextField txtPages;
    @FXML private TextField txtStock;
    @FXML private TextField txtSinopsis;
    @FXML private TextField txtPrice;
    @FXML private TextField txtEditorial;
    @FXML private ImageView idFrontPage;
    @FXML private Button btnUploadFile;

    private File archivoPortada;
    private String modo;
    private Book libroActual;
    private final ClassDAO dao = new DBImplementation();
    
    // RUTA DEFINITIVA: Guardar dentro de src/images/
    private final String RUTA_IMAGENES = "src/images/";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnConfirmar.setOnAction(this::confirmarAccion);
        btnReturn.setOnAction(this::volver);
        
        txtISBN.setOnAction(event -> {
            if (!"create".equals(modo)) buscarLibro();
        });
        txtISBN.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtISBN.getText().isEmpty() && !"create".equals(modo)) buscarLibro();
        });
    }

    public void setModo(String modo) {
        this.modo = modo;
        switch (modo) {
            case "create":
                btnConfirmar.setText("Añadir Libro");
                limpiarCampos();
                habilitarCampos(true);
                break;
            case "modify":
                btnConfirmar.setText("Modificar Libro");
                limpiarCampos();
                habilitarCampos(false);
                txtISBN.setDisable(false);
                break;
            case "delete":
                btnConfirmar.setText("Eliminar Libro");
                limpiarCampos();
                habilitarCampos(false);
                txtISBN.setDisable(false);
                btnConfirmar.setDisable(true);
                break;
        }
    }

    private void buscarLibro() {
        String isbnText = txtISBN.getText().trim();
        if (isbnText.isEmpty()) return;
        try {
            long isbn = Long.parseLong(isbnText);
            Book libro = dao.getBookData(isbn);

            if (libro != null) {
                this.libroActual = libro;
                rellenarDatos(libro);
                if ("modify".equals(modo)) {
                    habilitarCampos(true);
                    txtISBN.setDisable(true);
                } else if ("delete".equals(modo)) {
                    btnConfirmar.setDisable(false);
                }
            } else {
                mostrarAlerta("No encontrado", "No existe libro con ese ISBN.", Alert.AlertType.WARNING);
                limpiarCampos();
                txtISBN.setText(isbnText);
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El ISBN debe ser numérico.", Alert.AlertType.ERROR);
        }
    }

    private void rellenarDatos(Book libro) {
        txtTitle.setText(libro.getTitle());
        if (libro.getAuthor() != null) {
            txtAuthorName.setText(libro.getAuthor().getName());
            txtAuthorLastname.setText(libro.getAuthor().getSurname());
        }
        txtPages.setText(String.valueOf(libro.getSheets()));
        txtStock.setText(String.valueOf(libro.getStock()));
        txtSinopsis.setText(libro.getSypnosis());
        txtPrice.setText(String.valueOf(libro.getPrice()));
        txtEditorial.setText(libro.getEditorial());

        // Cargar imagen
        String nombreFoto = libro.getCover();
        try {
            if (nombreFoto != null && !nombreFoto.isEmpty()) {
                // Buscamos directamente el archivo físico en src/images
                File fotoFisica = new File(RUTA_IMAGENES + nombreFoto);
                if (fotoFisica.exists()) {
                    idFrontPage.setImage(new Image(fotoFisica.toURI().toString()));
                } else {
                    // Si no está, intentamos cargar el default desde el classpath
                    // Nota: Si usas getClass().getResource, la ruta empieza desde 'classes', no 'src'
                    URL recurso = getClass().getResource("/images/default.png");
                    if (recurso != null) idFrontPage.setImage(new Image(recurso.toString()));
                }
            } else {
                 // Cargar default si no hay nombre
                 URL recurso = getClass().getResource("/images/default.png");
                 if (recurso != null) idFrontPage.setImage(new Image(recurso.toString()));
            }
        } catch (Exception e) { /* Ignorar */ }
    }

    @FXML
    private void confirmarAccion(ActionEvent event) {
        try {
            if (txtISBN.getText().trim().isEmpty()) {
                mostrarAlerta("Error", "ISBN obligatorio.", Alert.AlertType.WARNING);
                return;
            }
            long isbn = Long.parseLong(txtISBN.getText());

            if ("delete".equals(modo)) {
                dao.deleteBook(isbn); // Aquí llamamos al método que ya has rellenado en DBImplementation
                mostrarAlerta("Éxito", "Libro eliminado.", Alert.AlertType.INFORMATION);
                cerrarVentana();
                return;
            }

            // Validar campos vacíos
            if (txtTitle.getText().isEmpty() || txtAuthorName.getText().isEmpty() || 
                txtAuthorLastname.getText().isEmpty() || txtPages.getText().isEmpty() ||
                txtStock.getText().isEmpty() || txtPrice.getText().isEmpty()) {
                mostrarAlerta("Datos faltantes", "Rellena todos los campos.", Alert.AlertType.WARNING);
                return;
            }

            // Guardar Imagen en src/images
            String nombreImagen = guardarImagenFisica();
            
            if (nombreImagen == null) {
                if ("modify".equals(modo) && libroActual != null) nombreImagen = libroActual.getCover();
                else nombreImagen = "default.png"; // Ojo: Asegúrate de tener default.png en src/images también
            }
            
            Author autor = dao.getOrCreateAuthor(txtAuthorName.getText().trim(), txtAuthorLastname.getText().trim());

            Book libro = new Book(
                isbn,
                nombreImagen,
                txtTitle.getText(),
                autor,
                Integer.parseInt(txtPages.getText()),
                Integer.parseInt(txtStock.getText()),
                txtSinopsis.getText(),
                Float.parseFloat(txtPrice.getText()),
                txtEditorial.getText(),
                0f
            );

            if ("create".equals(modo)) {
                dao.createBook(libro); // Llama al método que has rellenado
                mostrarAlerta("Éxito", "Libro creado.", Alert.AlertType.INFORMATION);
            } else if ("modify".equals(modo)) {
                dao.modifyBook(libro); // Llama al método que has rellenado
                mostrarAlerta("Éxito", "Libro modificado.", Alert.AlertType.INFORMATION);
            }

            cerrarVentana();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Revisa los campos numéricos.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void volver(ActionEvent event) {
        cerrarVentana();
    }

    @FXML
    private void uploadFrontPage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(btnUploadFile.getScene().getWindow());
        if (file != null) {
            this.archivoPortada = file;
            idFrontPage.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void dragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) event.acceptTransferModes(TransferMode.COPY);
        event.consume();
    }

    @FXML
    private void dropImage(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            File file = db.getFiles().get(0);
            String nombre = file.getName().toLowerCase();
            if (nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) {
                this.archivoPortada = file;
                idFrontPage.setImage(new Image(file.toURI().toString()));
                success = true;
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    // --- UTILIDADES ---
    
    private String guardarImagenFisica() {
        if (archivoPortada == null) return null;
        try {
            // Creamos la carpeta src/images si no existe
            File carpeta = new File(RUTA_IMAGENES);
            if (!carpeta.exists()) carpeta.mkdirs();
            
            String ext = "";
            int i = archivoPortada.getName().lastIndexOf('.');
            if (i > 0) ext = archivoPortada.getName().substring(i);
            
            String nombreFinal = UUID.randomUUID().toString() + ext;
            
            // Copiamos a src/images/nombreFinal
            Files.copy(archivoPortada.toPath(), Paths.get(RUTA_IMAGENES, nombreFinal), StandardCopyOption.REPLACE_EXISTING);
            return nombreFinal;
        } catch (Exception e) { 
            e.printStackTrace();
            return null; 
        }
    }

    private void habilitarCampos(boolean b) {
        txtTitle.setDisable(!b);
        txtAuthorName.setDisable(!b);
        txtAuthorLastname.setDisable(!b);
        txtPages.setDisable(!b);
        txtStock.setDisable(!b);
        txtSinopsis.setDisable(!b);
        txtPrice.setDisable(!b);
        txtEditorial.setDisable(!b);
        btnUploadFile.setDisable(!b);
    }

    private void limpiarCampos() {
        txtISBN.setText("");
        txtTitle.setText("");
        txtAuthorName.setText("");
        txtAuthorLastname.setText("");
        txtPages.setText("");
        txtStock.setText("");
        txtSinopsis.setText("");
        txtPrice.setText("");
        txtEditorial.setText("");
        idFrontPage.setImage(null);
        archivoPortada = null;
        libroActual = null;
    }

    private void cerrarVentana() {
        ((Stage) btnReturn.getScene().getWindow()).close();
    }

    private void mostrarAlerta(String t, String c, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(t);
        alert.setContentText(c);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}