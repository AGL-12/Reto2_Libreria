package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javax.persistence.*;

@Entity
@Table(name = "commentate")
public class Commentate implements Serializable {

    @EmbeddedId
    private CommentateId id;

    @Column(columnDefinition = "TEXT")
    private String commentary;

    @Column(name = "date_creation")
    private Timestamp dateCreation;
    private float valuation;
    
    @ManyToOne
    @MapsId("userCode") // Conecta con el campo userCode del ID
    @JoinColumn(name = "id_user") // Nombre de la columna en BD
    private User user;

    @ManyToOne
    @MapsId("isbnBook") // Conecta con el campo isbnBook del ID
    @JoinColumn(name = "id_book") // Nombre de la columna en BD
    private Book book;

    public Commentate() {
    }
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

    public String getFormattedDate() {
        if (dateCreation != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(dateCreation);
        }
        return "";
    }
    
    

}
