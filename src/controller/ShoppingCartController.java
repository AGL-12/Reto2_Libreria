/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import model.Author;
import model.Book;
import model.ClassDAO;
import model.DBImplementation;
import model.Profile;
import model.UserSession;

/**
 * FXML Controller class
 *
 * @author ander
 */
public class ShoppingCartController implements Initializable, EventHandler<ActionEvent> {

    @FXML
    private VBox vBoxContenedorLibros;
    @FXML
    private Label lblTotal;
    @FXML
    private VBox vBoxResumen;
    @FXML
    public HeaderController headerController;

    private TilePane tileLibros;

    List<Book> libros = new ArrayList<>();
    private final ClassDAO dao = new DBImplementation();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Profile userLogged = UserSession.getInstance().getUser();
        if (userLogged != null) {
            // 2. LLAMADA AL DAO: Usamos tu método nuevo "pendingBook"
            // Esto llena la lista con los libros que están en bought=false
            libros = dao.pendingBook(userLogged.getId());
            
            System.out.println(libros);

            // 3. Cargar la parte visual
            if (libros != null && !libros.isEmpty()) {
                cargarVistaLibros();
            } else {
                lblTotal.setText("El carrito está vacío.");
            }
        }

    }

    private void cargarVistaLibros() {
        try {
            // Recorremos la lista de libros REALES
            for (Book lib : libros) {
                // Cargar el FXML de la cajita individual
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/PreOrder.fxml"));
                VBox libroBox = fxmlLoader.load();

                // Añadir el CheckBox dinámicamente
                CheckBox cb = new CheckBox("Seleccionar");
                cb.setSelected(true);
                cb.setOnAction(this); // Vinculamos el evento para recalcular precio al hacer clic
                libroBox.getChildren().add(cb);

                // Pasar los datos del libro al controlador pequeño (PreOrderController)
                PreOrderController preOrderController = fxmlLoader.getController();
                preOrderController.setData(lib);

                // Añadir la cajita al VBox grande del carrito
                vBoxContenedorLibros.getChildren().add(libroBox);
            }
            // Calcular el total inicial
            actualizarPrecioTotal();

        } catch (IOException ex) {
            Logger.getLogger(ShoppingCartController.class.getName()).log(Level.SEVERE, "Error cargando vista", ex);
        }
    }

    private void actualizarPrecioTotal() {
        double total = 0;
        int libro = 0;

        // Recorremos los hijos del VBox (cada libroBox)
        for (Node nodoLibro : vBoxContenedorLibros.getChildren()) {
            if (nodoLibro instanceof VBox) {
                VBox libroVBox = (VBox) nodoLibro;

                // Buscamos el CheckBox dentro de ese libro
                for (Node hijo : libroVBox.getChildren()) {
                    if (hijo instanceof CheckBox) {
                        CheckBox cb = (CheckBox) hijo;
                        if (cb.isSelected()) {
                            // Sumamos el precio del libro correspondiente a esta posición
                            total += libros.get(libro).getPrice();
                        }
                    }
                }
                libro++;
            }
        }

        // Actualizamos el Label
        lblTotal.setText("Total: $" + String.format("%.2f", total));
    }

    public void handle(ActionEvent event) {
        // Cada vez que se pulse CUALQUIER checkbox, se ejecutará esto
        actualizarPrecioTotal();
    }
}
