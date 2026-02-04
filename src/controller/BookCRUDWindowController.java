package controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import util.LogInfo;

/**
 * Controlador de la ventana de gestión (CRUD) de libros.
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnConfirm.setOnAction(this::confirmAction);
        btnReturn.setOnAction(this::returnAction);
        initGlobalContextMenu();

        txtISBN.setOnAction(event -> {
            if (!"create".equals(modo)) buscarLibro();
        });

        txtISBN.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtISBN.getText().isEmpty() && !"create".equals(modo)) {
                buscarLibro();
            }
        });
        LogInfo.getInstance().logInfo("Ventana CRUD de Libros inicializada.");
    }

    public void setModo(String modo) {
        this.modo = modo;
        if ("create".equals(modo)) {
            btnConfirm.setText("Añadir Libro");
            habilitarCampos(true);
        } else if ("modify".equals(modo)) {
            btnConfirm.setText("Modificar Libro");
            habilitarCampos(false);
            txtISBN.setDisable(false);
        }
        limpiarCampos();
    }

    @FXML
    private void handleClearAction(ActionEvent event) {
        LogInfo.getInstance().logInfo("Limpieza de campos solicitada en CRUD Libros.");
        limpiarCampos();
    }

    @FXML
    private void handleCreateAction(ActionEvent event) {
        LogInfo.getInstance().logInfo("Modo 'Crear' activado en CRUD Libros.");
        setModo("create");
    }

    @FXML
    private void handleModifyAction(ActionEvent event) {
        LogInfo.getInstance().logInfo("Modo 'Modificar' activado en CRUD Libros.");
        setModo("modify");
    }

    @FXML
    private void confirmAction(ActionEvent event) {
        try {
            if (txtISBN.getText().isEmpty() || txtTitle.getText().isEmpty()) {
                LogInfo.getInstance().logWarning("Intento de guardado de libro incompleto (falta ISBN o Título).");
                mostrarAlerta("Error", "ISBN y Título son obligatorios.", Alert.AlertType.WARNING);
                return;
            }

            Author autor = dao.getOrCreateAuthor(txtNombreAutor.getText().trim(), txtApellidoAutor.getText().trim());
            String portada = (archivoPortada != null) ? guardarImagenEnDisco(archivoPortada) : (libroActual != null ? libroActual.getCover() : "default.png");

            Book libro = new Book(Long.parseLong(txtISBN.getText()), portada, txtTitle.getText(), autor, 
                    Integer.parseInt(txtPages.getText()), Integer.parseInt(txtStock.getText()), 
                    txtSinopsis.getText(), Float.parseFloat(txtPrice.getText()), txtEditorial.getText(), 0f);

            if ("create".equals(modo)) {
                dao.createBook(libro);
                LogInfo.getInstance().logInfo("Libro creado con éxito: ISBN " + libro.getISBN());
                mostrarAlerta("Éxito", "Libro creado correctamente.", Alert.AlertType.INFORMATION);
            } else {
                dao.modifyBook(libro);
                LogInfo.getInstance().logInfo("Libro modificado con éxito: ISBN " + libro.getISBN());
                mostrarAlerta("Éxito", "Libro modificado correctamente.", Alert.AlertType.INFORMATION);
            }
            limpiarCampos();
        } catch (Exception e) {
            LogInfo.getInstance().logSevere("Error al confirmar la operación de libro en BD", e);
            mostrarAlerta("Error", "Datos inválidos o error en la base de datos.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void returnAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/OptionsAdmin.fxml"));
            Stage stage = (Stage) btnReturn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            LogInfo.getInstance().logInfo("Regresando al menú de administración.");
        } catch (IOException ex) {
            LogInfo.getInstance().logSevere("Error al intentar volver a OptionsAdmin", ex);
        }
    }

    @FXML
    private void handleInformeTecnico(ActionEvent event) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookstore", "root", "abcd*1234")) {
            InputStream is = getClass().getResourceAsStream("/reports/InformeTecnico.jrxml");
            JasperPrint jp = JasperFillManager.fillReport(JasperCompileManager.compileReport(is), null, con);
            JasperViewer.viewReport(jp, false);
            LogInfo.getInstance().logInfo("Informe técnico generado desde CRUD Libros.");
        } catch (Exception e) { 
            LogInfo.getInstance().logSevere("Error al generar el informe técnico Jasper", e);
            mostrarAlerta("Error", "No se pudo generar el informe técnico.", Alert.AlertType.ERROR); 
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
                LogInfo.getInstance().logInfo("Búsqueda exitosa de libro ISBN: " + txtISBN.getText());
            }
        } catch (Exception e) { 
            LogInfo.getInstance().logWarning("ISBN no encontrado en el sistema: " + txtISBN.getText());
            mostrarAlerta("Error", "ISBN no encontrado.", Alert.AlertType.WARNING); 
        }
    }

    private String guardarImagenEnDisco(File s) {
        try {
            String n = UUID.randomUUID().toString() + s.getName().substring(s.getName().lastIndexOf('.'));
            java.nio.file.Files.copy(s.toPath(), java.nio.file.Paths.get(RUTA_IMAGENES, n), StandardCopyOption.REPLACE_EXISTING);
            LogInfo.getInstance().logInfo("Imagen de portada guardada físicamente como: " + n);
            return n;
        } catch (Exception e) { 
            LogInfo.getInstance().logSevere("Error al intentar guardar la imagen en el servidor local", e);
            return "default.png"; 
        }
    }

    private void limpiarCampos() {
        txtISBN.clear(); txtTitle.clear(); txtNombreAutor.clear(); txtApellidoAutor.clear();
        txtPages.clear(); txtStock.clear(); txtSinopsis.clear(); txtPrice.clear();
        txtEditorial.clear(); idFrontPage.setImage(null);
        archivoPortada = null; libroActual = null;
        if ("modify".equals(modo)) {
            txtISBN.setDisable(false);
            habilitarCampos(false);
        }
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

    private void initGlobalContextMenu() {
        globalMenu = new ContextMenu();
        globalMenu.setAutoHide(true);
        MenuItem itemLimpiar = new MenuItem("Limpiar Campos");
        itemLimpiar.setOnAction(e -> handleClearAction(null));
        MenuItem itemInforme = new MenuItem("Generar Informe Técnico");
        itemInforme.setOnAction(this::handleInformeTecnico);
        MenuItem itemManual = new MenuItem("Manual de Usuario");
        itemManual.setOnAction(e -> handleReportAction(null));
        MenuItem itemExit = new MenuItem("Salir");
        itemExit.setOnAction(e -> handleExit(null));
        globalMenu.getItems().addAll(itemLimpiar, new SeparatorMenuItem(), itemInforme, itemManual, new SeparatorMenuItem(), itemExit);
        if (rootPane != null) {
            rootPane.setOnContextMenuRequested(event -> {
                globalMenu.show(rootPane, event.getScreenX(), event.getScreenY());
                event.consume();
            });
            rootPane.setOnMousePressed(event -> {
                if (event.isPrimaryButtonDown() && globalMenu.isShowing()) {
                    globalMenu.hide();
                }
            });
        }
    }

    @FXML private void handleExit(ActionEvent event) { 
        LogInfo.getInstance().logInfo("Cierre de aplicación desde CRUD Libros.");
        Platform.exit(); 
        System.exit(0); 
    }
    
    @FXML private void handleAboutAction(ActionEvent event) { 
        mostrarAlerta("Acerca de", "BookStore App v1.0", Alert.AlertType.INFORMATION); 
    }
    
    @FXML private void handleReportAction(ActionEvent event) { 
        abrirDoc("/documents/Manual_Usuario.pdf"); 
    }

    private void abrirDoc(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            File temp = File.createTempFile("Manual", ".pdf");
            java.nio.file.Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Desktop.getDesktop().open(temp);
            LogInfo.getInstance().logInfo("Manual de usuario visualizado.");
        } catch (Exception e) { 
            LogInfo.getInstance().logSevere("Fallo al abrir el documento de ayuda", e);
        }
    }

    private void mostrarAlerta(String t, String c, Alert.AlertType tp) {
        Alert a = new Alert(tp); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }

    @FXML private void dragOver(DragEvent e) { if (e.getDragboard().hasFiles()) e.acceptTransferModes(TransferMode.COPY); e.consume(); }
    @FXML private void dropImage(DragEvent e) { 
        Dragboard db = e.getDragboard(); 
        if (db.hasFiles()) { archivoPortada = db.getFiles().get(0); idFrontPage.setImage(new Image(archivoPortada.toURI().toString())); }
        e.setDropCompleted(db.hasFiles()); e.consume(); 
    }
    @FXML private void uploadFrontPage(ActionEvent e) {
        FileChooser fc = new FileChooser(); File f = fc.showOpenDialog(btnUploadFile.getScene().getWindow());
        if (f != null) { archivoPortada = f; idFrontPage.setImage(new Image(f.toURI().toString())); }
    }
}