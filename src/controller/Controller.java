/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.util.List;
import model.Book;
import model.ClassDAO;
import model.Profile;
import org.hibernate.Session;

/**
 * Controller class that handles interaction between the GUI and the database.
 * Provides login, signup, deletion, modification, and data retrieval methods.
 *
 * Author: acer
 */
public class Controller {

    private ClassDAO dao;

    /**
     * Constructor for Controller.
     *
     * @param dao The DAO implementation to handle database operations
     */
    public Controller(ClassDAO dao) {
        this.dao = dao;
    }

    /**
     * Attempts to log in a user or admin.
     *
     * @param username The username
     * @param password The password
     * @return Profile object if login succeeds, null otherwise
     */
    public Profile logIn(Session session, String username, String password) {
        return dao.logIn(session, username, password);
    }

    /**
     * Signs up a new user.
     *
     * @return true if signup succeeds, false otherwise
     */
    public void signUp(Session session, Profile profile) {
        dao.signUp(session, profile);
    }

    /**
     * Deletes a user account.
     */
    public Boolean dropOutUser(String username, String password) {
        return dao.dropOutUser(username, password);
    }

    public Boolean dropOutAdmin(String usernameToDelete, String adminUsername, String adminPassword) {
        return dao.dropOutAdmin(usernameToDelete, adminUsername, adminPassword);
    }

    /**
     * Modifies user information.
     */
    public Boolean modificarUser(String password, String email, String name, String telephone, String surname, String username, String gender) {
        return dao.modificarUser(password, email, name, telephone, surname, username, gender);
    }

    /**
     * Retrieves a list of usernames for GUI combo boxes.
     */
    public List comboBoxInsert() {
        return dao.comboBoxInsert();
    }
    public List<Book> buscarLibros(Session session, String texto){
        return dao.buscarLibros(session, texto);
    }
}
