/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uiowa.cs.proofpreviews;

import static edu.uiowa.cs.coqtopwrapping.TestCoqtopWrapperImp.test3;
import edu.uiowa.cs.itpwrapping.ITPOutputEvent;
import java.io.IOException;

/**
 *
 * @author hde
 */
public class PPTest {

    public static class Listener implements ITPListenerPP {

       @Override
        public void errorEventReceived(ITPOutputEvent event) {
            System.out.println("\nPAYLOAD: \n"+event.getPayload());
            System.out.println("\nNEW STATE: "+event.getState()+"\n");
        }

        @Override
        public void standardEventReceived(ITPOutputEvent event) {
            System.out.println("\nPAYLOAD: \n"+event.getPayload());
            System.out.println("\nNEW STATE: "+event.getState()+"\n");
        }

        @Override
        public void proofPreviewsReceived(ProofPreviewList pps) {
            System.out.println("\nPPS Received!");
            System.out.println(pps.toString());
        }
    }
       
    public static void main(String[] args) throws IOException, InterruptedException {
        ProofPreviewsWrapper coqtop = new ProofPreviewsWrapper("coqtop");
        
        coqtop.registerListener(new PPTest.Listener());
        coqtop.registerPPListener(new PPTest.Listener());
        
        //test1(coqtop);
        //testPPSintros(coqtop);
        testPPSapply(coqtop);
	//testNonProofMode(coqtop);
        
        // Wait for the output.
        Thread.sleep(500);
        System.out.println("Hit shutdown.");
        coqtop.shutdownITP();
    }
    
    // Tests proof previews - apply.
    public static void testPPSapply(ProofPreviewsWrapper coqtop) throws InterruptedException {
        coqtop.sendToITP("Lemma a : 1 = 1 -> 1 = 1.", 1); 
        coqtop.sendToITP("Proof.", 2);
        coqtop.sendToITP("intros.", 3);
        Thread.sleep(100);
        int r = coqtop.requestProofPreviewList();
        if (r == -1)
            System.out.println("Cannot do proof previews. ");
        coqtop.sendToITP("auto.", 3); 
        coqtop.sendToITP("Qed.", 4);
    }
    
    // Tests proof previews - intros.
    public static void testPPSintros(ProofPreviewsWrapper coqtop) throws InterruptedException {
        coqtop.sendToITP("Lemma a : 1 = 1.", 1); 
        coqtop.sendToITP("Proof.", 2);
        Thread.sleep(100);
        int r = coqtop.requestProofPreviewList();
        if (r == -1)
            System.out.println("Cannot do proof previews. ");
        coqtop.sendToITP("auto.", 3); 
        coqtop.sendToITP("Qed.", 4);
        
        Thread.sleep(500);
        coqtop.sendToITP("Lemma b : 1 = 1 -> 1 = 1.", 1); 
        coqtop.sendToITP("Proof.", 2);
        Thread.sleep(300);
        r = coqtop.requestProofPreviewList();
        if (r == -1)
            System.out.println("Cannot do proof previews. ");
        coqtop.sendToITP("auto.", 3); 
        coqtop.sendToITP("Qed.", 4);
        
        Thread.sleep(500);
        coqtop.sendToITP("Lemma c : 1 = 1 -> 2 = 2 -> 3 = 3 -> 4 = 4 -> 1 = 1 -> 1 = 1.", 1); 
        coqtop.sendToITP("Proof.", 2);
        coqtop.sendToITP("intros H0.", 3);
        coqtop.sendToITP("intros A.", 4);
        coqtop.sendToITP("intros H1.", 3);
        Thread.sleep(300);
        r = coqtop.requestProofPreviewList();
        if (r == -1)
            System.out.println("Cannot do proof previews. ");
        coqtop.sendToITP("auto.", 3); 
        coqtop.sendToITP("Qed.", 4);
    }
    
    // Tests basic Coqtop (no proof previews).
    public static void test1(ProofPreviewsWrapper coqtop) throws InterruptedException {
        coqtop.sendToITP("Inductive Coqbool :  Set := true:Coqbool | false:Coqbool.", 1);
        coqtop.sendToITP("Definition succ := fun (x : nat) => x + 1.", 2);
        coqtop.sendToITP("Print succ.", 3);
        Thread.sleep(500);
        coqtop.back(2);
        coqtop.sendToITP("Print succ.", 2);
        
        coqtop.sendToITP("Axiom rw1 : 1 = 2.", 3);
        coqtop.sendToITP("Axiom rw2 : 2 = 1.", 4);
        coqtop.sendToITP("Hint Rewrite rw1 : db1.", 5);
        coqtop.sendToITP("Hint Rewrite rw2 : db1.", 6);
        coqtop.sendToITP("Lemma t : 2 = 1.", 7);
        coqtop.sendToITP("Proof.", 8);
        coqtop.sendToITP("autorewrite with db1.", 9);
        Thread.sleep(500);
        coqtop.back(2);
        coqtop.sendToITP("Print succ.", 2);
        
        coqtop.sendToITP("admit.", 3);
        coqtop.sendToITP("Qed.", 4);
        
        coqtop.sendToITP("Lemma a : 1 = 1.", 5); 
        coqtop.sendToITP("Proof.", 6);
        coqtop.sendToITP("auto.", 7); 
        coqtop.sendToITP("Qed.", 8);
	
        coqtop.sendToITP("Print a.", 9);
        coqtop.sendToITP("Print a.",10);
        coqtop.sendToITP("Print a.",11);
        coqtop.sendToITP("Print a.",12);
    }
    
    public static void testNonProofMode(ProofPreviewsWrapper coqtop) {
	    int r = coqtop.requestProofPreviewList();
	    if (r == -2) {
		    System.out.println("Success!  Non-proof-mode detected");
	    }
    }
   
}
