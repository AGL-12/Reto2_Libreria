package model;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;

@Embeddable
public class CommentateId implements Serializable {

    private int userCode; // Coincide con el PK de User/Profile
    private int isbnBook;

    public CommentateId() {
    }

    public CommentateId(int userCode, int isbnBook) {
        this.userCode = userCode;
        this.isbnBook = isbnBook;
    }

    // Getters y Setters
    public int getUserCode() {
        return userCode;
    }

    public void setUserCode(int userCode) {
        this.userCode = userCode;
    }

    public int getIsbnBook() {
        return isbnBook;
    }

    public void setIsbnBook(int isbnBook) {
        this.isbnBook = isbnBook;
    }

    // HashCode y Equals OBLIGATORIOS
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommentateId that = (CommentateId) o;
        return userCode == that.userCode && isbnBook == that.isbnBook;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userCode, isbnBook);
    }

}
