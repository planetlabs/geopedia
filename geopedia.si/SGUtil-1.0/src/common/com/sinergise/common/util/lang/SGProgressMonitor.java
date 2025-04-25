package com.sinergise.common.util.lang;

public interface SGProgressMonitor {
	SGProgressMonitor NO_OP = new SGProgressMonitor() {
		@Override
		public void progress(String message, double fractionCompleted) {
		}
		
		@Override
		public void nestedStageStarting(String message, double stageFractionInParent) {
		}
		
		@Override
		public void nestedStageFinished() {
		}
	};
	public void nestedStageFinished();

	/**
	 * Notifies start of a nested progress.
	 * 
	 * @param message The message describing the operation.
	 * @param stageFractionInParent Progress fraction of the nested operation within the parent stage
	 * 
	 * @see #endProgress()
	 */
	public void nestedStageStarting(String message, double stageFractionInParent);

	public void progress(String message, double fractionCompleted);

}
