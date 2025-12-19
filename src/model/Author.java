package model;

import java.util.List;

public class Author {

    private int idAuthor;
    private String nombre;
    private String apellido;
    private List<Book> booksList;

    public Author(int idAuthor, String nombre, String apellido, List<Book> booksList) {
        this.idAuthor = idAuthor;
        this.nombre = nombre;
        this.apellido = apellido;
        this.booksList = booksList;
    }

    public int getIdAuthor() {
        return idAuthor;
    }

    public void setIdAuthor(int idAuthor) {
        this.idAuthor = idAuthor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public List<Book> getBooksList() {
        return booksList;
    }

    public void setBooksList(List<Book> booksList) {
        this.booksList = booksList;
    }

    @Override
    public String toString() {
        return nombre + " " + apellido;
    }

}
