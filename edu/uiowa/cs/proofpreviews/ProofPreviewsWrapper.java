/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uiowa.cs.proofpreviews;

import edu.uiowa.cs.coqtopwrapping.CoqtopWrapperImpl;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hde
 */
public class ProofPreviewsWrapper extends CoqtopWrapperImpl implements ITPWrapperPP {
    private List<ITPListenerPP> listeners;
    private int start_state_depth;
    
    public ProofPreviewsWrapper () throws IOException {
        super();
        
        init();
    }
    
    public ProofPreviewsWrapper (String coqtoppath) throws IOException {
        super(coqtoppath);
        
        init();
    }    

    private void init() {
       // Allocate listener list.
       this.listeners = Collections.synchronizedList(new LinkedList<ITPListenerPP>()); 
       
       // Remember out state depth.
       start_state_depth = super.current_state_depth;
    }
    
    @Override
    public void registerPPListener(ITPListenerPP listener) {
        this.listeners.add(listener);
    }

    private String newHypName(String goal) {
        String[] lines = goal.split("\n");
        int hypIndex   = 0;
        
        for (int i = 0; i < lines.length; i++) {
            String[] pieces = lines[i].trim().split(" ");
            String h = pieces[0];
            if (h.length() > 1) {
                if(h.charAt(0) == 'H') {
                    int h_index = Integer.parseInt(h.substring(1));
                    if (hypIndex < h_index) {
                        hypIndex = h_index + 1;
                    }
                } else if (h.charAt(0) == '=') {
                    break;
                }
            }
        }
        return ("H"+hypIndex);
    }
    
