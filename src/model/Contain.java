package model;

class Contain {

    private int idOrder;
    private int idBook;
    private int quantity;
    private float sum; 

    public void setIdOrder(int idOrder) {
        this.idOrder = idOrder;
    }

    public void setIdBook(int idBook) {
        this.idBook = idBook;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setSum(float sum) {
        this.sum = sum;
    }

    public int getIdOrder() {
        return idOrder;
    }

    public int getIdBook() {
        return idBook;
    }

    public int getQuantity() {
        return quantity;
    }

    public float getSum() {
        return sum;
    }

}
