package com.sinergise.geopedia.core.util;

//import com.cosylab.gisopedia.client.messages.AppMessages;

public class LinkUtils {

//	public static final String EXTERNAL_LINK_JS = "return window.confirm('"+AppMessages.INSTANCE.LinkUtils_leaving()+"')";//"Odhajate na drugo spletno stran, za katere vsebino Geopedia ne odgovarja. Želite nadaljevati?"
	public static final String EXTERNAL_LINK_JS = "return window.confirm('"+"Odhajate na drugo spletno stran, za katere vsebino Geopedia ne odgovarja. Želite nadaljevati?"+"')";

	private static final String prefixHTTP = "http://"; 
	private static final String prefixWWW = "www.";
	private static final String whitespace = " \n\r\t";

	
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

	/** parses text and returns parser state at next occurence of any link prefix */
	public static LinkPosition findNextLinkPosition(StringBuffer sb, int searchStart)
	{
		int i = sb.indexOf(prefixHTTP,searchStart);
		int j = sb.indexOf(prefixWWW,searchStart);
		if (i != -1 && j != -1)
		{
			if (i < j)
				return getLinkPosition(sb,prefixHTTP,searchStart,i);
			else
				return getLinkPosition(sb,prefixWWW,searchStart,j);
		}
		else if (i != -1)
			return getLinkPosition(sb,prefixHTTP,searchStart,i);
		else if (j != -1)
			return getLinkPosition(sb,prefixWWW,searchStart,j);
		else // no prefix found anymore
			return null;
	}

	private static LinkPosition getLinkPosition(StringBuffer sb, String prefix, int searchStart, int linkStart)
	{
		int endOfWord = sb.length(); // endOfWord = where link address ends
		for (int i = linkStart; i < sb.length(); i++)
		{
			//if (Character.isWhitespace(sb.charAt(i)))
			if (whitespace.indexOf(sb.charAt(i)) != -1) // found whitespace char
			{
				endOfWord = i;
				break;
			}
		}

		LinkPosition linkPosition = new LinkPosition();
		linkPosition.linkStart = linkStart;
		linkPosition.linkEnd = /*linkStart + */ endOfWord;
		linkPosition.clickableText = sb.substring(linkStart,endOfWord);
		linkPosition.linkAddress = (prefix == prefixWWW ? "http://" : "")
				+ linkPosition.clickableText;
		linkPosition.plainTextBeforeLink = sb.substring(searchStart,linkStart);
		return linkPosition;
	}
	
	public static class LinkPosition
	{
		public int linkStart = -1; // the position where the link address starts in ordinary text
		public int linkEnd = -1;
		public String plainTextBeforeLink;
		public String clickableText;
		public String linkAddress;
	}
	
	
	public static void main(String[] args)
	{
		String test1 = "tralala http://www.google.com je link";
		System.out.println("input = " + test1);
		System.out.println("output = " + generateHyperlinks(test1));
		
		String test2 = "tralala http://maps.google.com je link";
		System.out.println("input = " + test2);
		System.out.println("output = " + generateHyperlinks(test2));

		String test3 = "tralala www.google.com je link";
		System.out.println("input = " + test3);
		System.out.println("output = " + generateHyperlinks(test3));

		String test4 = "tralala maps.google.com NI veljaven link";
		System.out.println("input = " + test4);
		System.out.println("output = " + generateHyperlinks(test4));

		String test5 = "tralala http://www.google.com je link, pa tudi www.google.com je link";
		System.out.println("input = " + test5);
		System.out.println("output = " + generateHyperlinks(test5));

		String test6 = "tole naj bi bil link www.google.com oziroma http://www.google.com";
		System.out.println("input = " + test6);
		System.out.println("output = " + generateHyperlinks(test6));

		

	}

	
}
