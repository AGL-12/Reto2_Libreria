/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import controller.OrderDetailController;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Order;
import model.ClassDAO;
import model.DBImplementation;
import model.Profile;
import model.UserSession;

import util.LogInfo;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * Controlador para la ventana de Historial de Compras. Permite al usuario
 * visualizar sus pedidos pasados en una tabla, ver detalles mediante doble clic
 * o menú contextual, y generar informes técnicos.
 *
 * * @author ander
 */
public class ShoppingHistoryController implements Initializable {

    private final LogInfo logger = LogInfo.getInstance();

    @FXML
    private TableColumn<Order, Integer> colId;
    @FXML
    private TableColumn<Order, java.sql.Timestamp> colFecha;
    @FXML
    private TableColumn<Order, Float> colTotal;

    private List<Order> allShops = new ArrayList<>();
    private final ClassDAO dao = new DBImplementation();

    @FXML
    private Button btnVolver;
    @FXML
    private TableView<Order> tableOrders;

    @FXML
    private MenuBar menuBar;

    /**
     * Inicializa el controlador, configura las columnas de la tabla, el menú
     * contextual y carga los datos del historial del usuario logueado.
     *
     * * @param url La ubicación relativa del objeto raíz.
     * @param rb Los recursos utilizados para localizar el objeto raíz.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.logInfo("Accediendo a la ventana de Historial de Compras.");
        colId.setCellValueFactory(new PropertyValueFactory<>("idOrder"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        ContextMenu contextMenu = new ContextMenu();
        MenuItem itemDetalle = new MenuItem("Ver Detalle del Pedido");
        MenuItem itemJasper = new MenuItem("Exportar a JasperReport");
        itemDetalle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Order pedido = tableOrders.getSelectionModel().getSelectedItem();
                if (pedido != null) {
                    abrirDetalle(pedido);
                }
            }
        });

        itemJasper.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleInformeTecnico(event);
            }
        });

        contextMenu.getItems().addAll(itemDetalle, itemJasper);
        tableOrders.setContextMenu(contextMenu);
        Profile userLogged = UserSession.getInstance().getUser();
        if (userLogged != null) {
            allShops = dao.getHistory(userLogged.getId());
            logger.logInfo("Historial cargado correctamente para el usuario ID: " + userLogged.getId() + ". Pedidos encontrados: " + allShops.size());
            if (allShops != null) {
                tableOrders.getItems().setAll(allShops);
                logger.logInfo("Historial cargado: " + allShops.size() + " pedidos encontrados.");
            } else {
                logger.logInfo("El usuario ID: " + userLogged.getId() + " no tiene pedidos en su historial.");
            }
        } else {
            logger.logWarning("Se intentó cargar el historial sin una sesión de usuario activa.");
        }
    }

    /**
     * Gestiona el evento de clic sobre una fila de la tabla. Si se detecta un
     * doble clic, abre la ventana de detalles del pedido.
     *
     * * @param event El evento de ratón disparado.
     */
    @FXML
    private void clickFila(MouseEvent event) {
        // AQUÍ ESTÁ EL TRUCO:
        // Preguntamos al evento: "¿El contador de clics es igual a 2?"
        if (event.getClickCount() == 2) {

            // 1. Cogemos el pedido que el usuario ha pinchado
            Order pedido = tableOrders.getSelectionModel().getSelectedItem();

            // 2. Seguridad: Si ha hecho doble clic en una fila válida (no en blanco)
            if (pedido != null) {
                // 3. Abrimos la ventana
                abrirDetalle(pedido);
            }
        }
    }

    /**
     * Abre una nueva ventana mostrando el desglose detallado del pedido
     * seleccionado.
     *
     * * @param order El pedido del cual se mostrarán los detalles.
     */
    private void abrirDetalle(Order order) {
        try {
            logger.logInfo("Abriendo detalle del pedido ID: " + order.getIdOrder());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OrderDetail.fxml"));
            Parent root = loader.load();
            OrderDetailController controller = loader.getController();
            controller.setOrderData(order);

            Stage stage = new Stage();
            stage.setTitle("Detalle Pedido " + order.getIdOrder());
            stage.setScene(new Scene(root));
            stage.show();
            logger.logInfo("Ventana de detalles del pedido ID: " + order.getIdOrder() + " desplegada con éxito.");
        } catch (IOException ex) {
            logger.logSevere("Error al abrir la ventana de detalle de pedido", ex);
            ex.printStackTrace();
        }
    }

