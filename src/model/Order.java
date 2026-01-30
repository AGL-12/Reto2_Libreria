package model;

import java.sql.Timestamp;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "orders") // "order" es reservada en SQL
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_order")
    private int idOrder;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user; // Hibernate creará la columna id_user (FK)

    @Transient
    private float total;

    @Column(name = "purchase_date")
    private Timestamp purchaseDate;

    @Column(name="bought")
    private boolean bought;

    // --- CAMBIO: CascadeType.ALL hacia Contain ---
    // Si se borra este pedido (porque se borró su usuario), se borran sus líneas de detalle.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Contain> listPreBuy;

    public int getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(int idOrder) {
        this.idOrder = idOrder;
    }

    public User getUsuer() {
        return user;
    }

    public void setIdUsuer(User usuer) {
        this.user = usuer;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public Timestamp getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Timestamp purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }

    public List<Contain> getListPreBuy() {
        return listPreBuy;
    }

    public void setListPreBuy(List<Contain> listPreBuy) {
        this.listPreBuy = listPreBuy;
    }
}