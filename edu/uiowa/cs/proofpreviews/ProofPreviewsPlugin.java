package edu.uiowa.cs.proofpreviews;

import edu.uiowa.cs.coqtopwrapping.CoqtopListener;
import edu.uiowa.cs.coqtopwrapping.CoqtopWrapper;
import edu.uiowa.cs.coqtopwrapping.CoqtopWrapperImpl;
import edu.uiowa.cs.itpwrapping.ITPOutputEvent;
import edu.uiowa.cs.coqedit.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBPlugin;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.CompletionPopup;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.TextAreaPainter;

/**
 *
 * @author Benjamin Berman
 */

public class ProofPreviewsPlugin extends EBPlugin 
		implements CoqtopResponseResponderFactory /*implements ITPListenerPP*/ {
	public static String NAME = "proofpreviews";
	public static String AUTHOR = "Benjamin Berman";
	public static String VERSION = "0.0.1";
	public static String OPTION_PREFIX = "options.proofpreviews.";
	
	private static ProofPreviewsPlugin INSTANCE;

	public static ProofPreviewsPlugin getInstance() {
		return INSTANCE; 
	}
//	 
//	private View view;
//	private EditPane editPane;
//	private JEditTextArea textArea;
//	private Buffer buffer;
//	private TextAreaPainter tap;
	
	
//	public View getView() {return this.view;}
//	public EditPane getEditPane() {return this.editPane;}
//	public JEditTextArea getTextArea() {return this.textArea;}
//	public Buffer getBuffer() {return this.buffer;}
//	public TextAreaPainter getTextAreaPainter() {return this.tap;}
	
//	private ProofPreviewsWrapper ppw;
//	private SentenceOffsetFinder sentenceOffsetFinder;
//	private SectionHighlighter queuedSectionHighlighter;
//	private SectionHighlighter evaluatedSectionHighlighter;
//	private BufferLocker bufferLocker;
	
	@Override
	public void start() {
		INSTANCE = this;
		
//		view = jEdit.getActiveView();
//		editPane = view.getEditPane();
//		textArea = editPane.getTextArea();
//		buffer = editPane.getBuffer();
//		tap = textArea.getPainter();	
//		
//		try {
//			ppw = new ProofPreviewsWrapper();
//			CoqEditPlugin.getInstance().resetCoqtopWrapper(ppw);
//			ppw.registerPPListener(this);
//		} catch (IOException ex) {
//			//Logger.getLogger(ProofPreviewsPlugin.class.getName()).log(Level.SEVERE, null, ex);
//		}
		
		
		
	}
	
	
	@Override
	public void stop() {
		
	}
	
	ProofPreviewsCompletionPopup ppcp;
	ProofPreviewList pps;
	
	BasicCoqEditController coqEditController;
	BasicCoqEditModel coqEditModel;
	BasicCoqEditView coqEditView;
	BasicCoqtopWrapper coqtopWrapper;
	
	Point popupLocation;
	
	private boolean lastEvaluatedSentenceResultsInGoal() {
		int lesn = coqEditModel.getLastEvaluatedSentenceNumber();
		CoqtopResponse lastCachedResponse;
		try {
			lastCachedResponse = coqEditModel.getCachedResponseForSentence(lesn);
		} catch (NoCachedSentenceException ex) {
			Macros.message(jEdit.getActiveView(), "Something went wrong while trying to see "
					+ "if the last evaluated sentence is in proof mode");
			return false;
		}
		
		return coqtopWrapper.isGoalResponse(lastCachedResponse);
		
		//return coqEditController.getCoqtopWrapper().isProofModeResponse(lastCachedResponse);
	}
	
	private boolean processingSomething() {
		boolean A = coqEditModel.getDequeueCount() > 0;
		boolean B = coqEditModel.getSentenceQueue().size() > 0;
		boolean C = coqEditModel.getInterruptCount() > 0;
		return A || B || C;
	}
	
	private void updateMVCandWrapper() {
		coqEditController = CoqEditPlugin.getInstance().getCurrentController();
		if (coqEditController == null)
			return;
		coqEditModel = coqEditController.getCoqEditModel();
		coqEditView = coqEditController.getCoqEditView();
		coqtopWrapper = coqEditController.getCoqtopWrapper();
	}
	
	private void moveCursor() {
		int lesn = coqEditModel.getLastEvaluatedSentenceNumber();
		int endOffset;
		try {
			endOffset = coqEditModel.getEndOffset(lesn);
		} catch (NoOffsetForSentenceNumberException ex) {
			Macros.message(jEdit.getActiveView(), "Something went wrong while trying to move "
					+ "the cursor to the end of the last evaluated sentence");
			return;
		}
		JEditTextArea textArea = jEdit.getActiveView().getTextArea();
		textArea.setCaretPosition(endOffset, true);
	}
	
	private Point getPopupLocation() {
		JEditTextArea textArea = jEdit.getActiveView().getTextArea();
		int caret = textArea.getCaretPosition();
		Point location = textArea.offsetToXY(caret);
		TextAreaPainter tap = textArea.getPainter();
		location.y += tap.getFontMetrics().getHeight();
		SwingUtilities.convertPointToScreen(location, tap);
		
		return location;
	}
	
	public void getSuggestions() {
		
		updateMVCandWrapper();
		if (coqEditController == null) {
			return;
		}
		
		synchronized (coqEditModel) {
			
			if (coqEditModel.getErrorSentenceNumber() != -1) { //if there is an error sentence
				//if currently viewing error sentence, set currently viewing sentence to last evaluated sentence
				if (coqEditModel.getCurrentlyViewingSentenceNumber() == coqEditModel.getErrorSentenceNumber()) {
					coqEditModel.setCurrenlyViewingSentenceNumber(coqEditModel.getLastEvaluatedSentenceNumber());
				}
				//set error sentence to -1
				coqEditModel.setErrorSentenceNumber(-1);
				//remove last cached response
				coqEditModel.removeResponsesFromCacheAtIndicesGreaterThanOrEqualTo(
						coqEditModel.getResponseCacheSize() - 1);
			}
			

			if (processingSomething()) {

				coqEditController
						.getCoqEditView()
						.setBottomOutputText("Cannot get proof previews"
						+ " while processing.  Interrupt, then try again.");

				return;

			} else if (!lastEvaluatedSentenceResultsInGoal()) {

				coqEditController
						.getCoqEditView()
						.setBottomOutputText("The last evaluated sentence must"
						+ " result in a goal");

				return;

			}

			moveCursor();

			popupLocation = getPopupLocation();

//			Macros.message(jEdit.getActiveView(),"About to create "
//					+ "new ProofPreviewsCompletionPopup");
//			
//			ppcp = new ProofPreviewsCompletionPopup(
//					jEdit.getActiveView(), popupLocation);

//			String[] initialList = {" "};
//
//			ProofPreviewsCandidates ppc = new ProofPreviewsCandidates(initialList);
//
//			ppcp.reset(ppc, true);

			coqEditView
					.setBottomOutputText("Getting suggestions.  "
					+ "Interrupt to stop.");
			
			
			coqEditModel.setCoqtopResponseResponderFactory(this);
			
			generateInitialTacticList();
			
//			initialTacticListAttemptIndex = 0;
//			processingBackCommand = false;
			
			proofPreviewsPairs.clear();
			responseList.clear();
			
			enqueueInitialTacticListWithBackTos();
		}

		//		
	//		
	//		int errorCode = ppw.requestProofPreviewList();
	//		
	//		if (errorCode == -1) {
	//			Macros.message(view, "error: queue not empty");
	//		} else if (errorCode == -2) {
	//			Macros.message(view, "error: not in proof mode");
	//		}
	//		
	//		String[] availableTactics = {"apple","banana","orange","mango"};
	//		
	//		ProofPreviewsCandidates ppc = new ProofPreviewsCandidates(availableTactics);
	//
		
			
	}
	
	private CoqtopResponse initialGoal;
	
	private List<String> currentGoalHypothesisNameList = new ArrayList<String>();
	private List<String> initialTacticList = new ArrayList<String>();
	private int initialTacticListAttemptIndex;
	private boolean processingBackCommand;
	
	private Map<String,CoqtopResponse> proofPreviewsPairs = new HashMap<String,CoqtopResponse>();
	private List<CoqtopResponse> responseList = new ArrayList<CoqtopResponse>();
	
	
	private void generateInitialTacticList() {
		initialTacticList.clear();
		
		//to do
		getInitialGoalAndHypothesisNames();
		
		addApplyTacticsToInitialList();
		initialTacticList.add("assumption.");
		initialTacticList.add("split.");
		initialTacticList.add("intro.");
		initialTacticList.add("intros.");
		initialTacticList.add("left.");
		initialTacticList.add("right.");
		initialTacticList.add("unfold not.");
		addUnfoldNotInTacticsToInitialList();
		addDestructTacticsToInitialList();
		
		
//		//for testing
//		String initialTacticListString = "";
//		for (String name : initialTacticList)
//			initialTacticListString = initialTacticListString + name + "\n";
//		coqEditController.setBottomOutputPanelText(initialTacticListString);
//		
	}
	
	private void addApplyTacticsToInitialList() {
		for (String hypothesisName : currentGoalHypothesisNameList) {
			initialTacticList.add("apply "+hypothesisName+".");
		}
	}
	
	private void addUnfoldNotInTacticsToInitialList() {
		for (String hypothesisName : currentGoalHypothesisNameList) {
			initialTacticList.add("unfold not in "+hypothesisName+".");
		}
	}
	
	private void addDestructTacticsToInitialList() {
		for (String hypothesisName : currentGoalHypothesisNameList) {
			initialTacticList.add("destruct "+hypothesisName+".");
		}
	}
	
	
	private void getInitialGoalAndHypothesisNames() {
		
		synchronized(coqEditModel) {
			
			coqEditModel.setProcessingSynchronously(true);
			
			coqEditModel.setCurrentResponse(null);
			
			coqEditModel.enqueueSentence("Show.");
			
			CoqtopResponse response;
			
			while ((response = coqEditModel.getCurrentResponse()) == null) {
				try {
					coqEditModel.wait();
				} catch (InterruptedException ex) {
				}
			}
			
			this.initialGoal = response;
			
			extractHypothesisNamesFromGoalResponse(response);
			
			coqEditModel.setCurrentResponse(null);
			
			//not including the "Show."
			int lesn = coqEditModel.getLastEvaluatedSentenceNumber();
			int lesnsd = coqEditModel
					.getStateDepthAfterEvaluatingSentenceNumber(lesn);
			String backCommand = coqtopWrapper.getBackCommandForStateDepth(lesnsd);
			
			coqEditModel.setIsNavigating(true);
			coqEditModel.enqueueSentence(backCommand);
		
			while ((response = coqEditModel.getCurrentResponse()) == null) {
				try {
					coqEditModel.wait();
				} catch (InterruptedException ex) {
				}
			}
			
			coqEditController.createCoqtopResponseResponder(response).run();

			coqEditModel.setCurrentResponse(null);
			coqEditModel.setIsNavigating(false);
			coqEditModel.setProcessingSynchronously(false);
			
			//for testing:
//			String nameList = "";
//			for (String name : currentGoalHypothesisNameList)
//				nameList = nameList + name + "\n";
//			coqEditController.setBottomOutputPanelText(nameList);
		}
		
	}
	
	
	
	private void extractHypothesisNamesFromGoalResponse(CoqtopResponse response) {
		currentGoalHypothesisNameList.clear();
		String goalText = response.getMessage();

		String[] lines = goalText.split("\n");

		for (int i = 0; i < lines.length - 1; i++) {
			String[] pieces = lines[i].trim().split(":");
			if (pieces.length > 1) {
				currentGoalHypothesisNameList.add(pieces[0].trim());
			} else if (pieces[0].startsWith("========")) {
				break;
			}
		}

	}

	private void enqueueInitialTacticListWithBackTos() {
		synchronized(coqEditModel) {
			coqEditModel.setIsNavigating(true);
			int lesn = coqEditModel.getLastEvaluatedSentenceNumber();
			int lesnsd = coqEditModel.getStateDepthAfterEvaluatingSentenceNumber(lesn);
			String backCommand = coqtopWrapper.getBackCommandForStateDepth(lesnsd);
			for (String tacticToTry : initialTacticList) {
				coqEditModel.enqueueSentence(tacticToTry);
				coqEditModel.enqueueSentence(backCommand);
//				pumsg("enqueued tactic: "+tacticToTry
//						+"\nenqueued back command: "+backCommand);
			}
			
			
		}
	}
	

//	public void proofPreviewsReceived(ProofPreviewList pps) {
//		this.pps = pps;
//		
//		int numPP = pps.previews.size();
//		
//		String[] availableTactics = new String[numPP];
//		
//		int i = 0;
//		for (PreviewPair pair : pps.previews) {
//			availableTactics[i] = pair.tactic;
//			i++;
//		}
//		
//		//String[] availableTactics = {"apple","banana","orange","mango"};
//		
//		
//		ProofPreviewsCandidates ppc = new ProofPreviewsCandidates(availableTactics);
//		
//		//to do: set dockable text here
//		
//		ppcp.reset(ppc, true);
//		
//		
//	}

	
	private void generateProofPreviewsPairs() {
		boolean isBackResponse = false;
		int i = 0;
		for (CoqtopResponse response : responseList) {
			if (isBackResponse) {
				isBackResponse = false;
				i++;
			} else {
				boolean responseIsGoalResponse;
				responseIsGoalResponse = coqtopWrapper.isGoalResponse(response);
				if (responseIsGoalResponse) {
					String responseMsg = response.getMessage();
					String initialGoalMsg = initialGoal.getMessage();
					boolean responseMsgMatchesInitialGoalMsg =  messagesMatch(responseMsg,initialGoalMsg);//responseMsg.equals(initialGoalMsg);
					if (!responseMsgMatchesInitialGoalMsg) {
						proofPreviewsPairs.put(initialTacticList.get(i),response);
					}
				}
				isBackResponse = true;
			}
		}
	}
	
	private boolean messagesMatch(String msg1, String msg2) {
		String[] msg1Array = msg1.split("\n");
		String[] msg2Array = msg2.split("\n");
		
		if (msg1Array.length != msg2Array.length) {
			return false;
		} else {
			for (int i = 1; i< msg1Array.length; i++) {
				if (!(msg1Array[i].equals(msg2Array[i])))
					return false;
			}
			return true;
		}
	}
	
	@Override
	public Runnable createCoqtopResponseResponder(CoqtopResponse response) {
		return new ProofPreviewsResponseResponder(response);
		
	}

	protected class ProofPreviewsResponseResponder implements Runnable {
		private final CoqtopResponse response;
		public ProofPreviewsResponseResponder(CoqtopResponse response) {
			this.response = response;
		}

		@Override
		public void run() {
			//to do: deal with interrupts...
			
			
			responseList.add(response);
			
			//for debugging
			
			
			if (responseList.size() == 2*initialTacticList.size()) {
				generateProofPreviewsPairs();
				
				synchronized(coqEditModel) {
					coqEditModel.setIsNavigating(false);
					coqEditModel.setDequeueCount(0);
					coqEditModel.setCoqtopResponseResponderFactory(coqEditController);
				}
				
				ppcp = new ProofPreviewsCompletionPopup(
					jEdit.getActiveView(), popupLocation);
				
				String[] candidateList = 
						proofPreviewsPairs.keySet().toArray(new String[0]);
				
				ProofPreviewsCandidates ppc = 
						new ProofPreviewsCandidates(candidateList);
				
				ppcp.reset(ppc, true);
				
				
				if (candidateList.length > 0) {
					String goal = 
							proofPreviewsPairs
							.get(candidateList[0])
							.getMessage();
					synchronized(coqEditModel) {
						coqEditController.setBottomOutputPanelText(goal);
					}
				}
				
				//for debugging
//				synchronized(coqEditModel) {
//					String debugmsg =
//							"coqEditModel.getLastEvaluatedSentenceNumber(): "
//							+coqEditModel.getLastEvaluatedSentenceNumber()
//							+"\ncoqEditModel.getSentenceQueue().size(): "
//							+coqEditModel.getSentenceQueue().size()
//							+"\ncoqEditModel.getLastQueuedSentenceNumber(): "
//							+coqEditModel.getLastQueuedSentenceNumber()
//							+"\ncoqEditModel.getDequeueCount(): "
//							+coqEditModel.getDequeueCount();
//					coqEditView.setBottomOutputText(debugmsg);
//				}
			} 
			
			
			
			
//			
//			if (processingBackCommand) {
//			
//				pumsg("processing response to back command");
//				
//				
//				if (initialTacticListAttemptIndex == initialTacticList.size()) {
//					synchronized(coqEditModel) {
//						if (coqEditModel
//								.getCoqtopResponseResponderFactory()
//								.equals(ProofPreviewsPlugin.this)) {
//							
//							coqEditModel.setCoqtopResponseResponderFactory(coqEditController);
//							//update popup list:
//							String[] candidateList = 
//									proofPreviewsPairs
//									.keySet()
//									.toArray(new String[0]);
//							ProofPreviewsCandidates ppc = 
//									new ProofPreviewsCandidates(candidateList);
//							ppcp.reset(ppc, true);
//						}
//						coqEditModel.setIsNavigating(false);
//					}
//				} else {
//					processingBackCommand = false;
//					initialTacticListAttemptIndex++;
//				}
//				
//			} else {
//				
//				pumsg("not processing back command");
//				
//				if (coqtopWrapper.isGoalResponse(response)) {
//					//to do:  if the response message is the same as the old, should not add to list
//					
//					String tactic = initialTacticList.get(initialTacticListAttemptIndex);
//					proofPreviewsPairs.put(tactic, response);
//					
//				}
//				
//				processingBackCommand = true;
//			}
//			
//			
//			//if all potential tactics tested, switch response responder back to the basiccoqeditController
		}
	}
	
	
	class ProofPreviewsCompletionPopup extends CompletionPopup {

		public ProofPreviewsCompletionPopup(View view, Point location) {
			super(view, location);
		}

		@Override
		public void keyPressed(KeyEvent evt) {
			String goalText;
			if (getCandidates().getSize() == 0) {
				goalText = "";
			} else if (evt.getKeyCode() == KeyEvent.VK_DOWN
				&& getSelectedIndex() < getCandidates().getSize() - 1) {
//				goalText =
//					((ProofPreviewsCandidates) getCandidates())
//					.getCandidateText(getSelectedIndex() + 1);
				String tactic = ((ProofPreviewsCandidates) getCandidates())
						.getCandidateText(getSelectedIndex()+1);
				goalText = //pps.previews.get(getSelectedIndex() + 1).goal;
						proofPreviewsPairs.get(tactic).getMessage();

			} else if (evt.getKeyCode() == KeyEvent.VK_UP
				&& getSelectedIndex() > 0) {
//				goalText =
//					((ProofPreviewsCandidates) getCandidates())
//					.getCandidateText(getSelectedIndex() - 1);
				String tactic = ((ProofPreviewsCandidates) getCandidates())
						.getCandidateText(getSelectedIndex()-1);
				goalText = //pps.previews.get(getSelectedIndex() - 1).goal;
						proofPreviewsPairs.get(tactic).getMessage();
			} else {
//				goalText =
//					((ProofPreviewsCandidates) getCandidates())
//					.getCandidateText(getSelectedIndex());
				String tactic = ((ProofPreviewsCandidates) getCandidates())
						.getCandidateText(getSelectedIndex());
				goalText = //pps.previews.get(getSelectedIndex()).goal;
						proofPreviewsPairs.get(tactic).getMessage();
			}

			//textArea.setSelectedText(" candidate text: " + goalText);
			
			coqEditController.setBottomOutputPanelText(goalText);
			
//			OutputPanel outputPanel = (OutputPanel) view.
//				getDockableWindowManager().
//				getDockableWindow("coqeditoutput");
//			
//			outputPanel.setTopPaneText(goalText);
		}
	}
	
	class ProofPreviewsCandidates implements CompletionPopup.Candidates {
		
		private DefaultListCellRenderer renderer = new DefaultListCellRenderer();
		private String[] completions;

		public ProofPreviewsCandidates(String[] completions) {
			this.completions = completions;
		}

		@Override
		public int getSize() {
			return completions.length;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public void complete(int i) {
			String insertion = "\n" + completions[i];
			JEditTextArea textArea = jEdit.getActiveView().getTextArea();
			textArea.replaceSelection(insertion);
			
			coqEditController.forwardOneSentence();
		}

		@Override
		public Component getCellRenderer(JList list, int index,
			boolean isSelected, boolean cellHasFocus) {

			renderer.getListCellRendererComponent(list,
				null, index, isSelected, cellHasFocus);

			String text = completions[index];

			Font font = list.getFont();

			renderer.setText(text);
			renderer.setFont(font);
			return renderer;
		}

		@Override
		public String getDescription(int i) {
			return null;
		}
		
		public String getCandidateText(int index) {
			return completions[index];
		}
	}
	
	private void pumsg(String msg) {
		Macros.message(jEdit.getActiveView(), msg);
	}
	
}
