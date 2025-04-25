/*
 *
 */
package com.sinergise.common.util.event.selection;

import static com.sinergise.common.util.collections.CollectionUtil.first;

import java.util.ArrayList;

public class ExcludeContext implements ToggleListener {
	ArrayList<Selectable> store = new ArrayList<Selectable>();
	boolean forceSelection = false;
	
	public void register(final Selectable src) {
		if (store.contains(src)) {
			return;
		}
		store.add(src);
		src.addToggleListener(this);
	}
	
	public void deregister(final Selectable src) {
		store.remove(src);
		if (forceSelection && getCurrentSelection() == null) {
			forceSelection(null);
		} 
	}
	
	protected void forceSelection(final Selectable forceOn) {
		Selectable current = getCurrentSelection();
		if (current != null) {
			return; //something already selected
		}
		
		if (forceOn != null) {
			forceOn.setSelected(true);
		} else {
			Selectable first = first(store);
			if (first != null) {
				first.setSelected(true);
			}
		}
	}

	public void setForceSelection(boolean forceSelection) {
		this.forceSelection = forceSelection;
	}
	
	@Override
	public void toggleStateChanged(final SourcesToggleEvents source, final boolean newOn) {
		Exception e = null;
		if (newOn) {
			for (final Selectable s : store) {
				if (s != source) {
					try {
						s.setSelected(false);
					} catch(final Exception ex) {
						e = ex;
					}
				}
			}
		} else {
			if (forceSelection) {
				forceSelection((Selectable)source);
			}
		}
		if (e != null) {
			throw new RuntimeException("Toggle state changed failed", e);
		}
	}
	
	protected Selectable getCurrentSelection() {
		for (Selectable s : store) {
			if (s.isSelected()) {
				return s;
			}
		}
		return null;
	}

	public boolean deselectAll() {
		boolean ret = false;
		for (final Selectable s : store) {
			if (s.isSelected()) {
				s.setSelected(false);
				ret = true;
			}
		}
		return ret;
	}
}
