package model;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;

/**
 * Clase que representa la clave primaria compuesta para la entidad Commentate.
 * Esta clase es de tipo {@link Embeddable}, lo que permite que sus atributos
 * sean integrados como parte de otra entidad en la base de datos. * Implementa
 * {@link Serializable} para permitir la persistencia y el manejo de la clave
 * por parte de Hibernate/JPA.
 *
 * * @author alex
 * @version 1.0
 */
@Embeddable
public class CommentateId implements Serializable {

    /**
     * Código identificador del usuario. Coincide con la clave primaria (PK) de
     * la entidad User/Profile.
     */
    private int userCode;

    /**
     * Número de ISBN del libro comentado.
     */
    private long isbnBook;

    /**
     * Constructor por defecto (vacío). Requerido por JPA/Hibernate para la
     * instanciación de la clave.
     */
    public CommentateId() {
    }

    /**
     * Constructor parametrizado para inicializar la clave compuesta.
     *
     * * @param userCode Identificador único del usuario.
     * @param isbnBook Identificador ISBN único del libro.
     */
    public CommentateId(int userCode, long isbnBook) {
        this.userCode = userCode;
        this.isbnBook = isbnBook;
    }

    /**
     * Obtiene el código del usuario asociado a la clave.
     *
     * @return El código del usuario.
     */
    public int getUserCode() {
        return userCode;
    }

    /**
     * Establece el código del usuario asociado a la clave.
     *
     * @param userCode El nuevo código del usuario.
     */
    public void setUserCode(int userCode) {
        this.userCode = userCode;
    }

    /**
     * Obtiene el ISBN del libro asociado a la clave.
     *
     * @return El número de ISBN del libro.
     */
    public long getIsbnBook() {
        return isbnBook;
    }

    /**
     * Establece el ISBN del libro asociado a la clave.
     *
     * @param isbnBook El nuevo número de ISBN del libro.
     */
    public void setIsbnBook(long isbnBook) {
        this.isbnBook = isbnBook;
    }

    /**
     * Compara esta instancia con otro objeto para verificar su igualdad. Es
     * obligatorio sobrescribir este método en claves compuestas de JPA.
     *
     * * @param o Objeto a comparar.
     * @return true si ambos objetos tienen el mismo userCode e isbnBook; false
     * en caso contrario.
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
     * Genera un valor hash basado en los atributos de la clave compuesta. Es
     * obligatorio sobrescribir este método en claves compuestas de JPA para el
     * correcto funcionamiento de colecciones y caché de persistencia.
     *
     * @return Valor hash de la instancia.
     */
    @Override
    public int hashCode() {
        return Objects.hash(userCode, isbnBook);
    }

}
