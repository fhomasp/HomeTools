package com.peeterst.listener;

import javafx.scene.Node;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 10/03/13
 * Time: 18:13
 * To change this template use File | Settings | File Templates.
 */
public interface DockletListener {
    //Dock item being added
    void added(Node n);
    //Dock item being removed
    void removed(String id);

}
