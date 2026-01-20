package model;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;

@Embeddable
public class ContainId implements Serializable {

    private int idOrder;
    private long isbnBook;

    public ContainId() {
    }

    public ContainId(int idOrder, long isbnBook) {
        this.idOrder = idOrder;
        this.isbnBook = isbnBook;
    }

    // Getters, Setters, Equals y HashCode (OBLIGATORIOS)
    public long getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(int idOrder) {
        this.idOrder = idOrder;
    }

    public long getIsbnBook() {
        return isbnBook;
    }

    public void setIsbnBook(long isbnBook) {
        this.isbnBook = isbnBook;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContainId that = (ContainId) o;
        return idOrder == that.idOrder && isbnBook == that.isbnBook;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idOrder, isbnBook);
    }
}
