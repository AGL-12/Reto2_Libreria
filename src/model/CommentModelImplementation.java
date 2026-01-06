/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author mikel
 */
public class CommentModelImplementation implements ICommentModel {

    // LOGGING: Cumple el requisito de uso de bitácoras (IL5.5)
    private static final Logger LOGGER = Logger.getLogger(CommentModelImplementation.class.getName());
    
    // Simulación de Base de Datos
    private List<Commentate> listComments;

    public CommentModelImplementation() {
        this.listComments = new ArrayList<>();
        loadDummyData();
    }

    private void loadDummyData() {
        long now = System.currentTimeMillis();
        Timestamp ts = new Timestamp(now);

        // Tus datos de prueba originales
        listComments.add(new Commentate(1, 101, "¡Me ha encantado este libro!", ts, 5, "Mikel (Teacher)"));
        listComments.add(new Commentate(2, 101, "Un poco lento al principio.", ts, 3, "Ana García"));
        listComments.add(new Commentate(3, 101, "El envío fue muy rápido.", ts, 4, "Jon Pérez"));
        
        LOGGER.info("Datos dummy cargados en memoria correctamente.");
    }

    @Override
    public List<Commentate> getComments() {
        return listComments;
    }

    @Override
    public void addComment(Commentate comment) {
        if (comment != null) {
            listComments.add(comment);
            LOGGER.info("Nuevo comentario añadido: " + comment.getTempUsername());
        }
    }

    @Override
    public void deleteComment(Commentate comment) {
        if (comment != null) {
            listComments.remove(comment);
            LOGGER.info("Comentario eliminado.");
        }
    }
}