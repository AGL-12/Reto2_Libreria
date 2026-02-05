package controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Author;
import model.Book;
import model.ClassDAO;
import model.DBImplementation;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import util.LogInfo;

/**
 * Controlador de la ventana de gestión de libros.
 * Hace la operación de crear y modificar libros.
 * Ventana solo accesible para el administrador.
 * * @author unai azkorra
 * @version 1.2
 */
public class BookCRUDWindowController implements Initializable {

    @FXML private GridPane rootPane;
    @FXML private TextField txtISBN, txtStock, txtSinopsis, txtEditorial, txtTitle, txtPages, txtPrice, txtNombreAutor, txtApellidoAutor;
    @FXML private Button btnConfirm, btnReturn, btnUploadFile;
    @FXML private ImageView idFrontPage;

    private File archivoPortada;
    private String modo;
    private final ClassDAO dao = new DBImplementation();
    private Book libroActual;
    private final String RUTA_IMAGENES = "src/images/";
    private ContextMenu globalMenu;
    
    // Bandera para evitar que la alerta de búsqueda se dispare múltiples veces (foco + enter)
    private boolean buscando = false;

    /**
     * Metodo para inicializar la ventana con la configuración necesaria.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnConfirm.setOnAction(this::confirmAction);
        btnReturn.setOnAction(this::returnAction);
        
        initGlobalContextMenu();

        // Lógica de búsqueda unificada para evitar duplicidad de alertas
        txtISBN.setOnAction(event -> {
            if (!"create".equals(modo)) ejecutarBusquedaSegura();
        });

        txtISBN.focusedProperty().addListener((obs, oldVal, newVal) -> {
            // Si pierde el foco (newVal == false), no está vacío y no estamos en modo crear
            if (!newVal && !txtISBN.getText().isEmpty() && !"create".equals(modo)) {
                ejecutarBusquedaSegura();
            }
        });

        // REQUERIMIENTO: Iniciar siempre en modo creación
        setModo("create");
        LogInfo.getInstance().logInfo("Ventana CRUD de Libros cargada en modo CREAR.");
    }

    /**
     * Evita que la alerta se abra varias veces sincronizando los eventos de Foco y Action.
     */
    private void ejecutarBusquedaSegura() {
        if (buscando) return;
        buscando = true;
        
        Platform.runLater(() -> {
            buscarLibro();
            buscando = false;
        });
    }

    /**
     * Establece el modo de la ventana.
     * @param modo "create" o "modify"
     */
    public void setModo(String modo) {
        this.modo = modo;
        limpiarCampos();

        if ("create".equals(modo)) {
            btnConfirm.setText("Añadir Libro");
            habilitarCampos(true);
            txtISBN.setDisable(false); 
        } else if ("modify".equals(modo)) {
            btnConfirm.setText("Modificar Libro");
            habilitarCampos(false);
            txtISBN.setDisable(false);
        }
    }

    @FXML
    private void handleExit(ActionEvent event) { 
        LogInfo.getInstance().logInfo("Cerrando aplicación desde el menú.");
        Platform.exit();
        System.exit(0);
    }
    
    @FXML
    private void handleCreateAction(ActionEvent event) {
        setModo("create");
    }

    @FXML
    private void handleModifyAction(ActionEvent event) {
        setModo("modify");
    }

    @FXML
    private void handleClearAction(ActionEvent event) {
        limpiarCampos();
    }

    @FXML
    private void handleAboutAction(ActionEvent event) {
        mostrarAlerta("Acerca de", "BookStore App v1.0\nDesarrollado en JavaFX.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleReportAction(ActionEvent event) {
        abrirDoc("/documents/Manual_Usuario.pdf");
    }

    @FXML
    private void handleInformeTecnico(ActionEvent event) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookstore", "root", "abcd*1234")) {
            InputStream is = getClass().getResourceAsStream("/reports/InformeTecnico.jrxml");
            JasperPrint jp = JasperFillManager.fillReport(JasperCompileManager.compileReport(is), null, con);
            JasperViewer.viewReport(jp, false);
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al generar Jasper", e);
            mostrarAlerta("Error", "No se pudo generar el informe técnico.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void confirmAction(ActionEvent event) {
        try {
            if (txtISBN.getText().isEmpty() || txtTitle.getText().isEmpty()) {
                mostrarAlerta("Campos Obligatorios", "El ISBN y el Título no pueden estar vacíos.", Alert.AlertType.WARNING);
                return;
            }

            Author autor = dao.getOrCreateAuthor(txtNombreAutor.getText().trim(), txtApellidoAutor.getText().trim());
            String nombrePortada = (archivoPortada != null) ? guardarImagenEnDisco(archivoPortada) : (libroActual != null ? libroActual.getCover() : "default.png");

            Book libro = new Book(
                Long.parseLong(txtISBN.getText()),
                nombrePortada,
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
                dao.createBook(libro);
                mostrarAlerta("Éxito", "Libro añadido correctamente.", Alert.AlertType.INFORMATION);
            } else {
                dao.modifyBook(libro);
                mostrarAlerta("Éxito", "Libro actualizado correctamente.", Alert.AlertType.INFORMATION);
            }
            
            setModo(this.modo); 
            
        } catch (NumberFormatException e) {
            mostrarAlerta("Error de formato", "Páginas, Stock y Precio deben ser números.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error en Confirmación", e);
            mostrarAlerta("Error", "Ocurrió un problema con la base de datos.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void returnAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/OptionsAdmin.fxml"));
            Stage stage = (Stage) btnReturn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException ex) {
            LogInfo.getInstance().logSevere("Error al volver al menú principal", ex);
        }
    }

