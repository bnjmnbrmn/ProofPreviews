/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uiowa.cs.proofpreviews;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author hde
 */
public class ProofPreviewList {
    protected List<PreviewPair> previews; 
    
    public ProofPreviewList() {
        previews = Collections.synchronizedList(new LinkedList<PreviewPair>()); 
    }
    
    @Override
    public String toString() {
        String r = "";
        for (int i = 0; i < previews.size(); i++) {
            r += "Goal: \n"+previews.get(i).goal+"\n\nTactic: "+previews.get(i).tactic+"\n\n";
        }
        return r;
    }
}
