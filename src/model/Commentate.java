package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javax.persistence.*;

@Entity
@Table(name = "commentate")
public class Commentate implements Serializable {

    /**
     * Identificador compuesto de la entidad (User ID + Book ISBN).
     */
    @EmbeddedId
    private CommentateId id;

    /**
     * Texto del comentario u opinión escrito por el usuario. Almacenado como
     * tipo TEXT en la base de datos.
     */
    @Column(columnDefinition = "TEXT")
    private String commentary;
    /**
     * Fecha y hora exacta en la que se creó el comentario.
     */
    @Column(name = "date_creation")
    private Timestamp dateCreation;
    /**
     * Valoración numérica otorgada al libro (ej. de 0 a 5 estrellas).
     */
    @Column(name = "valuation")
    private float valuation;
    /**
     * El usuario que ha realizado el comentario.
     * <p>
     * Mapeado con {@code @MapsId("userCode")} para vincularlo con la parte
     * correspondiente de la clave compuesta {@link CommentateId}.
     * </p>
     */

    @ManyToOne
    @MapsId("userCode") // Conecta con el campo userCode del ID
    @JoinColumn(name = "id_user") // Nombre de la columna en BD
    private User user;
    /**
     * El libro sobre el cual se realiza el comentario.
     * <p>
     * Mapeado con {@code @MapsId("isbnBook")} para vincularlo con la parte
     * correspondiente de la clave compuesta {@link CommentateId}.
     * </p>
     */
    @ManyToOne
    @MapsId("isbnBook") // Conecta con el campo isbnBook del ID
    @JoinColumn(name = "id_book") // Nombre de la columna en BD
    private Book book;

    public Commentate() {
    }

    /**
     * Constructor principal para crear un nuevo comentario. Inicializa
     * automáticamente la fecha de creación al momento actual
     * ({@code System.currentTimeMillis()}) y construye la clave compuesta
     * {@link CommentateId} basándose en el usuario y el libro proporcionados.
     *
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

    public CommentateId getId() {
        return id;
    }

    public void setId(CommentateId id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    public float getValuation() {
        return valuation;
    }

    public void setValuation(float valuation) {
        this.valuation = valuation;
    }

    /**
     * Devuelve una representación en texto de la fecha de creación formateada.
     * <p>
     * Utiliza el patrón <b>"dd/MM/yyyy"</b>.
     * </p>
     *
     * @return Una cadena con la fecha formateada, o una cadena vacía si la
     * fecha es nula.
     */
    public String getFormattedDate() {
        if (dateCreation != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(dateCreation);
        }
        return "";
    }

}
