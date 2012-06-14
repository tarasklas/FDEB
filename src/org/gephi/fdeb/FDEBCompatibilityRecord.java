/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb;

import org.gephi.graph.api.Edge;

/**
 *
 * @author Администратор
 */
public class FDEBCompatibilityRecord {
    double compatibility;
    Edge edgeWith;

    public FDEBCompatibilityRecord(double compatibility, Edge edgeWith) {
        this.compatibility = compatibility;
        this.edgeWith = edgeWith;
    }
    
}
