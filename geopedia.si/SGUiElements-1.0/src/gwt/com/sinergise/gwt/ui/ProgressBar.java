package com.sinergise.gwt.ui;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.gwt.util.html.CSS;

//FIXME: why is it deprecated and what should be used instead
@Deprecated
public class ProgressBar extends FlowPanel{
	private SimplePanel pPanel;
	private int progress = 0;
	public ProgressBar() {
		this(0);
	}
	public ProgressBar(int progress) {
		setStyleName("progressBar");
		pPanel = new SimplePanel();
		pPanel.setStyleName("progress");
		add(pPanel);
		setProgress(progress);
	}
	
	
	public void setProgress(double prog) {
		setProgress((int)(prog*100.0));
	}
	public int getProgress() {
		return progress;
	}
	
	public void setProgress(int progress) {
		if (progress<0)
			this.progress=0;
		else if (progress>100)
			this.progress=100;
		else
			this.progress=progress;
		CSS.width(pPanel.getElement(), progress+"%");
	}

}
