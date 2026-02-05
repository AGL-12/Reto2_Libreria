package model;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;

/**
 * Clase que representa la clave primaria compuesta para la entidad
 * {@link Commentate}.
 * <p>
 * En el modelo relacional, esta clase permite mapear la relación de muchos a
 * muchos entre Usuarios y Libros, asegurando que la combinación de
 * {@code userCode} e {@code isbnBook} sea única.
 * </p>
 * * Al ser una clase {@code @Embeddable}, Hibernate la utiliza para gestionar
 * la persistencia de la identidad de cada comentario. Implementa
 * {@link Serializable} como requisito de JPA para claves compuestas.
 *
 * * @author mikel
 * @version 1.0
 */
@Embeddable
public class CommentateId implements Serializable {

    /**
     * Identificador único del usuario que realiza el comentario. Corresponde a
     * la clave primaria de la entidad Profile/User.
     */
    private int userCode; // Coincide con el PK de User/Profile
    /**
     * Identificador ISBN del libro sobre el que se realiza el comentario.
     * Corresponde a la clave primaria de la entidad Book.
     */
    private long isbnBook;

    /**
     * Constructor por defecto requerido por Hibernate para la instanciación
     * mediante reflexión.
     */

    public CommentateId() {
    }

    /**
     * Constructor completo para inicializar la clave compuesta.
     *
     * * @param userCode Código único del usuario.
     * @param isbnBook ISBN del libro asociado.
     */
    public CommentateId(int userCode, long isbnBook) {
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

    public long getIsbnBook() {
        return isbnBook;
    }

    public void setIsbnBook(long isbnBook) {
        this.isbnBook = isbnBook;
    }

    /**
     * Compara este objeto con otro para verificar su igualdad.
     * <p>
     * Fundamental para que JPA pueda identificar de forma única la entidad en
     * la caché de primer nivel y en operaciones de búsqueda.
     * </p>
     *
     * * @param o Objeto a comparar.
     * @return {@code true} si ambos identificadores coinciden; {@code false} en
     * caso contrario.
     */
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

    /**
     * Genera un código hash basado en los atributos de la clave compuesta.
     *
     * * @return Valor entero hash representativo de la identidad del objeto.
     */
    @Override
    public int hashCode() {
        return Objects.hash(userCode, isbnBook);
    }

}
