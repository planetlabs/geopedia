package com.sinergise.geopedia.client.ui.panels;

import java.util.HashMap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.ui.CenteredBox;
import com.sinergise.geopedia.core.constants.LinkConstants;
import com.sinergise.geopedia.core.entities.WebLink;
import com.sinergise.geopedia.core.entities.WebLink.Group;
import com.sinergise.gwt.ui.Heading;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.SGParagraph;
import com.sinergise.gwt.ui.maingui.DecoratedAnchor;

public class LinksDialog extends CenteredBox {

	private FlowPanel contentPanel = null;
	
	public LinksDialog(HashMap<String, Group> webLinks) {
		addStyleName("linksDialog");
		setContent(getContent(webLinks));
		setHeaderTitle(Messages.INSTANCE.LinksDialog_Title());
	}
	
	protected String dynamicURLRewrite(WebLink webLink) {
		return webLink.URL;
	}
	
	private FlowPanel getContent(HashMap<String, Group> webLinks) {
		if (contentPanel!=null)
			return contentPanel;
		contentPanel = new FlowPanel();
		contentPanel.add(new Heading.H2(Messages.INSTANCE.LinksDialog_Subtitle()));
		
		WebLink.Group linksGroup = webLinks.get(LinkConstants.GROUP_LIGHTLINKS);
		for (final WebLink link:linksGroup.values()) {
			FlowPanel linkPanel = new FlowPanel();
			linkPanel.setStyleName("linkPanel");
			linkPanel.addStyleName(link.name);
			ImageAnchor img = new ImageAnchor("links/"+link.name+".png");
			img.addClickHandler(webLinkHandler(link));
			
			DecoratedAnchor anch = new DecoratedAnchor(Messages.INSTANCE.LinksDialog_FollowLink(), webLinkHandler(link));
			linkPanel.add(img);
			linkPanel.add(new Heading.H3(link.displayName));
			linkPanel.add(new SGParagraph(link.description));
			linkPanel.add(anch);
			contentPanel.add(linkPanel);
		}
		
		return contentPanel;
	}

	private ClickHandler webLinkHandler(final WebLink link) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.open(dynamicURLRewrite(link), "_blank", null);
				LinksDialog.this.hide();
			}
		};
	}
}