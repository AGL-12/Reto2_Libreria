package controller;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
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
import model.Profile;

public class BookCRUDWindowController implements Initializable {

    @FXML private Button btnConfirmar;
    @FXML private Button btnVolver;
    @FXML private TextField txtISBN;
    @FXML private TextField txtTitulo;
    @FXML private TextField txtIdAutor;
    @FXML private TextField txtHojas;
    @FXML private TextField txtStock;
    @FXML private TextField txtSinopsis;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtEditorial;
    @FXML private ImageView idPortada;
    @FXML private Button btnSubirArchivo;

    private Controller cont;
    private Profile profile;
    private String modo; // "create", "modify", "delete"
    private File archivoPortada; // Para guardar la referencia a la imagen

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

    // --- MÉTODOS DE CONFIGURACIÓN ---

    public void setCont(Controller cont) { this.cont = cont; }
    public void setProfile(Profile profile) { this.profile = profile; }

    /**
     * Configura la interfaz según la acción que vamos a realizar.
     * Este método se llama desde LibroOptionWindowController.
     */
    public void setModo(String modo) {
        this.modo = modo;
        
        switch (modo) {
            case "create":
                btnConfirmar.setText("Añadir Libro");
                limpiarCampos();
                habilitarCampos(true); // Todo editable
                break;
                
            case "modify":
                btnConfirmar.setText("Modificar Libro");
                limpiarCampos();
                habilitarCampos(false); // Bloqueado hasta que busque ISBN
                txtISBN.setDisable(false); // ISBN siempre editable para buscar
                txtISBN.setPromptText("Escribe ISBN y pulsa Enter");
                break;
                
            case "delete":
                btnConfirmar.setText("Eliminar Libro");
                limpiarCampos();
                habilitarCampos(false); // Todo bloqueado (solo lectura)
                txtISBN.setDisable(false);
                txtISBN.setPromptText("Escribe ISBN y pulsa Enter");
                // En delete, el botón confirmar se deshabilita hasta encontrar el libro
                btnConfirmar.setDisable(true); 
                break;
        }
    }

    // --- LÓGICA DE BÚSQUEDA ---

    private void buscarLibro() {
        String isbn = txtISBN.getText().trim();
        if (isbn.isEmpty()) return;

        // Llamamos al método que añadimos al Controller/DAO
        Book libro = cont.getBookData(Integer.parseInt(isbn)); // Asumiendo que ISBN es int según tu modelo

        if (libro != null) {
            rellenarDatos(libro);
            
            if ("modify".equals(modo)) {
                habilitarCampos(true);
                txtISBN.setDisable(true); // Bloqueamos ISBN para no cambiar la clave primaria
            } else if ("delete".equals(modo)) {
                btnConfirmar.setDisable(false); // Ahora sí podemos borrar
            }
        } else {
            mostrarAlerta("No encontrado", "No existe ningún libro con el ISBN: " + isbn, Alert.AlertType.WARNING);
            limpiarCampos();
        }
    }

    private void rellenarDatos(Book libro) {
        txtTitulo.setText(libro.getTitulo());
        // Manejo seguro del autor por si es null
        txtIdAutor.setText(String.valueOf(libro.getIdAuthor()));
        txtHojas.setText(String.valueOf(libro.getSheets()));
        txtStock.setText(String.valueOf(libro.getStock()));
        txtSinopsis.setText(libro.getSypnosis());
        txtPrecio.setText(String.valueOf(libro.getPrice()));
        txtEditorial.setText(libro.getEditorial());

        // Cargar imagen si existe
        if (libro.getCover() != null && !libro.getCover().isEmpty()) {
            try {
                // Intenta cargar desde recursos o ruta absoluta
                String ruta = "/images/" + libro.getCover(); 
                Image img = new Image(getClass().getResourceAsStream(ruta));
                idPortada.setImage(img);
            } catch (Exception e) {
                // Si falla, no rompemos la app, solo no mostramos imagen
                System.out.println("No se pudo cargar la imagen: " + libro.getCover());
            }
        }
    }

    // --- LÓGICA DE BOTONES ---

    @FXML
    private void confirmarAccion() {
        try {
            // Recoger datos (Validar que sean números)
            int isbn = Integer.parseInt(txtISBN.getText());
            
            if ("delete".equals(modo)) {
                if (cont.deleteBook(isbn)) {
                    mostrarAlerta("Éxito", "Libro eliminado correctamente", Alert.AlertType.INFORMATION);
                    cerrarVentana();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el libro", Alert.AlertType.ERROR);
                }
                return; // Salimos, no hace falta leer el resto de campos para borrar
            }

            // Para Create y Modify leemos el resto de campos
            String titulo = txtTitulo.getText();
            int idAutor = Integer.parseInt(txtIdAutor.getText());
            int hojas = Integer.parseInt(txtHojas.getText());
            int stock = Integer.parseInt(txtStock.getText());
            String sinopsis = txtSinopsis.getText();
            float precio = Float.parseFloat(txtPrecio.getText());
            String editorial = txtEditorial.getText();
            String nombrePortada = (archivoPortada != null) ? archivoPortada.getName() : "default.png";

            // Crear objeto Autor dummy (solo necesitamos el ID para la BD)
            Author autor = new Author(); 
            autor.setIdAuthor(idAutor);

            // Crear objeto Book
            Book book = new Book(isbn, nombrePortada, titulo, autor.getIdAuthor(), hojas, stock, sinopsis, precio, editorial, 0);

            boolean exito = false;
            if ("create".equals(modo)) {
                exito = cont.createBook(book);
            } else if ("modify".equals(modo)) {
                exito = cont.modifyBook(book);
            }

            if (exito) {
                mostrarAlerta("Éxito", "Operación realizada correctamente", Alert.AlertType.INFORMATION);
                cerrarVentana();
            } else {
                mostrarAlerta("Error", "No se pudo guardar en la base de datos", Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Error de formato", "Por favor revisa que los campos numéricos (ISBN, Stock, Precio...) sean correctos.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Error", "Ocurrió un error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void volver() {
        cerrarVentana();
    }

    // --- UTILIDADES ---

    private void habilitarCampos(boolean habilitar) {
        txtTitulo.setDisable(!habilitar);
        txtIdAutor.setDisable(!habilitar);
        txtHojas.setDisable(!habilitar);
        txtStock.setDisable(!habilitar);
        txtSinopsis.setDisable(!habilitar);
        txtPrecio.setDisable(!habilitar);
        txtEditorial.setDisable(!habilitar);
        btnSubirArchivo.setDisable(!habilitar);
        // El ISBN se controla aparte
    }

    private void limpiarCampos() {
        txtISBN.setText("");
        txtTitulo.setText("");
        txtIdAutor.setText("");
        txtHojas.setText("");
        txtStock.setText("");
        txtSinopsis.setText("");
        txtPrecio.setText("");
        txtEditorial.setText("");
        idPortada.setImage(null);
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnVolver.getScene().getWindow();
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
    @FXML
    private void arrastrarSobre(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void soltarImagen(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            File file = files.get(0);
            // Validar extensión simple
            if (file.getName().toLowerCase().endsWith(".png") || file.getName().toLowerCase().endsWith(".jpg")) {
                archivoPortada = file;
                idPortada.setImage(new Image(file.toURI().toString()));
                success = true;
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    private void subirPortada() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar portada");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        Stage stage = (Stage) btnSubirArchivo.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            archivoPortada = file;
            idPortada.setImage(new Image(file.toURI().toString()));
        }
    }
}