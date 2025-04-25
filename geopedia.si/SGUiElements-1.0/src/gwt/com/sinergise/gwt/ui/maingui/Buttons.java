package com.sinergise.gwt.ui.maingui;

import static com.sinergise.gwt.ui.maingui.StandardUIConstants.STANDARD_CONSTANTS;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.ui.ButtonFactory;
import com.sinergise.gwt.util.html.CSS;


public abstract class Buttons extends Composite implements ClickHandler {
	public static final HorizontalAlignmentConstant LEFT = HasHorizontalAlignment.ALIGN_LEFT;
	public static final HorizontalAlignmentConstant CENTER = HasHorizontalAlignment.ALIGN_CENTER;
	public static final HorizontalAlignmentConstant RIGHT = HasHorizontalAlignment.ALIGN_RIGHT;

	public static final int OK = 1;
	public static final int CANCEL = 2;
	public static final int SAVE = 4;
	public static final int YES = 16;
	public static final int NO = 32;
	public static final int RESET = 64;

	public static final int YES_NO_CANCEL = YES | NO | CANCEL;
	public static final int YES_NO = YES | NO;
	public static final int SAVE_CANCEL = SAVE | CANCEL;
	public static final int SAVE_CANCEL_RESET = SAVE | CANCEL | RESET;
	public static final int OK_CANCEL = OK | CANCEL;

	private FlowPanel panel = new FlowPanel();
	private ButtonBase ok, cancel, save, yes, no, delete, reset;
	private Label status = new Label();
	private int whichButtons;
	
	public Buttons(int which) {
		this(which, true, HasHorizontalAlignment.ALIGN_CENTER, true);
	}

	public Buttons(int which, boolean fullWidth, HorizontalAlignmentConstant align) {
		this(which, fullWidth, align, true);
	}

	public Buttons(int which, boolean fullWidth, HorizontalAlignmentConstant align, boolean useIcons) {
		this.whichButtons = which;
		if (align == HasHorizontalAlignment.ALIGN_CENTER || align == null) {
			panel.add(status);
		} else {
			CSS.display(status.getElement(), CSS.DISP_INLINE);
		}

		if (align == HasHorizontalAlignment.ALIGN_RIGHT) {
			panel.add(status);
		}

		ok = makeButtonIf(STANDARD_CONSTANTS.buttonOK(), which, OK, useIcons);
		cancel = makeButtonIf(STANDARD_CONSTANTS.buttonCancel(), which, CANCEL, useIcons);
		save = makeButtonIf(STANDARD_CONSTANTS.buttonSave(), which, SAVE, useIcons);
		yes = makeButtonIf(STANDARD_CONSTANTS.buttonYes(), which, YES, useIcons);
		no = makeButtonIf(STANDARD_CONSTANTS.buttonNo(), which, NO, useIcons);
		reset = makeButtonIf(STANDARD_CONSTANTS.buttonReset(), which, RESET, useIcons);

		if (align == HasHorizontalAlignment.ALIGN_LEFT) {
			panel.add(status);
		}

		// initWidget(outer);
		initWidget(panel);

		if (!fullWidth) {
			CSS.display(panel, "inline-block");
		}
		if (align != null) {
			CSS.textAlign(getElement(), align.getTextAlignString());
		}
	}

	@Deprecated
	/**
	 * This method is not used nowhere. Also status is usually used in other places.
	 */
	public void setStatus(String status) {
		this.status.setText(status == null ? "" : status);
	}

	public void onClick(ClickEvent event) {
		Object sender = event.getSource();
		if (sender == ok) {
			ok();
		} else if (sender == cancel) {
			cancel();
		} else if (sender == save) {
			save();
		} else if (sender == yes) {
			yes();
		} else if (sender == no) {
			no();
		} else if (sender == delete) {
			delete();
		} else if (sender == reset) {
			reset();
		}
	}

	public void setButtonEnabledCond(int which, boolean enabled) {
		enableButtonIf(ok, OK, which, enabled);
		enableButtonIf(yes, YES, which, enabled);
		enableButtonIf(no, NO, which, enabled);
		enableButtonIf(cancel, CANCEL, which, enabled);
		enableButtonIf(save, SAVE, which, enabled);
		enableButtonIf(reset, RESET, which, enabled);
	}

	protected void enableButtonIf(ButtonBase b, int butFlag, int mask, boolean enabled) {
		if (b == null || (0 == (butFlag & mask))) {
			return;
		}

		b.setEnabled(enabled);
	}

	public void setButtonVisible(int which, boolean visible) {
		showButtonIf(ok, OK, which, visible);
		showButtonIf(yes, YES, which, visible);
		showButtonIf(no, NO, which, visible);
		showButtonIf(cancel, CANCEL, which, visible);
		showButtonIf(save, SAVE, which, visible);
		showButtonIf(reset, RESET, which, visible);
	}

	protected void showButtonIf(ButtonBase b, int butFlag, int mask, boolean visible) {
		if (b == null || (0 == (butFlag & mask))) {
			return;
		}
		b.setVisible(visible);
	}

