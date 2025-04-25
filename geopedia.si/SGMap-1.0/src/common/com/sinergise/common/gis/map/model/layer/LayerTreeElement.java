/*
 *
 */
package com.sinergise.common.gis.map.model.layer;

import com.sinergise.common.geometry.display.DisplayBounds;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.util.collections.tree.TreeNode;
import com.sinergise.common.util.event.selection.Selectable;
import com.sinergise.common.util.event.selection.SelectableImpl;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.naming.Identifiable;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.state.gwt.StateGWT;


public abstract class LayerTreeElement extends TreeNode<LayerTreeElement, MapContextLayers> implements Identifiable {
	
	public final class PropSelectable extends SelectableImpl {
		private final String propName;
		
		public PropSelectable(String propName) {
			this(propName, false);
		}

		public PropSelectable(String propName, boolean createSelected) {
			super(createSelected);
			this.propName = propName;
		}

		
		@Override
		public void setSelected(boolean sel) {
			if (sel == selected) {
				return;
			}
			super.setSelected(sel);
			notifyChange(propName);
		}
	}  
	
	public static final int		INHERITANCE_NONE				= 0;
	public static final int		INHERITANCE_CHILD_OVERRIDES		= 1;
	public static final int		INHERITANCE_PARENT_OVERRIDES	= 2;

	/**
	 * Sets this layer as the activeLayer.
	 */
	public static final String	PROP_ACTIVE						= "active";
	public static final String	PROP_DEFAULT_SCALE				= "defaultScale";
	/**
	 * Hide or show the layer on the map.
	 */
	public static final String	PROP_ON							= "on";
	public static final String	PROP_TITLE						= "title";
	public static final String	PROP_FULL_TITLE_SEPARATOR		= "fullTitleSeparator";
	
	public static final String PROP_SHOW_LEGEND					= "showLegend";
	
	public static final String PROP_SNAPON						= "snapon";

	/**
	 * Hide or show the node in the tree.
	 */
	public static final String	PROP_VISIBLE					= "visible";

	public static final String	PROP_LAST_MODIFIED				= "lastModified";

	protected Selectable		active							= new PropSelectable(PROP_ACTIVE);
	protected Selectable		on								= new PropSelectable(PROP_ON);

	protected double			defaultScale					= Double.NaN;

	protected String			title							= null;

	protected boolean			visible							= true;

	protected Identifier		id;

	protected StateGWT			genericProperties;

	public LayerTreeElement(String localID) {
		this(localID, null);
	}

	public LayerTreeElement(String localID, String title) {
		id = new Identifier(Identifier.ROOT, localID);
		this.title = title;
		on.setSelected(true); //Don't call setOn() as it may be overridden
	}

	public String getGenericProperty(String propName, int inheritanceType) {
		String myVal = null;
		if (genericProperties != null) myVal = genericProperties.getString(propName, null);
		if (parent == null || inheritanceType == INHERITANCE_NONE) return myVal;
		String parentVal = parent.getGenericProperty(propName, inheritanceType);
		if (inheritanceType == INHERITANCE_CHILD_OVERRIDES) {
			return myVal == null ? parentVal : myVal;
		} else if (inheritanceType == INHERITANCE_PARENT_OVERRIDES) { return parentVal == null ? myVal : parentVal; }
		throw new IllegalArgumentException("Invalid inheritance type " + inheritanceType);
	}

	public LayerTreeElement setGenericProperty(String propName, String propValue) {
		if (genericProperties == null) genericProperties = new StateGWT(false);
		genericProperties.putString(propName, propValue);
		return this;
	}

	public LayerTreeElement setGenericProperty(String propName, boolean propValue) {
		if (genericProperties == null) genericProperties = new StateGWT(false);
		genericProperties.putBoolean(propName, propValue);
		return this;
	}
	
	public boolean showLegend() {
		return !"false".equals(getGenericProperty(PROP_SHOW_LEGEND, INHERITANCE_CHILD_OVERRIDES));
	}
	
	public boolean isExpanded() {
		return "true".equals(getGenericProperty("expanded", INHERITANCE_NONE));
	}

	public LayerTreeElement setShowLegend(boolean show) {
		setGenericProperty(PROP_SHOW_LEGEND, show ? "true" : "false");
		return this;
	}

	@Override
	public void attachedToModel(MapContextLayers newModel) {
		super.attachedToModel(newModel);
		if (isRoot()) {
			id = Identifier.ROOT;
		} else {
			id = new Identifier(parent.getQualifiedID(), id.getLocalID());
		}
	}

	@Override
	public String getLocalID() {
		return id.getLocalID();
	}

	@Override
	public Identifier getQualifiedID() {
		return isRoot() ? Identifier.ROOT : id;
	}

