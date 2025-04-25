/*
 *
 */
package com.sinergise.common.util.event.selection;

public interface Selectable extends SourcesToggleEvents {
	
	public static abstract class SelectableHelper implements Selectable {

		protected ToggleListenerCollection tlc = null;

		@Override
		public void addToggleListener(final ToggleListener l) {
			if (tlc == null) {
				tlc = new ToggleListenerCollection();
			}
			tlc.add(l);
		}

		@Override
		public void removeToggleListener(final ToggleListener l) {
			if (tlc == null) {
				return;
			}
			tlc.remove(l);
		}
		
		public void fireToggleAction(Selectable source, boolean newValue) {
			if (tlc != null) {
				tlc.fireActionPerformed(source, !newValue, newValue);
			}
		}
	}
	
	boolean isSelected();
	
	void setSelected(boolean sel);
}
