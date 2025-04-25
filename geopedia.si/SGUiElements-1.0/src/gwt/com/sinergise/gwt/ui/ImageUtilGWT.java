package com.sinergise.gwt.ui;


import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.gwt.ui.impl.PNGImageImpl;
import com.sinergise.gwt.util.UtilGWT;
import com.sinergise.gwt.util.html.CSS;


public class ImageUtilGWT
{   
	public static interface ImageLoadListener {
		void onLoad(Object imageObject);
		void onError(Object imageObject);
	}
	
    public static interface ImageFetchCallback extends AsyncCallback<Element> {
        /**
         * @param nTries Number of (unsuccessful) tries so far
         * @return
         */
        public boolean shouldRefetch(int nTries);
    }
    
	public static final String VA_MIDDLE = "middle";

	public static Element fetchImage(final String url, final int millisBeforeRetry, final ImageFetchCallback cb)
	{
		Element pf = DOM.createImg();
		fetchImage(pf, url, 0, millisBeforeRetry, cb);
		return pf;
	}

	public static String fetchImage(final Element pf, final String url, final int nTries, final int millisBeforeRetry, final ImageFetchCallback cb)
	{
        DOM.sinkEvents(pf, Event.ONLOAD | Event.ONERROR);
		DOM.setEventListener(pf, new EventListener() {
			public void onBrowserEvent(Event event)
			{
			    try {
				switch (DOM.eventGetType(event)) {
				case Event.ONLOAD:
					// Window.alert("prefetch load");
					DOM.setEventListener(pf, null);
					cb.onSuccess(pf);
					return;
				case Event.ONERROR:
					DOM.setEventListener(pf, null);
					if (cb.shouldRefetch(nTries+1)) {
					    new Timer() {
                            @Override
							public void run() {
                                //System.out.println("Refetch "+nTries+":"+url);
                                fetchImage(pf, url, nTries+1, (int)(millisBeforeRetry*1.5), cb);
                            }
                        }.schedule(millisBeforeRetry);
					} else {
                        //System.out.println("Refetch failed :"+url);
						cb.onFailure(null);
					}
					break;
				default:
					break;
				}
			    } catch (Exception e) {}
			}
		});
		return setSource(pf, url);
	}

	public static void cancelFetch(Element fetchEl)
	{
		DOM.setEventListener(fetchEl, null);
		clearSource(fetchEl);
	}
	
	public static void ensureInterpolation(Element imageEl)
	{
		if (UtilGWT.isIE6or7()) {
			CSS.ie6Interpolation(imageEl, CSS.IE6_IMG_INT_BICUBIC);
		}
	}

	private static PNGImageImpl impl;

	private ImageUtilGWT()
	{
	}

	public static Element createPNGElement()
	{
		checkImpl();
		return impl.createPNGElement();
	}

	public static void setTranslucentBackground(Element e, String srcImg)
	{
		checkImpl();
		impl.setPNGBackground(e, srcImg);
	}

	public static void setTranslucentSize(Element el, String url, int w, int h)
	{
		checkImpl();
		impl.setPNGSize(el, url, w, h);
	}

	public static void setTranslucentSource(Element e, String src, boolean autoSize)
	{
		checkImpl();
		impl.setPNGsource(e, src, autoSize);
	}

	private static void checkImpl()
	{
		if (impl == null) {
			impl = new PNGImageImpl();
		}
	}

	public static String setSource(Element element, String url)
	{
		DOM.setElementAttribute(element, "src", url);
		return getSrc(element);
	}

	public static void setSize(Element element, DimI size) {
		setSize(element, size.w(), size.h());
	}
	
	public static void setSize(Element element, int w, int h)
	{
		DOM.setElementAttribute(element, "width", w < 0 ? "" : String.valueOf(w));
		DOM.setElementAttribute(element, "height", h < 0 ? "" : String.valueOf(h));
		if (w >= 0)
			CSS.width(element, w + CSS.U_PIXELS);
		else
			CSS.width(element, "");
		if (h >= 0)
			CSS.height(element, h + CSS.U_PIXELS);
		else
			CSS.height(element, "");
	}

	public static native boolean isLoaded(Element img) /*-{
	 // During the onload event, IE correctly identifies any images that
	 // weren't downloaded as not complete. Others should too. Gecko-based
	 // browsers act like NS4 in that they report this incorrectly.
	 if (!img.complete) {
	 return false;
	 }

	 // However, they do have two very useful properties: naturalWidth and
	 // naturalHeight. These give the true size of the image. If it failed
	 // to load, either of these should be zero.
	 if (typeof img.naturalWidth != "undefined" && img.naturalWidth == 0) {
	 return false;
	 }

	 // No other way of checking: assume it's ok.
	 return true;        
	 }-*/;

