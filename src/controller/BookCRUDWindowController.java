package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID; // Para generar nombres únicos si quieres
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    // --- VARIABLES FXML ---
    @FXML private Button btnConfirm;
    @FXML private Button btnReturn;
    @FXML private TextField txtISBN;
    @FXML private TextField txtTitle;
    @FXML private TextField txtNombreAutor;
    @FXML private TextField txtApellidoAutor;
    @FXML private TextField txtPages;
    @FXML private TextField txtStock;
    @FXML private TextField txtSinopsis;
    @FXML private TextField txtPrice;
    @FXML private TextField txtEditorial;
    @FXML private ImageView idFrontPage;
    @FXML private Button btnUploadFile;

    // --- VARIABLES LÓGICAS ---
    private File archivoPortada;
    private String modo; 
    private final ClassDAO dao = new DBImplementation();
    private Book libroActual; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnConfirm.setOnAction(this::confirmAction);
        btnReturn.setOnAction(this::returnAction);

        txtISBN.setOnAction(event -> {
            if (!"create".equals(modo)) buscarLibro();
        });
        txtISBN.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtISBN.getText().isEmpty() && !"create".equals(modo)) {
                buscarLibro();
            }
        });
    }

    public void setModo(String modo) {
        this.modo = modo;
        switch (modo) {
            case "create":
                btnConfirm.setText("Añadir Libro");
                limpiarCampos();
                habilitarCampos(true); 
                break;
            case "modify":
                btnConfirm.setText("Modificar Libro");
                limpiarCampos();
                habilitarCampos(false); 
                txtISBN.setDisable(false); 
                txtISBN.setPromptText("Escribe ISBN y pulsa Enter");
                break;
            case "delete":
                btnConfirm.setText("Eliminar Libro");
                limpiarCampos();
                habilitarCampos(false); 
                txtISBN.setDisable(false);
                txtISBN.setPromptText("Escribe ISBN y pulsa Enter");
                btnConfirm.setDisable(true); 
                break;
        }
    }

    // --- MÉTODOS DE EVENTOS FXML ---

    @FXML 
    private void dragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML 
    private void dropImage(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            File file = files.get(0);
            String name = file.getName().toLowerCase();
            if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                archivoPortada = file;
                idFrontPage.setImage(new Image(file.toURI().toString()));
                success = true;
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML 
    private void uploadFrontPage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar portada");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );
        Stage stage = (Stage) btnUploadFile.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            archivoPortada = file;
            idFrontPage.setImage(new Image(file.toURI().toString()));
        }
    }

    // --- LÓGICA PRINCIPAL (MODIFICADA PARA GUARDAR IMAGEN) ---

    private void confirmAction(ActionEvent event) {
        try {
            if (txtISBN.getText().isEmpty() || txtTitle.getText().isEmpty() ||
                txtNombreAutor.getText().isEmpty() || txtApellidoAutor.getText().isEmpty()) {
                mostrarAlerta("Datos faltantes", "Rellena los campos obligatorios.", Alert.AlertType.WARNING);
                return;
            }

            long isbn = Long.parseLong(txtISBN.getText());

            if ("delete".equals(modo)) {
                dao.deleteBook(isbn);
                mostrarAlerta("Éxito", "Libro eliminado correctamente.", Alert.AlertType.INFORMATION);
                closeWindow();
                return;
            }

            // Recogida de datos
            String titulo = txtTitle.getText();
            String nombreAutor = txtNombreAutor.getText().trim();
            String apellidoAutor = txtApellidoAutor.getText().trim();
            int hojas = Integer.parseInt(txtPages.getText());
            int stock = Integer.parseInt(txtStock.getText());
            String sinopsis = txtSinopsis.getText();
            float precio = Float.parseFloat(txtPrice.getText());
            String editorial = txtEditorial.getText();

            // --- LÓGICA DE GUARDADO DE IMAGEN ---
            String nombrePortada;
            
            if (archivoPortada != null) {
                // 1. Si el usuario subió una imagen nueva, la guardamos físicamente
                nombrePortada = archivoPortada.getName();
                guardarImagenEnDisco(archivoPortada); 
            } else {
                // 2. Si no subió nada, mantenemos la anterior o ponemos la default
                nombrePortada = (libroActual != null && libroActual.getCover() != null) 
                                ? libroActual.getCover() 
                                : "default.png";
            }
            // ------------------------------------

            Author autor = dao.getOrCreateAuthor(nombreAutor, apellidoAutor);

            Book libro = new Book(isbn, nombrePortada, titulo, autor, hojas, stock, sinopsis, precio, editorial, 0f);

            if ("create".equals(modo)) {
                dao.createBook(libro);
                mostrarAlerta("Éxito", "Libro creado.", Alert.AlertType.INFORMATION);
            } else if ("modify".equals(modo)) {
                dao.modifyBook(libro);
                mostrarAlerta("Éxito", "Libro modificado.", Alert.AlertType.INFORMATION);
            }
            this.closeWindow();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Revisa los campos numéricos.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace(); // Útil para ver errores de copia de archivo en consola
            mostrarAlerta("Error", "Error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Copia el archivo seleccionado a la carpeta src/images del proyecto.
     */
    private void guardarImagenEnDisco(File source) {
        try {
            // 1. Definimos la carpeta de destino: "src/images" dentro del proyecto
            // El "." representa la raíz del proyecto cuando lo corres desde NetBeans
            String destinationFolderPath = "src/images";
            File carpeta = new File(destinationFolderPath);

            // Crear carpeta si no existe (por seguridad)
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            // 2. Definimos el archivo destino (mismo nombre que el original)
            Path destinoPath = Paths.get(destinationFolderPath, source.getName());

            // 3. Copiamos el archivo (REEMPLAZANDO si ya existe uno igual)
            Files.copy(source.toPath(), destinoPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Imagen guardada en: " + destinoPath.toAbsolutePath());
            
            // OPCIONAL: Copiar también a la carpeta "build" para que se vea YA sin tener que reiniciar/recompilar
            // Esto es un truco para que JavaFX la encuentre en 'getClass().getResource' inmediatamente
            String buildPath = "build/classes/images";
            File carpetaBuild = new File(buildPath);
            if (carpetaBuild.exists()) {
                 Path destinoBuild = Paths.get(buildPath, source.getName());
                 Files.copy(source.toPath(), destinoBuild, StandardCopyOption.REPLACE_EXISTING);
                 System.out.println("Imagen copiada también a build: " + destinoBuild.toAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("Error al copiar la imagen: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Aviso", "No se pudo guardar la imagen en la carpeta, pero se guardará la referencia en la BD.", Alert.AlertType.WARNING);
        }
    }

    private void returnAction(ActionEvent event) {
        closeWindow();
    }

    private void buscarLibro() {
        String isbnText = txtISBN.getText().trim();
        if (isbnText.isEmpty()) return;

        try {
            long isbn = Long.parseLong(isbnText);
            libroActual = dao.getBookData(isbn);

            if (libroActual != null) {
                rellenarDatos(libroActual);
                if ("modify".equals(modo)) {
                    habilitarCampos(true);
                    txtISBN.setDisable(true);
                } else if ("delete".equals(modo)) {
                    btnConfirm.setDisable(false);
                }
            } else {
                mostrarAlerta("No encontrado", "No existe ese ISBN.", Alert.AlertType.WARNING);
                limpiarCampos();
                txtISBN.setText(isbnText);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "ISBN inválido.", Alert.AlertType.ERROR);
        }
    }

    private void rellenarDatos(Book libro) {
        txtTitle.setText(libro.getTitle());
        if (libro.getAuthor() != null) {
            txtNombreAutor.setText(libro.getAuthor().getName());
            txtApellidoAutor.setText(libro.getAuthor().getSurname());
        }
        txtPages.setText(String.valueOf(libro.getSheets()));
        txtStock.setText(String.valueOf(libro.getStock()));
        txtSinopsis.setText(libro.getSypnosis());
        txtPrice.setText(String.valueOf(libro.getPrice()));
        txtEditorial.setText(libro.getEditorial());

        String imgName = (libro.getCover() != null && !libro.getCover().isEmpty()) ? libro.getCover() : "default.png";
        try {
            java.io.InputStream stream = getClass().getResourceAsStream("/images/" + imgName);
            if (stream == null) stream = getClass().getResourceAsStream("/images/Book&Bugs_Logo.png");
            if (stream != null) idFrontPage.setImage(new Image(stream));
        } catch (Exception e) { 
        }
    }

    private void habilitarCampos(boolean b) {
        txtTitle.setDisable(!b);
        txtNombreAutor.setDisable(!b);
        txtApellidoAutor.setDisable(!b);
        txtPages.setDisable(!b);
        txtStock.setDisable(!b);
        txtSinopsis.setDisable(!b);
        txtPrice.setDisable(!b);
        txtEditorial.setDisable(!b);
        btnUploadFile.setDisable(!b);
    }

    private void limpiarCampos() {
        txtISBN.clear();
        txtTitle.clear();
        txtNombreAutor.clear();
        txtApellidoAutor.clear();
        txtPages.clear();
        txtStock.clear();
        txtSinopsis.clear();
        txtPrice.clear();
        txtEditorial.clear();
        idFrontPage.setImage(null);
        archivoPortada = null;
    }

    private void closeWindow() {
        try {
            // 1. Cargar la vista anterior (BookOptionWindow)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookOptionWindow.fxml"));
            Parent root = fxmlLoader.load();
            
            // 2. Crear nueva ventana
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestión de Libros"); // Título opcional
            stage.show();

            // 3. Cerrar la ventana actual (BookCRUDWindow)
            Stage currentStage = (Stage) btnReturn.getScene().getWindow();
            currentStage.close();

        } catch (IOException ex) {
            Logger.getLogger(BookCRUDWindowController.class.getName()).log(Level.SEVERE, "Error al volver al menú de opciones", ex);
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