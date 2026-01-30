package main;

import controller.MainBookStoreController;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import model.Contain;
import model.Order;
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
            admin.setUsername("admin");
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

            Map<Integer, Author> autores = new HashMap<>();

            String[][] datosAutores = {
                {"1", null, null},
                {"2", "J.K.", "Rowling"},
                {"3", "Miguel de", "Cervantes"},
                {"4", "George", "Orwell"},
                {"5", "Gabriel", "García Márquez"},
                {"6", "Brandon", "Sanderson"},
                {"8", "Fiódor", "Dostoievski"},
                {"9", "Osamu", "Dazai"}
            };

            for (String[] d : datosAutores) {
                Author a = new Author();
                a.setName(d[1]);
                a.setSurname(d[2]);
                session.save(a);
                autores.put(Integer.parseInt(d[0]), a);
            }

            // ==========================================
            // 3. CREAR LIBROS (Procesando tu lista de texto)
            // ==========================================
            // Estructura: ISBN | Titulo | ID_Autor | Paginas | Stock | Sinopsis | Precio | Editorial | Imagen
            Map<Long, Book> librosMap = new HashMap<>(); // Para recuperar libros luego por ISBN

            Object[][] datosLibros = {
                {9780307474728L, "Cien años de soledad", 5, 471, 8, "La saga de la familia Buendía...", 18.5f, "Cátedra", "cien_anos.jpg"},
                {9780439139595L, "Harry Potter y el Cáliz de Fuego", 2, 636, 10, "Harry se enfrenta a desafíos...", 22.5f, "Salamandra", "hp4.jpg"},
                {9780451524935L, "1984", 4, 328, 20, "El Gran Hermano te vigila...", 12.0f, "Debolsillo", "1984.jpg"},
                {9788416440047L, "NOCHES BLANCAS", 8, 128, 30, "Un joven solitario e introvertido...", 17.1f, "Nórdica Libros", "noches_blancas.jpg"},
                {9788419035769L, "INDIGNO DE SER HUMANO", 9, 240, 10, "Indigno de ser humano es la obra maestra...", 22.8f, "Satori Ediciones", "indigno_de_ser_humano.jpg"},
                {9788419306074L, "SAKAMOTO DAYS 1", 6, 192, 20, "Taro Sakamoto era un asesino...", 7.6f, "Ivrea", "sakamoto_days1.jpg"},
                {9788420412146L, "Don Quijote de la Mancha", 3, 1345, 5, "Las aventuras de un hidalgo...", 15.99f, "Alfaguara", "quijote.jpg"},
                {9788466657523L, "El Imperio Final", 6, 672, 12, "En un mundo donde cae ceniza...", 21.9f, "Nova", "mistborn.jpg"}
            };

            for (Object[] d : datosLibros) {
                Book b = new Book();
                b.setISBN((Long) d[0]);
                b.setTitle((String) d[1]);
                b.setAuthor(autores.get((Integer) d[2])); // Buscamos el autor por el ID del mapa
                b.setSheets((Integer) d[3]);
                b.setStock((Integer) d[4]);
                b.setSypnosis((String) d[5]);
                b.setPrice((Float) d[6]);
                b.setEditorial((String) d[7]);
                b.setCover((String) d[8]);
                session.save(b);
                librosMap.put(b.getISBN(), b);
            }

            // ==========================================
            // 4. AÑADIR COMENTARIOS (Usando el mapa)
            // ==========================================
            // Ejemplo: Comentario para Cien años de soledad (9780307474728L)
            session.save(new Commentate(user1, librosMap.get(9780307474728L), "Simplemente magistral.", 5.0f));
            // Ejemplo: Comentario para Harry Potter (9780439139595L)
            session.save(new Commentate(user1, librosMap.get(9780439139595L), "Increíble.", 4.5f));

            // ==========================================
            // 5. HISTORIAL DE COMPRAS (Usando el mapa)
            // ==========================================
            Order o1 = new Order();
            o1.setIdUsuer(user1);
            o1.setPurchaseDate(new java.sql.Timestamp(System.currentTimeMillis()));
            o1.setBought(true);
            session.save(o1);

            // Recuperamos el libro del mapa para la línea de pedido
            Book libroPedido = librosMap.get(9788419306074L); // Sakamoto Days
            if (libroPedido != null) {
                Contain line1 = new Contain(1, o1, libroPedido);
                line1.setSum(libroPedido.getPrice());
                session.save(line1);
            }

            tx.commit();
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
