/*
 *
 */
package com.sinergise.gwt.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class RichLabel extends CompositeExt {
    HorizontalPanel hp;
    protected String iconURL=null;
    protected HTML label=null;
    protected Image iconImage=null;
    public RichLabel(String text) {
        this(text, null);
    }
    public RichLabel(String text, String iconURL) {
        hp=new HorizontalPanel();
        hp.add(label=new HTML());
        hp.setCellWidth(label, "100%");
        hp.setCellVerticalAlignment(label, HasVerticalAlignment.ALIGN_MIDDLE);
        initWidget(hp);
        setText(text);
        setImageURL(iconURL);
    }
    
    public void setText(String text) {
        label.setText(text);
    }
    public void setHTML(String html) {
        label.setHTML(html);
    }
    public void setImageURL(String imageURL) {
        if (iconImage!=null) {
            hp.remove(0);
        }
        iconURL=imageURL;
        if (iconURL!=null) {
            hp.insert(iconImage=new Image(GWT.getModuleBaseURL()+imageURL), 0);
            hp.setCellVerticalAlignment(iconImage, HasVerticalAlignment.ALIGN_MIDDLE);
        } else {
            iconImage=null;
        }
    }
    public String getImageURL() {
        return iconURL;
    }
    
    public void addToRight(Widget w) {
        hp.add(w);
    }
    public void addToLeft(Widget w) {
        hp.insert(w, 0);
    }
    public void remove(Widget w) {
        hp.remove(w);
    }
}
