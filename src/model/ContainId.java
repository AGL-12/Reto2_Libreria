package model;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;

@Embeddable
public class ContainId implements Serializable {

    private int idOrder;
    private int isbnBook;

    public ContainId() {
    }

    public ContainId(int idOrder, int isbnBook) {
        this.idOrder = idOrder;
        this.isbnBook = isbnBook;
    }

    // Getters, Setters, Equals y HashCode (OBLIGATORIOS)
    public int getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(int idOrder) {
        this.idOrder = idOrder;
    }

    public int getIsbnBook() {
        return isbnBook;
    }

    public void setIsbnBook(int isbnBook) {
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
