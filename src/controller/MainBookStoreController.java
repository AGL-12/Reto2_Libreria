package controller;

import exception.MyFormException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Book;
import model.ClassDAO;
import model.DBImplementation;
import util.LogInfo;
import util.UtilGeneric;

/**
 * Controlador principal de la vista de la librería (Tienda).
 * <p>
 * Esta clase gestiona la visualización del catálogo de libros, el sistema de
 * búsqueda en tiempo real, la carga dinámica de items y la gestión del menú
 * contextual global.
 * </p>
 *
 * * @author Alexander
 */
public class MainBookStoreController {

    @FXML
    private BorderPane mainRoot;
    @FXML
    private TilePane tileBooks;
    @FXML
    public HeaderController headerController;
    @FXML
    private Menu menuArchivo;
    @FXML
    private MenuItem iSalir;
    @FXML
    private Menu menuAcciones;
    @FXML
    private MenuItem iManual;
    @FXML
    private MenuItem iJasper;
    @FXML
    private Menu menuAyuda;
    @FXML
    private MenuItem iAcercaDe;

    private List<Book> allBooks = new ArrayList<>();
    private final ClassDAO dao = new DBImplementation();

    // El temporizador para el delay
    private PauseTransition pause;
    private ContextMenu globalMenu;

    /**
     * Método de inicialización del controlador. Se ejecuta automáticamente al
     * cargar el FXML. Inicializa el menú contextual y carga los libros desde la
     * base de datos.
     */
    public void initialize() {
        initContexMenu();
        initRenderBooks();
    }

    /**
     * Filtra la lista de libros mostrados basándose en el texto proporcionado.
     * Si el texto está vacío, restaura la lista completa.
     *
     * * @param text El texto a buscar.
     */
    private void searchBooks(String text) {
        if (text == null || text.trim().isEmpty()) {
            showBooks(allBooks);
            return;
        }

        String busqueda = text.toLowerCase();

        List<Book> filteredBooks = filterBook(busqueda);

        showBooks(filteredBooks);
    }

