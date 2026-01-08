package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.query.Query;
import threads.HiloConnection;

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
    public DBImplementation() {

    }

    /**
     * Logs in a user or admin from the database.
     *
     * @param username The username to log in
     * @param password The password to validate
     * @return Profile object (User or Admin) if found, null otherwise
     */
    @Override
    public Profile logIn(Session session, String username, String password) {
        String hql = "FROM Profile p WHERE p.username = :user AND p.password = :pass";
        // Simplemente ejecutamos la consulta usando la sesión prestada
        Query<Profile> query = session.createQuery(hql, Profile.class);
        query.setParameter("user", username);
        query.setParameter("pass", password);
        
        return query.uniqueResult();
    }
    /**
     * Signs up a new user in the database.
     *
     * @return true if signup was successful, false otherwise
     */
    @Override
    public void signUp(Session session, Profile profile) {
        session.save(profile);
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
    public List<Book> buscarLibros(Session session, String busqueda) {
        // Si la búsqueda está vacía, devolvemos TODOS los libros
        if (busqueda == null || busqueda.trim().isEmpty()) {
            return session.createQuery("FROM Book", Book.class).list();
        }

        // HQL: Busca si el título coincide O si el nombre del autor coincide O si el ISBN coincide
        // Usamos str(b.ISBN) para convertir el numero a texto y poder buscar trozos (ej: buscar "978")
        String hql = "FROM Book b WHERE " +
                     "lower(b.title) LIKE :q OR " +
                     "lower(b.author.name) LIKE :q OR " +
                     "str(b.ISBN) LIKE :q";

        return session.createQuery(hql, Book.class)
                      .setParameter("q", "%" + busqueda.toLowerCase() + "%") // Los % son los comodines
                      .list();
    }

    @Override
    public List<Commentate> getCommentsByBook(Session session, int isbn) {
        // HQL: Selecciona los comentarios (c) donde el ISBN del libro asociado (c.book.ISBN) coincida
        // IMPORTANTE: Asegúrate de que en tu clase Commentate el atributo se llame 'book' 
        // y en tu clase Book el atributo se llame 'ISBN'.
        String hql = "FROM Commentate c WHERE c.book.ISBN = :isbn";

        return session.createQuery(hql, Commentate.class)
                      .setParameter("isbn", isbn)
                      .list();    
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
