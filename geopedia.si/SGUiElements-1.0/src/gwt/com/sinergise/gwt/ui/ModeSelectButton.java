/**
 * 
 */
package com.sinergise.gwt.ui;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.ui.action.ToggleAction;
import com.sinergise.common.util.event.update.UpdateListenerAdapter;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.gwt.ui.combo.AdvancedDropDown;
import com.sinergise.gwt.ui.resources.Theme;


/**
 * A push button with selectable modes.
 * 
 * @author tcerovski
 */
public class ModeSelectButton extends Composite {
	public static final String STYLE_LEFT_BUTTON = StyleConsts.MODESELECT_LEFT;
	public static final String STYLE_RIGHT_BUTTON = StyleConsts.MODESELECT_RIGHT;
	
	public static class Model extends Action {
		public static final String PROP_SELECTED_MODE = "selectedMode";
		
		public int selectedModeIdx = 0;
		public final int[] modes;
		public final String[] modeIcons;
		public final String[] modeLabels;
		
		public Model(String title, int[] modes, String[] icons, String[] labels) {
			super(title);
			this.modes = modes;
			this.modeIcons = icons;
			this.modeLabels = labels;
		}
		
		public String getModeImageUrl(int modeIdx) {
			if(modeIcons == null || modeIcons.length <= modeIdx || modeIdx<0)
				return null;
			return modeIcons[modeIdx];
		}
		
		public String getModeLabel(int modeIdx) {
			if(modeLabels == null || modeLabels.length <= modeIdx || modeIdx<0)
				return null;
			return modeLabels[modeIdx];
		}
		
		public void setSelectedModeIdx(int selectedModeIdx) {
			if (this.selectedModeIdx == selectedModeIdx) return;
			this.selectedModeIdx = selectedModeIdx;
			performAction();
			// Notify Listeners
			setProperty(PROP_SELECTED_MODE, Integer.valueOf(getSelectedMode()));
		}
		
		@Override
		protected void actionPerformed() {
		}

		public int getSelectedMode() {
			return modes[selectedModeIdx];
		}
	}
	
	/**
	 * Creates a mode button with modes represented as images and text labels.
	 * 
	 * @param modes
	 * @param texts
	 * @param images
	 */
	public ModeSelectButton(Model optionsModel, ToggleAction modeAction) {
		this(optionsModel.modeIcons[0], optionsModel, modeAction);
	}
	
	private final HorizontalPanel panel = new HorizontalPanel();
	
	protected final Action modeAction;
	protected final Model optionsModel;
	
	protected Action selectModeAction = new Action("Select Mode") {
		{
			setIcon(Theme.getTheme().standardIcons().arrowDown10());
		}
		@Override
		protected void actionPerformed() {
			showSelectionPanel();
		}
	};
	
	private ActionPushButton showModesButton = null;
	
	private ModeSelectButton(String actionImage, Model optionsModel, ToggleAction modeActionIn) {
		super();
		this.optionsModel = optionsModel;
		optionsModel.addPropertyChangeListener(new PropertyChangeListener<Object>() {
			public void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {
				if (Model.PROP_SELECTED_MODE.equals(propertyName)) {
					update();
				}
			}
		});
		
		boolean modeEnabled = true;
		if (modeActionIn == null) {
			modeEnabled = false;
			modeAction = new Action("Selected Shape") {
				@Override
				protected void actionPerformed() {}
			};
		} else {
			modeAction = modeActionIn;
		}
		setIcon(actionImage);
		
		if (modeEnabled) {
			panel.add(new ActionToggleButton(modeAction));
		} else {
			panel.add(new ActionImage(modeAction));
		}
		panel.add(showModesButton = new ActionPushButton(selectModeAction));
		initWidget(panel);
	}
	
	public int getSelectedMode() {
		return optionsModel.getSelectedMode();
	}
	
	private void update()
	{
		if(optionsModel.modeIcons != null)
			setIcon(optionsModel.getModeImageUrl(optionsModel.selectedModeIdx));
	}
	
	private void setIcon(String imgpath) {
		modeAction.setIconURL(imgpath);
	}

	private PopupPanel selectionPanel = null;
	
	public void showSelectionPanel() {
		if(selectionPanel == null) {
			Widget[] opts = new Widget[optionsModel.modes.length];
	        for (int i = 0; i < optionsModel.modes.length; i++) {
	        	opts[i] = createModeOptionWidget(i);
	        }
			AdvancedDropDown dd = new SelectionList(opts, 1);
			dd.setSelectedIndex(optionsModel.selectedModeIdx);
			dd.setFocus(true);
			selectionPanel = new PopupPanel(true, false);
			selectionPanel.setStyleName("cosylab-ModeSelectButton-SelectionPanel");
			selectionPanel.add(dd);
			
			dd.addUpdateListener(new UpdateListenerAdapter() {
				@Override
				public void itemUpdateConfirmed(Object sender) {
					int idx = ((AdvancedDropDown)sender).getSelectedIndex();
					selectionPanel.hide();
					optionsModel.setSelectedModeIdx(idx);
				}
			});
		}
		
		selectionPanel.setPopupPositionAndShow(new PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int top = ModeSelectButton.this.showModesButton.getAbsoluteTop() + ModeSelectButton.this.showModesButton.getOffsetHeight();
				int left = ModeSelectButton.this.showModesButton.getAbsoluteLeft();
				selectionPanel.setPopupPosition(left, top);
			}
		});
    }

	protected Widget createModeOptionWidget(int modeIdx) {
		HorizontalPanel opt = new HorizontalPanel();
		if(optionsModel.modeIcons != null) {
			String imgpath = GWT.getModuleBaseURL() + optionsModel.getModeImageUrl(modeIdx);
			opt.add(new Image(imgpath));
		}
		
		if(optionsModel.modeLabels != null) {
			Label lab = new Label(optionsModel.getModeLabel(modeIdx));
			opt.add(lab);
			opt.setCellWidth(lab, "100%");
		}
		DOM.setStyleAttribute(opt.getElement(), "width", "100%");
		opt.setStyleName("cosylab-ModeSelectButton-SelectionPanel-Option");
		return opt;
	}
	
	public void setEnabled(boolean enabled)
	{
		modeAction.setExternalEnabled(enabled);
		selectModeAction.setExternalEnabled(enabled);
	}
	
	class SelectionList extends AdvancedDropDown {
		
		SelectionList(Widget[] choices, int numCols) {
			super(choices, numCols);
		}
		
		@Override
		public void setSelectedIndex(int index)
		{
			if (index>=len) index = len-1;
		    if (selIdx>=0) {
		    	tbl.getWidget(selIdx/numCols, selIdx%numCols).removeStyleDependentName("selected");
			}
			selIdx=index;
			if (selIdx>=0) {
				tbl.getWidget(selIdx/numCols, selIdx%numCols).addStyleDependentName("selected");
			}
	    }
	}
}


