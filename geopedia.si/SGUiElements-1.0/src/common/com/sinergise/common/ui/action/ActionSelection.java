package com.sinergise.common.ui.action;



public class ActionSelection extends Action  {
	
	private final static int NO_SELECTION_IDX = -1;
	
	private Action[] selections;
	private int selectedIdx = NO_SELECTION_IDX;
	
	public ActionSelection(String name) {
		super(name);
	}
	
	public ActionSelection(String name, Action[] selections) {
		this(name);
		setSelections(selections);
	}
	
	public void setSelections(Action[] selections) {
		if (selections == null || selections.length == 0) throw new IllegalArgumentException("Selections cannot be empty or null");
		this.selections = selections;
	}
	
	public Action[] getSelections() {
		return selections;
	}
	
	public void setSelectedAction(Action action) {
		for (int i=0; i<selections.length; i++) {
			if (selections[i].equals(action)) {
				setSelectedActionIndex(i);
				return;
			}
		}
		setSelectedActionIndex(NO_SELECTION_IDX);
	}
	
	public void setSelectedActionIndex(int actionIdx) {
		if (actionIdx != NO_SELECTION_IDX && (actionIdx < 0 || actionIdx >= selections.length)) {
			throw new IndexOutOfBoundsException(actionIdx+", len: "+selections.length);
		}
		selectedIdx = actionIdx;
	}
	
	private Action getSelectedAction() {
		if (selectedIdx == NO_SELECTION_IDX) return null;
		return selections[selectedIdx];
	}
	
	@Override
	protected void actionPerformed() {
		Action selected = getSelectedAction();
		if (selected != null) selected.actionPerformed();
	}

}
