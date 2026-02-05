package model;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;

/**
 * Clase que representa el identificador compuesto para la entidad {@link Contain}.
 * Se utiliza para mapear la clave primaria de la tabla que relaciona pedidos y libros.
 * Al estar anotada con {@link Embeddable}, sus campos se integran en la entidad que la utiliza.
 * * @author alex
 * @version 1.0
 */
@Embeddable
public class ContainId implements Serializable {

    /**
     * Identificador del pedido relacionado.
     */
    private int idOrder;

    /**
     * Código ISBN del libro relacionado.
     */
    private long isbnBook;

    /**
     * Constructor por defecto requerido por Hibernate/JPA para la instanciación de la clave.
     */
    public ContainId() {
    }

    /**
     * Constructor que inicializa los campos que conforman la clave primaria compuesta.
     * @param idOrder Identificador del pedido.
     * @param isbnBook Código ISBN del libro.
     */
    public ContainId(int idOrder, long isbnBook) {
        this.idOrder = idOrder;
        this.isbnBook = isbnBook;
    }

    /**
     * Obtiene el identificador del pedido.
     * @return El ID del pedido asociado a esta clave.
     */
    public long getIdOrder() {
        return idOrder;
    }

    /**
     * Establece el identificador del pedido.
     * @param idOrder El nuevo identificador del pedido.
     */
    public void setIdOrder(int idOrder) {
        this.idOrder = idOrder;
    }

    /**
     * Obtiene el ISBN del libro.
     * @return El código ISBN asociado a esta clave.
     */
    public long getIsbnBook() {
        return isbnBook;
    }

    /**
     * Establece el ISBN del libro.
     * @param isbnBook El nuevo código ISBN del libro.
     */
    public void setIsbnBook(long isbnBook) {
        this.isbnBook = isbnBook;
    }

    /**
     * Compara este identificador con otro objeto para verificar su igualdad.
     * La implementación es obligatoria para el correcto funcionamiento de las claves compuestas en JPA.
     * * @param o Objeto a comparar.
     * @return true si ambos objetos son iguales en sus valores de idOrder e isbnBook; false en caso contrario.
     */
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

    /**
     * Genera un código hash único basado en los campos que componen la clave.
     * Es esencial para el manejo de la entidad en colecciones y por el gestor de persistencia.
     * * @return El código hash generado para esta instancia.
     */
    @Override
    public int hashCode() {
        return Objects.hash(idOrder, isbnBook);
    }
}