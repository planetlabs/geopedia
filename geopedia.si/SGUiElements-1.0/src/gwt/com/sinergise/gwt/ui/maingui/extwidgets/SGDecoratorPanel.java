/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.ui.StyleConsts;

public class SGDecoratorPanel extends SimplePanel {
    /**
     * The default style name.
     */
    public static final String DEFAULT_STYLENAME = StyleConsts.SG_DECORATOR_PANEL;

    /**
     * The default styles applied to each row.
     */
    public static final String[] DEFAULT_ROW_STYLENAMES = {"top", "middle", "bottom"};

    /**
     * The default styles suffixes applied to each column.
     */
    public static final String[] DEFAULT_COL_STYLESUFFIXES = {"Left", "Center", "Right"};

    /**
     * Create a new row with a specific style name. The row will contain three
     * cells (Left, Center, and Right), each prefixed with the specified style
     * name.
     * 
     * This method allows Widgets to reuse the code on a DOM level, without
     * creating a DecoratorPanel Widget.
     * 
     * @param styleName the style name
     * @return the new row {@link Element}
     */
    public static Element createTR(String styleName, String[] colSuffixes, boolean createInner) {
      Element trElem = DOM.createTR();
      setStyleName(trElem, styleName);
      if (LocaleInfo.getCurrentLocale().isRTL()) {
          for (int i = colSuffixes.length-1; i >= 0; i--) {
              DOM.appendChild(trElem, createTD(styleName + colSuffixes[i], createInner));
          }
      } else {
          for (int i = 0; i < colSuffixes.length; i++) {
              DOM.appendChild(trElem, createTD(styleName + colSuffixes[i], createInner));
          }
      }
      return trElem;
    }

    /**
     * Create a new table cell with a specific style name.
     * 
     * @param styleName the style name
     * @return the new cell {@link Element}
     */
    private static Element createTD(String styleName, boolean createInner) {
      Element tdElem = DOM.createTD();
      if (createInner) {
          Element inner = DOM.createDiv();
          DOM.appendChild(tdElem, inner);
          setStyleName(inner, styleName + "Inner");
      }
      setStyleName(tdElem, styleName);
      return tdElem;
    }

    /**
     * The container element at the center of the panel.
     */
    private Element containerElem;

    /**
     * The table element.
     */
    private Element table;
   
    /**
     * The table body element.
     */
    private Element tbody;

    /**
     * Create a new {@link DecoratorPanel}.
     */
    public SGDecoratorPanel() {
      this(DEFAULT_ROW_STYLENAMES, DEFAULT_COL_STYLESUFFIXES, 1, true);
    }

    public SGDecoratorPanel(String[] rowStyles, int contentRow) {
        this(rowStyles, DEFAULT_COL_STYLESUFFIXES, contentRow, true);
      }
    
    /**
     * Creates a new panel using the specified style names to apply to each row.
     * Each row will contain three cells (Left, Center, and Right). The Center
     * cell in the containerIndex row will contain the {@link Widget}.
     * 
     * @param rowStyles an array of style names to apply to each row
     * @param containerIndex the index of the container row
     */
    public SGDecoratorPanel(String[] rowStyles, String[] colSuffixes, int containerIndex, boolean createInner) {
    	this(rowStyles, colSuffixes, containerIndex, createInner, true);
    }
    public SGDecoratorPanel(String[] rowStyles, String[] colSuffixes, int containerIndex, boolean createInner, boolean divAround) {
      super(divAround?DOM.createDiv():DOM.createTable());
      if (divAround) {
    	  table = DOM.createTable();
      } else {
    	  table = getElement();
      }
      // Add a tbody
      tbody = DOM.createTBody();
      DOM.appendChild(table, tbody);
      DOM.setElementPropertyInt(table, "cellSpacing", 0);
      DOM.setElementPropertyInt(table, "cellPadding", 0);

      // Add each row
      for (int i = 0; i < rowStyles.length; i++) {
        Element row = createTR(rowStyles[i], colSuffixes, createInner);
        DOM.appendChild(tbody, row);
        if (i == containerIndex) {
            if (createInner) {
                containerElem = DOM.getFirstChild(DOM.getChild(row, 1));
            } else {
                containerElem = DOM.getChild(row, 1);
            }
        }
      }
      if (divAround) {
    	  getElement().appendChild(table);
      }
      // Set the overall style name
      setStyleName(DEFAULT_STYLENAME);
    }

    public String getTableHTML() {
        return getElement().getInnerHTML();
    }
    
    /**
     * Get a specific Element from the panel.
     * 
     * @param row the row index
     * @param cell the cell index
     * @return the Element at the given row and cell
     */
    protected Element getCellElement(int row, int cell) {
      Element tr = DOM.getChild(tbody, row);
      Element td = DOM.getChild(tr, cell);
      return DOM.getFirstChild(td);
    }

    @Override
    protected Element getContainerElement() {
      return containerElem;
    }
    
    @Override
    protected Element getStyleElement() {
        return table;
    }

    public void setContentText(String text) {
        containerElem.setInnerText(text);
    }

    public void setContentHTML(String html) {
        containerElem.setInnerHTML(html);
    }
    
    public void setContentWidget(Widget widget) {
        setWidget(widget);
    }
}
