package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import threads.SessionHolderThread;
import utilities.HibernateUtil;

/**
 * Implementation of ClassDAO using database operations. Handles all database
 * interactions for users and admins. Provides login, signup, deletion,
 * modification, and retrieval of usernames.
 *
 * Author: acer
 */
public class DBImplementation implements ClassDAO {

    private PreparedStatement stmt;

    // SQL statements
    private final String SLQSELECTNUSER = "SELECT u.USERNAME FROM USER_ u;";

    // Asumiendo tabla 'commentate' basada en tu entidad Java
    private final String SELECT_ALL_COMMENTS = "SELECT * FROM commentate";
    private final String DELETE_COMMENT = "DELETE FROM commentate WHERE id_user=? AND id_book=?";

    /**
     * Logs in a user or admin from the database.
     *
     * @param username The username to log in
     * @param password The password to validate
     * @return Profile object (User or Admin) if found, null otherwise
     */
    @Override
    public Profile logIn(String username, String password) {
        Session session = HibernateUtil.getSessionFactory().openSession(); // 1. Abrimos aquí
        Transaction tx = null;
        Profile userFound = null;

        try {
            tx = session.beginTransaction();

            // 2. Consulta
            String hql = "FROM Profile p WHERE p.username = :user AND p.password = :pass";
            userFound = session.createQuery(hql, Profile.class)
                    .setParameter("user", username)
                    .setParameter("pass", password)
                    .uniqueResult();

            tx.commit(); // 3. Confirmamos transacción (liberamos bloqueos de BD)

            // 4. AQUÍ ESTÁ EL TRUCO MAESTRO:
            // No cerramos la sesión. Se la pasamos al hilo para que la retenga.
            new SessionHolderThread(session).start();

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            // Si hay error, cerramos aquí porque el hilo no arrancó
            if (session.isOpen()) {
                session.close();
            }
        }

        // 5. Devolvemos el dato INMEDIATAMENTE. La UI no espera.
        return userFound;
    }

    /**
     * Signs up a new user in the database.
     *
     * @return true if signup was successful, false otherwise
     */
    @Override
    public void signUp(Profile profile) {
        // 1. Abrimos sesión
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            // 2. Guardamos (Hibernate inserta en Profile y User/Admin)
            session.save(profile);

            // 3. Confirmamos
            tx.commit();

            // 4. EL TRUCO: Lanzamos el hilo para que retenga la conexión 30s
            // El usuario ya recibe el 'ok', pero la conexión sigue ocupada en fondo.
            new SessionHolderThread(session).start();

        } catch (Exception e) {
            // Si hay error (ej: usuario duplicado), deshacemos
            if (tx != null) {
                tx.rollback();
            }

            // IMPORTANTE: Si falló, el hilo no arrancó, así que cerramos nosotros
            if (session.isOpen()) {
                session.close();
            }

            // Relanzamos la excepción para que salga la Alerta roja en la ventana
            throw e;
        }
    }

    /**
     * Deletes a standard user from the database.
     */
    @Override
    public void dropOutUser(Profile profile) {
    }

    /**
     * Deletes a user selected by admin from the database.
     */
    @Override
    public void dropOutAdmin(Profile profile) {
    }

    /**
     * Modifies the information of a user in the database.
     */
    @Override
    public void modificarUser(Profile profile) {
    }

    /**
     * Retrieves a list of usernames from the database.
     *
     * @return List of usernames
     */
    @Override
    public List comboBoxInsert() {
        List<String> listaUsuarios = new ArrayList<>();
        Connection con = null;
        try {
            con = ConnectionPool.getConnection();
            stmt = con.prepareStatement(SLQSELECTNUSER);
            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                listaUsuarios.add(result.getString("USERNAME"));
            }
        } catch (SQLException e) {
            System.out.println("Database error on retrieving usernames");
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing DB connection after retrieving usernames");
                e.printStackTrace();
            }
        }
        return listaUsuarios;
    }

    @Override
    public void  createBook(Book book) {
    }

    @Override
    public void modifyBook(Book book) {
    }

    @Override
    public void deleteBook(int isbn) {
    }

    @Override
    public Book getBookData(int isbn) {
        return null;
    }

    @Override
    public List<Book> getAllBooks() {
        List<Book> libros = new ArrayList<>();
        return libros;
    }

    @Override
    public List<Book> buscarLibros(String busqueda) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Book> resultados = null;

        try {
            // No hace falta Transaction para solo leer (Select)

            // Lógica de "Volver a ningún filtro"
            if (busqueda == null || busqueda.trim().isEmpty()) {
                return session.createQuery("FROM Book", Book.class).list();
            }

            // Consulta HQL
            String search = "%" + busqueda.toLowerCase() + "%";
            String hql = "FROM Book b WHERE "
                    + "lower(b.title) LIKE :q OR "
                    + "lower(b.author.name) LIKE :q OR "
                    + "str(b.ISBN) LIKE :q";

            resultados = session.createQuery(hql, Book.class)
                    .setParameter("q", search)
                    .list();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // PARA BÚSQUEDAS: Cerramos INMEDIATAMENTE.
            // Si retuvieras aquí 30s, el buscador en tiempo real mataría el Pool.
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return resultados;
    }

    // --- IMPLEMENTACIÓN COMENTARIOS ---
    @Override
    public List<Commentate> getCommentsByBook(int isbn) {
        Session session = HibernateUtil.getSessionFactory().openSession(); // 1. Abrimos aquí
        Transaction tx = null;
        List<Commentate> comments = null;

        try {
            tx = session.beginTransaction();

            // HQL: Selecciona los comentarios (c) donde el ISBN del libro asociado (c.book.ISBN) coincida
            // IMPORTANTE: Asegúrate de que en tu clase Commentate el atributo se llame 'book' 
            // y en tu clase Book el atributo se llame 'ISBN'.
            String hql = "FROM Commentate c WHERE c.book.ISBN = :isbn";
            comments = session.createQuery(hql, Commentate.class)
                    .setParameter("isbn", isbn)
                    .list();

            tx.commit(); // 3. Confirmamos transacción (liberamos bloqueos de BD)

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            // Si hay error, cerramos aquí porque el hilo no arrancó
            if (session.isOpen()) {
                session.close();
            }
        }
        return comments;
    }

    @Override
    public void addComment(Commentate comment) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteComment(Commentate comment) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
