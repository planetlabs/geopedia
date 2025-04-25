package com.sinergise.common.util.lang;

import java.util.Stack;

public abstract class SGProgressMonitorImpl implements SGProgressMonitor {
	private final static class Stage {
		public double fractionCurrent;
		public double fractionInParent;
		public String messageBefore;
		
		public Stage(String messageBefore, double sizeInParent) {
			this.messageBefore = messageBefore;
			this.fractionInParent = sizeInParent;
		}
		
		public void setProgress(double fractionCurrent) {
			this.fractionCurrent = fractionCurrent;
		}
	}
	
	protected Stack<Stage> stages = new Stack<Stage>();
	protected String lastMessage = null;
	
	public SGProgressMonitorImpl() {
		stages.push(new Stage(null, 1));
	}
	
	
    /**
     * Notifies start of a nested progress.
     * 
     * @param message The message describing the operation. If null or "" a default message will be generated
     * @param stageFractionInParent Progress fraction of the nested operation within the parent stage
     * 
     * @see #endProgress()
     */
	@Override
	public void nestedStageStarting(String message, double stageFractionInParent) {
		stages.push(new Stage(lastMessage, stageFractionInParent));
		setMessage(message);
	}
	
	@Override
	public void progress(String message, double fractionCompleted) {
		stages.peek().setProgress(fractionCompleted);
		progressUpdated(getTotalProgress());
		if (message != null && !message.equals(lastMessage)) {
			lastMessage = message;
			messageUpdated(message);
		}
	}
	
	@Override
	public void nestedStageFinished() {
		Stage finishedSt = stages.pop();
		Stage parentSt = stages.peek();
		parentSt.fractionCurrent += finishedSt.fractionInParent;
		setMessage(finishedSt.messageBefore);
		progressUpdated(getTotalProgress());
	}
	
	public void setMessage(String message) {
		if (message != null && !message.equals(lastMessage)) {
			lastMessage = message;
			messageUpdated(message);
		}
	}
	
	public double getTotalProgress() {
		double tot = 0;
		double factor = 1;
		for (Stage s : stages) {
			factor *= s.fractionInParent;
			tot += factor * s.fractionCurrent;
		}
		return tot;
	}
	
	protected abstract void progressUpdated(double fractionComplete);
	protected abstract void messageUpdated(String newMessage);
}
