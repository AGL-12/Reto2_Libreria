package main;

import controller.MainBookStoreController;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Admin;
import model.Author;
import model.Book;
import model.Commentate;
import model.User;
import model.UserSession;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
        FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/view/MainBookStore.fxml"));
        Parent root = fxmlloader.load();
        MainBookStoreController main = fxmlloader.getController();
        main.headerController.setMode(UserSession.getInstance().getUser(), null);
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

        // PRECARGAR DATOS (Si la BD está vacía)
        preloadData();
        // Una vez comprobado, ya puedes lanzar la app
        launch(args);

        // Al cerrar la app
        HibernateUtil.shutdown();
    }

    /**
     * Método estático para rellenar la BD con datos de prueba
     */
    private static void preloadData() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            // --- PASO 0: EVITAR DUPLICADOS ---
            // Comprobamos si ya existe el admin. Si existe, no hacemos nada.
            Long count = (Long) session.createQuery("SELECT count(p) FROM Profile p WHERE p.username = :u")
                    .setParameter("u", "admin1")
                    .uniqueResult();

            if (count > 0) {
                System.out.println(">> La Base de Datos ya tiene datos. Omitiendo precarga.");
                return;
            }

            System.out.println(">> BD vacía detectada. Insertando datos de prueba...");
            tx = session.beginTransaction();

            // ==========================================
            // 1. CREAR USUARIOS (User y Admin)
            // ==========================================
            // --- ADMIN ---
            Admin admin = new Admin();
            admin.setUsername("admin1");
            admin.setPassword("1234");
            admin.setEmail("admin@libreria.com");
            admin.setName("Jefe");
            admin.setSurname("Supremo");
            admin.setTelephone("600111222");
            admin.setCurrentAccount("ES00-1234-5678"); // Campo específico de Admin
            session.save(admin);

            // --- USER ---
            User user1 = new User();
            user1.setUsername("user1");
            user1.setPassword("1234");
            user1.setEmail("user@cliente.com");
            user1.setName("Pepe");
            user1.setSurname("Cliente");
            user1.setTelephone("600333444");
            user1.setGender("Man");                // Campo específico de User
            user1.setCardNumber("1111-2222-3333"); // Campo específico de User
            session.save(user1);

            User user2 = new User();
            user2.setUsername("user2");
            user2.setPassword("1234");
            user2.setEmail("user2@cliente.com");
            user2.setName("Marta");
            user2.setSurname("Lectora");
            user2.setTelephone("700999888");
            user2.setGender("Woman");
            user2.setCardNumber("4444-5555-6666");
            session.save(user2);

            // ==========================================
            // 2. CREAR AUTORES Y LIBROS
            // ==========================================
            // --- LIBRO 1: HARRY POTTER ---
            Author rowling = new Author();
            rowling.setName("J.K.");
            rowling.setSurname("Rowling");
            session.save(rowling);

            Book b1 = new Book();
            b1.setISBN(9788498386655L); // <--- CAMBIO: ISBN de 13 dígitos (con L de long)
            b1.setTitle("Harry Potter y el Cáliz de Fuego");
            b1.setCover("mood-heart.png");
            b1.setAuthor(rowling);
            b1.setPrice(25.50f);
            b1.setStock(50);
            b1.setSheets(600);
            b1.setSypnosis("Harry se enfrenta a un torneo mortal de magia y dragones.");
            b1.setEditorial("Salamandra");
            session.save(b1);

            // --- LIBRO 2: CLEAN CODE ---
            Author martin = new Author();
            martin.setName("Robert C.");
            martin.setSurname("Martin");
            session.save(martin);

            Book b2 = new Book();
            b2.setISBN(9780132350884L); // <--- CAMBIO: ISBN de 13 dígitos
            b2.setTitle("Clean Code");
            b2.setCover("images.jpg");
            b2.setAuthor(martin);
            b2.setPrice(45.00f);
            b2.setStock(20);
            b2.setSheets(464);
            b2.setSypnosis("Manual de artesanía de software ágil. Lectura obligatoria.");
            b2.setEditorial("Pearson");
            session.save(b2);

            // --- LIBRO 3: DON QUIJOTE ---
            Author cervantes = new Author();
            cervantes.setName("Miguel de");
            cervantes.setSurname("Cervantes");
            session.save(cervantes);

            Book b3 = new Book();
            b3.setISBN(9788420412146L); // <--- CAMBIO: ISBN de 13 dígitos
            b3.setTitle("Don Quijote de la Mancha");
            b3.setCover("Book&Bugs_logo.png");
            b3.setAuthor(cervantes);
            b3.setPrice(15.99f);
            b3.setStock(100);
            b3.setSheets(1000);
            b3.setSypnosis("En un lugar de la Mancha, de cuyo nombre no quiero acordarme...");
            b3.setEditorial("Cátedra");
            session.save(b3);

            // ==========================================
            // 3. AÑADIR COMENTARIOS
            // ==========================================
            // Comentario User 1 -> Harry Potter
            Commentate c1 = new Commentate(user1, b1, "¡Me ha encantado! No pude parar de leer.", 5.0f);
            session.save(c1);

            // Comentario User 1 -> Clean Code
            Commentate c2 = new Commentate(user1, b2, "Es denso pero fundamental para programar bien.", 4.5f);
            session.save(c2);

            // Comentario User 2 -> Clean Code
            Commentate c3 = new Commentate(user2, b2, "Buenos ejemplos, aunque un poco antiguos.", 4.0f);
            session.save(c3);

            // Comentario User 1 -> Don Quijote
            Commentate c4 = new Commentate(user1, b3, "Un clásico inmortal.", 5.0f);
            session.save(c4);

            tx.commit();
            System.out.println(">> ¡Datos precargados con éxito!");

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            System.err.println(">> Error al precargar datos: " + e.getMessage());
        } finally {
            session.close();
        }
    }
}