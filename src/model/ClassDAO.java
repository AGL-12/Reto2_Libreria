/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for database operations.
 * Provides methods to interact with user and admin records in the database.
 */
public interface ClassDAO {

    public Profile logIn(String username, String password);
    public Boolean signUp(String gender, String cardNumber, String username, String password, String email, String name, String telephone, String surname);
    public Boolean dropOutUser(String username, String password);
    public Boolean dropOutAdmin(String usernameToDelete, String adminUsername, String adminPassword);
    public Boolean modificarUser (String password, String email, String name, String telephone, String surname, String username, String gender);
// --- GESTIÓN DE LIBROS (Book_) ---
    public boolean createBook(Book book);
    public boolean modifyBook(Book book);
    public boolean deleteBook(int isbn); // El SQL define ISBN como char(13), pero en Java lo tienes como int. Ojo con esto.
    public Book getBookData(int isbn); // Para comprobar si existe o cargar datos
    public List<Book> getAllBooks(); // Útil para la tienda o listados

    // --- GESTIÓN DE COMENTARIOS ---
    // Nota: No vi la tabla de comentarios en el snippet del SQL, asumo que existe 
    // y se llama 'commentate' o similar basada en tu entidad Java.
    public List<Commentate> getAllComments(); 
    public boolean deleteComment(int idUser, int idBook);
  
    List comboBoxInsert();
}
