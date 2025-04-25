package com.sinergise.geopedia.light.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.sinergise.geopedia.client.core.NativeAPI;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.ui.CenteredBox;
import com.sinergise.geopedia.core.entities.News;

public class NewsPanel extends FlowPanel {
	
	public static native void initNative() /*-{
	    $wnd.gp_newsProcessLink = function(s) {
	        @com.sinergise.geopedia.light.client.NewsPanel::processLink(Ljava/lang/String;)(s);
	    };  
	}-*/;
	
	
	public static void processLink(String link) {
		if (shownDialog!=null) {
			shownDialog.hide();
			shownDialog=null;
		}
		NativeAPI.processLink(link);
	}
	private static class NewsDialog extends CenteredBox {
    	private FlowPanel contentPanel;
    	NewsDialog(String html) {
    		addStyleName("newsDialog");
    		setHeaderTitle(Messages.INSTANCE.NewsTitle());
    		contentPanel = new FlowPanel();
    		contentPanel.add(new HTML(html));
            setContent(contentPanel);
    	}
    }
	private Label lblNewsTitle;
	private Label lblNewsDate;
	private Anchor btnNewsMiddle;
	private String newsText = "";
	
	private static NewsDialog shownDialog = null;
	
	public NewsPanel() {
		setStyleName("news");
		addStyleName("bubble");
		InlineHTML newsRight = new InlineHTML();
		newsRight.setStyleName("b_right");
		InlineHTML newsLeft = new InlineHTML();
		newsLeft.setStyleName("b_left");
	
	
		lblNewsTitle = new Label();
		lblNewsTitle.setStyleName("title");
		
		lblNewsDate = new Label();
		lblNewsDate.setStyleName("date");
		btnNewsMiddle = new Anchor();
		btnNewsMiddle.getElement().appendChild(lblNewsTitle.getElement());
		btnNewsMiddle.getElement().appendChild(lblNewsDate.getElement());
		
		btnNewsMiddle.setStyleName("b_middle");
		add(newsLeft);
		add(btnNewsMiddle);
		add(newsRight);
		
		btnNewsMiddle.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (newsText!=null && newsText.length()>0) {
					if (shownDialog!=null) {
						shownDialog.hide();
					}
					shownDialog = new NewsDialog(newsText);
					shownDialog.show();
				}
			}
		});
        initNative();
	}
	
	
	
	

	public void updateNews() {
		newsText = "";
		RemoteServices.getSessionServiceInstance().getNews(null, 1, false, new AsyncCallback<News[]>() {
			
			@Override
			public void onSuccess(News[] result) {
				if (result!=null && result.length>0) {
					News n = result[0];
					lblNewsDate.setText(DateTimeFormat.getShortDateFormat().format(n.date));
					lblNewsTitle.setText(n.title);
					newsText = n.data;
				}				
			}
			
			@Override
			public void onFailure(Throwable caught) {			
				// TODO Auto-generated method stub
				
			}
		});
	}
}
