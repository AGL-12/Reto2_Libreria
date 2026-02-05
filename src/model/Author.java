package model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

/**
 * Representa la entidad Autor en el sistema.
 * Esta clase está mapeada a la tabla "author" en la base de datos y contiene
 * la información básica de un autor y su relación con los libros que ha escrito.
 * * @author alex
 * @version 1.0
 */
@Entity
@Table(name = "author")
public class Author implements Serializable {

    /**
     * Identificador único del autor. Se genera automáticamente en la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idAuthor;

    /**
     * Nombre del autor.
     */
    @Column(name="name")
    private String name;

    /**
     * Apellido del autor.
     */
    @Column(name="surname")
    private String surname;
    
    /**
     * Lista de libros asociados a este autor. 
     * Relación de uno a muchos mapeada por el atributo "author" en la clase Book.
     */
    @OneToMany(mappedBy = "author")
    private List<Book> books;

    /**
     * Constructor con parámetros básicos.
     * * @param idAuthor Identificador único del autor.
     * @param nombre Nombre del autor.
     * @param apellido Apellido del autor.
     */
    public Author(int idAuthor, String nombre, String apellido) {
        this.idAuthor = idAuthor;
        this.name = nombre;
        this.surname = apellido;
    }

    /**
     * Constructor vacío requerido por JPA/Hibernate.
     */
    public Author() {}

    /**
     * Constructor completo con todos los atributos, incluyendo la lista de libros.
     * * @param idAuthor Identificador único del autor.
     * @param name Nombre del autor.
     * @param surname Apellido del autor.
     * @param books Lista de libros escritos por el autor.
     */
    public Author(int idAuthor, String name, String surname, List<Book> books) {
        this.idAuthor = idAuthor;
        this.name = name;
        this.surname = surname;
        this.books = books;
    }

    /**
     * Obtiene el ID del autor.
     * @return El identificador único del autor.
     */
    public int getIdAuthor() {
        return idAuthor;
    }

    /**
     * Establece el ID del autor.
     * @param idAuthor El nuevo identificador único.
     */
    public void setIdAuthor(int idAuthor) {
        this.idAuthor = idAuthor;
    }

    /**
     * Obtiene el nombre del autor.
     * @return El nombre del autor.
     */
    public String getName() {
        return name;
    }

    /**
     * Establece el nombre del autor.
     * @param name El nuevo nombre.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtiene el apellido del autor.
     * @return El apellido del autor.
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Establece el apellido del autor.
     * @param surname El nuevo apellido.
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * Obtiene la lista de libros del autor.
     * @return Una lista de objetos {@link Book}.
     */
    public List<Book> getBooks() {
        return books;
    }

    /**
     * Establece la lista de libros del autor.
     * @param books La nueva lista de libros.
     */
    public void setBooks(List<Book> books) {
        this.books = books;
    }

    /**
     * Devuelve una representación en cadena del autor.
     * @return El nombre y apellido concatenados.
     */
    @Override
    public String toString() {
        return name + " " + surname;
    }
}