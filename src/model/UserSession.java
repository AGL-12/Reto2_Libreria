package model;

import java.util.ArrayList;
import java.util.List;

public class UserSession {

    // 1. La única instancia que existirá (static)
    private static UserSession instance;

    // 2. El dato que queremos guardar (el usuario logueado)
    private Profile user;
    
    private List<Book> cart = new ArrayList<>();
    
    // 3. Constructor privado para que nadie haga "new UserSession()"
    private UserSession() {
    }

    // 4. Método para obtener la instancia única (Si no existe, la crea)
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // 5. Métodos para guardar y leer el usuario
    public Profile getUser() {
        return user;
    }

    public void setUser(Profile user) {
        this.user = user;
    }

    // 6. Método para cerrar sesión
    public void cleanUserSession() {
        this.user = null; // Borramos el usuario
    }

    // Helper para saber si hay alguien logueado rápido
    public boolean isLoggedIn() {
        return user != null;
    }
    
    public void addBookToCart(Book book) {
        cart.add(book);
    }

    public List<Book> getCart() {
        return cart;
    }
    
    public void clearCart() { // Útil para cuando compre
        cart.clear();
    }
}