    /**
     * Redirige al usuario a la ventana del menú principal.
     *
     * * @param event El evento de acción disparado.
     */
    @FXML
    private void volver(ActionEvent event) {
        logger.logInfo("El usuario regresa al menú principal desde el historial.");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MenuWindow.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            logger.logSevere("Error al intentar regresar al MenuWindow.fxml", e);
            e.printStackTrace();
        }
    }

    /**
     * Redirige al usuario desde la ventana de historial hacia la ventana del
     * carrito de compras.
     * <p>
     * Este método carga el FXML del carrito, accede a su controlador para
     * configurar el modo del encabezado (Header) y cambia la escena actual.
     * </p>
     *
     * * @param event El evento de acción disparado por el botón o elemento de
     * menú.
     */
    @FXML
    private void goToCart(ActionEvent event) {
        try {
            logger.logInfo("Llendo al carrito de compra desde el historial.");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ShoppingCart.fxml"));
            Parent root = loader.load();
            ShoppingCartController controller = loader.getController();
            controller.headerController.setMode(UserSession.getInstance().getUser(), "buying");
            Stage stage = (Stage) tableOrders.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finaliza la ejecución de la aplicación.
     *
     * * @param event Evento de acción disparado.
     */
    @FXML
    private void handleExit(ActionEvent event) {
        javafx.application.Platform.exit();
        System.exit(0);
    }

    /**
     * Cierra la sesión del usuario actual y redirige a la pantalla de Login.
     *
     * * @param event Evento de acción disparado.
     */
    @FXML
    private void handleLogOut(ActionEvent event) {
        logger.logInfo("Cerrando sesión desde el historial.");
        UserSession.getInstance().cleanUserSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/LogInWindow.fxml"));
            Stage stage = (Stage) tableOrders.getScene().getWindow();
            stage.setScene(new Scene(root));
            logger.logInfo("Sesión cerrada y redirección a Login completada.");
        } catch (IOException e) {
            logger.logSevere("Fallo en la redirección a Login tras cerrar sesión.", e);
            e.printStackTrace();
        }
    }

    /**
     * Genera un informe técnico en PDF utilizando JasperReports basado en el
     * historial. Establece conexión JDBC y carga el recurso .jrxml.
     *
     * * @param event El evento de acción disparado por el botón o menú.
     */
    @FXML
    private void handleInformeTecnico(ActionEvent event) {
        Connection con = null;
        try {
            logger.logInfo("Iniciando generación de JasperReport técnico desde historial.");
            // 1. Obtener los datos de conexión desde tu configuración de Hibernate o manual
            // Nota: JasperReports necesita una conexión JDBC estándar
            String url = "jdbc:mysql://localhost:3306/bookstore?useSSL=false&serverTimezone=Europe/Madrid";
            String user = "root";
            String pass = "abcd*1234"; // Usa tu contraseña real

            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, user, pass);

            // 2. Localizar el archivo del informe (.jrxml o .jasper)
            // Si quieres usar el mismo que en el carrito:
            InputStream reportStream = getClass().getResourceAsStream("/reports/InformeTecnico.jrxml");

            if (reportStream == null) {
                logger.logSevere("No se pudo encontrar el archivo del reporte en /reports/", null);
                return;
            }

            // 3. (Opcional) Pasar parámetros al reporte, como el ID del usuario actual
            Map<String, Object> parameters = new HashMap<>();
            Profile loggedUser = UserSession.getInstance().getUser();
            if (loggedUser != null) {
                parameters.put("USUARIO_ID", loggedUser.getId());
            }

            // 4. Compilar, llenar y mostrar
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, con);

            JasperViewer.viewReport(jasperPrint, false); // false para no cerrar toda la app al cerrar el visor
            logger.logInfo("Reporte de historial generado para el usuario: " + loggedUser.getName());

        } catch (Exception e) {
            logger.logSevere("Error al generar el reporte Jasper en historial", e);
            e.printStackTrace();
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Abre el manual de usuario en formato PDF utilizando el visor
     * predeterminado del sistema.
     *
     * * @param event Evento de acción disparado.
     */
    @FXML
    private void handleHelpAction(ActionEvent event) {
        try {
            logger.logInfo("El usuario ha solicitado abrir el Manual de Usuario.");
            // 1. Ruta al PDF del Manual (Asegúrate de que el archivo se llame así en src/documents)
            String resourcePath = "/documents/Manual_Usuario.pdf";

            // 2. Cargar archivo
            InputStream pdfStream = getClass().getResourceAsStream(resourcePath);

            if (pdfStream == null) {
                logger.logSevere("No se pudo localizar el manual PDF en la ruta: " + resourcePath, null);
                showAlert("Error: No se encuentra el manual en: " + resourcePath, Alert.AlertType.ERROR);
                return;
            }

            // 3. Crear temporal y abrir
            File tempFile = File.createTempFile("Manual_Usuario", ".pdf");
            tempFile.deleteOnExit();
            Files.copy(pdfStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            } else {
                showAlert("No se puede abrir el PDF automáticamente.", Alert.AlertType.ERROR);
            }
            logger.logInfo("Manual de usuario abierto en el visor del sistema.");
        } catch (Exception e) {
            logger.logSevere("Error al intentar abrir el archivo PDF de ayuda.", e);
            e.printStackTrace();
            showAlert("Error al abrir el manual: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Muestra una ventana de diálogo de alerta al usuario.
     *
     * * @param message Mensaje a mostrar.
     * @param alertType Tipo de alerta (ERROR, INFORMATION, etc).
     */
    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Mensaje");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
