/**
 * Copyright (c) 2008 by Cosylab d.d.
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the LICENSE file.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE ASSUMES _NO_
 * RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION, OR
 * REDISTRIBUTION OF THIS SOFTWARE.
 */
package com.sinergise.common.util.event.selection;

import com.sinergise.common.util.event.selection.Selectable.SelectableHelper;

/**
 * @author Miha
 */
public class SelectableImpl extends SelectableHelper {
	protected boolean                  selected = false;
	
	public SelectableImpl() {
		this(false);
	}
	
	public SelectableImpl(final boolean selected) {
		this.selected = selected;
	}
	
	@Override
	public boolean isSelected() {
		return selected;
	}
	
	@Override
	public void setSelected(final boolean sel) {
		if (this.selected == sel) {
			return;
		}
		this.selected = sel;
		fireToggleAction(this, sel);
	}
}

/* __oOo__ */