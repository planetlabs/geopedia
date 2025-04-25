package org.outerj.daisy.htmlcleaner;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.sinergise.geopedia.core.util.LinkUtils;
import com.sinergise.geopedia.core.util.LinkUtils.LinkPosition;

abstract class AttributesFaker implements Attributes
{
	Attributes base;
	int baseNum;
	
	public AttributesFaker(Attributes base)
	{
		this.base = base;
		if (base != null)
			this.baseNum = base.getLength();
	}

	// these below shouldn't get called by StylingHtmlSerializer
	public int getIndex(String qName)
	{
		throw new IllegalStateException();
	}
	
	public int getIndex(String uri, String localName)
	{
		throw new IllegalStateException();
	}
	
	public String getQName(int index)
	{
		throw new IllegalStateException();
	}
	
	public String getType(int index)
	{
		throw new IllegalStateException();
	}
	
	public String getType(String qName)
	{
		throw new IllegalStateException();
	}
	
	public String getType(String uri, String localName)
	{
		throw new IllegalStateException();
	}
	
	public String getURI(int index)
	{
		throw new IllegalStateException();
	}
	
	public String getValue(String qName)
	{
		throw new IllegalStateException();
	}
	
	public String getValue(String uri, String localName)
	{
		throw new IllegalStateException();
	}
}

class FixedAttrs extends AttributesFaker
{
	String[] values;
	
	public FixedAttrs(String ... values)
	{
		super(null);
		this.values = values;
	}
	
	public int getLength()
	{
		return values.length >>> 1;
	}
	
	public String getLocalName(int index)
	{
		return values[index << 1];
	}
	
	public String getValue(int index)
	{
		return values[(index << 1) + 1];
	}
}

class fixOutsideA extends AttributesFaker
{
	public fixOutsideA(Attributes base)
	{
		super(base);
	}
	
	public int getLength()
    {
		return baseNum == 0 ? 0 : baseNum + 2;
    }

	public String getLocalName(int index)
    {
		if (index >= baseNum) {
			switch(index - baseNum) {
			case 0: return "onclick";
			case 1: return "target";
			}
			throw new IllegalArgumentException();
		} else {
			return base.getLocalName(index);
		}
    }

	public String getValue(int index)
    {
		if (index >= baseNum) {
			switch(index - baseNum) {
//			case 0: return LinkUtils.EXTERNAL_LINK_JS;
			case 0:
			case 1: return "_blank";
			}
			throw new IllegalArgumentException();
		} else {
			return base.getValue(index);
		}
    }
}

class GoogleAttrs extends AttributesFaker
{
	int w;
	int h;
	String theStyle;
	String theSrc;
	String theFlashvars;
	
	public GoogleAttrs(int w, int h, String theStyle, String theSrc, String theFlashvars)
	{
		super(null);
		
		this.w = w;
		this.h = h;
		this.theStyle = theStyle;
		this.theSrc = theSrc;
		this.theFlashvars = theFlashvars;
	}
	
	public int getLength()
	{
		return 5;
	}
	
	public String getLocalName(int index)
	{
		switch(index) {
		case 0: return "style";
		case 1: return "id";
		case 2: return "type";
		case 3: return "src";
		case 4: return "flashvars";
		}
		throw new IllegalStateException();
	}
	
	public String getValue(int index)
	{
		switch(index) {
		case 0: return theStyle;
		case 1: return "VideoPlayback";
		case 2: return "application/x-shockwave-flash";
		case 3: return theSrc;
		case 4: return theFlashvars;
		}
		throw new IllegalStateException();
	}

	public String embedSrc()
    {
		return "<embed style=\""+theStyle+"\" id=\"VideoPlayback\" type=\"application/x-shockwave-flash\" src=\""+theSrc.replaceAll("(['\\\\])", "\\\\$1")+"\" flashvars=\""+theFlashvars.replaceAll("(['\\\\])", "\\\\$1")+"\"></embed>";
    }
}

public class FixPediaStuff implements ContentHandler
{
    public static final String PANORAMIC_IMG_PREFIX="panorama:";
	ContentHandler consumer;
	
	boolean inHead = false;
	int inEmbed = 0;
	boolean inLink = false;
	
	boolean forDisplay;
	
	public FixPediaStuff(ContentHandler consumer, boolean forDisplay)
	{
		this.consumer = consumer;
		this.forDisplay = forDisplay;
	}

