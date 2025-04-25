package com.sinergise.gwt.ui.dialog;


import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.StyleConsts;
import com.sinergise.gwt.ui.maingui.Buttons;
import com.sinergise.gwt.ui.resources.Theme;

public class OptionDialog extends DialogBox {

	public static final int BUTTONS_OK_CANCEL = Buttons.OK_CANCEL;
	public static final int BUTTONS_YES_NO_CANCEL = Buttons.YES_NO_CANCEL;
	public static final int BUTTONS_YES_NO = Buttons.YES_NO;
	public static final int BUTTONS_OK = Buttons.OK;
	public static final String STYLE_NAME = StyleConsts.MESSAGE_DIALOG;

	public static interface ButtonsListener {
		/**
		 * 
		 * @param whichButton
		 * @return true to close the message dialog
		 */
		public boolean buttonClicked(int whichButton);
	}

	protected Buttons myButtons;
	ButtonsListener listener = null;
	Widget mainWidget;
	VerticalPanel content;
	private int defaultButton;

	public OptionDialog(String titleHTML, int whichButtons, Widget mainWidget) {
		super(false, true);
		this.mainWidget = mainWidget;
		addStyleName(STYLE_NAME);
		setGlassEnabled(true);
		setHTML(titleHTML);
		
		content = new VerticalPanel();
		content.add(mainWidget);
		content.add(myButtons = new Buttons(whichButtons) {
			@Override
			protected void ok() {
				processClick(OK);
			}
			@Override
			protected void cancel() {
				processClick(CANCEL);
			}
			@Override
			protected void save() {
				processClick(SAVE);
			}
			@Override
			protected void yes() {
				processClick(YES);
			}
			@Override
			protected void no() {
				processClick(NO);
			}
			@Override
			protected void reset() {
				processClick(RESET);
			}
		});
		myButtons.addStyleName(STYLE_NAME+"-buttons");
		
		setWidget(content);
	}

	public Widget getMainWidget() {
		return mainWidget;
	}
	
	protected void processClick(int button) {
		if (listener != null) {
			if (!listener.buttonClicked(button)) return;
		}
		hide();
	}

	public OptionDialog showCentered() {
		setPopupPositionAndShow(new PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int top = Window.getClientHeight()/2-offsetHeight/2;
				int left = Window.getClientWidth()/2-offsetWidth/2;
				if (top < 0) top = 0;
				if (left < 0) left = 0;
				setPopupPosition(left, top);
			}
		});
		return this;
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			setDefaultButton(defaultButton);
		}
	}

	public OptionDialog setListener(ButtonsListener bl) {
		this.listener = bl;
		return this;
	}

	public OptionDialog setDefaultButton(int but) {
		this.defaultButton = but;
		myButtons.setFocused(but);
		return this; 
	}

	public OptionDialog addHelpAnchor(String url) {
		String html = getHTML();
		html += new ImageAnchor(Theme.getTheme().standardIcons().help(), url, "_blank").getElement().toString();
		setHTML(html);
		return this;
	}

}