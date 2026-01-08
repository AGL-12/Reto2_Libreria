package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import threads.HiloConnection;
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
    private final String SQLSINGUPPROFILE = "INSERT INTO PROFILE_ (USERNAME, PASSWORD_, EMAIL, NAME_, TELEPHONE, SURNAME) VALUES (?,?,?,?,?,?);";
    private final String SQLSIGNUPUSER = "INSERT INTO USER_ (USERNAME, GENDER, CARD_NUMBER) VALUES (?,?,?);";

    private final String SLQDELETEPROFILE = "DELETE FROM PROFILE_ WHERE USERNAME = ? AND PASSWORD_ = ?;";
    private final String SLQDELETEPROFILEADMIN = "DELETE p FROM PROFILE_ p JOIN USER_ u ON p.USERNAME = u.USERNAME JOIN ADMIN_ a ON p.USERNAME = a.USERNAME WHERE p.PASSWORD_ = ? AND u.username = ?;";

    private final String SLQLOGINUSER = "SELECT p.*, u.GENDER, u.CARD_NUMBER FROM PROFILE_ p JOIN USER_ u ON p.USERNAME= u.USERNAME WHERE u.USERNAME = ? AND p.PASSWORD_ = ?;";
    private final String SLQLOGINADMIN = "SELECT p.*, a.CURRENT_ACCOUNT FROM PROFILE_ p JOIN ADMIN_ a ON p.USERNAME= a.USERNAME WHERE a.USERNAME = ? AND p.PASSWORD_ = ?;";

    final String SQLMODIFYPROFILE = "UPDATE PROFILE_ P SET P.PASSWORD_ = ?, P.EMAIL = ?, P.NAME_ = ?, P.TELEPHONE = ?, P.SURNAME = ? WHERE USERNAME = ?;";
    final String SQLMODIFYUSER = "UPDATE USER_ U SET U.GENDER = ? WHERE USERNAME = ?";

    private final String SLQSELECTNUSER = "SELECT u.USERNAME FROM USER_ u;";

    /**
     * Default constructor that loads DB configuration.
     */
    public DBImplementation() {}

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
    public Boolean dropOutUser(String username, String password) {
        HiloConnection connectionThread = new HiloConnection(30);
        connectionThread.start();
        boolean success = false;
        PreparedStatement stmtUser = null;
        try {
            Connection con = waitForConnection(connectionThread);

            // verificar password
            String checkPassword = "SELECT PASSWORD_ FROM PROFILE_ WHERE USERNAME = ?";
            stmt = con.prepareStatement(checkPassword);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("PASSWORD_");
                if (!dbPassword.equals(password)) {
                    return false;
                }
            } else {
                return false;
            }
            rs.close();
            stmt.close();

            // eliminar de USER_ primero
            String deleteUser = "DELETE FROM USER_ WHERE USERNAME = ?";
            stmtUser = con.prepareStatement(deleteUser);
            stmtUser.setString(1, username);
            stmtUser.executeUpdate();
            stmtUser.close();

            // eliminar de PROFILE_
            stmt = con.prepareStatement(SLQDELETEPROFILE);
            stmt.setString(1, username);
            stmt.setString(2, password);
            success = stmt.executeUpdate() > 0;
        } catch (SQLException | InterruptedException e) {
            System.out.println("Database error on deleting user");
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (stmtUser != null) {
                    stmtUser.close();
                }
                connectionThread.releaseConnection();
            } catch (SQLException e) {
                System.out.println("Error closing DB connection after deleting user");
                e.printStackTrace();
            }
        }
        return success;
    }

    /**
     * Deletes a user selected by admin from the database.
     */
    @Override
    public Boolean dropOutAdmin(String usernameToDelete, String adminUsername, String adminPassword) {
        HiloConnection connectionThread = new HiloConnection(30);
        connectionThread.start();
        boolean success = false;
        PreparedStatement stmtDeleteUser = null;
        PreparedStatement stmtDeleteAdmin = null;
        try {
            Connection con = waitForConnection(connectionThread);

            // verificar password del admin logueado
            String checkAdminPassword = "SELECT PASSWORD_ FROM PROFILE_ WHERE USERNAME = ?";
            stmt = con.prepareStatement(checkAdminPassword);
            stmt.setString(1, adminUsername);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("PASSWORD_");
                if (!dbPassword.equals(adminPassword)) {
                    return false;
                }
            } else {
                return false;
            }
            rs.close();
            stmt.close();

            // eliminar de USER_ si existe
            String deleteUser = "DELETE FROM USER_ WHERE USERNAME = ?";
            stmtDeleteUser = con.prepareStatement(deleteUser);
            stmtDeleteUser.setString(1, usernameToDelete);
            stmtDeleteUser.executeUpdate();
            stmtDeleteUser.close();

            // eliminar de ADMIN_ si existe
            String deleteAdmin = "DELETE FROM ADMIN_ WHERE USERNAME = ?";
            stmtDeleteAdmin = con.prepareStatement(deleteAdmin);
            stmtDeleteAdmin.setString(1, usernameToDelete);
            stmtDeleteAdmin.executeUpdate();
            stmtDeleteAdmin.close();

            // eliminar de PROFILE_
            String deleteProfile = "DELETE FROM PROFILE_ WHERE USERNAME = ?";
            stmt = con.prepareStatement(deleteProfile);
            stmt.setString(1, usernameToDelete);
            success = stmt.executeUpdate() > 0;
        } catch (SQLException | InterruptedException e) {
            System.out.println("Database error on deleting admin");
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (stmtDeleteUser != null) {
                    stmtDeleteUser.close();
                }
                if (stmtDeleteAdmin != null) {
                    stmtDeleteAdmin.close();
                }
                connectionThread.releaseConnection();
            } catch (SQLException e) {
                System.out.println("Error closing DB connection after deleting admin");
                e.printStackTrace();
            }
        }
        return success;
    }

    /**
     * Modifies the information of a user in the database.
     */
    @Override
    public Boolean modificarUser(String password, String email, String name, String telephone, String surname, String username, String gender) {
        HiloConnection connectionThread = new HiloConnection(30);
        connectionThread.start();
        boolean success = false;
        PreparedStatement stmtUser = null;

        try {
            Connection con = waitForConnection(connectionThread);

            // actualizar PROFILE_
            stmt = con.prepareStatement(SQLMODIFYPROFILE);
            stmt.setString(1, password);
            stmt.setString(2, email);
            stmt.setString(3, name);
            stmt.setString(4, telephone);
            stmt.setString(5, surname);
            stmt.setString(6, username);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                // actualizar USER_ si existe
                stmtUser = con.prepareStatement(SQLMODIFYUSER);
                stmtUser.setString(1, gender);
                stmtUser.setString(2, username);
                stmtUser.executeUpdate();
                stmtUser.close();

                success = true;
            } else {
                System.out.println("Usuario no encontrado en la base de datos");
                success = false;
            }
        } catch (SQLException | InterruptedException e) {
            System.out.println("Database error on modifying user");
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (stmtUser != null) {
                    stmtUser.close();
                }
                connectionThread.releaseConnection();

            } catch (SQLException e) {
                System.out.println("Error closing DB connection after modifying user");
                e.printStackTrace();
            }
        }
        return success;
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

    /**
     * Waits for a connection from a HiloConnection thread.
     *
     * @param thread The HiloConnection thread
     * @return Connection object
     * @throws InterruptedException if thread is interrupted
     */
    private Connection waitForConnection(HiloConnection thread) throws InterruptedException {
        int attempts = 0;
        while (!thread.isReady() && attempts < 50) {
            Thread.sleep(10);
            attempts++;
        }
        return thread.getConnection();
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
                    + // titulo cambiado a title
                    "lower(b.author.name) LIKE :q OR "
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

    @Override
    public List<Commentate> getComments() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
