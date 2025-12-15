package controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class EstrellaPuntuacionController {

    @FXML
    private ImageView estrellaFrente;
    @FXML
    private StackPane root;

    // Ajusta esto SIEMPRE al tamaño real de tu imagen png
    private final double ANCHO_ESTRELLAS = 120.0;
    private final double ALTO_ESTRELLAS = 24.0;

    private boolean esEditable = false;

    // CAMBIO: Ahora usamos double para guardar decimales (ej: 3.5)
    private double puntuacionActual = 0.0;

    // --- API PÚBLICA ---
    public void setEditable(boolean editable) {
        this.esEditable = editable;
        // Cambiamos el cursor para indicar interactividad
        root.setStyle(editable ? "-fx-cursor: hand;" : "-fx-cursor: default;");
    }

    public void setNota(double nota) {
        this.puntuacionActual = nota;
        actualizarGrafico(nota / 5.0);
    }

    // CAMBIO: El getter ahora devuelve double
    public double getNotaUsuario() {
        return puntuacionActual;
    }

    // --- EVENTOS ---
    @FXML
    private void manejarMovimiento(MouseEvent event) {
        if (!esEditable) {
            return;
        }

        // Efecto visual "Hover": Calculamos la nota temporal mientras mueve el ratón
        double mouseX = event.getX();
        double notaCalculada = calcularNota(mouseX);

        // Mostramos esa nota temporalmente (sin guardarla)
        actualizarGrafico(notaCalculada / 5.0);
    }

    @FXML
    private void manejarClic(MouseEvent event) {
        if (!esEditable) {
            return;
        }

        double mouseX = event.getX();

        // 1. Calculamos la nota basada en la posición (con pasos de 0.5)
        this.puntuacionActual = calcularNota(mouseX);

        // 2. Fijamos el gráfico
        actualizarGrafico(this.puntuacionActual / 5.0);

        System.out.println("Nota guardada: " + this.puntuacionActual);
    }

    @FXML
    private void manejarSalida(MouseEvent event) {
        if (!esEditable) {
            return;
        }
        // Al salir del área, el gráfico vuelve a mostrar la nota que estaba guardada
        actualizarGrafico(puntuacionActual / 5.0);
    }

    // --- LÓGICA MATEMÁTICA ---
    // Método auxiliar para calcular la nota con saltos de 0.5
    private double calcularNota(double coordX) {
        double porcentaje = coordX / ANCHO_ESTRELLAS;

        // Convertimos a escala 0-10 para redondear enteros, luego dividimos por 2
        // Ejemplo: 3.2 estrellas -> 6.4 -> Ceil(7) -> 3.5
        double pasos = Math.ceil(porcentaje * 5 * 2);
        double nota = pasos / 2.0;

        // Limites de seguridad
        if (nota > 5.0) {
            nota = 5.0;
        }
        if (nota < 0.5) {
            nota = 0.5; // Mínimo 0.5 si tocas la primera estrella
        }
        return nota;
    }

    // --- LÓGICA VISUAL ---
    private void actualizarGrafico(double porcentaje) {
        if (porcentaje < 0) {
            porcentaje = 0;
        }
        if (porcentaje > 1) {
            porcentaje = 1;
        }

        double anchoVisible = ANCHO_ESTRELLAS * porcentaje;

        Rectangle recorte = new Rectangle(anchoVisible, ALTO_ESTRELLAS);
        estrellaFrente.setClip(recorte);
    }
}
