package com.sinergise.gwt.ui.layout;

import java.util.Arrays;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;

public class SGVerticalLayoutPanel extends SGFlowPanel {
	
	public SGVerticalLayoutPanel() {
		super("sgVerticalLayoutPanel");
		setHeight("100%");
	}
	
	@Override
	public void onResize() {
		int nWidgets = getWidgetCount();
		int fullH = getOffsetHeight();
		
		WidgetHeightPair[] pH = new WidgetHeightPair[nWidgets];
		for (int i = 0; i < nWidgets; i++) {
			Widget w = getWidget(i);
			int preferredHeight = ((HasPreferredHeight)w).getPreferredHeight();
			
			pH[i] = new WidgetHeightPair(w, preferredHeight);
		}
		

			
		int availableH = fullH;
		
		//we sort the widgets by it's preferredHeight (ascending)
		Arrays.sort(pH);//sorts ascending
		
		//we loop through the widgets first time to give the preferred height to the ones we can
		for (int i = 0; i < nWidgets; i++) {
			//the height that can be given to which widget relying on the currently available height as to have an evenly size of all the widgets if they are too big 
			double refH = ((double)availableH)/(nWidgets-i);
			
			//if refH is smaller than 0 setHeight throws an error
			if(refH <= 0){
				break;
			}
			if (pH[i].preferredHeight <= refH) {
				//if the widget has a preferred height that is smaller than the height that we would give to each widget then we set the preferredHeight
				availableH -= pH[i].preferredHeight;
				pH[i].w.setHeight(pH[i].preferredHeight+"px");
			} else {
				//since the pH is ordered we know that from now on none will fulfill the condition
				availableH -= refH;
				pH[i].w.setHeight(refH+"px");
			}
		
		}
			
		super.onResize();
	}
	
	//Widget and its preferredHeight have to be together because we order the array of preferred heights 
	//and therefore can't rely on the getWidget(i) anymore. 
	private static class WidgetHeightPair implements Comparable<WidgetHeightPair> {
		
		Widget w;
		int preferredHeight;
		
		WidgetHeightPair(Widget w, int preferredHeight) {
			this.w = w;
			this.preferredHeight = preferredHeight;
		}
		
		@Override
		public int compareTo(WidgetHeightPair other){
			if(preferredHeight < other.preferredHeight){
				return -1;
			} else if(preferredHeight > other.preferredHeight){
				return 1;
			} else {
				return 0;
			}
		}
		
	}
}