	public native static int getNaturalWidth(Element img) /*-{
	 var naturalWidth  = -1;
	 if(img.naturalWidth != null)
	 {
	 naturalWidth = img.naturalWidth;
	 }
	 else if(img.runtimeStyle)
	 {
	 img.runtimeStyle.width= 'auto';
	 img.runtimeStyle.height= 'auto';
	 img.runtimeStyle.borderWidth= '0';
	 img.runtimeStyle.padding= '0';
	 naturalWidth =  img.offsetWidth;
	 img.runtimeStyle.width= '';
	 img.runtimeStyle.height= '';
	 img.runtimeStyle.borderWidth= '';
	 img.runtimeStyle.padding= '';
	 }else 
	 {
	 var imgBk = img.cloneNode(true);
	 img.className = '';
	 img.style.width = 'auto !important';
	 img.style.height = 'auto !important';
	 img.style.borderWidth= '0 !important';
	 img.style.padding= '0 !important';
	 img.removeAttribute('width');
	 img.removeAttribute('height');
	 
	 naturalWidth =  img.width;
	 img.setAttribute('width' , imgBk.getAttribute('width') );
	 img.setAttribute('height', imgBk.getAttribute('height') );
	 img.style.width = imgBk.style.width ; 
	 img.style.height = imgBk.style.height ; 
	 img.style.padding = imgBk.style.padding ; 
	 img.style.borderWidth=  imgBk.style.borderWidth ; 
	 img.style.className = imgBk.style.className ; 
	 
	 };
	 return naturalWidth;
	 }-*/;

	public native static int getNaturalHeight(Element img) /*-{
	 var naturalHeight  = -1;
	 if(img.naturalHeight != null)
	 {
	 naturalHeight = img.naturalHeight;
	 }
	 else if(img.runtimeStyle)
	 {
	 img.runtimeStyle.width= 'auto';
	 img.runtimeStyle.height= 'auto';
	 img.runtimeStyle.borderWidth= '0';
	 img.runtimeStyle.padding= '0';
	 naturalHeight =  img.offsetHeight;
	 img.runtimeStyle.width= '';
	 img.runtimeStyle.height= '';
	 img.runtimeStyle.borderWidth= '';
	 img.runtimeStyle.padding= '';
	 }else 
	 {
	 var imgBk = img.cloneNode(true);
	 img.className = '';
	 img.style.width = 'auto !important';
	 img.style.height = 'auto !important';
	 img.style.borderWidth= '0 !important';
	 img.style.padding= '0 !important';
	 img.removeAttribute('width');
	 img.removeAttribute('height');
	 
	 naturalHeight =  img.height;

	 img.setAttribute('width' , imgBk.getAttribute('width') );
	 img.setAttribute('height', imgBk.getAttribute('height') );

	 img.style.width = imgBk.style.width ; 
	 img.style.height = imgBk.style.height ; 
	 img.style.padding = imgBk.style.padding ; 
	 img.style.borderWidth=  imgBk.style.borderWidth ; 
	 img.style.className = imgBk.style.className ; 
	 
	 };

	 return naturalHeight;
	 }-*/;

	public static void setSource(Element imgEl, String src, boolean trans)
	{
		setSource(imgEl, src, trans, false);
	}

	public static void setSource(Element imgEl, String src, boolean trans, boolean autoSize)
	{
		if (trans)
			setTranslucentSource(imgEl, src, autoSize);
		else {
		    if (autoSize) {
		        DOM.removeElementAttribute(imgEl, "width");
		        DOM.removeElementAttribute(imgEl, "height");
		        CSS.size(imgEl, "", "");
		    }
			setSource(imgEl, src);
		}
	}

	public static void clearSource(Element el)
	{
		checkImpl();
		impl.clearSource(el);
	}

	public static void valign(Element img, String va)
	{
		DOM.setElementAttribute(img, "valign", va);
	}

	public static String getSrc(Element element)
	{
		return DOM.getElementAttribute(element, "src");
	}

	public static Element createPNGElement(String imgpath, boolean autoSize)
	{
		checkImpl();
		Element el = impl.createPNGElement();
		impl.setPNGsource(el, imgpath, autoSize);
		return el;
	}

	public static final int FILL_ICON_WIDTH = 32;
	public static final int FILL_ICON_HEIGHT = 32;
	public static final int SYM_ICON_WIDTH = 32;
	public static final int SYM_ICON_HEIGHT = 32;
	public static final int LINE_ICON_WIDTH = 48;
	public static final int LINE_ICON_HEIGHT = 16;
	public static final int FONT_ICON_WIDTH = 64;
	public static final int FONT_ICON_HEIGHT = 20;

    public static void border(Element element, String val) {
        DOM.setElementAttribute(element, "border", val);
    }
    
    public static void titleSet(Element img, String string) {
        DOM.setElementProperty(img, "title", string);
        DOM.setElementProperty(img, "alt", string);
    }

    public static void titleAppend(Element img, String string) {
        DOM.setElementProperty(img, "title", string+DOM.getElementProperty(img, "title"));
        DOM.setElementProperty(img, "alt", string+DOM.getElementProperty(img, "alt"));
    }

	public static DimI getNaturalSize(Element imgEl) {
		return DimI.create(getNaturalWidth(imgEl), getNaturalHeight(imgEl));
	}
}
