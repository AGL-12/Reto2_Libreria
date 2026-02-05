package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javax.persistence.*;

/**
 * Representa un comentario y valoración realizado por un usuario sobre un
 * libro. Esta entidad utiliza una clave primaria compuesta definida en
 * {@link CommentateId}.
 */
@Entity
@Table(name = "commentate")
public class Commentate implements Serializable {

    /**
     * Identificador compuesto que vincula al usuario y al libro comentado.
     */
    @EmbeddedId
    private CommentateId id;

    /**
     * Texto descriptivo del comentario. Mapeado como tipo TEXT en la base de
     * datos.
     */
    @Column(columnDefinition = "TEXT")
    private String commentary;

    /**
     * Marca de tiempo que indica cuándo se creó el comentario.
     */
    @Column(name = "date_creation")
    private Timestamp dateCreation;

    /**
     * Calificación numérica otorgada al libro.
     */
    @Column(name = "valuation")
    private float valuation;

    /**
     * Usuario que ha realizado el comentario. Utiliza {@code MapsId} para
     * sincronizar con la clave primaria compuesta.
     */
    @ManyToOne
    @MapsId("userCode") // Conecta con el campo userCode del ID
    @JoinColumn(name = "id_user") // Nombre de la columna en BD
    private User user;

    /**
     * Libro al que hace referencia el comentario. Utiliza {@code MapsId} para
     * sincronizar con la clave primaria compuesta.
     */
    @ManyToOne
    @MapsId("isbnBook") // Conecta con el campo isbnBook del ID
    @JoinColumn(name = "id_book") // Nombre de la columna en BD
    private Book book;

    /**
     * Constructor por defecto requerido por JPA/Hibernate.
     */
    public Commentate() {
    }

    /**
     * Crea un nuevo comentario inicializando la fecha de creación y la clave
     * compuesta.
     *
     * @param user El usuario que comenta.
     * @param book El libro comentado.
     * @param commentary El contenido del comentario.
     * @param valuation La puntuación asignada.
     */
    public Commentate(User user, Book book, String commentary, float valuation) {
        this.user = user;
        this.book = book;
        this.commentary = commentary;
        this.valuation = valuation;
        this.dateCreation = new Timestamp(System.currentTimeMillis());

        // Instanciamos el ID al crear el objeto
        this.id = new CommentateId(user.getUserCode(), book.getISBN());
    }

    /**
     * Obtiene el identificador compuesto del comentario.
     *
     * @return El objeto CommentateId.
     */
    public CommentateId getId() {
        return id;
    }

    public void setId(CommentateId id) {
        this.id = id;
    }

    /**
     * Obtiene el libro asociado al comentario.
     *
     * @return El objeto Book.
     */
    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    /**
     * Obtiene el usuario autor del comentario.
     *
     * @return El objeto User.
     */
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Obtiene el texto del comentario.
     *
     * @return El comentario en formato String.
     */
    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    /**
     * Obtiene la marca de tiempo de la creación.
     *
     * @return Objeto Timestamp.
     */
    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    /**
     * Obtiene la valoración numérica.
     *
     * @return Valor float de la puntuación.
     */
    public float getValuation() {
        return valuation;
    }

    public void setValuation(float valuation) {
        this.valuation = valuation;
    }

    /**
     * Devuelve la fecha de creación formateada como cadena de texto
     * (dd/MM/yyyy).
     *
     * @return La fecha formateada o una cadena vacía si la fecha es nula.
     */
    public String getFormattedDate() {
        if (dateCreation != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(dateCreation);
        }
        return "";
    }
}
