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
import java.util.UUID;
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

/**
 * Controlador de la ventana de gestión (CRUD) de libros.
 * Permite crear y modificar
 * la subida de portadas mediante archivos o Drag & Drop.
 * * @author unai azkorra
 * @version 1.0
 */
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
    @FXML
    private TextField txtNombreAutor;
    @FXML
    private TextField txtApellidoAutor;

    // --- VARIABLES LÓGICAS ---
    private File archivoPortada;
    private String modo;
    private final ClassDAO dao = new DBImplementation();
    private Book libroActual;

    // RUTA: Guardar dentro de src/images/
    private final String RUTA_IMAGENES = "src/images/";

    /**
     * Inicializa los componentes de la ventana y configura los listeners
     * de búsqueda automática por ISBN.
     */
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnConfirm.setOnAction(this::confirmAction);
        btnReturn.setOnAction(this::returnAction);

        // Listener para buscar libro al pulsar Enter en ISBN
        txtISBN.setOnAction(event -> {
            if (!"create".equals(modo)) {
                buscarLibro();
            }
        });

        // Listener para buscar libro al perder el foco del campo ISBN
        txtISBN.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtISBN.getText().isEmpty() && !"create".equals(modo)) {
                buscarLibro();
            }
        });
    }

    /**
     * Configura la interfaz según el modo establecido.
     * * @param modo El modo de la ventana: "create" o "modify".
     */
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
                txtISBN.setDisable(false); // Se habilita para buscar primero
                break;
            case "delete":
                btnConfirm.setText("Eliminar Libro");
                limpiarCampos();
                habilitarCampos(false);
                txtISBN.setDisable(false);
                txtISBN.setPromptText("Escribe ISBN y pulsa Enter");
                // En delete, el botón confirmar se deshabilita hasta encontrar el libro
                btnConfirm.setDisable(true);
                break;
        }
    }

    // --- MÉTODOS DE EVENTOS FXML (DRAG & DROP Y UPLOAD) ---
    /**
     * Gestiona el evento de arrastre sobre el área de la interfaz. 
     * Verifica si el contenido arrastrado contiene archivos para aceptar la transferencia.
     * @param event El evento DragEvent capturado por la interfaz.
     */
    @FXML
    private void dragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    /**
     * Procesa la caída de un archivo de imagen. 
     * Valida que el archivo tenga una extensión permitida (.png, .jpg, .jpeg) y 
     * actualiza la vista previa en el ImageView.
     * @param event El evento de DropEvent que contiene los archivos.
     */
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

    /**
     * Abre un explorador de archivos para seleccionar una portada . 
     * Configura filtros de extensión para asegurar que solo se seleccionen formatos de imagen válidos.
     * @param event El evento de acción disparado por el botón "Subir Archivo".
     */
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

    // --- LÓGICA PRINCIPAL ---
    /**
     * Procesa la acción de confirmación (Crear o Modificar)
     * validando los campos de entrada.
     * * @param event El evento de acción disparado por el botón.
     */
    private void confirmAction(ActionEvent event) {
        try {
            if (txtISBN.getText().trim().isEmpty()) {
                mostrarAlerta("Datos faltantes", "El ISBN es obligatorio.", Alert.AlertType.WARNING);
                return;
            }

            long isbn = Long.parseLong(txtISBN.getText());


            // Validación de campos vacíos para Crear/Modificar
            if (txtTitle.getText().isEmpty()
                    || txtNombreAutor.getText().isEmpty()
                    || txtApellidoAutor.getText().isEmpty()
                    || txtPages.getText().isEmpty()
                    || txtStock.getText().isEmpty()
                    || txtPrice.getText().isEmpty()) {

                mostrarAlerta("Datos faltantes", "Rellena los campos obligatorios.", Alert.AlertType.WARNING);
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
                // Si hay nueva imagen, la guardamos y obtenemos el nuevo nombre
                nombrePortada = guardarImagenEnDisco(archivoPortada);
            } else {
                // Si no hay nueva imagen, mantenemos la anterior 
                if (libroActual != null && libroActual.getCover() != null) {
                    nombrePortada = libroActual.getCover();
                } else {
                    nombrePortada = "default.png";
                }
            }

            // Gestión del Autor
            Author autor = dao.getOrCreateAuthor(nombreAutor, apellidoAutor);

            // Creación del objeto Libro
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
            mostrarAlerta("Error", "Revisa los campos numéricos (ISBN, Páginas, Stock, Precio).", Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Copia el archivo seleccionado a la carpeta src/images del proyecto usando
     * UUID.
     */
    private String guardarImagenEnDisco(File source) {
        try {
            // Generar nombre único para evitar sobreescribir otras portadas
            String ext = "";
            int i = source.getName().lastIndexOf('.');
            if (i > 0) {
                ext = source.getName().substring(i);
            }
            String nombreFinal = UUID.randomUUID().toString() + ext;

            // 1. Definimos la carpeta de destino: "src/images"
            File carpeta = new File(RUTA_IMAGENES);
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            // 2. Copiamos a src/images (Persistencia)
            Path destinoPath = Paths.get(RUTA_IMAGENES, nombreFinal);
            Files.copy(source.toPath(), destinoPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Imagen guardada en: " + destinoPath.toAbsolutePath());

            // 3. Copiamos también a "build/classes/images" (Para verla al instante sin recompilar)
            String buildPath = "build/classes/images";
            File carpetaBuild = new File(buildPath);
            if (carpetaBuild.exists()) {
                Path destinoBuild = Paths.get(buildPath, nombreFinal);
                Files.copy(source.toPath(), destinoBuild, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Imagen copiada también a build: " + destinoBuild.toAbsolutePath());
            }

            return nombreFinal; // Retornamos el nombre generado para la BD

        } catch (IOException e) {
            System.err.println("Error al copiar la imagen: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * metodo que se utiliza para volver a la pagina anterior cerrando la ventana actual
     * @param event disparado por el boton de return
     */
    private void returnAction(ActionEvent event) {
        closeWindow();
    }
    /**
     * metodo que se usa en el modo de modificar, introduciendo un ISBN
     * al hacer "intro" los datos del libro se autocompletaran en los inputs
     */
    private void buscarLibro() {
        String isbnText = txtISBN.getText().trim();
        if (isbnText.isEmpty()) {
            return;
        }
        try {
            long isbn = Long.parseLong(isbnText);
            Book libro = dao.getBookData(isbn);

            if (libro != null) {
                this.libroActual = libro;
                rellenarDatos(libro);
                if ("modify".equals(modo)) {
                    habilitarCampos(true);
                    txtISBN.setDisable(true); // Bloqueamos ISBN tras encontrarlo
                } else if ("delete".equals(modo)) {
                    btnConfirm.setDisable(false);
                }
            } else {
                mostrarAlerta("No encontrado", "No existe ese ISBN.", Alert.AlertType.WARNING);
                limpiarCampos();
                // Restauramos el texto del ISBN para que el usuario pueda corregirlo
                txtISBN.setText(isbnText);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "ISBN inválido o error de conexión.", Alert.AlertType.ERROR);
        }
    }

    /**
     * recive del metodo de buscar libro un libro y rellena los inputs
     * @param libro 
     */
    private void rellenarDatos(Book libro) {
        txtTitle.setText(libro.getTitle());
        // Manejo seguro del autor por si es null
        //txtIdAutor.setText(String.valueOf(libro.getIdAuthor()));
        if (libro.getAuthor() != null) {
            txtNombreAutor.setText(libro.getAuthor().getName());
            txtApellidoAutor.setText(libro.getAuthor().getSurname());
        }
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
    
    /**
     * recibe un parametro boolean para habilitar los campos del formulario
     * en el modo modificar hasta no introducir el isbn de un libro no se habilitan los cammpos
     * @param b 
     */
    // --- UTILIDADES ---
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
        // El ISBN se gestiona por separado
    }
    /**
     * Limpia todos los campos de texto, reinicia la imagen de portada y 
     * anula las variables del libro actual para dejar la ventana lista para una nueva operación.
     */
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
        libroActual = null;
    }

    /**
     * cierra la ventana y abre la ventana anterior 
     */
    private void closeWindow() {
        try {
            // 1. Cargar la vista anterior (BookOptionWindow)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookOptionWindow.fxml"));
            Parent root = fxmlLoader.load();

            // 2. Crear nueva ventana
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestión de Libros");
            stage.show();

            // 3. Cerrar la ventana actual
            Stage currentStage = (Stage) btnReturn.getScene().getWindow();
            currentStage.close();

        } catch (IOException ex) {
            Logger.getLogger(BookCRUDWindowController.class.getName()).log(Level.SEVERE, "Error al volver al menú de opciones", ex);
        }
    }
    /**
     * se usa para mostrar errores al usuario y guiarlo por la aplicacion
     * @param titulo
     * @param contenido
     * @param tipo 
     */
    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