    private void undo_last() throws IOException {
        // Always go back to the start.
        //super.back(this.start_state_depth, false);
        super.sent_evaluating = true;
        super.coqtopRWRunnable.sendToCoq("BackTo "+this.start_state_depth+".");
        super.coqtopRWRunnable.readFromCoq();
        super.sent_evaluating = false;
        // Give back some time to complete.
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(ProofPreviewsWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private synchronized String get_goal() throws IOException {
      super.sent_evaluating = true;
      super.coqtopRWRunnable.sendToCoq("Show.");
     
      String goal = super.coqtopRWRunnable.readFromCoq();
      super.sent_evaluating = false;
      
      undo_last();
      
      return goal;
    }
    
    private String try_tactic(String tactic) throws IOException {
        // Try to do an intros.
        while (!super.steList.isEmpty() || super.sent_evaluating) {}
        super.sent_evaluating = true;
        super.coqtopRWRunnable.sendToCoq(tactic);
        
        // Read the result.
        String result = super.coqtopRWRunnable.readFromCoq();
        super.sent_evaluating = false;
        
        if (result.toLowerCase().contains("error")) {
            return "error";
        } else {
            undo_last();
        }
        
        return result;
    }
    
    private PreviewPair try_intros() throws IOException {
        String goal;
        String tactic;
        
        PreviewPair p = new PreviewPair();
        
        /* Get the current goal. Note: This does not
           modify the state. */
        goal = get_goal();
        
        // No focused proof.
        if (goal.toLowerCase().contains("error")) {
            p.goal = "-1";
            return p;
        }
        
        // Compute a proper hyp. name. 
        String hypname = newHypName(goal);
        tactic = "intros "+hypname+".";
        String r = try_tactic(tactic);
        if (r.toLowerCase().contains("error"))
            p.goal = "-1";
        else {
            p.goal = r;
            p.tactic = tactic;
        }
        
        return p;
    }
    
    private static class Pair <T1,T2> {
        public T1 fst;
        public T2 snd;
        
        public Pair(T1 fst, T2 snd) {
            this.fst = fst;
            this.snd = snd;
        }
    }

    private static class StringPair extends Pair <String,String> {
        StringPair(String fst, String snd) {
            super(fst, snd);
        }
    }
    
    private List<StringPair> get_hypstype(String goal) {
        // The list of hypotheses.
        List<StringPair> hyps = Collections.synchronizedList(new LinkedList<StringPair>());
        
        // Parse the goal for all the hypothesis.
        String[] lines = goal.split("\n");
        
        for (int i = 0; i < lines.length - 1; i++) {
            String[] pieces = lines[i].trim().split(":");
            if(pieces.length > 1) {
                hyps.add(new StringPair (pieces[0].trim(),pieces[1].trim()));
            } else if (pieces[0].startsWith("========")) {
                break;
            }
        }
        
        return hyps;
    }
    
    private List<String> get_hyps(String goal) {
        // The list of hypotheses.
        List<StringPair> hypstypes = get_hypstype(goal);
        List<String> hyps = Collections.synchronizedList(new LinkedList<String>());
        
        /* This is the same as [map fst hypstypes] in Haskell.
         * It is a pain I have to do this in this way. */
        for (int i = 0; i < hypstypes.size(); i++) {
            hyps.add(hypstypes.get(i).fst);
        }
        
        return hyps;
    }
    
    protected void log_list(List<String> l) {
        for (int i = 0; i < l.size(); i++) {
            log(l.get(i));
        }
    }
    
    protected void log_tacs_list(List<PreviewPair> l) {
        for (int i = 0; i < l.size(); i++) {
            log(l.toString());
        }
    }
    
    private List<PreviewPair> try_tactic_hyps (String part_tactic) throws IOException {
        String goal;
        String tactic;
        
        List<PreviewPair> tacs_apply = Collections.synchronizedList(new LinkedList<PreviewPair>());
        
        /* Get the current goal. Note: This does not
           modify the state. */
        goal = get_goal();
        
        // No focused proof.
        if (goal.toLowerCase().contains("error")) {
            return tacs_apply;
        }
        
        List<String> hyps = get_hyps(goal);
        for (int i = 0; i < hyps.size(); i++) {
            tactic = part_tactic+hyps.get(i)+".";
            
            // Try to apply the tactic.
            String r = try_tactic(tactic);
            if (!r.toLowerCase().contains("error")) {
                tacs_apply.add(new PreviewPair(tactic,r));
            }
        }
        return tacs_apply;
    }
    
    private PreviewPair try_basic(String tactic) throws IOException {
        String goal;
        
        /* Get the current goal. Note: This does not
           modify the state. */
        goal = get_goal();
        
        // No focused proof.
        if (goal.toLowerCase().contains("error")) {
            return null;
        }
        
        PreviewPair p = new PreviewPair();
        
        String r = try_tactic(tactic);
        
        if (!r.toLowerCase().contains("error")) {
                p.goal = r;
                p.tactic = tactic;
        } else {
            p = null;
        }
        
        return p;
    }
    
    private List<PreviewPair> try_apply() throws IOException {
        return try_tactic_hyps("apply ");
    }
    
    private PreviewPair try_assumption() throws IOException {
        return try_basic ("assumption.");
    }
    
    private PreviewPair try_left() throws IOException {
        return try_basic ("left.");
    }
    
    private PreviewPair try_right() throws IOException {
        return try_basic ("right.");
    }
    
    private PreviewPair try_split() throws IOException {
        return try_basic ("split.");
    }
    
    private List<PreviewPair> try_unfold() throws IOException {
        // Try to unfold not in all of the hyps.
        List<PreviewPair> unfolds_apply = try_tactic_hyps("unfold not in ");
        
        // Try to unfold not in the goal.
        PreviewPair p = try_basic("unfold not.");
        
        if (p != null) {
            unfolds_apply.add(p);
        }
        
        return unfolds_apply;
    }
    
    private List<PreviewPair> try_destruct() throws IOException {
        String goal = get_goal();
        
        // No focused proof.
        if (goal.toLowerCase().contains("error")) {
            return null;
        }
        
        List<PreviewPair> tacs = Collections.synchronizedList(new LinkedList<PreviewPair>());
        
        // Get hyps+types.
        List<StringPair> hyptypes = get_hypstype(goal);
        
        // Get a new hyp. name.
        String H1 = newHypName(goal);
        String H2 = "H"+(Integer.parseInt((H1.charAt(1)+""))+1);
        
        // Find each a hyp. that has a [/\] in it.
        for (int i = 0; i < hyptypes.size(); i++) {
            String hyp = hyptypes.get(i).fst;
            String type = hyptypes.get(i).snd;
            
            if (type.contains("/\\")) {
                String tac = "destruct "+hyp+" as ["+H1+" "+H2+"].";
                // Try the to destruct this hyp.
                PreviewPair p = try_basic(tac);
                if (p != null) {
                    tacs.add(p);
                }
            }
        }
        
        return tacs;
    }
    
    /** Notifies all the listeners of an event. 
     * 
     * @param event the event which took place.
     * @param error true if this is an error event false otherwise.
     */
    private synchronized void notifyListeners(ProofPreviewList pps) {
        final ProofPreviewList ppss = pps;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < listeners.size(); i++) {
                    listeners.get(i).proofPreviewsReceived(ppss);
                }
            }
        });
        t.start();
    }

    @Override
    /**
     * @return: -1 on failure and 0 otherwise.
     */
    @SuppressWarnings("empty-statement")
    public synchronized int requestProofPreviewList() {
        
        if (super.show_debug_info) {
            log("\n\n>>>>>>>>>>>>>>>>>>>>>\nProof Previews Output:\n");
            log("State depth: "+super.current_state_depth);
        }
        
        // Set the starting state.
        start_state_depth = super.current_state_depth;
        
        // If we are not all green, then we fail.
        if (!super.steList.isEmpty()) {
            return -1;
        }
        
        ProofPreviewList applicable_tacs = new ProofPreviewList();
        

        try {
            PreviewPair p;
            
            p = try_intros();
            if (!p.goal.equals("-1")) {
                applicable_tacs.previews.add(p);
            }
            
            List<PreviewPair> ps = try_apply();
            if (ps.size() > 0) {
                applicable_tacs.previews.addAll(ps);
            }
            
            p = try_assumption();
            if (p != null) {
                applicable_tacs.previews.add(p);
            }
            
            p = try_left();
            if (p != null) {
                applicable_tacs.previews.add(p);
            }
            
            p = try_right();
            if (p != null) {
                applicable_tacs.previews.add(p);
            }
            
            p = try_split();
            if (p != null) {
                applicable_tacs.previews.add(p);
            }
            
            ps = try_unfold();
            if (ps.size() > 0) {
                applicable_tacs.previews.addAll(ps);
            }
            
            ps = try_destruct();
            if (ps.size() > 0) {
                applicable_tacs.previews.addAll(ps);
            }
        } catch (IOException ex) {
            Logger.getLogger(ProofPreviewsWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        // Notify listeners.
        this.notifyListeners(applicable_tacs);
        
        // Return success.  
        return 0;
    }
}
