/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import javafx.scene.control.ToggleGroup;

import exception.passwordequalspassword;

import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.ClassDAO;
import model.DBImplementation;
import model.Profile;
import model.User;
import model.UserSession;

/**
 * Controller for the SignUp window. Handles user registration and navigation to
 * login or main menu.
 */
public class SignUpWindowController implements Initializable {

    @FXML
    private TextField textFieldEmail, textFieldName, textFieldSurname, textFieldTelephone;
    @FXML
    private TextField textFieldCardN, textFieldPassword, textFieldCPassword, textFieldUsername;
    @FXML
    private RadioButton rButtonM, rButtonW, rButtonO;
    @FXML
    private Button buttonSignUp, buttonLogIn;

    private final ClassDAO dao = new DBImplementation();
    private ToggleGroup grupOp;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        grupOp = new ToggleGroup();
        rButtonM.setToggleGroup(grupOp);
        rButtonW.setToggleGroup(grupOp);
        rButtonO.setToggleGroup(grupOp);
    }

    /**
     * Navigates back to login window.
     */
    @FXML
    private void login() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/LogInWindow.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            Stage currentStage = (Stage) buttonLogIn.getScene().getWindow();
            currentStage.close();
        } catch (IOException ex) {
            Logger.getLogger(SignUpWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Signs up a new user and navigates to MenuWindow if successful.
     */
    @FXML
    private void signup() throws passwordequalspassword {
        // 1. Recogida de datos
        String email = textFieldEmail.getText();
        String name = textFieldName.getText();
        String surname = textFieldSurname.getText();
        String telephone = textFieldTelephone.getText();
        String pass = textFieldPassword.getText();
        String passC = textFieldCPassword.getText();
        String username = textFieldUsername.getText();
        String cardN = textFieldCardN.getText();

        // Validación de radio buttons
        String gender = "Other";
        if (grupOp.getSelectedToggle() != null) {
            if (rButtonM.isSelected()) {
                gender = "Man";
            } else if (rButtonW.isSelected()) {
                gender = "Woman";
            } else if (rButtonO.isSelected()) {
                gender = "Other";
            }
        }
        // 2. Validaciones simples (sin BD)
        if (!pass.equals(passC)) {
            throw new passwordequalspassword("Las contraseñas no coinciden");
        }

        if (username.isEmpty() || pass.isEmpty() || email.isEmpty()) {
            showAlert("Rellena los campos obligatorios");
            return;
        }

        // 3. CREAR EL OBJETO COMPLETO (Polimorfismo: User es un Profile)
        User userNew = new User();

        // Datos de Profile
        userNew.setUsername(username);
        userNew.setPassword(pass);
        userNew.setEmail(email);
        userNew.setName(name);
        userNew.setSurname(surname);
        userNew.setTelephone(telephone);

        // Datos de User
        userNew.setGender(gender);
        userNew.setCardNumber(cardN);

        // 3. LLAMADA AL DAO (Síncrona visualmente, Asíncrona en limpieza)
        try {
            // Esto guarda, hace commit y lanza el hilo basura.
            // Devuelve el control casi al instante.
            dao.signUp(userNew); 
            
            // Si no ha saltado error, es que todo fue bien.
            
            // 4. Auto-Login y cambio de ventana
            // Como ya tenemos el objeto 'nuevoUsuario' relleno, lo pasamos directo
            goToMainBookStore(userNew);
            
        } catch (Exception e) {
            // Si el DAO falla (ej: usuario duplicado), salta aquí
            showAlert("Error al registrar: " + e.getMessage());
        }
    }

    public void goToMainBookStore(Profile userNew) {
            // Nota: No hace falta hacer LogIn otra vez. Hibernate ya actualizó 
            // el objeto 'perfilRegistrado' con su ID. Ya está listo.
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MenuWindow.fxml"));
                Parent root = fxmlLoader.load();
                
                //Usamos UserSesion Singlenton
                UserSession.getInstance().setUser(userNew);
                
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();

                // Cerramos ventana actual
                Stage currentStage = (Stage) buttonSignUp.getScene().getWindow();
                currentStage.close();

            } catch (IOException ex) {
                Logger.getLogger(SignUpWindowController.class.getName()).log(Level.SEVERE, null, ex);
            }

    }

    private void showAlert(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.show();
    }
}
