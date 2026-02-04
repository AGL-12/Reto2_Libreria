package util;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogInfo {

    private static Logger ourLogger;
    private static LogInfo instance;

    // Carpetas y archivos
    private static final String LOG_FOLDER = "logs";
    private static final String LOG_FILE = "logs/aplicacion.log";
    private static final String FILE_ERROR = "logs/errores.log";

    private LogInfo() {
        try {
            // 1. Crear carpeta si no existe
            File carpeta = new File(LOG_FOLDER);
            if (!carpeta.exists()) {
                carpeta.mkdir();
            }

            // 2. OBTENER EL LOGGER RAÍZ (ROOT)
            // Al usar "", capturamos todo lo que pase en la JVM (incluido Hibernate)
            ourLogger = Logger.getLogger("");

            // Opcional: Si quieres ver los logs de Hibernate detallados, ajusta el nivel aquí
            // Level.INFO muestra arranque y paradas. 
            // Level.FINE muestra SQL (pero genera MUCHO texto).
            ourLogger.setLevel(Level.INFO);

            // 3. Configurar el archivo para INFO y general
            // 'true' = append (no borra el fichero al arrancar, añade al final)
            FileHandler infoHandler = new FileHandler(LOG_FILE, true);
            infoHandler.setFormatter(new SimpleFormatter());
            infoHandler.setLevel(Level.INFO);
            ourLogger.addHandler(infoHandler);

            // 4. Configurar el archivo para ERRORES
            FileHandler errorHandler = new FileHandler(FILE_ERROR, true);
            errorHandler.setFormatter(new SimpleFormatter());
            errorHandler.setLevel(Level.WARNING); // Solo avisos y errores graves
            ourLogger.addHandler(errorHandler);

        } catch (IOException e) {
            System.err.println("Error al crear el sistema de logs: " + e.getMessage());
        }
    }

    public static LogInfo getInstance() {
        if (instance == null) {
            instance = new LogInfo();
        }
        return instance;
    }

    // Métodos para usar tú manualmente
    public void logInfo(String mensaje) {
        ourLogger.info("MI APP: " + mensaje);
    }

    public void logWarning(String mensaje) {
        ourLogger.warning("MI APP: " + mensaje);
    }

    public void logSevere(String mensaje, Exception e) {
        ourLogger.log(Level.SEVERE, "MI APP: " + mensaje, e);
    }
}
