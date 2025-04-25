/*
 * Copyright 2006 Robert Hanson <iamroberthanson AT gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sinergise.gwt.ui;


import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Image;
import com.sinergise.gwt.util.html.CSS;

/**
 * Image widget that overcomes PNG browser incompatabilities. Although meant for
 * PNG images, it will work with any image format supported by the browser. If
 * the image file ends with ".png" or ".PNG" it will use the PNG specific
 * routines, otherwise will use generic non-PNG specific routines. The URL,
 * width, and height are required at the creation of the widget, and may not be
 * updated via the widget methogs. Calling setUrl() will throw a
 * RuntimeException. This is in part due to the workarounds required for IE 5.5
 * and IE6.
 * 
 * @author rhanson
 */
@Deprecated
public class PNGImage extends Image {
    private int w = -1;
    private int h = -1;

    private boolean trans = true;
    private String url = null;

    private boolean waitForLoad = false;
    private boolean loaded = false;

    public PNGImage() {
        super();
        DOM.setStyleAttribute(getElement(), "border", "0");
        DOM.sinkEvents(getElement(), 0);
    }
    
    public PNGImage(String url) {        
        //this(url, url.toLowerCase().endsWith(".png") || url.indexOf("image/png")>=0, false);
        this(url, true, false);
    }

    public PNGImage(String url, boolean trans, boolean waitForLoad) {
        this();
        this.trans = trans;
        this.waitForLoad=waitForLoad;
        setUrl(url);
    }
    
    public PNGImage(String url, boolean trans, int width, int height) {
        this();
        this.trans = trans;
        setSize(width + "px", height + "px");
        setUrl(url);
    }

    public PNGImage(String url, boolean trans, int width, int height, boolean waitForLoad) {
        this();
        this.trans = trans;
        this.waitForLoad=waitForLoad;
        setSize(width + "px", height + "px");
        setUrl(url);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isWaitForLoad() {
        return waitForLoad;
    }

    public void setWaitForLoad(boolean waitForLoad) {
        this.waitForLoad = waitForLoad;
        doLayout();
    }

    @Override
	public void setUrl(String url) {
        this.url = url;
        
        loaded=true;
        updateSource();
    }

    @Override
	public String getUrl() {
        return url;
    }

    protected void doLayout() {
        if (!loaded && waitForLoad) {
            setVisible(false);
        } else {
            setVisible(true);
        }
        if (trans && url!=null) {
            ImageUtilGWT.setTranslucentSize(getElement(), url, w, h);
        } else {
            ImageUtilGWT.setSize(getElement(), w, h);
        }
    }
    
    private void updateSource() {
        ImageUtilGWT.setSource(getElement(), url, trans, w <= 0 || h <= 0);
        doLayout();
    }

    @Override
	public void setVisible(boolean visible) {
        CSS.display(getElement(), visible?CSS.DISP_DEFAULT:CSS.DISP_NONE);
    }

    @Override
	public void setHeight(String h) {
        super.setHeight(h);
        if (h != null && h.endsWith("px")) {
            this.h = Integer.parseInt(h.substring(0, h.length() - 2));
        } else {
            this.h = -1;
        }
        doLayout();
    }

    @Override
	public void setWidth(String w) {
        super.setWidth(w);
        if (w != null && w.endsWith("px")) {
            this.w = Integer.parseInt(w.substring(0, w.length() - 2));
        } else {
            this.w = -1;
        }
        doLayout();
    }

    public void setTrans(boolean trans) {
        if (this.trans!=trans) {
            this.trans=trans;
            updateSource();
        }
    }
}
