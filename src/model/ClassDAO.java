/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.List;

public interface ClassDAO {

    public Profile logIn(String username, String password);
    public void signUp(Profile profile);
    public void dropOutUser(Profile profile);
    public void dropOutAdmin(Profile profile);
    public void modificarUser (Profile profile);
    
    // --- GESTIÓN DE LIBROS ---
    public void createBook(Book book);
    public void modifyBook(Book book);
    public void deleteBook(long isbn); 
    public Book getBookData(long isbn); 
    public List<Book> getAllBooks(); 

    // --- GESTIÓN DE COMENTARIOS ---
    public List<Commentate> getCommentsByBook(long isbn);
    public void addComment(Commentate comment);
    public void updateComment(Commentate comment);
    public void deleteComment(Commentate comment);
    public List<Commentate> getCommentsByUser(String username);
    public List<Book> buscarLibros(String busqueda);
    public List comboBoxInsert();
    
    // --- NUEVO MÉTODO PARA AUTORES ---
    /**
     * Busca un autor por nombre y apellido. Si no existe, lo crea.
     */
    public Author getOrCreateAuthor(String name, String surname);
}