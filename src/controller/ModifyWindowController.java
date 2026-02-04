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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Admin;
import model.DBImplementation;
import model.Profile;
import model.User;
import model.UserSession;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

public class ModifyWindowController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ModifyWindowController.class.getName());
    private DBImplementation db = new DBImplementation(); 
    private Profile profileToModify;
    private ContextMenu contextMenu;

    @FXML private GridPane rootPane;
    @FXML private Label LabelUsername, LabelEmail;
    @FXML private TextField TextField_Name, TextField_Surname, TextField_Telephone;
    @FXML private PasswordField TextField_NewPass, TextField_ConfirmPass;
    @FXML private ComboBox<User> comboUsers;
    @FXML private Button Button_SaveChanges, Button_Cancel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupUserData();
        initContextMenu();
    }

    private void initContextMenu() {
        contextMenu = new ContextMenu();
        
        MenuItem itemSave = new MenuItem("Guardar Cambios");
        itemSave.setOnAction(e -> save(new ActionEvent(Button_SaveChanges, null)));
        
        MenuItem itemCancel = new MenuItem("Cancelar/Volver");
        itemCancel.setOnAction(e -> cancel(new ActionEvent(Button_Cancel, null)));
        
        SeparatorMenuItem sep = new SeparatorMenuItem();
        
        MenuItem itemManual = new MenuItem("Manual de Usuario");
        itemManual.setOnAction(this::handleReportAction);

        contextMenu.getItems().addAll(itemSave, itemCancel, sep, itemManual);

        rootPane.setOnContextMenuRequested(event -> {
            contextMenu.show(rootPane, event.getScreenX(), event.getScreenY());
        });
        rootPane.setOnMousePressed(event -> {
                if (event.isPrimaryButtonDown() && contextMenu.isShowing()) {
                    contextMenu.hide();
                }
            });
    }

    private void setupUserData() {
        Profile loggedProfile = UserSession.getInstance().getUser();
        if (loggedProfile instanceof Admin) {
            comboUsers.setVisible(true);
            loadUsers();
            comboUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) fillFields(newVal);
            });
        } else {
            comboUsers.setVisible(false);
            fillFields(loggedProfile);
        }
    }

    private void loadUsers() {
        try {
            ObservableList<User> users = FXCollections.observableArrayList(db.getAllUsers());
            comboUsers.setItems(users);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error cargando usuarios", e);
        }
    }

    private void fillFields(Profile p) {
        this.profileToModify = p;
        LabelUsername.setText(p.getUsername());
        LabelEmail.setText(p.getEmail());
        TextField_Name.setText(p.getName());
        TextField_Surname.setText(p.getSurname());
        TextField_Telephone.setText(p.getTelephone());
    }

    @FXML
    private void save(ActionEvent event) {
        if (!TextField_NewPass.getText().equals(TextField_ConfirmPass.getText())) {
            new Alert(Alert.AlertType.ERROR, "Las contraseñas no coinciden").show();
            return;
        }

        try {
            profileToModify.setName(TextField_Name.getText());
            profileToModify.setSurname(TextField_Surname.getText());
            profileToModify.setTelephone(TextField_Telephone.getText());
            if (!TextField_NewPass.getText().isEmpty()) {
                profileToModify.setPassword(TextField_NewPass.getText());
            }

            db.modificarUser(profileToModify);
            new Alert(Alert.AlertType.INFORMATION, "Usuario actualizado correctamente").show();
            handleNavigation(event); 

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Error al guardar los cambios").show();
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        handleNavigation(event);
    }

    // --- MÉTODOS DE MENÚ BAR ---
    @FXML private void handleExit(ActionEvent event) { Platform.exit(); System.exit(0); }
    
    @FXML private void handleAboutAction(ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION, "BookStore App v1.0").show();
    }

    @FXML private void handleReportAction(ActionEvent event) {
        try {
            InputStream is = getClass().getResourceAsStream("/documents/Manual_Usuario.pdf");
            if (is != null) {
                File temp = File.createTempFile("Manual", ".pdf");
                Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Desktop.getDesktop().open(temp);
            }
        } catch (Exception e) { LOGGER.log(Level.SEVERE, "Error manual", e); }
    }

    @FXML
    private void handleInformeTecnico(ActionEvent event) {
        Connection con = null;
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookstore?useSSL=false&serverTimezone=UTC", "root", "abcd*1234");
            InputStream reportStream = getClass().getResourceAsStream("/reports/InformeTecnico.jrxml");
            if (reportStream != null) {
                JasperReport jr = JasperCompileManager.compileReport(reportStream);
                JasperPrint jp = JasperFillManager.fillReport(jr, null, con);
                JasperViewer.viewReport(jp, false);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar informe técnico", e);
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) { }
        }
    }

    private void handleNavigation(ActionEvent event) {
        try {
            Profile loggedProfile = UserSession.getInstance().getUser();
            String fxmlPath = (loggedProfile instanceof Admin) ? "/view/OptionsAdmin.fxml" : "/view/MenuWindow.fxml";
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error navegación", ex);
        }
    }
}