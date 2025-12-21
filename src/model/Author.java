package model;

import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "author")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idAuthor;
    private String nombre;
    private String apellido;
    
    @OneToMany(mappedBy = "author")
    private List<Book> books;

    public Author(int idAuthor, String nombre, String apellido, List<Book> books) {
        this.idAuthor = idAuthor;
        this.nombre = nombre;
        this.apellido = apellido;
        this.books = books;
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

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    @Override
    public String toString() {
        return nombre + " " + apellido;
    }

}
