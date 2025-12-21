package main;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utilities.HibernateUtil;

public class Main extends Application {

    /**
     * Starts the JavaFX application by loading the login window.
     *
     * @param stage the primary stage for this application
     * @throws Exception if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/MainBookStore.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Libreria che");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Main method to launch the JavaFX application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // --- PRUEBA DE CONEXIÓN ---
        System.out.println("Intentando conectar con Hibernate...");

        // Al llamar a getSessionFactory, Hibernate leerá el XML y creará las tablas
        HibernateUtil.getSessionFactory();

        System.out.println("¡Conexión establecida y tablas creadas (si no existían)!");

        // Una vez comprobado, ya puedes lanzar la app
        launch(args);

        // Al cerrar la app
        HibernateUtil.shutdown();
    }

}
