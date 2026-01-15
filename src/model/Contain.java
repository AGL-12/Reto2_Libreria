package model;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "contain_line") // "contain" a veces da problemas, mejor nombre explícito
public class Contain implements Serializable {

    // 1. Clave Compuesta incrustada
    @EmbeddedId
    private ContainId id;

    // 2. Atributos propios
    private int quantity;

    @Transient
    private float sum;

    // 3 Relaciones con @MapsId
    @ManyToOne
    @MapsId("idOrder") // <-- "Usa el campo 'idOrder' de ContainId para mi FK"
    @JoinColumn(name = "id_order")
    private Order order;

    @ManyToOne
    @MapsId("isbnBook") // <-- "Usa el campo 'isbnBook' de ContainId para mi FK"
    @JoinColumn(name = "isbn_book")
    private Book book;

    public Contain(int quantity, Order order, Book book) {
        this.quantity = quantity;
        this.order = order;
        this.book = book;

        // Creamos el ID automáticamente usando los datos de los objetos
        this.id = new ContainId(order.getIdOrder(), book.getISBN());
    }

    public Contain() {
      
    }

    public ContainId getId() {
        return id;
    }

    public void setId(ContainId id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
        // Si cambias el objeto, actualiza también el ID
        if (id != null && order != null) {
            id.setIdOrder(order.getIdOrder());
        }
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
        if (id != null && book != null) {
            id.setIsbnBook(book.getISBN());
        }
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setSum(float sum) {
        this.sum = sum;
    }

    public int getQuantity() {
        return quantity;
    }

    public float getSum() {
        return sum;
    }

}