    /**
     * Renderiza visualmente una lista de libros en el {@link TilePane}.
     * <p>
     * Este método limpia el contenedor actual y, para cada libro de la lista,
     * carga dinámicamente el archivo FXML {@code BookItem.fxml} y lo añade a la
     * vista.
     * </p>
     *
     * * @param allBooks La lista de objetos {@link Book} a visualizar.
     */
    private void showBooks(List<Book> allBooks) {
        tileBooks.getChildren().clear();

        try {
            for (Book lib : allBooks) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BookItem.fxml"));
                VBox libroBox = fxmlLoader.load();
                BookItemController itemController = fxmlLoader.getController();
                itemController.setData(lib);
                tileBooks.getChildren().add(libroBox);
            }
        } catch (IOException ex) {
            LogInfo.getInstance().logSevere("Error carga/render de libros", ex);
        }
    }

    /**
     * Aplica la lógica de filtrado sobre la lista maestra de libros. Comprueba
     * si el texto de búsqueda coincide con el título, el autor o el ISBN.
     *
     * * @param busqueda Texto de búsqueda en minúsculas.
     * @return Una sublista de libros que coinciden con el criterio.
     */
    private List<Book> filterBook(String busqueda) {
        List<Book> resultados = new ArrayList<>();

        for (Book b : allBooks) {
            boolean coincideTitulo = b.getTitle().toLowerCase().contains(busqueda);
            boolean coincideAutor = b.getAuthor() != null
                    && b.getAuthor().toString().toLowerCase().contains(busqueda);
            boolean coincideISBN = String.valueOf(b.getISBN()).contains(busqueda);
            if (coincideTitulo || coincideAutor || coincideISBN) {
                resultados.add(b);
            }
        }

        return resultados;
    }

    /**
     * Configura e inicializa el menú contextual (clic derecho) de la ventana
     * principal. Define las opciones disponibles y delega las acciones a
     * {@link UtilGeneric}.
     */
    private void initContexMenu() {
        // 1. Inicializamos el menú
        globalMenu = new ContextMenu();
        globalMenu.setAutoHide(true);
        // --- Opción 1: Limpiar Búsqueda ---
        MenuItem searchClear = new MenuItem("Limpiar Busqueda");
        searchClear.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                headerController.clearSearch();
            }
        });
        // --- Opción 2: Informe Técnico ---
        MenuItem itemInforme = new MenuItem("Generar Informe Técnico");
        itemInforme.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleJasperReport(event);
            }
        });
        // --- Opción 3: Salir ---
        MenuItem itemExit = new MenuItem("Salir");
        itemExit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleExit(event);
            }
        });
        // --- Opción 4: Manual ---
        MenuItem itemManual = new MenuItem("Manual de Usuario");
        itemManual.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleHelpAction(event);
            }
        });
        // --- Opción 5: Acerca De nosotros ---
        MenuItem itemAbout = new MenuItem("Acerca de Nosotros");
        itemAbout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleAboutAction(event);
            }
        });

        // 2. AÑADIR TODO AL MENÚ EN ORDEN
        globalMenu.getItems().addAll(
                searchClear,
                itemInforme,
                new SeparatorMenuItem(),
                itemExit,
                new SeparatorMenuItem(),
                itemManual,
                itemAbout
        );

        // 3. Asignar eventos al panel principal (rootPane)
        if (mainRoot != null) {
            mainRoot.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    // Mostrar el menú donde se hizo clic
                    globalMenu.show(mainRoot, event.getScreenX(), event.getScreenY());
                    event.consume();
                }
            });

            // Ocultar el menú si se hace clic izquierdo fuera
            mainRoot.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.isPrimaryButtonDown() && globalMenu.isShowing()) {
                        globalMenu.hide();
                    }
                }
            });
        }
    }

    /**
     * Cierra la aplicación de forma segura utilizando la utilidad genérica.
     *
     * * @param event El evento que disparó la acción.
     * @see UtilGeneric#exit()
     */
    @FXML
    private void handleExit(ActionEvent event) {
        UtilGeneric.getInstance().exit();
    }

    /**
     * Genera el informe técnico de JasperReports. Utiliza la configuración
     * centralizada de conexión y reporte.
     *
     * @param event El evento de menú.
     * @see UtilGeneric#getJasperReport()
     */
    @FXML
    private void handleJasperReport(ActionEvent event) {
        UtilGeneric.getInstance().getJasperReport();
    }

    /**
     * Maneja la acción del botón de ayuda. Delega la apertura del PDF a la
     * clase de utilidad.
     *
     * @param event El evento de acción.
     * @see util.UtilGeneric#helpAction()
     */
    private void handleHelpAction(ActionEvent event) {
        UtilGeneric.getInstance().helpAction();
    }

    /**
     * Muestra la ventana modal "Acerca de Nosotros".
     *
     * * @param event El evento de acción.
     * @see UtilGeneric#aboutAction()
     */
    @FXML
    private void handleAboutAction(ActionEvent event) {
        UtilGeneric.getInstance().aboutAction();
    }

    /**
     * Maneja la acción del menú superior "Manual de Usuario". Delega la
     * apertura del PDF a la clase de utilidad.
     *
     * @param event El evento de acción.
     * @see UtilGeneric#helpAction()
     */
    @FXML
    private void handleReportAction(ActionEvent event) {
        UtilGeneric.getInstance().helpAction();
    }

    /**
     * Inicializa la carga de libros desde la base de datos y configura el
     * listener de búsqueda.
     * <p>
     * Configura un {@link PauseTransition} para crear un efecto de "debounce"
     * (retardo) al escribir en la barra de búsqueda, evitando consultas
     * excesivas a la base de datos mientras el usuario teclea.
     * </p>
     */
    private void initRenderBooks() {
        try {
            allBooks = dao.getAllBooks();
        } catch (MyFormException ex) {
            LogInfo.getInstance().logSevere("Carga inicial: " + allBooks.size() + " libros recuperados de la BD.", ex);
            UtilGeneric.getInstance().showAlert(ex.getMessage(), Alert.AlertType.ERROR, "Error");
        }
        LogInfo.getInstance().logInfo("Carga inicial: " + allBooks.size() + " libros recuperados de la BD.");

        showBooks(allBooks);
        // Configurar el delay (0.5 segundos)
        pause = new PauseTransition(Duration.seconds(0.5));

        // Acción al terminar el delay
        pause.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Obtenemos el texto actual del header y buscamos
                String textoABuscar = headerController.getSearchTextField().getText().trim();
                LogInfo.getInstance().logInfo("Buscando en BD: " + textoABuscar);
                searchBooks(textoABuscar);
            }
        });

        // Conectar el listener al campo de texto del Header
        if (headerController != null) {
            headerController.getSearchTextField().textProperty().addListener((obs, oldVal, newVal) -> {
                // Reiniciamos el timer cada vez que se escribe una letra
                pause.playFromStart();
            });
        }
    }
}
