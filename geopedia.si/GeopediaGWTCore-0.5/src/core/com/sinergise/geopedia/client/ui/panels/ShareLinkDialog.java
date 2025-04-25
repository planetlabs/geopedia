package com.sinergise.geopedia.client.ui.panels;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.core.util.GMStats;
import com.sinergise.geopedia.client.resources.GeopediaCommonStyle;
import com.sinergise.geopedia.client.ui.CenteredBox;
import com.sinergise.gwt.ui.Heading;
import com.sinergise.gwt.ui.SGParagraph;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;
import com.sinergise.gwt.ui.maingui.DecoratedAnchor;

public class ShareLinkDialog extends CenteredBox{

	private FlowPanel contentPanel = null;
	private TextBox linkBox;
	
	private Anchor actionCopy;
	private DecoratedAnchor actionSendViaEmail;
	
	public ShareLinkDialog() {
		addStyleName("shareDialog");
		setContent(buildContent());
		setHeaderTitle(Messages.INSTANCE.ShareLink_Title());
	}
	
	public void setLink(String link) {
		if (link==null)
			return;
		linkBox.setText(link);	
		actionSendViaEmail.setHref(generateMailtoHREF(link));
    	GMStats.stats(GMStats.DIALOG_LINK_DISPLAY, new String[]{"link"}, 
    			new String[]{link});
	}
	
	private FlowPanel buildContent() {
		if (contentPanel!=null)
			return contentPanel;
		contentPanel = new FlowPanel();
		
		linkBox = new TextBox();
		linkBox.setReadOnly(true);
		
		
		actionCopy = new Anchor(StandardUIConstants.STANDARD_CONSTANTS.buttonCopy());
		actionCopy.setStyleName("actionCopy");
		
		actionSendViaEmail = new DecoratedAnchor(Messages.INSTANCE.ShareLink_ActionSendEmail());
		actionSendViaEmail.setImage(GeopediaCommonStyle.INSTANCE.sendBlue());
		
		
		FlowPanel actionBox = new FlowPanel();
		actionBox.setStyleName("actions");
		//actionBox.add(actionCopy);
		actionBox.add(actionSendViaEmail);
		
		contentPanel.add(new Heading.H2(Messages.INSTANCE.ShareLink_Subtitle()));
		contentPanel.add(new SGParagraph(Messages.INSTANCE.ShareLink_Text()));
		contentPanel.add(linkBox);
		contentPanel.add(actionBox);
		return contentPanel;
	}
	
	
	private String generateMailtoHREF(String link) {
		String mailTo = "mailto:?subject="+ Messages.INSTANCE.ShareLink_MailSubject() +"&body=";
		mailTo+=URL.encode(link);
		return  mailTo;
	}
	@Override
	public void show() {
		super.show();
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			
			@Override
			public void execute() {
				linkBox.setFocus(true);
				linkBox.selectAll();
			}
		});
	}
}
