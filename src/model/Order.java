package model;

import java.sql.Timestamp;
import java.util.List;
import javax.persistence.*;

/**
 * Representa la entidad Pedido en el sistema.
 * Esta clase está mapeada a la tabla "orders" en la base de datos y gestiona
 * la información relativa a las compras realizadas por los usuarios, incluyendo
 * su estado de finalización y el desglose de productos asociados.
 * * @author alex
 * @version 1.0
 */
@Entity
@Table(name = "orders") // "order" es reservada en SQL
public class Order {

    /**
     * Identificador único del pedido. Se genera automáticamente en la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_order")
    private int idOrder;

    /**
     * Usuario que ha realizado el pedido.
     * Representa una relación de muchos a uno con la entidad User.
     */
    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user; // Hibernate creará la columna id_user (FK)

    /**
     * Precio total calculado del pedido. 
     * Este campo es transitorio y no se persiste directamente en la base de datos.
     */
    @Transient
    private float total;

    /**
     * Fecha y hora en la que se realizó la compra.
     */
    @Column(name = "purchase_date")
    private Timestamp purchaseDate;

    /**
     * Estado del pedido. Indica si la compra ha sido finalizada (true) o si
     * todavía es un carrito activo (false).
     */
    @Column(name="bought")
    private boolean bought;

    // --- CAMBIO: CascadeType.ALL hacia Contain ---
    // Si se borra este pedido (porque se borró su usuario), se borran sus líneas de detalle.
    /**
     * Lista de líneas de detalle que componen el pedido.
     * Utiliza persistencia en cascada total y borrado de huérfanos para mantener
     * la integridad con la entidad {@link Contain}.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Contain> listPreBuy;

    /**
     * Obtiene el identificador del pedido.
     * @return El ID del pedido.
     */
    public int getIdOrder() {
        return idOrder;
    }

    /**
     * Establece el identificador del pedido.
     * @param idOrder El nuevo ID del pedido.
     */
    public void setIdOrder(int idOrder) {
        this.idOrder = idOrder;
    }

    /**
     * Obtiene el usuario propietario del pedido.
     * @return El objeto {@link User} asociado.
     */
    public User getUsuer() {
        return user;
    }

    /**
     * Establece el usuario propietario del pedido.
     * @param usuer El objeto {@link User} a asignar.
     */
    public void setIdUsuer(User usuer) {
        this.user = usuer;
    }

    /**
     * Obtiene el importe total del pedido.
     * @return El total calculado.
     */
    public float getTotal() {
        return total;
    }

    /**
     * Establece el importe total del pedido.
     * @param total El importe a establecer.
     */
    public void setTotal(float total) {
        this.total = total;
    }

    /**
     * Obtiene la fecha de compra del pedido.
     * @return El {@link Timestamp} de la fecha de compra.
     */
    public Timestamp getPurchaseDate() {
        return purchaseDate;
    }

    /**
     * Establece la fecha de compra del pedido.
     * @param purchaseDate El nuevo {@link Timestamp} de fecha.
     */
    public void setPurchaseDate(Timestamp purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    /**
     * Indica si el pedido ya ha sido comprado.
     * @return true si está comprado, false si es un carrito.
     */
    public boolean isBought() {
        return bought;
    }

    /**
     * Establece el estado de compra del pedido.
     * @param bought true para marcar como comprado.
     */
    public void setBought(boolean bought) {
        this.bought = bought;
    }

    /**
     * Obtiene la lista de detalles del pedido (libros contenidos).
     * @return Una lista de objetos {@link Contain}.
     */
    public List<Contain> getListPreBuy() {
        return listPreBuy;
    }

    /**
     * Establece la lista de detalles del pedido.
     * @param listPreBuy La lista de objetos {@link Contain} a asignar.
     */
    public void setListPreBuy(List<Contain> listPreBuy) {
        this.listPreBuy = listPreBuy;
    }
}