    private void buscarLibro() {
        try {
            Book libro = dao.getBookData(Long.parseLong(txtISBN.getText().trim()));
            if (libro != null) {
                this.libroActual = libro;
                rellenarDatos(libro);
                habilitarCampos(true);
                txtISBN.setDisable(true);
            } else {
                mostrarAlerta("No encontrado", "No existe un libro con ese ISBN.", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "El ISBN debe ser un número válido.", Alert.AlertType.WARNING);
        }
    }

    private void limpiarCampos() {
        txtISBN.clear(); txtTitle.clear(); txtNombreAutor.clear(); txtApellidoAutor.clear();
        txtPages.clear(); txtStock.clear(); txtSinopsis.clear(); txtPrice.clear();
        txtEditorial.clear(); idFrontPage.setImage(null);
        archivoPortada = null; libroActual = null;
        txtISBN.setDisable(false);
    }

    private void habilitarCampos(boolean b) {
        txtTitle.setDisable(!b); txtNombreAutor.setDisable(!b); txtApellidoAutor.setDisable(!b);
        txtPages.setDisable(!b); txtStock.setDisable(!b); txtSinopsis.setDisable(!b);
        txtPrice.setDisable(!b); txtEditorial.setDisable(!b); btnUploadFile.setDisable(!b);
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
    }

    private void abrirDoc(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new Exception("Recurso no encontrado: " + path);
            File temp = File.createTempFile("Manual", ".pdf");
            java.nio.file.Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Desktop.getDesktop().open(temp);
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al abrir manual", e);
        }
    }

    private String guardarImagenEnDisco(File s) {
        try {
            String n = UUID.randomUUID().toString() + s.getName().substring(s.getName().lastIndexOf('.'));
            java.nio.file.Files.copy(s.toPath(), java.nio.file.Paths.get(RUTA_IMAGENES, n), StandardCopyOption.REPLACE_EXISTING);
            return n;
        } catch (Exception e) { return "default.png"; }
    }

    private void initGlobalContextMenu() {
        globalMenu = new ContextMenu();
        MenuItem itemLimpiar = new MenuItem("Limpiar");
        itemLimpiar.setOnAction(e -> handleClearAction(null));
        MenuItem itemExit = new MenuItem("Salir");
        itemExit.setOnAction(e -> handleExit(null));
        globalMenu.getItems().addAll(itemLimpiar, new SeparatorMenuItem(), itemExit);
        
        rootPane.setOnContextMenuRequested(e -> globalMenu.show(rootPane, e.getScreenX(), e.getScreenY()));
    }

    private void mostrarAlerta(String t, String c, Alert.AlertType tp) {
        Alert a = new Alert(tp); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }

    @FXML private void dragOver(DragEvent e) { if (e.getDragboard().hasFiles()) e.acceptTransferModes(TransferMode.COPY); e.consume(); }
    
    @FXML private void dropImage(DragEvent e) { 
        Dragboard db = e.getDragboard(); 
        if (db.hasFiles()) { 
            archivoPortada = db.getFiles().get(0); 
            idFrontPage.setImage(new Image(archivoPortada.toURI().toString())); 
        }
        e.setDropCompleted(db.hasFiles()); e.consume(); 
    }
    
    @FXML private void uploadFrontPage(ActionEvent e) {
        FileChooser fc = new FileChooser(); 
        File f = fc.showOpenDialog(btnUploadFile.getScene().getWindow());
        if (f != null) { 
            archivoPortada = f; 
            idFrontPage.setImage(new Image(f.toURI().toString())); 
        }
    }
}