package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
 *
 * @author Alexander
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

    public void initialize() {
        initContexMenu();
        initRenderBooks();
    }

    private void searchBooks(String text) {
        if (text == null || text.trim().isEmpty()) {
            showBooks(allBooks);
            return;
        }

        String busqueda = text.toLowerCase();

        List<Book> filteredBooks = filterBook(busqueda);

        showBooks(filteredBooks);
    }

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

    private void initContexMenu() {
        // 1. Inicializamos el menú
        globalMenu = new ContextMenu();
        globalMenu.setAutoHide(true);

        // --- Opción 1: Añadir al Carrito ---
        MenuItem searchClear = new MenuItem("Limpiar Busqueda");
        searchClear.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                headerController.clearSearch();
            }
        });

        // =========================================================
        // --- NUEVO: OPCIÓN INFORME TÉCNICO (JASPER) ---
        // =========================================================
        MenuItem itemInforme = new MenuItem("Generar Informe Técnico");
        itemInforme.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleJasperReport(event);
            }
        });
        // =========================================================

        MenuItem itemExit = new MenuItem("Salir");
        itemExit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleExit(event);
            }
        });

        MenuItem itemManual = new MenuItem("Manual de Usuario");
        itemManual.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleHelpAction(event);
            }
        });

        MenuItem itemAbout = new MenuItem("Acerca de...");
        itemAbout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleAboutAction(event);
            }
        });

        // 2. AÑADIR TODO AL MENÚ EN ORDEN
        globalMenu.getItems().addAll(
                searchClear, // 1. Comprar
                itemInforme, // 2. Informe Técnico (NUEVO)
                new SeparatorMenuItem(), // Línea separadora
                itemExit, // 4. Salir
                new SeparatorMenuItem(), // Línea separadora
                itemManual, // 5. Ayuda
                itemAbout // 6. About
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

    @FXML
    private void handleExit(ActionEvent event) {
        UtilGeneric.getInstance().exit();
    }

    // --- MÉTODO NUEVO: GENERAR INFORME JASPER ---
    @FXML
    private void handleJasperReport(ActionEvent event) {
        UtilGeneric.getInstance().getJasperReport();
    }

    private void handleHelpAction(ActionEvent event) {
        UtilGeneric.getInstance().helpAction();
    }

    @FXML
    private void handleAboutAction(ActionEvent event) {
        UtilGeneric.getInstance().aboutAction();
    }

    @FXML
    private void handleReportAction(ActionEvent event) {
        UtilGeneric.getInstance().helpAction();
    }

    private void initRenderBooks() {
        allBooks = dao.getAllBooks();

        showBooks(allBooks);
        // 2. CONFIGURAR EL DELAY (Por ejemplo, 0.5 segundos)
        // Esto crea un timer que espera 500ms antes de disparar su acción.
        pause = new PauseTransition(Duration.seconds(0.5));

        // Qué pasa cuando el timer termina (se acabó el tiempo de espera)
        pause.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Obtenemos el texto actual del header y buscamos
                String textoABuscar = headerController.getSearchTextField().getText().trim();
                LogInfo.getInstance().logInfo("Buscando en BD: " + textoABuscar);
                searchBooks(textoABuscar);
            }
        });

        // 3. CONECTAR EL LISTENER
        if (headerController != null) {
            headerController.getSearchTextField().textProperty().addListener((obs, oldVal, newVal) -> {
                // MAGIA: Cada vez que escribes una letra...

                // A. Reiniciamos el timer desde cero (si estaba contando, se para y vuelve a empezar)
                pause.playFromStart();

                // Resultado: Si escribes rápido "Harry", el timer se reinicia 5 veces
            });
        }
    }
}