	public void setButtonsEnabled(int which) {
		setButtonEnabledCond(which, true);
		setButtonEnabledCond(~which, false);
	}

	public void enable(int which) {
		setButtonEnabledCond(which, true);
	}

	public void disable(int which) {
		setButtonEnabledCond(which, false);
	}

	public void enableAll() {
		setButtonEnabledCond(0xFFFFFFFF, true);
	}

	public void disableAll() {
		setButtonEnabledCond(0xFFFFFFFF, false);
	}
	
	public boolean hasButtons(int which) {
		return (which & whichButtons) == which;
	}
	
	public int whichButtons() {
		return whichButtons;
	}

	protected void ok() {
		throw new UnsupportedOperationException();
	}

	protected void cancel() {
		throw new UnsupportedOperationException();
	}

	protected void save() {
		throw new UnsupportedOperationException();
	}

	protected void yes() {
		throw new UnsupportedOperationException();
	}

	protected void no() {
		throw new UnsupportedOperationException();
	}

	protected void delete() {
		throw new UnsupportedOperationException();
	}

	protected void reset() {
		throw new UnsupportedOperationException();
	}

	private ButtonBase makeButtonIf(String text, int which, int flag) {
		if ((which & flag) == 0) {
			return null;
		}

		final ButtonBase b;
		switch (which & flag) {
			case OK : {
				b = ButtonFactory.createOkButton();
				break;
			}
			case CANCEL : {
				b = ButtonFactory.createCancelButton();
				break;
			}
			case SAVE : {
				b = ButtonFactory.createSaveButton();
				break;
			}
			case YES : {
				b = ButtonFactory.createYesButton();
				break;
			}
			case NO : {
				b = ButtonFactory.createNoButton();
				break;
			}
			case RESET : {
				b = ButtonFactory.createResetButton();
				break;
			}
			default : {
				b = ButtonFactory.createPlainButton(text);
			}
		}
		b.getElement().getStyle().setProperty("margin", "3px");
		panel.add(b);
		b.addClickHandler(this);
		return b;
	}

	private ButtonBase makeButtonIf(String text, int which, int flag, boolean useIcons) {
		if (useIcons) {
			return makeButtonIf(text, which, flag);
		}

		if ((which & flag) == 0) {
			return null;
		}

		final ButtonBase b = ButtonFactory.createPlainButton(text);
		if (b != null) {
			b.getElement().getStyle().setProperty("margin", "3px");
			panel.add(b);
			b.addClickHandler(this);
		}
		return b;
	}

	/**
	 * @param which
	 *            - which button: OK | CANCEL | SAVE | YES | NO | RESET
	 * @param html
	 *            - new button label
	 */
	public void renameButton(int which, String html) {
		ButtonBase b = get(which);
		if (b == null) {
			return;
		}
		b.setHTML(html);
	}
	
	public void setButtonText(int which, String html) {
		ButtonBase b = get(which);
		if (b == null) {
			return;
		}
		b.setText(html);
	}

	/**
	 * @param which
	 *            - which button: OK | CANCEL | SAVE | YES | NO | RESET
	 * @param title
	 *            - button tool-tip
	 */
	public void setButtonTitle(int which, String title) {
		ButtonBase b = get(which);
		if (b == null) {
			return;
		}
		b.setTitle(title);
	}

	private int index(int which) {
		return index(get(which));
	}

	private int index(ButtonBase b) {
		if (b == null) {
			return -1;
		}
		int index = panel.getWidgetIndex(b);
		return index;
	}

	public void position(int which, int new_index) {
		int index = index(which);
		if ((index == -1) || (new_index > panel.getWidgetCount())
				|| (index == new_index)) {
			return;
		}
		ButtonBase b = (ButtonBase) panel.getWidget(index);
		b.removeFromParent();
		panel.insert(b, new_index - 1);
	}

	private ButtonBase get(int which) {
		switch (which) {
			case OK : {
				return ok;
			}
			case CANCEL : {
				return cancel;
			}
			case SAVE : {
				return save;
			}
			case YES : {
				return yes;
			}
			case NO : {
				return no;
			}
			case RESET : {
				return reset;
			}
			default : {
				return null;
			}
		}
	}

	public void decorate(int which, Widget decoration, boolean before) {
		ButtonBase but = get(which);
		if (but == null || decoration == null) {
			return;
		}
		int index = index(but);
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		panel.insert(horizontalPanel, index);
		CSS.textAlign(horizontalPanel.getElement(), CSS.CENTER);
		if (before) {
			horizontalPanel.add(decoration);
			horizontalPanel.add(but);
		} else {
			horizontalPanel.add(but);
			horizontalPanel.add(decoration);
		}
	}

	public void decorate(int which, Widget decoration) {
		decorate(which, decoration, true);
	}

	public void setFocused(int but) {
		ButtonBase b = get(but);
		if (b != null) {
			b.setFocus(true);
		}
	}

}