	static GoogleAttrs extractGoogleVideo(Attributes atts)
	{
		String style = null, id = null, type = null, src = null, flashvars = null;
		int nAtts = atts.getLength();
		for (int a=0; a<nAtts; a++) {
			String name = atts.getLocalName(a);
			if ("style".equals(name)) {
				style = atts.getValue(a);
			} else
			if ("id".equals(name)) {
				id = atts.getValue(a);
			} else
			if ("type".equals(name)) {
				type = atts.getValue(a);
			} else
			if ("src".equals(name)) {
				src = atts.getValue(a);
			} else
			if ("flashvars".equals(name)) {
				flashvars = atts.getValue(a);
			} else
				return null;
		}
		
		if (flashvars == null)
			flashvars = "";
		
		if (style == null || id == null || type == null || src == null)
			return null;
		
		
		if (!"application/x-shockwave-flash".equalsIgnoreCase(type))
			return null;
		if (!src.toLowerCase().startsWith("http://video.google.com/googleplayer.swf?"))
			return null;
		if (!"VideoPlayback".equals(id))
			return null;
		
		int w = -1, h = -1;
		
		String[] tmp = style.split(";");
		for (String s : tmp) {
			s = s.trim();
			if (s.length() < 1)
				continue;
			
			if (s.matches("^width[ \\t]*[:][ \\t]*[1-9][0-9][0-9]?[ \\t]*px$")) {
				w = Integer.parseInt(s.replaceAll("[^0-9]", ""));
			} else
			if (s.matches("^height[ \\t]*[:][ \\t]*[1-9][0-9][0-9]?[ \\t]*px$")) {
				h = Integer.parseInt(s.replaceAll("[^0-9]", ""));
			} else
				return null;
		}
		if (w < 0 || h < 0 || w > 800 || h > 600)
			return null;
		
		return new GoogleAttrs(w, h, "width: "+w+"px; height: "+h+"px;", src, flashvars);
	}
	
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
		if (localName.equals("html") || localName.equals("body"))
			return;
		if (localName.equals("head")) {
			inHead = true;
			return;
		}
		if (inHead || inEmbed != 0)
			return;
		
		if (localName.equals("embed")) {
			GoogleAttrs googleVideo = extractGoogleVideo(atts);
			if (googleVideo == null) {
				inEmbed = 2;
			} else
			if (forDisplay) {
				inEmbed = 2;
				consumer.startElement("", "a", "a", new FixedAttrs(
					"href", "javascript://",
					"onclick", "gp_showVideoFrame('"+googleVideo.embedSrc()+"', "+googleVideo.w+", "+googleVideo.h+")"
				));
				consumer.startElement("", "img", "img", new FixedAttrs(
					"src", "img/action/movie.gif"
				));
				consumer.endElement("", "img", "img");
				consumer.endElement("", "a", "a");
			} else {
				inEmbed = 1;
				consumer.startElement(uri, localName, qName, googleVideo);
			}
			
			return;
		}
		
		if (forDisplay && localName.equals("a")) {
			inLink = true;
			if (!internalLink(atts)) {
				AttributesImpl newAtts=new AttributesImpl(atts);
                String target = removeLocal("target", newAtts);
                if (target==null) {
                	newAtts.addAttribute("", "target", "target", "CDATA", "_blank");
                } else {
                	newAtts.addAttribute("", "target", "target", "CDATA", target);
                }
		        atts=newAtts;
            } else {
                AttributesImpl newAtts=new AttributesImpl(atts);
                String href=removeLocal("href", newAtts);
                removeLocal("target", newAtts);
                String linkStr=href.substring(href.indexOf('#')+1);
                newAtts.addAttribute("", "href", "href", "CDATA", "javascript://");
                newAtts.addAttribute("", "onclick","onclick","CDATA","gp_processInternalLink('"+linkStr+"');");
                atts=newAtts;
            }
        }
        
        if (forDisplay && localName.equals("img")) {
        	
        	String imageSource = getValueLocal("src", atts);
        	if (imageSource == null) {
        		throw new RuntimeException("<img> tag is missing src attribute");
        	}
        	
        	if (imageSource.startsWith(PANORAMIC_IMG_PREFIX)) {
        		
        		AttributesImpl newAtts=new AttributesImpl(atts);
                
                int w=400;
                try {
                    w = Integer.parseInt(removeLocal("width", newAtts));
                } catch (Exception e) {}
                
                int h=300;
                try {
                    h = Integer.parseInt(removeLocal("height", newAtts));
                } catch (Exception e) {}
                
                String src=removeLocal("src", newAtts).substring(PANORAMIC_IMG_PREFIX.length());
                
                StringBuffer link=new StringBuffer("gp_showPanoramicFrame('");
                link.append(src);
                link.append("',");
                link.append(w);
                link.append(',');
                link.append(h);
                link.append(')');
                newAtts.addAttribute("", "onclick", "onclick", "CDATA", link.toString());
                newAtts.addAttribute("", "width", "width","CDATA","220");
                newAtts.addAttribute("", "src", "src", "CDATA", src);
                atts=newAtts;
        	}
        }
		