	public boolean deepOn() {
		if (!on.isSelected()) return false;
		return parentDeepOn();
	}

	public abstract DisplayBounds.Disp getBounds();

	public LayerTreeElement setScaleBounds(double minScale, double maxScale) {
		getBounds().setScaleBounds(minScale, maxScale);
		return this;
	}
	
	public DisplayBounds.Disp getBoundsDeep() {
		if (getParent() != null && (getBounds() == null || getBounds().isEmpty())) {
			return getParent().getBoundsDeep();
		}
		return getBounds();
	}

	public double getDefaultScale() {
		if (!Double.isNaN(defaultScale)) return defaultScale;
		if (parent != null) return parent.getDefaultScale();
		return defaultScale;
	}
	
	public long getLastModified() {
		long ret = 0;
		for (LayerTreeElement child : children) {
			ret = Math.max(child.getLastModified(), ret);
		}
		return ret;
	}
	
	/**
	 * Forces the layer images to be re-fetched on next repaint
	 */
	public abstract void setDirty();

	public double getMaxScale() {
		return getBounds().maxScale();
	}

	public double getMinScale() {
		return getBounds().minScale();
	}

	@Override
	public MapContextLayers getModel() {
		return super.getModel();
	}

	public String getFullTitle() {
		String sep = getGenericProperty(PROP_FULL_TITLE_SEPARATOR, INHERITANCE_NONE);
		if (sep != null) { return getParent().getFullTitle() + sep + getTitle(); }
		return getTitle();
	}

	public String getTitle() {
		return title == null ? getLocalID() : title;
	}

	public boolean hasAnythingToRender() {
		return deepOn();
	}

	public boolean hasAnythingToRender(DisplayCoordinateAdapter coords) {
		return hasAnythingToRender(coords.getScale(), coords.pixSizeInMicrons, coords.worldRect);
	}

	public boolean hasAnythingToRender(double scale, double pixSizeInMicrons, Envelope mbr) {
		if (!deepOn()) return false;
		DisplayBounds bounds = getBoundsDeep();
		if (bounds == null) return true;
		return bounds.intersects(scale, pixSizeInMicrons, mbr);
	}
	
	public int getVisibleChildCount() {
		int cnt = 0;
		if (children != null) {
			for (LayerTreeElement child : children) {
				if (child.isVisible()) {
					cnt++;
				}
			}
		}
		return cnt;
	}

	public boolean isActive() {
		return active.isSelected();
	}

	public boolean isOn() {
		return on.isSelected();
	}

	abstract public boolean isRenderable();

	public boolean isVisible() {
		return (visible);
	}

	public boolean parentDeepOn() {
		if (getParent() == null) return true;
		return getParent().deepOn();
	}

	public Selectable selectableForActive() {
		return active;
	}

	public Selectable selectableForOn() {
		return on;
	}

	public LayerTreeElement setActive(boolean active) {
		this.active.setSelected(active);
		return this;
	}

	public LayerTreeElement setDefaultScale(double defaultScale) {
		if (Double.compare(this.defaultScale, defaultScale) == 0) return this;
		this.defaultScale = defaultScale;
		notifyChange(PROP_DEFAULT_SCALE);
		return this;
	}

	public LayerTreeElement setOn(boolean on) {
		this.on.setSelected(on);
		return this;
	}
	
	/**
	 * Turns on this and all parent layers in on is <code>true</code>.
	 * If on is <code>false</code> it will turn off only this layer.
	 * @param on
	 */
	public LayerTreeElement setDeepOn(boolean on) {
		setOn(on);
		if (on && getParent() != null) {
			getParent().setDeepOn(true);
		}
		return this;
	}

	public LayerTreeElement setTitle(String title) {
		this.title = title;
		notifyChange(PROP_TITLE);
		return this;
	}

	public LayerTreeElement addFilterCapabilities(FilterCapabilities filterCaps) {
		if (this instanceof FeatureDataLayer) ((FeatureDataLayer)this).appendFilterCapabilities(filterCaps);
		return this;
	}

	public LayerTreeElement setVisible(boolean visible) {
		this.visible = visible;
		notifyChange(PROP_VISIBLE);
		return this;
	}

	public abstract boolean isOpaque();

	public boolean isOpaque(double scale, Envelope mbr) {
		if (!isOpaque()) return false;
		DisplayBounds.Disp bnds = getBounds();
		if (!bnds.scaleIntersects(scale)) return false;
		return bnds.mbr.contains(mbr);
	}

	public boolean isOpaque(DisplayCoordinateAdapter dca) {
		return isOpaque(dca.getScale(), dca.worldRect);
	}

	public int getRenderDelayOnChange() {
		return 2000;
	}
	
}
