/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.List;

/**
 *
 * @author mikel
 */
public interface ICommentModel {
    public List<Commentate> getComments();
    public void addComment(Commentate comment);
    public void deleteComment(Commentate comment);
}