		consumer.startElement(uri, localName, qName, atts);
    }
	
	
	
	private String getValueLocal(String localName, Attributes atts) {
        for (int i = 0; i < atts.getLength(); i++) {
            if (atts.getLocalName(i).equals(localName)) return atts.getValue(i);
        }
        return null;
    }
    
    private String removeLocal(String localName, AttributesImpl atts) {
        for (int i = 0; i < atts.getLength(); i++) {
            if (atts.getLocalName(i).equals(localName)) {
                String ret=atts.getValue(i);
                atts.removeAttribute(i);
                return ret;
            }
        }
        return null;
    }

    static boolean internalLink(Attributes atts)
	{
		int n = atts.getLength();
		for (int a=0; a<n; a++) {
			String name = atts.getLocalName(a);
			if (name.equalsIgnoreCase("href")) {
				String url = atts.getValue(a);
				if (url.toLowerCase().startsWith("http://www.geopedia.si/#") || url.startsWith("#") || url.startsWith("/") || 
                        url.toLowerCase().startsWith("http://www.geopedia.si#"))
					return true;
			}
		}
		return false;
	}

	public void endElement(String uri, String localName, String qName) throws SAXException
    {
		if (inHead) {
			if ("head".equals(localName))
				inHead = false;
			return;
		}
		if (inEmbed != 0) {
			if ("embed".equals(localName)) {
				if (inEmbed == 1)
					consumer.endElement("", "embed", "embed");
				inEmbed = 0;
			}
			return;
		}
		if (localName.equals("html") || localName.equals("body"))
			return;
		if (localName.equals("a"))
			inLink = false;
		
		consumer.endElement(uri, localName, qName);
    }

	public void characters(char[] ch, int start, int length) throws SAXException
    {
		if (inHead || inEmbed != 0)
			return;
		
		if (!inLink)
		{
			StringBuffer sb = new StringBuffer(new String(ch,start,length));
			int searchStart = 0;
			LinkPosition linkPosition = LinkUtils.findNextLinkPosition(sb, searchStart);
			while (linkPosition != null)
			{
				// ordinary text outside (before) link
				consumer.characters(linkPosition.plainTextBeforeLink.toCharArray(),0,linkPosition.plainTextBeforeLink.length());
				
				// link (anchor) start
                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute("", "href", "href", "CDATA", linkPosition.linkAddress);
//                attrs.addAttribute("", "onclick","onclick","CDATA",LinkUtils.EXTERNAL_LINK_JS);
                attrs.addAttribute("", "target", "target", "CDATA", "_blank");
				consumer.startElement("", "a", "a", attrs);
				
				// clickable text
				consumer.characters(linkPosition.clickableText.toCharArray(),0,linkPosition.clickableText.length());
				
				// link (anchor) end
				consumer.endElement("", "a", "a");

				// find next
				searchStart = linkPosition.linkEnd;
				linkPosition = LinkUtils.findNextLinkPosition(sb,searchStart);
			}
			// the rest of text after last link
			consumer.characters(ch, searchStart, sb.length()-searchStart);
		}
		else
			consumer.characters(ch, start, length);
    }

	public void endDocument() throws SAXException
    {
		consumer.endDocument();
    }
	
	public void endPrefixMapping(String prefix) throws SAXException
    {
		consumer.endPrefixMapping(prefix);
    }

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
		if (inHead || inEmbed != 0)
			return;

		consumer.ignorableWhitespace(ch, start, length);
    }

	public void processingInstruction(String target, String data) throws SAXException
    {
		if (inHead || inEmbed != 0)
			return;
		
		consumer.processingInstruction(target, data);
    }

	public void setDocumentLocator(Locator locator)
    {
		consumer.setDocumentLocator(locator);
    }

	public void skippedEntity(String name) throws SAXException
    {
		if (inHead || inEmbed != 0)
			return;
		
		consumer.skippedEntity(name);
    }

	public void startDocument() throws SAXException
    {
		consumer.startDocument();
    }

	public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
		consumer.startPrefixMapping(prefix, uri);
    }
}
