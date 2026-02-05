package model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

/**
 * Representa la entidad Libro en el sistema.
 * Esta clase almacena toda la información relativa a un libro, incluyendo su 
 * relación con autores y los comentarios realizados por los usuarios.
 */
@Entity
@Table(name = "book")
public class Book implements Serializable {

    /**
     * Identificador único del libro (ISBN).
     */
    @Id
    @Column(name = "isbn")
    private long ISBN; // Asumo que el ISBN lo pones tú manualmente, si no, añade @GeneratedValue

    /**
     * Ruta o identificador de la imagen de portada del libro.
     */
    @Column(name = "cover")
    private String cover;

    /**
     * Título del libro.
     */
    @Column(name="title")
    private String title;

    /**
     * Autor asociado al libro. Relación de muchos a uno.
     */
    @ManyToOne
    @JoinColumn(name = "id_author")
    private Author author;

    /**
     * Número de páginas del libro.
     */
    @Column(name="sheets")
    private int sheets;

    /**
     * Cantidad de ejemplares disponibles en inventario.
     */
    @Column(name="stock")
    private int stock;

    /**
     * Breve resumen o sinopsis de la obra.
     */
    @Column(length = 1000, name="synopsis") // Para textos largos
    private String sypnosis;

    /**
     * Precio de venta al público.
     */
    @Column(name="price")
    private float price;

    /**
     * Empresa editorial que publica el libro.
     */
    @Column(name="editorial")
    private String editorial;

    /**
     * Lista de comentarios y valoraciones asociados al libro.
     * Se cargan de forma perezosa (Lazy) por defecto.
     */
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<Commentate> comments;

    /**
     * Valoración media del libro. Campo calculado que no se persiste en la BD.
     */
    @Transient
    private float avgValuation;

    /**
     * Constructor con todos los parámetros para inicializar un objeto Book.
     * * @param ISBN Identificador ISBN.
     * @param cover Portada del libro.
     * @param titulo Título de la obra.
     * @param author Autor del libro.
     * @param sheets Número de páginas.
     * @param stock Unidades disponibles.
     * @param sypnosis Sinopsis del contenido.
     * @param price Precio del ejemplar.
     * @param editorial Nombre de la editorial.
     * @param avgValuation Valoración media calculada.
     */
    public Book(long ISBN, String cover, String titulo, Author author, int sheets, int stock, String sypnosis, float price, String editorial, float avgValuation) {
        this.ISBN = ISBN;
        this.cover = cover;
        this.title = titulo;
        this.author = author;
        this.sheets = sheets;
        this.stock = stock;
        this.sypnosis = sypnosis;
        this.price = price;
        this.editorial = editorial;
        this.avgValuation = avgValuation;
    }

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Book() {
    }

    /**
     * Obtiene el ISBN del libro.
     * @return El código ISBN.
     */
    public long getISBN() {
        return ISBN;
    }

    public void setISBN(long ISBN) {
        this.ISBN = ISBN;
    }

    /**
     * Obtiene la portada del libro.
     * @return Cadena con la ruta o nombre de la imagen.
     */
    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    /**
     * Obtiene el título del libro.
     * @return El título de la obra.
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Obtiene el autor del libro.
     * @return El objeto Author asociado.
     */
    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public int getSheets() {
        return sheets;
    }

    public void setSheets(int sheets) {
        this.sheets = sheets;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    /**
     * Obtiene la sinopsis del libro.
     * @return El resumen del libro.
     */
    public String getSypnosis() {
        return sypnosis;
    }

    public void setSypnosis(String sypnosis) {
        this.sypnosis = sypnosis;
    }

    /**
     * Obtiene el precio del libro.
     * @return El precio como float.
     */
    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    /**
     * Obtiene la lista de comentarios del libro.
     * @return Lista de objetos Commentate.
     */
    public List<Commentate> getComments() {
        return comments;
    }

    public void setComments(List<Commentate> comments) {
        this.comments = comments;
    }

    /**
     * Obtiene la valoración media del libro.
     * @return El promedio de valoraciones.
     */
    public float getAvgValuation() {
        return avgValuation;
    }

    public void setAvgValuation(float avgValuation) {
        this.avgValuation = avgValuation;
    }

}