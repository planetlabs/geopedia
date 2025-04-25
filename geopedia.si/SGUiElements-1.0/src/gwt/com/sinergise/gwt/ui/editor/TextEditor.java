package com.sinergise.gwt.ui.editor;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.sinergise.common.ui.controls.EditableComponent;
import com.sinergise.common.util.Util;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.event.SourcesValueChangeEvents;
import com.sinergise.common.util.event.ValueChangeListener;
import com.sinergise.common.util.event.ValueChangeListenerCollection;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.gwt.ui.StyleConsts;


/**
 * Extends TextBox functionality with:
 * <ul>
 * <li>editable/non-editable option</li>
 * <li>validation method</li>
 * </ul>
 * 
 * You can catch value change events with adding ValueChangeListener. The only
 * check that this class is capable of, is non-empty string check (configure
 * this via isEmptyStringAllowed(), setEmptyStringAllowed(...) ). You can do
 * this by calling validate() You should override validate() method to implement
 * more complex checks (maybe even RPC based checks)...
 * 
 * @author ???
 * 
 */
public class TextEditor extends TextBox implements SourcesValueChangeEvents<String>, EditableComponent {
	private ValueChangeListenerCollection<String> listeners;
	private String lastText;
	protected int suppressEvents = 0;
	protected boolean emptyStringAllowed = true;

	public TextEditor() {
		this(null);
	}

	public TextEditor(String text) {
		setText(text);

		setStylePrimaryName(StyleConsts.TEXT_EDITOR);

		addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				fireChange(getText());
			}
		});
	}

	protected volatile ArrayList<String> changesLoop = null;

	/**
	 * @param newText
	 * @return true if the new value is different than the old one
	 */
	protected final boolean fireChange(String newText) {
		if (newText == null || newText.length() < 1) {
			newText = null;
		}
		boolean hasChange = !Util.safeEquals(lastText, newText);
		if (suppressEvents > 0) {
			return hasChange;
		}
		if (hasChange) {
			eventLoopAdd(newText);
			String oldText = lastText;
			lastText = newText;
			try {
				fireChangeNoCheck(oldText, newText);
			} finally {
				eventLoopRemove(newText);
			}
		}
		return hasChange;
	}

	/**
	 * 
	 */
	private void eventLoopRemove(String which) {
		Object obj = changesLoop.get(changesLoop.size() - 1);
		boolean throwIt = false;
		if (obj == null && which != null)
			throwIt = true;
		else if (which == null && obj != null)
			throwIt = true;
		else if (which != null && !which.equals(obj))
			throwIt = true;

		if (throwIt) {
			StringBuffer buf = new StringBuffer("Change events illegal state {");
			buf.append(CollectionUtil.toString(changesLoop, ","));
			buf.append("} ");
			buf.append("should remove ");
			buf.append('\'');
			buf.append(which);
			buf.append('\'');
			if (throwIt)
				throw new IllegalStateException(buf.toString());
		}

		changesLoop.remove(changesLoop.size() - 1);
		// System.out.println(hashCode()+" -"+prev+" |"+lastText);
		if (changesLoop.isEmpty()) {
			changesLoop = null;
		}
	}

	/**
	 * @param newText
	 */
	private void eventLoopAdd(String newText) {
		boolean throwIt = false;
		if (changesLoop == null) {
			changesLoop = new ArrayList<String>();
		} else if (changesLoop.contains(newText)) {
			throwIt = true;
		}
		// System.out.println(hashCode()+" +"+newText+" |"+lastText);
		changesLoop.add(newText);

		// sysout the exception
		if (throwIt) {
			StringBuffer buf = new StringBuffer("Change events in a possibly endless loop {");
			buf.append(CollectionUtil.toString(changesLoop, ","));
			buf.append("}");
			throw new IllegalStateException(buf.toString());
		}
	}

	protected void fireChangeNoCheck(String prevText, String newText) {
		if (listeners != null) {
			listeners.fireChange(this, prevText, newText);
		}
	}

	public void addValueChangeListener(ValueChangeListener<? super String> listener) {
		if (listeners == null) {
			listeners = new ValueChangeListenerCollection<String>();
		}
		listeners.add(listener);
	}

	public void removeValueChangeListener(ValueChangeListener<? super String> listener) {
		if (listeners == null)
			return;
		listeners.remove(listener);
	}

	public void setObjectValue(Object o){
		setText(StringUtil.toString(o, ""));
	}
	
	@Override
	public void setText(String text) {
		text = text == null ? "" : text;
		super.setText(text);
		fireChange(text);
	}

	public boolean isEditable() {
		return isEnabled();
	}

	/**
	 * TODO checkk up id this methid duplicates the functionality of setReadOnly
	 */
	public void setEditable(boolean editable) {
		setReadOnly(!editable);
		if (!editable)
			addStyleDependentName("uneditable");
		else
			removeStyleDependentName("uneditable");
	}

	public boolean isEmptyStringAllowed() {
		return emptyStringAllowed;
	}

	public void setEmptyStringAllowed(boolean emptyStringAllowed) {
		this.emptyStringAllowed = emptyStringAllowed;
	}

	public void trigerValidation(){
		setText(getText());
	}
}
