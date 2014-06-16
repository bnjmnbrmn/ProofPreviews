/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uiowa.cs.proofpreviews;

import edu.uiowa.cs.itpwrapping.ITPListener;

/**
 *
 * @author hde
 */
public interface ITPListenerPP extends ITPListener {
    public void proofPreviewsReceived(ProofPreviewList pps);
}
