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
import net.sf.jasperreports.engine.JasperCompileManager;
import util.LogInfo;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.Alert;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

/**
 * FXML Controller class
 *
 * @author ander
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
    @FXML
    private VBox mainVBox;
    @FXML
    private Menu menuAcciones;
    @FXML
    private MenuItem iManual;
    @FXML
    private MenuItem iJasper;
    @FXML
    private HBox headerHBox;
    @FXML
    private Label headerLabel;
    @FXML
    private Label infoLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.logInfo("Accediendo a la ventana de Historial de Compras.");
        // 1. Vinculamos columnas con los atributos del modelo Order.java
        colId.setCellValueFactory(new PropertyValueFactory<>("idOrder"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // 2. Obtenemos el usuario de la sesión
        Profile userLogged = UserSession.getInstance().getUser();

        if (userLogged != null) {
            // 3. Cargamos los datos desde el DAO
            allShops = dao.getHistory(userLogged.getId());

            if (allShops != null) {
                tableOrders.getItems().setAll(allShops);
                logger.logInfo("Historial cargado: " + allShops.size() + " pedidos encontrados para el usuario " + userLogged.getName());
            }
        }
    }

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

    private void abrirDetalle(Order order) {
        try {
            logger.logInfo("Abriendo detalle del pedido ID: " + order.getIdOrder());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OrderDetail.fxml"));
            Parent root = loader.load();

            // Pasar los datos al controlador de la ventana nueva
            OrderDetailController controller = loader.getController();
            controller.setOrderData(order);

            // Mostrar la ventana
            Stage stage = new Stage();
            stage.setTitle("Detalle Pedido " + order.getIdOrder());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException ex) {
            logger.logSevere("Error al abrir la ventana de detalle de pedido", ex);
            ex.printStackTrace();
        }
    }

    @FXML
    private void volver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MenuWindow.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToCart(ActionEvent event) {
        try {
            logger.logInfo("Cerrando sesión desde el historial.");
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

    @FXML
    private void handleExit(ActionEvent event) {
        javafx.application.Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleLogOut(ActionEvent event) {
        logger.logInfo("Cerrando sesión desde el historial.");
        UserSession.getInstance().cleanUserSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/LogInWindow.fxml"));
            Stage stage = (Stage) tableOrders.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleInformeTecnico(ActionEvent event) {
        Connection con = null;
        try {
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

    @FXML
    private void handleHelpAction(ActionEvent event) {
        try {
            // 1. Ruta al PDF del Manual (Asegúrate de que el archivo se llame así en src/documents)
            String resourcePath = "/documents/Manual_Usuario.pdf";

            // 2. Cargar archivo
            InputStream pdfStream = getClass().getResourceAsStream(resourcePath);

            if (pdfStream == null) {
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

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error al abrir el manual: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String string, Alert.AlertType alertType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
