package com.sinergise.gwt.gis.map.ui.controls.mapLayersTree;

import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.style.ComponentStyle;
import com.sinergise.common.gis.map.model.style.Style;
import com.sinergise.common.gis.map.model.style.StyleComponent;
import com.sinergise.common.ui.action.ToggleAction;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.map.ui.controls.mapLayersTree.StyleWidgetFactory.StyleWidgetProvider;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.ui.ActionToggleButton;
import com.sinergise.gwt.ui.resources.Theme;


public class ComponentStyleWidgets implements StyleWidgetProvider {
	public static final HashMap<?, ?> COMP_ICONS=new HashMap<Object, Object>(){
		private static final long serialVersionUID = 1L;
	{
		put(ComponentStyle.COMP_TEXT, "img/layers_tree/DrawText.png");
		put(ComponentStyle.COMP_FILL, "img/layers_tree/DrawShade.png");
		put(ComponentStyle.COMP_LINE, "img/layers_tree/DrawLine.png");
		put(ComponentStyle.COMP_SYMBOL, "img/layers_tree/DrawSymbol.png");
		put(ComponentStyle.COMP_CLUSTER, "img/layers_tree/DrawCluster.png");
	}};
	public static class ComponentOnAction extends ToggleAction {
		private ComponentStyle cs;
		private String compName;

		public ComponentOnAction(ComponentStyle st, final String compName) {
			super(getDisplayName(compName));
			this.cs = st;
			this.compName = compName;
			setIcon(getIconResource(compName));
			setSelected(st.getComponent(compName).isOn());
			
			st.addPropertyChangeListener(new PropertyChangeListener<Object>() {
				@Override
				public void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {
					if (propertyName.equals(compName+".on") && newValue instanceof Boolean) {
						setSelected(((Boolean)newValue).booleanValue());
					}
				}
			});
		}

		private static String getDisplayName(String cName) {
			if (cName == null) return "";
			if (cName.equals(ComponentStyle.COMP_TEXT)) {
				cName = Tooltips.INSTANCE.layer_text();
			} else if (cName.equals(ComponentStyle.COMP_LINE)) {
				cName = Tooltips.INSTANCE.layer_line();
			} else if (cName.equals(ComponentStyle.COMP_FILL)) {
				cName = Tooltips.INSTANCE.layer_fill();
			} else if (cName.equals(ComponentStyle.COMP_SYMBOL)) {
				cName = Tooltips.INSTANCE.layer_symbol();
			} else if (cName.equals(ComponentStyle.COMP_CLUSTER)) {
				cName = Tooltips.INSTANCE.layer_cluster();
			}
			return cName;
		}
		private static ImageResource getIconResource(String cName) {
			if (cName.equals(ComponentStyle.COMP_TEXT)) {
				return Theme.getTheme().standardIcons().text();
			} else if (cName.equals(ComponentStyle.COMP_LINE)) {
				return GisTheme.getGisTheme().gisStandardIcons().line();
			} else if (cName.equals(ComponentStyle.COMP_FILL)) {
				return Theme.getTheme().standardIcons().shade();
			} else if (cName.equals(ComponentStyle.COMP_SYMBOL)) {
				return GisTheme.getGisTheme().gisStandardIcons().point();
			} else if (cName.equals(ComponentStyle.COMP_CLUSTER)) {
				return GisTheme.getGisTheme().gisStandardIcons().cluster();
			}
			else return Theme.getTheme().standardIcons().dummy();
		}

		@Override
		protected void selectionChanged(boolean newSelected) {
			StyleComponent styleComponent = cs.getComponent(compName);
			styleComponent.setOn(newSelected);
		}
	}
	
	public static class PopupDisplayAction extends ToggleAction {
		ActionToggleButton button = null;
		TextPropertyPanel popup;
		StyleComponent component;

		public PopupDisplayAction(StyleComponent component, String compName) {
			super("Edit text");
			this.component = component;
			setIcon(Theme.getTheme().standardIcons().arrowDown());
			if (component.canHandleAuxData(compName)) {
				popup = new TextPropertyPanel(component.getState(), component.getStyleSessionParamName(), component.getAuxParams(), compName);
			}
		}

		public void setButton(ActionToggleButton button) {
			this.button = button;
			popup.setParentButton(button);
		}

		@Override
		protected void selectionChanged(boolean newSelected) {
			if (popup != null && button != null) {
				popup.show();
				popup.setPopupPosition(button.getAbsoluteLeft() - popup.getOffsetWidth() + button.getOffsetWidth(), button.getAbsoluteTop() + button.getOffsetHeight());
			}
		}
	}

	public static class StyleComponentsWidget extends LayerStyleWidget {
		public StyleComponentsWidget(ComponentStyle cs) {
			super(cs);
			FlowPanel fp=new FlowPanel();
			fp.setStylePrimaryName("sgwebgis-styleComponentsWidget");
			for (Iterator<?> it = cs.namesIterator(); it.hasNext();) {
				String	cn = (String) it.next();
				ComponentOnAction coa = new ComponentOnAction(cs, cn);
				ActionToggleButton button = new ActionToggleButton(coa);
				fp.add(button);
				addPopupButton(cs, fp, cn);
			}
			initWidget(fp);
		}

		private static void addPopupButton(ComponentStyle cs, FlowPanel fp, String cn) {
			StyleComponent component = cs.getComponent(cn);
			if (component.canHandleAuxData(cn)) {
				PopupDisplayAction pda = new PopupDisplayAction(component, cn);
				ActionToggleButton popupButton = new ActionToggleButton(pda);
				pda.setButton(popupButton);
				popupButton.addStyleName("gwt-popupButton");
				fp.add(popupButton);
			}
		}
	}
	
	@Override
	public boolean canHandle(Layer layer) {
		Style st=layer.getStyle();
		if (st==null) return false;
		return (st instanceof ComponentStyle);
	}

	@Override
	public LayerStyleWidget createFor(Layer layer) {
		return new StyleComponentsWidget((ComponentStyle) layer.getStyle());
	}

}