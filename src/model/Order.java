package model;

import java.sql.Timestamp;
import java.util.List;

public class Order {

    private int idOrder;
    private int idUsuer;
    private float total;
    private Timestamp fechaComp;
    private boolean bought;
    private List<Contain> listPreBuy;

    public int getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(int idOrder) {
        this.idOrder = idOrder;
    }

    public int getIdUsuer() {
        return idUsuer;
    }

    public void setIdUsuer(int idUsuer) {
        this.idUsuer = idUsuer;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public Timestamp getFechaComp() {
        return fechaComp;
    }

    public void setFechaComp(Timestamp fechaComp) {
        this.fechaComp = fechaComp;
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
