/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uiowa.cs.proofpreviews;

import edu.uiowa.cs.itpwrapping.ITPWrapper;

/**
 *
 * @author hde
 */
public interface ITPWrapperPP extends ITPWrapper {
    void registerPPListener(ITPListenerPP listener);
    
    /*
       - Only allow proof previews when the queue is empty.
       - Only in proof mode.
          - When in neither of this return -1 and -2 resp.         
     */
    int requestProofPreviewList();
}
