/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uiowa.cs.proofpreviews;

/**
 *
 * @author hde
 */
public class PreviewPair {
    String tactic;
    String goal;
    
    public PreviewPair() {}
    public PreviewPair(String tac, String goal) {
        tactic = tac;
        this.goal = goal;
    }
    
    public String toString() {
        return tactic;
    }
}
