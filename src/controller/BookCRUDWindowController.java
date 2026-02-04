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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
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
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

public class BookCRUDWindowController implements Initializable {

    @FXML
    private TextField txtISBN, txtStock, txtSinopsis, txtEditorial, txtTitle, txtPages, txtPrice, txtNombreAutor, txtApellidoAutor;
    @FXML
    private Button btnConfirm, btnReturn, btnUploadFile;
    @FXML
    private ImageView idFrontPage;

    private File archivoPortada;
    private String modo;
    private final ClassDAO dao = new DBImplementation();
    private Book libroActual;
    private final String RUTA_IMAGENES = "src/images/";

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

    // --- NUEVA ACCIÓN PARA EL MENÚ ---
    @FXML
    private void handleClearAction(ActionEvent event) {
        limpiarCampos();
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
    private void confirmAction(ActionEvent event) {
        try {
            if (txtISBN.getText().isEmpty() || txtTitle.getText().isEmpty()) {
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
                mostrarAlerta("Éxito", "Libro creado.", Alert.AlertType.INFORMATION);
            } else {
                dao.modifyBook(libro);
                mostrarAlerta("Éxito", "Libro modificado.", Alert.AlertType.INFORMATION);
            }
            limpiarCampos();
        } catch (Exception e) {
            mostrarAlerta("Error", "Datos inválidos.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void returnAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/OptionsAdmin.fxml"));
            Stage stage = (Stage) btnReturn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(BookCRUDWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void limpiarCampos() {
        txtISBN.clear(); txtTitle.clear(); txtNombreAutor.clear(); txtApellidoAutor.clear();
        txtPages.clear(); txtStock.clear(); txtSinopsis.clear(); txtPrice.clear();
        txtEditorial.clear(); 
        idFrontPage.setImage(null);
        archivoPortada = null; 
        libroActual = null;
        
        // Si estamos en modo modificar, tras limpiar debemos re-habilitar el ISBN para buscar otro
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

    private void buscarLibro() {
        try {
            Book libro = dao.getBookData(Long.parseLong(txtISBN.getText().trim()));
            if (libro != null) {
                this.libroActual = libro;
                rellenarDatos(libro);
                habilitarCampos(true);
                txtISBN.setDisable(true);
            }
        } catch (Exception e) { mostrarAlerta("Error", "No encontrado.", Alert.AlertType.WARNING); }
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

    @FXML private void handleExit(ActionEvent event) { Platform.exit(); System.exit(0); }
    @FXML private void handleAboutAction(ActionEvent event) { mostrarAlerta("Acerca de", "BookStore App v1.0", Alert.AlertType.INFORMATION); }
    @FXML private void handleReportAction(ActionEvent event) { abrirDoc("/documents/Manual_Usuario.pdf"); }

    @FXML private void handleInformeTecnico(ActionEvent event) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookstore", "root", "abcd*1234")) {
            InputStream is = getClass().getResourceAsStream("/reports/InformeTecnico.jrxml");
            JasperPrint jp = JasperFillManager.fillReport(JasperCompileManager.compileReport(is), null, con);
            JasperViewer.viewReport(jp, false);
        } catch (Exception e) { mostrarAlerta("Error", "Error en informe.", Alert.AlertType.ERROR); }
    }

    private void abrirDoc(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            File temp = File.createTempFile("Manual", ".pdf");
            Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Desktop.getDesktop().open(temp);
        } catch (Exception e) { }
    }

    private void mostrarAlerta(String t, String c, Alert.AlertType tp) {
        Alert a = new Alert(tp); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }

    private String guardarImagenEnDisco(File s) {
        try {
            String n = UUID.randomUUID().toString() + s.getName().substring(s.getName().lastIndexOf('.'));
            Files.copy(s.toPath(), java.nio.file.Paths.get(RUTA_IMAGENES, n), StandardCopyOption.REPLACE_EXISTING);
            return n;
        } catch (Exception e) { return "default.png"; }
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