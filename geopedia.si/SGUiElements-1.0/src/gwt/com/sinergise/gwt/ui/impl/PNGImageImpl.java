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

package com.sinergise.gwt.ui.impl;



import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.sinergise.gwt.util.html.CSS;


public class PNGImageImpl
{
    
    public Element createPNGElement()
    {
        return DOM.createImg();
    }
    
    public native void setPNGSize(Element el, String url, int w, int h) /*-{
        if (w>=0) {
             el["width"]=w;
             el.style.width=w+'px';
        }
        else {
             el.removeAttribute('width');
             el.style.width='auto';
        }
        
        if (h>=0) {
             el["height"]=h;
             el.style.height=h+'px';
        }
        else {
             el.removeAttribute('height');
             el.style.height='auto';
        }
    }-*/;
    
    public native void setPNGsource(Element el, String src, boolean autoSize) /*-{
        el["src"]=src;
        if (autoSize) {
            el.style.width="auto";
            el.style.height="auto";
        }
    }-*/;

    public void setPNGBackground(Element e, String srcImg) {
        CSS.background(e, "url('"+srcImg+"')");
    }

    public native void clearSource(Element el) /*-{
        el["src"]="";
    }-*/;
}
