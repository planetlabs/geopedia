package com.sinergise.geopedia.client.ui.feature;

import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.core.util.LinkUtils;


public class TranslatedLinkUtils extends LinkUtils {

	public static final String EXTERNAL_LINK_JS = "return window.confirm('"+Messages.INSTANCE.msgLeavingPage()+"')";//"Odhajate na drugo spletno stran, za katere vsebino Geopedia ne odgovarja. Želite nadaljevati?"
	
	/** converts all words beginning with either http:// or www. to hyperlinks */
	public static String generateHyperlinks(String text)
	{
		StringBuffer input = new StringBuffer(text);
		int searchStart = 0;
		StringBuffer output = new StringBuffer();
		LinkPosition linkPosition = LinkUtils.findNextLinkPosition(input, searchStart);
		while (linkPosition != null)
		{
			// ordinary text outside (before) link
			output.append(linkPosition.plainTextBeforeLink);
			
			// link (anchor) start
			String linkHead =  "<a href='" + linkPosition.linkAddress + 
				"' target='_blank' onclick=\""+ EXTERNAL_LINK_JS +"\">";
			output.append(linkHead);
			
			// clickable text
			output.append(linkPosition.clickableText);
			
			// link (anchor) end
			output.append("</a>");
	
			// find next
			searchStart = linkPosition.linkEnd;
			linkPosition = LinkUtils.findNextLinkPosition(input,searchStart);
		}
		// the rest of text after last link
		output.append(input.substring(searchStart));
		return output.toString();
	}
}
