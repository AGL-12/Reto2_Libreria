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
import model.Profile;
import model.User;
import threads.SignUpThread;

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

    //private Controller cont;
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
            mostrarAlerta("Rellena los campos obligatorios");
            return;
        }

        // 3. CREAR EL OBJETO COMPLETO (Polimorfismo: User es un Profile)
        // Usamos User porque tiene campos específicos (gender, card)
        User nuevoUsuario = new User();

        // Datos de Profile
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setPassword(pass);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setName(name);
        nuevoUsuario.setSurname(surname);
        nuevoUsuario.setTelephone(telephone);

        // Datos de User
        nuevoUsuario.setGender(gender);
        nuevoUsuario.setCardNumber(cardN);

        // 4. BLOQUEO VISUAL
        buttonSignUp.setDisable(true);

        // 5. LANZAR HILO
        // Le pasamos el objeto ya montado
        SignUpThread hilo = new SignUpThread(nuevoUsuario, this);
        hilo.start();
    }

    // --- CALLBACK: LO QUE LLAMA EL HILO AL TERMINAR ---
    public void finalizarRegistro(boolean exito, String mensajeError, Profile perfilRegistrado) {
        buttonSignUp.setDisable(false);

        if (exito) {
            // REGISTRO EXITOSO -> ABRIR MENU DIRECTAMENTE
            // Nota: No hace falta hacer LogIn otra vez. Hibernate ya actualizó 
            // el objeto 'perfilRegistrado' con su ID. Ya está listo.
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MenuWindow.fxml"));
                Parent root = fxmlLoader.load();

                MenuWindowController controllerWindow = fxmlLoader.getController();
                controllerWindow.setUsuario(perfilRegistrado); // Pasamos el usuario ya logueado

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();

                // Cerramos ventana actual
                Stage currentStage = (Stage) buttonSignUp.getScene().getWindow();
                currentStage.close();

            } catch (IOException ex) {
                Logger.getLogger(SignUpWindowController.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            // REGISTRO FALLIDO
            mostrarAlerta("Error en el registro: " + mensajeError);
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.show();
    }
}
