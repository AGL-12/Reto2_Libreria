/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.List;

/**
 * Data Access Object interface for database operations.
 * Provides methods to interact with user and admin records in the database.
 */
public interface ClassDAO {

    public Profile logIn(String username, String password);
    public void signUp(Profile profile);
    public void dropOutUser(Profile profile);
    public void dropOutAdmin(Profile profile);
    public void modificarUser (Profile profile);
    // --- GESTIÓN DE LIBROS (Book_) ---
    public void createBook(Book book);
    public void modifyBook(Book book);
    public void deleteBook(int isbn); // El SQL define ISBN como char(13), pero en Java lo tienes como int. Ojo con esto.
    public Book getBookData(int isbn); // Para comprobar si existe o cargar datos
    public List<Book> getAllBooks(); // Útil para la tienda o listados

    // --- GESTIÓN DE COMENTARIOS ---
    // Nota: No vi la tabla de comentarios en el snippet del SQL, asumo que existe 
    // y se llama 'commentate' o similar basada en tu entidad Java.
    public List<Commentate> getCommentsByBook(int isbn);
    public void addComment(Commentate comment);
    public void updateComment(Commentate comment);
    public void deleteComment(Commentate comment);
    public List<Book> buscarLibros(String busqueda);
    public List comboBoxInsert();
}
