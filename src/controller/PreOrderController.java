/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Book;
import util.UtilGeneric;

/**
 * Controlador para los elementos individuales (filas) dentro del carrito de la compra.
 * Representa un libro específico en el pedido, permitiendo ajustar su cantidad o eliminarlo.
 * * @author ander
 * @version 1.0
 */
public class PreOrderController {

    @FXML
    private Spinner<Integer> spinnerCantidad;
    @FXML
    private ImageView imgBook;
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblPrice;

    private Book libro;
    private ShoppingCartController shoppingCartController;
    @FXML
    private HBox rootPreOrder;
    @FXML
    private Button btnDelete;

    
    /**
     * Configura los datos del libro en la fila del carrito y establece los límites
     * del selector de cantidad basándose en el stock disponible.
     * * @param libro El libro a mostrar.
     * @param cartController Referencia al controlador principal del carrito para permitir actualizaciones de precio.
     */
    public void setData(Book libro, ShoppingCartController cartController) {
        this.libro = libro;
        this.shoppingCartController = cartController;

        lblTitle.setText(libro.getTitle());
        lblTitle.setMaxWidth(100);
        lblPrice.setText(String.format("%.2f €", libro.getPrice()));

        // Esta es la parte que fallaba (Línea 57 aprox)
        // Ahora funcionará porque el FXML está bien importado
        SpinnerValueFactory<Integer> valueFactory
                = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, libro.getStock(), 1);
        spinnerCantidad.setValueFactory(valueFactory);

        spinnerCantidad.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (shoppingCartController != null) {
                shoppingCartController.actualizarPrecioTotal();
            }
        });

        if (libro.getCover() != null) {
            Image img = new Image(getClass().getResourceAsStream("/images/" + libro.getCover()));
            UtilGeneric.getInstance().cutOutImage(imgBook, img, 130, 180);
        }
    }

    public Book getBook() {
        return this.libro;
    }

    
    /**
     * Obtiene la cantidad de ejemplares seleccionada actualmente por el usuario mediante el Spinner.
     * * @return El valor entero seleccionado en el selector de cantidad.
     */
    public int getCantidadSeleccionada() {
        // Devolvemos el número que el usuario ha elegido en el selector
        return spinnerCantidad.getValue();
    }

    
    /**
     * Gestiona la acción de eliminar este libro específico del carrito de la compra.
     * * @param event El evento de pulsación del botón de borrado.
     */
    @FXML
    private void handleDelete(ActionEvent event) {
        if (shoppingCartController != null) {
            // Llamamos a un nuevo método que crearemos en el ShoppingCartController
            shoppingCartController.eliminarLibroDelCarrito(libro);
        }
    }

}
