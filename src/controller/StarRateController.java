package controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class StarRateController {

    @FXML
    private StackPane root;
    @FXML
    private ImageView frontstar;

    // Ajusta esto SIEMPRE al tamaño real de tu imagen png
    private final double WIDTH_STARS = 120.0;
    private final double HEIGHT_STARS = 24.0;

    private boolean isEditable = false;

    // CAMBIO: Ahora usamos double para guardar decimales (ej: 3.5)
    private double currentRate = 0.0;

    // --- API PÚBLICA ---
    public void setEditable(boolean editable) {
        this.isEditable = editable;
        // Cambiamos el cursor para indicar interactividad
        root.setStyle(editable ? "-fx-cursor: hand;" : "-fx-cursor: default;");
    }

    public void setValueStars(double nota) {
        this.currentRate = nota;
        updateStarsView(nota / 5.0);
    }

    // CAMBIO: El getter ahora devuelve double
    public double getValueUser() {
        return currentRate;
    }

    // --- EVENTOS ---
    @FXML
    private void handleMovement(MouseEvent event) {
        if (!isEditable) {
            return;
        }

        // Efecto visual "Hover": Calculamos la nota temporal mientras mueve el ratón
        double mouseX = event.getX();
        double rateCalculated = calculateRate(mouseX);

        // Mostramos esa nota temporalmente (sin guardarla)
        updateStarsView(rateCalculated / 5.0);
    }

    @FXML
    private void handleClick(MouseEvent event) {
        if (!isEditable) {
            return;
        }

        double mouseX = event.getX();

        // 1. Calculamos la nota basada en la posición (con pasos de 0.5)
        this.currentRate = calculateRate(mouseX);

        // 2. Fijamos el gráfico
        updateStarsView(this.currentRate / 5.0);

        System.out.println("Nota guardada: " + this.currentRate);
    }

    @FXML
    private void handleExit(MouseEvent event) {
        if (!isEditable) {
            return;
        }
        // Al salir del área, el gráfico vuelve a mostrar la nota que estaba guardada
        updateStarsView(currentRate / 5.0);
    }

    // --- LÓGICA MATEMÁTICA ---
    // Método auxiliar para calcular la nota con saltos de 0.5
    private double calculateRate(double coordX) {
        double porcentaje = coordX / WIDTH_STARS;

        // Convertimos a escala 0-10 para redondear enteros, luego dividimos por 2
        // Ejemplo: 3.2 estrellas -> 6.4 -> Ceil(7) -> 3.5
        double steps = Math.ceil(porcentaje * 5 * 2);
        double rate = steps / 2.0;

        // Limites de seguridad
        if (rate > 5.0) {
            rate = 5.0;
        }
        if (rate < 0.5) {
            rate = 0.5; // Mínimo 0.5 si tocas la primera estrella
        }
        return rate;
    }

    // --- LÓGICA VISUAL ---
    private void updateStarsView(double percentage) {
        if (percentage < 0) {
            percentage = 0;
        }
        if (percentage > 1) {
            percentage = 1;
        }

        double widthVisible = WIDTH_STARS * percentage;

        Rectangle cutOut = new Rectangle(widthVisible, HEIGHT_STARS);
        frontstar.setClip(cutOut);
    }
}
