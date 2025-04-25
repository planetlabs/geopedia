package com.sinergise.gwt.gis.map.shapes.editor;

import static com.sinergise.common.gis.map.model.layer.FeatureDataLayer.TYPE_ALL_GEOMETRIES;
import static com.sinergise.common.gis.map.model.layer.FeatureDataLayer.TYPE_COLLECTION;
import static com.sinergise.common.gis.map.model.layer.FeatureDataLayer.TYPE_LINE;
import static com.sinergise.common.gis.map.model.layer.FeatureDataLayer.TYPE_MULTI_POINT;
import static com.sinergise.common.gis.map.model.layer.FeatureDataLayer.TYPE_POINT;
import static com.sinergise.common.gis.map.model.layer.FeatureDataLayer.TYPE_POLYGON;
import static com.sinergise.gwt.gis.map.ui.vector.AbstractTextMarker.DEFAULT_WHITE_GLOW;
import static com.sinergise.gwt.ui.core.MouseHandler.BUTTON_LEFT;
import static com.sinergise.gwt.ui.core.MouseHandler.MOD_CTRL;
import static com.sinergise.gwt.ui.core.MouseHandler.MOD_NONE;
import static com.sinergise.gwt.ui.core.MouseHandler.isControlDown;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Focusable;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.GraphicMeasure;
import com.sinergise.common.geometry.index.PointQuadtree;
import com.sinergise.common.geometry.topo.Edge;
import com.sinergise.common.geometry.topo.GeometryBuilder;
import com.sinergise.common.geometry.topo.ITopoMap;
import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.topo.NodeStar;
import com.sinergise.common.geometry.topo.TopoEditorModel;
import com.sinergise.common.geometry.topo.TopoEditorModel.TopoEditorListener;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.gis.map.shapes.snap.SnapProvider;
import com.sinergise.common.gis.map.shapes.snap.SnapProvider.SnapProviderCallback;
import com.sinergise.common.gis.map.shapes.snap.SnapProvider.SnapProviderCallbackAdapter;
import com.sinergise.common.ui.action.ToggleAction;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.event.selection.Selectable;
import com.sinergise.common.util.geom.CoordinatePair;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.gwt.gis.map.ui.IMapComponent;
import com.sinergise.gwt.gis.map.ui.IOverlaysHolder;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.actions.MouseClickActionW;
import com.sinergise.gwt.gis.map.ui.actions.MouseDragActionW;
import com.sinergise.gwt.gis.map.ui.actions.MouseMoveActionW;
import com.sinergise.gwt.gis.map.ui.vector.AbstractTextMarker;
import com.sinergise.gwt.gis.map.ui.vector.LineLengthTextMarker;
import com.sinergise.gwt.gis.map.ui.vector.LineMarker;
import com.sinergise.gwt.gis.map.ui.vector.LineMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.Marker;
import com.sinergise.gwt.gis.map.ui.vector.TextMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.VectorFilter;
import com.sinergise.gwt.gis.map.ui.vector.signs.Sign;
import com.sinergise.gwt.ui.core.MouseHandler;

public class TopoEditor implements TopoEditorListener, Focusable {

	public static final int HOVER_PX_SQ = 8 * 8;
	protected static final LineMarkerStyle EDGE_STYLE = new LineMarkerStyle("lime", GraphicMeasure.fixedPixels(1));
	protected static final LineMarkerStyle LOCKED_EDGE_STYLE = new LineMarkerStyle("red", GraphicMeasure.fixedPixels(1));
	protected static final Sign MID_EDGE_SIGN = new Sign("edit-node middle", 3, 1);
	protected static final Sign MID_EDGE_SIGN_LOCKED = new Sign("edit-node middle locked", 3, 1);
	protected static final Sign NODE_SIGN = new Sign("edit-node", 5, 2);
	protected static final Sign NODE_SIGN_LOCKED = new Sign("edit-node locked", 5, 2);
	private static final String STYLE_HOVER_SUFFIX = "hover";
	private static final String STYLE_SELECTED_SUFFIX = "selected";
	
	public final static class MidEdge implements HasCoordinate {
		public final Edge e;
	
		public MidEdge(Edge e) {
			this.e = e;
		}
	
		@Override
		public double x() {
			return 0.5 * (e.x1() + e.x2());
		}
	
		@Override
		public double y() {
			return 0.5 * (e.y1() + e.y2());
		}
	}

	public static interface ITopoEditorBehavior {
		/**
		 * If true, a node is added if no node can be selected when mouse drag event occurs. If false a node is
		 * selected, otherwise nothing happens
		 */
		boolean addNodeOnMouseDrag();
		
		boolean splitEdgeOnClick();

		/**
		 * If true, mouse drag events can be handled by chained drag events, if TopologyEditor doesn't handle mouse drag
		 * event If false, only TopologyEditor MouseDragAction will handle mouse drag events
		 */
		boolean chainMouseDragEvents();
		
		int getSupportedGeometryTypes();
	}
	
	public static class DefaultTopoEditorBehavior implements ITopoEditorBehavior {
		@Override
		public boolean addNodeOnMouseDrag() {
			return true;
		}
		
		@Override
		public boolean splitEdgeOnClick() {
			return false;
		}

		@Override
		public boolean chainMouseDragEvents() {
			return false;
		}
		
		@Override
		public int getSupportedGeometryTypes() {
			return TYPE_ALL_GEOMETRIES;
		}
	}


	public static interface TopoEditorModificationListener {
		void topologyModified();
	}

	public static interface MarkerMoveListener {
		void onMarkerMoveStart(Marker marker);

		void onMarkerMoveEnd(Marker marker);
	}
	
	public static class TopoEditorSettings {
		
		public TextMarkerStyle textStyle = new TextMarkerStyle("#444444", DEFAULT_WHITE_GLOW); 
		public TextMarkerStyle getTextStyle() {
			return textStyle;
		}
		
		public boolean showLockedEdges = true;
		public boolean shouldShowLockedEdges() {
			return showLockedEdges;
		}
		
		public boolean showLockedNodes = true;
		public boolean shouldShowLockedNodes() {
			return showLockedNodes;
		}
		
		public boolean showLockedAdjacents = true;
		public boolean shouldShowLockedAdjacents() {
			return showLockedAdjacents;
		}
	}

	private final class HoverAction extends MouseMoveActionW {
		public HoverAction() {
			super(TopoEditor.this.dca, "Hover");
		}

		@Override
		protected void mouseMovedW(Position2D worldPos) {
			hover(worldPos);
		}
	}

	private final class MoveMarkerAction extends MouseDragActionW {
		public MoveMarkerAction() {
			super(TopoEditor.this.dca, "Move Marker");
		}

		@Override
		protected boolean dragStartW(Position2D startWorld) {
			TopoEditor.this.setFocus(true);
			boolean success = false;
			if (editorBehavior.addNodeOnMouseDrag()) {
				success = selectOrAddNode(startWorld, isControlDown(getCurrentModifiers()));
			} else {
				success = selectNode(startWorld);
			}
			if (success) {
				if (isSelectedMidPoint()) {
					dragStartMidEdge(selectedPoint, startWorld);
					
				} else if (isSelectedLockedNode()) {
					dragStartLockedNode(selectedPoint);
				}
				fireOnMarkerMoveStart(selectedPoint);
				return true;
			}
			return false;
		}

		private boolean isSelectedMidPoint() {
			return selectedPoint.worldPos instanceof MidEdge;
		}
		
		private boolean isSelectedLockedNode() {
			if (!(selectedPoint.worldPos instanceof Node)) {
				return false;
			}
			Node n = (Node)selectedPoint.worldPos;
			return n.isLocked();
		}

		@Override
		protected void dragMoveW(Position2D worldPos) {
			hover(worldPos);
			if (hoverPoint != null) {
				
				if (hoverPoint.worldPos instanceof Node) {
					worldPos.setLocation(hoverPoint);
					
				} else if (hoverPoint.worldPos instanceof MidEdge) {
					if (selectedPoint.worldPos instanceof Node) {
						if (!((Node)selectedPoint.worldPos).isConstrainedToDiagonal()) {
							Edge e = ((MidEdge)hoverPoint.worldPos).e;
							GeomUtil.pointLineStringNearest(e.x1(), e.y1(), e.x2(), e.y2(), worldPos.x, worldPos.y, worldPos);
						}
					}
				}
			}
			moveSelected(worldPos);
		}

		@Override
		protected void dragEndW(Position2D coordWorld) {
			if (selectedPoint == null) {
				return;
			}
			if (hoverPoint != selectedPoint && hoverPoint != null) {
				if (hoverPoint.worldPos instanceof Node) {
					mergeNodes();
				} else if (hoverPoint.worldPos instanceof MidEdge) {
					mergeNodeToEdge();
				}
			} else {
				snapNode(selectedPoint.worldPos);
			}
			fireOnMarkerMoveEnd(selectedPoint);
			fireTopologyModified();
		}
	}

	private final class TopoKeyListener implements KeyDownHandler {
		private HandlerRegistration keyHandler;

		@Override
		public void onKeyDown(KeyDownEvent event) {
			final boolean ctrl = event.isControlKeyDown();
			switch (event.getNativeKeyCode()) {
				case KeyCodes.KEY_LEFT:
					move(-1, 0, ctrl);
					break;
				case KeyCodes.KEY_RIGHT:
					move(1, 0, ctrl);
					break;
				case KeyCodes.KEY_UP:
					move(0, 1, ctrl);
					break;
				case KeyCodes.KEY_DOWN:
					move(0, -1, ctrl);
					break;
				case KeyCodes.KEY_DELETE:
					deleteSelected();
					break;
				case com.sinergise.common.ui.core.KeyCodes.KEY_V:
					validate();
					break;
				default:
					return;
			}
			event.preventDefault();
		}

		private void move(int dx, int dy, boolean ctrl) {
			moveSelected(dx, dy, ctrl);
			fireTopologyModified();
		}

		public void register(IOverlaysHolder widget) {
			if (keyHandler != null)
				throw new IllegalStateException("Already registered");
			keyHandler = widget.addDomHandler(this, KeyDownEvent.getType());
		}

		public void unregister() {
			if (keyHandler != null)
				keyHandler.removeHandler();
			keyHandler = null;
		}
	}

	private final class ClickSelectAction extends MouseClickActionW {
		public ClickSelectAction() {
			super(TopoEditor.this.dca, "Select Node");
		}

		@Override
		protected boolean mouseClickedW(double xWorld, double yWorld) {
			TopoEditor.this.setFocus(true);
			return selectOrAddNode(new Position2D(xWorld, yWorld), isControlDown(getCurrentModifiers()));
		}
	}

	private final class RightClickAction extends MouseClickActionW {
		public RightClickAction() {
			super(TopoEditor.this.dca, "Clear Selection");
		}

		@Override
		protected boolean mouseClickedW(double xWorld, double yWorld) {
			return setSelected(null);
		}
	}

	public final class ToggleEditorActiveAction extends ToggleAction {
		public ToggleEditorActiveAction() {
			super("EditingActive");
		}

		@Override
		protected void selectionChanged(boolean newSelected) {
			MouseHandler mh = map.getMouseHandler();
			if (newSelected) {
				mh.registerAction(actionMove, BUTTON_LEFT, MOD_NONE | MOD_CTRL);
				mh.registerAction(actionHover, MOD_NONE);
				mh.registerAction(actionClick, BUTTON_LEFT, MOD_NONE | MOD_CTRL, 1);
				mh.registerAction(actionRightClick, MouseHandler.BUTTON_RIGHT, MOD_NONE | MOD_CTRL, 1);
				keyListener.register(map.getOverlaysHolder());
			} else {
				mh.deregisterAction(actionMove);
				mh.deregisterAction(actionHover);
				mh.deregisterAction(actionClick);
				mh.deregisterAction(actionRightClick);
				keyListener.unregister();
			}
		}
	}

	protected ITopoEditorBehavior editorBehavior;
	protected final IMapComponent map;
	protected final DisplayCoordinateAdapter dca;
	protected final TopoEditorOverlay ovr;

	protected Marker hoverPoint;
	protected Marker selectedPoint;

	protected final TopoEditorModel topoModel;
	protected SnapProvider snapProvider;

	//Init in constructor, because we need map
	protected final MoveMarkerAction actionMove;
	private final HoverAction actionHover;
	private final ClickSelectAction actionClick;
	private final RightClickAction actionRightClick;
	private final TopoKeyListener keyListener;

	protected HashMap<Edge, LineMarker> edges = new HashMap<Edge, LineMarker>();
	protected Map<Edge, Marker> midPoints = new HashMap<Edge, Marker>();
	protected PointQuadtree<Marker> midPointsIndex; //Leave null so that building can be done on first use (otherwise it would be done incrementally)
	protected Map<Node, Marker> points = new HashMap<Node, Marker>();
	protected HashMap<Edge, LineLengthTextMarker> texts = new HashMap<Edge, LineLengthTextMarker>();

	protected ToggleAction displayLabelsSelectable = new ToggleAction("Display text labels") {
		{
			setSelected(true);
		}
		@Override
		protected void selectionChanged(boolean newSelected) {
			updateEdgeLabelsDisplay(newSelected);
		}
	};
	private ToggleEditorActiveAction activeAction = new ToggleEditorActiveAction();
	private Collection<TopoEditorModificationListener> modListeners;
	private Collection<MarkerMoveListener> markerMoveListeners;

	private TopoEditorSettings settings = new TopoEditorSettings();
	private boolean visible;

	public TopoEditor(IMapComponent map) {
		this(map, new TopoEditorModel());
	}

	public TopoEditor(IMapComponent map, ITopoEditorBehavior behavior) {
		this(map, new TopoEditorModel(), behavior);
	}

	public TopoEditor(IMapComponent map, TopoEditorModel model) {
		this(map, model, new DefaultTopoEditorBehavior());
	}

	public TopoEditor(IMapComponent map, TopoEditorModel model, ITopoEditorBehavior behavior) {
		this.editorBehavior = behavior;
		this.map = map;
		this.dca = map.getCoordinateAdapter();
		this.ovr = new TopoEditorOverlay(this);
		this.topoModel = model;
		model.setPeer(this);
	
		//Init here because we need map
		actionMove = new MoveMarkerAction();
		actionMove.setChainable(editorBehavior.chainMouseDragEvents());
		actionHover = new HoverAction();
		actionClick = new ClickSelectAction();
		actionRightClick = new RightClickAction();
		keyListener = new TopoKeyListener();
	
		//modification listener to update midPoints index
		addMarkerMoveListener(new MarkerMoveListener() {
			Set<Marker> moving = new HashSet<Marker>(5);
			@Override
			public void onMarkerMoveStart(Marker marker) {
				if (midPointsIndex == null) {
					return;
				}
				if (!(marker.worldPos instanceof Node)) {
					return;
				}
				Node n = (Node)marker.worldPos;
				moving.clear();
					
				//remove effected midpoints from index
				for (Edge e : topoModel.getTopoMap().getNodeStar(n)) {
					Marker edgeMarker = midPoints.get(e);
					if (edgeMarker != null) {
						moving.add(edgeMarker);
					}
				}
				midPointsIndex.removeAll(moving);
			}
			
			@Override
			public void onMarkerMoveEnd(Marker marker) {
				if (midPointsIndex == null || moving.isEmpty()) {
					return;
				}
				//put back to index
				midPointsIndex.addAll(moving);
				moving.clear();
			}
		});
	}

	private void updateEdgeLabelsDisplay(boolean newSelected) {
		if (newSelected) {
			for (Edge e : edges.keySet()) {
				ensureTextMarker(e);
			}
		} else {
			removeTextMarkers();
		}
	}

	public void addModificationListener(TopoEditorModificationListener l) {
		if (modListeners == null) {
			modListeners = new ArrayList<TopoEditorModificationListener>();
		}
		modListeners.add(l);
	}

	public void removeModificationListener(TopoEditorModificationListener l) {
		if (modListeners == null) {
			return;
		}
		modListeners.remove(l);
	}

	public void setSnapProvider(SnapProvider provider) {
		this.snapProvider = provider;
	}

	protected void fireTopologyModified() {
		if (modListeners == null) {
			return;
		}
		for (TopoEditorModificationListener l : modListeners) {
			l.topologyModified();
		}
	}


	public void validate() {
		try {
			List<List<Node>> rings = new GeometryBuilder(topoModel.getTopoMap()).buildLineStrings(true, true);
			for (List<Node> list : rings) {
				for (Node n : list) {
					System.out.print(n.x() + " " + n.y() + ", ");
				}
				System.out.println();
			}
		} catch(TopologyException e) {
			e.printStackTrace();
		}
	}


	protected void hover(HasCoordinate worldPos) {
		setHover(getNearestMarker(worldPos, getCurrentHoverDistSq(), actionMove.isDragging()));
	}

	private double getCurrentHoverDistSq() {
		return dca.worldFromPix.area(HOVER_PX_SQ);
	}

	public boolean selectNode(HasCoordinate pos) {
		hover(pos);
		if (hoverPoint == null)
			return false;
		return setSelected(hoverPoint);
	}

	public boolean selectOrAddNode(HasCoordinate pos, boolean controlDown) {
		hover(pos);
		Node added = addPoint(pos, controlDown);
		return setSelected(added == null ? hoverPoint : points.get(added));
	}

	public TopoEditorModel getTopoModel() {
		return topoModel;
	}

	public ITopoMap getTopology() {
		return topoModel.getTopoMap();
	}

	public Selectable getSelectableForLabelsDisplay() {
		return displayLabelsSelectable;
	}
	
	public TopoEditorSettings getSettings() {
		return settings;
	}
	
	public void setSettings(TopoEditorSettings settings) {
		this.settings = settings;
	}
	
	protected boolean canAddNode(boolean single) {
		int topoType = editorBehavior.getSupportedGeometryTypes();
		
		//if only points allowed
		if (TYPE_POINT == topoType || TYPE_MULTI_POINT == topoType) {
			if (TYPE_POINT == topoType) {
				return topoModel.isEmpty();
			}
			return true;
		}
		
		//if adding midpoint or end point
		if (!single || (TYPE_COLLECTION & topoType) > 0) {
			return true;
		}
		
		if ((TYPE_POLYGON & topoType) > 0) {
			//TODO: check if adding an island 
			//find a ring from nearest node and check if point location is inside of that ring
			return true;
		}
		
		if ((TYPE_LINE & topoType) > 0) {
			//if adding first line
			return edges.size() == 0;
		}
		
		return false;
	}
	
	protected boolean canAddEdge(Node startNode) {
		if (startNode == null) {
			return false;
		}
		if (startNode.isLocked() && !startNode.isEnabledEdgeChangesWhenLocked()) {
			return false;
		}
		int topoType = editorBehavior.getSupportedGeometryTypes();
		
		//if only points allowed
		if (TYPE_POINT == topoType || TYPE_MULTI_POINT == topoType) {
			return false;
		}
		
		return true;
	}

	public Node addPoint(HasCoordinate pos, boolean ctrl) {
		if (!canAddNode(false)) {
			return null;
		}
		
		Node startNode = null;
		if (selectedPoint != null && (selectedPoint.worldPos instanceof Node)) {
			startNode = (Node)selectedPoint.worldPos;
			
		} else if (hoverPoint != null && (hoverPoint.worldPos instanceof Node)) { // we're just selecting an existing node
			return null;
		}

		try {
			if (hoverPoint == null && !canAddEdge(startNode)) { // adding a single node without creating an edge 
				if (!canAddNode(true)) {
					return null;
				}
				return internalCreateNode(pos);
			}
			// probably create an edge too 
			if (hoverPoint == null) { // by adding a new node
				if (shouldAutoAddEdge(startNode, pos, ctrl)) {
					return internalCreateEdgeAndNode(startNode, pos);
				} else if (canAddNode(true)) {
					return internalCreateNode(pos);
				}
			} else if ((editorBehavior.getSupportedGeometryTypes() & TYPE_POLYGON) > 0) { // between two existing nodes if polygons allowed			
				HasCoordinate endNode = hoverPoint.worldPos;
				if (startNode == endNode) {
					return null;
				}
				if (startNode == null && hoverPoint.worldPos instanceof MidEdge
					&& editorBehavior.splitEdgeOnClick()) 
				{
					return internalCreateNodeOnEdge(hoverPoint, pos);
					
				} else if (shouldAutoAddEdge(startNode, endNode, ctrl)) {
					if (endNode instanceof Node) {
						return internalCreateEdgeAndNode(startNode, endNode);
					} else if (endNode instanceof MidEdge) {
						return internalCreateEdgeToEdge(startNode, hoverPoint, pos);
					}
				}
			}
			return null;

		} catch(TopologyException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Node internalCreateNodeOnEdge(Marker midEdge, HasCoordinate clickedPoint) {
		Edge e = ((MidEdge)midEdge.worldPos).e;
		if (e.isLocked() && !e.isAllowedAttachNodeWhenLocked()) {
			return null;
		}
		Marker newMid = promoteMidPointToNode(midEdge);
		topoModel.setNodeLocation((Node)newMid.worldPos, clickedPoint);
		setSelected(newMid);
		return (Node)newMid.worldPos;
	}
	
	public Node internalCreateEdgeToEdge(Node startNode, Marker midEdge, HasCoordinate clickedPoint) {
		Edge e = ((MidEdge)midEdge.worldPos).e;
		if (e.isLocked() && !e.isAllowedAttachNodeWhenLocked()) {
			return null;
		}
		Marker newMid = promoteMidPointToNode(midEdge);
		Node newNode = internalCreateEdgeAndNode(startNode, newMid.worldPos);
		topoModel.setNodeLocation((Node)newMid.worldPos, clickedPoint);
		setSelected(points.get(newNode));
		return newNode;
	}

	public Node internalCreateNode(HasCoordinate pos) {
		Node addedNode = topoModel.createNode(pos);
		fireTopologyModified();
		snapNode(addedNode);
		return addedNode;
	}

	public Node internalCreateEdgeAndNode(Node startNode, HasCoordinate pos) {
		final Node ret;
		if (pos instanceof Node) {
			ret = (Node)pos;
			topoModel.createEdge(startNode, ret);
		} else {
			Edge addedEdge = topoModel.createNodeAndEdge(startNode, pos);
			ret = addedEdge.getOtherNode(startNode);
			snapNode(ret);
		}
		fireTopologyModified();
		return ret;
	}

	private boolean shouldAutoAddEdge(Node startNode, HasCoordinate endPos, boolean ctrl) {
		if (shouldAutoAddEdgeToEndpoint(startNode) && shouldAutoAddEdgeToEndpoint(endPos)) {
			return !ctrl;
		}
		return ctrl;
	}

	private boolean shouldAutoAddEdgeToEndpoint(HasCoordinate startNode) {
		if (startNode instanceof Node) {
			int cntUnlocked = topoModel.unlockedEdgeCount((Node)startNode);
			int cntAll = topoModel.edgeCount((Node)startNode);
			return (cntUnlocked == 0 || cntAll < 2);
		}
		if (startNode instanceof MidEdge) {
			Edge e = ((MidEdge)startNode).e;
			return e.isLocked() && e.isAllowedAttachNodeWhenLocked();
		}
		return true;
	}

	public void mergeNodes() {
		Marker activePoint = selectedPoint;
		if (!setSelected(hoverPoint)) {
			setSelected(null);
		}
		try {
			Node mergedNode = topoModel.mergeNodes((Node)activePoint.worldPos, (Node)hoverPoint.worldPos);
			setSelected(points.get(mergedNode));
		} catch(TopologyException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void mergeNodeToEdge() {
		assert selectedPoint.worldPos instanceof Node;
		assert hoverPoint.worldPos instanceof MidEdge;
		
		Marker activePoint = selectedPoint;
		setSelected(null);
		Position2D selPos = new Position2D(activePoint);
		Marker newMarker = promoteMidPointToNode(hoverPoint);
		Node merged = topoModel.mergeNodes((Node)activePoint.worldPos, (Node)newMarker.worldPos);
		setSelected(points.get(merged));
		moveSelected(selPos);		
	}

	public void moveSelected(int dx, int dy, boolean large) {
		if (selectedPoint != null && selectedPoint.worldPos instanceof Node) {
			Position2D pos = new Position2D(selectedPoint);
			pos.x += dca.worldFromPix.length(large ? 5 * dx : dx);
			pos.y += dca.worldFromPix.length(large ? 5 * dy : dy);
			moveSelected(pos);
		}
	}

	public void deleteSelected() {
		if (selectedPoint == null) {
			return;
		}
		setHover(null);
		Node toSelect = null;
		boolean deleted = false;
		if (selectedPoint.worldPos instanceof Node) {
			try {
				Node toDelete = (Node)selectedPoint.worldPos;
				if (toDelete.isLocked()) {
					return;
				}
				setSelected(null);
				toSelect = topoModel.getNextNode(toDelete, null, true);
				deleted = topoModel.deleteNode(toDelete);
			} catch(TopologyException e) {
				throw new RuntimeException(e);
			}
		} else if (selectedPoint.worldPos instanceof MidEdge) {
			try {
				Edge e = ((MidEdge)selectedPoint.worldPos).e;
				if (e.isLocked()) {
					return;
				}
				setSelected(null);
				toSelect = e.getStartNode();
				deleted = topoModel.deleteEdge(e);
			} catch(TopologyException e) {
				throw new RuntimeException(e);
			}
		}
		if (deleted) {
			setSelected(points.get(toSelect));
			fireTopologyModified();
		}
	}
	
	public void dragStartMidEdge(Marker midPointMarker, Position2D startPoint) {
		Edge e = ((MidEdge)midPointMarker.worldPos).e;
		if (e.isLocked()) {
			if (!e.isAllowedAttachNodeWhenLocked()) {
				return;
			}
			Marker newMid = promoteMidPointToNode(midPointMarker);
			topoModel.setNodeLocation((Node)newMid.worldPos, startPoint);
			setSelected(newMid);
		} else {
			promoteMidPointToNode(midPointMarker);
		}
	}

	public Marker promoteMidPointToNode(Marker midPointMarker) {
		try {
			boolean sel = midPointMarker == selectedPoint;
			boolean hov = midPointMarker == hoverPoint;
			
			Node newNode = topoModel.splitEdge(((MidEdge)midPointMarker.worldPos).e);
			Marker newMrkr = points.get(newNode);

			if (hov) {
				setHover(newMrkr);
			}
			if (sel) {
				setSelected(newMrkr);
			}
			return newMrkr;
		} catch(TopologyException e2) {
			throw new RuntimeException(e2);
		}
	}
	
	public void dragStartLockedNode(Marker nodeMarker) {
		try {
			boolean sel = nodeMarker == selectedPoint;
			boolean hov = nodeMarker == hoverPoint;

			Node oldNode = (Node)nodeMarker.worldPos;
			Node newNode = topoModel.createNode(new Position2D(oldNode));
			newNode.setLocked(false);
			topoModel.createEdge(oldNode, newNode);
			Marker newMrkr = points.get(newNode);

			if (hov) {
				setHover(newMrkr);
			}
			if (sel) {
				setSelected(newMrkr);
			}
		} catch(TopologyException e2) {
			throw new RuntimeException(e2);
		}
	}

	public boolean setSelected(Marker toSelect) {
		if (!isMarkerNodeSelectable(toSelect)) {
			return false;
		}
		Marker oldSelected = selectedPoint;
		selectedPoint = null;
		
		if (oldSelected != null) {
			oldSelected.removeStyleName(STYLE_SELECTED_SUFFIX);
			ovr.updateMarkerForScale(oldSelected);
			ovr.updateNeighbours(oldSelected);
		}
		if (toSelect != null && !toSelect.isVisible()) {
			return false;
		}
		selectedPoint = toSelect;
		if (selectedPoint != null) {
			ovr.updateMarkerForScale(selectedPoint);
			ovr.updateNeighbours(selectedPoint);
			selectedPoint.addStyleName(STYLE_SELECTED_SUFFIX);
			return true;
		}
		return false;
	}

	private static boolean isMarkerNodeSelectable(Marker m) {
		if (m == null) {
			return true;
		}
		if (!(m.worldPos instanceof Node)) {
			return true;
		}
		Node n = (Node)m.worldPos;
		if (n.isLocked()) {
			return n.isEnabledEdgeChangesWhenLocked();
		}
		return true;
	}

	public void setTopology(Collection<? extends Edge> editedTopo) {
		setTopology(null, editedTopo);
	}

	public void setTopology(Collection<? extends Node> nodes, Collection<? extends Edge> edgeTopo) {
		try {
			this.topoModel.setTopology(nodes, edgeTopo);
			afterTopoLoad();
		} catch(TopologyException e) {
			throw new RuntimeException(e);
		}
	}

	public void setTopology(ITopoMap editedTopo) {
		setTopology(editedTopo.getNodes(), editedTopo.getEdges());
	}
	
	private void afterTopoLoad() {
		fireTopologyModified();
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		if (visible) {
			map.getOverlaysHolder().insertOverlay(ovr, MapComponent.Z_TOP_CONSTRUCTION, true);
		} else {
			map.getOverlaysHolder().removeOverlay(ovr);
		}
	}

	public void setHover(Marker hoverMarker) {
		Marker oldHover = hoverPoint;
		hoverPoint = null;

		if (oldHover != null) {
			oldHover.removeStyleName(STYLE_HOVER_SUFFIX);
			ovr.updateMarkerForScale(oldHover);
		}
		if (hoverMarker == null) {
			return;
		}
		ovr.updateMarkerForScale(hoverMarker);
		if (!hoverMarker.isVisible()) {
			return;
		}
		hoverPoint = hoverMarker;
		hoverPoint.addStyleName(STYLE_HOVER_SUFFIX);
	}

	public void setActive(boolean active) {
		activeAction.setSelected(active);
	}


	public void setLineStyle(VectorFilter filter) {
		EDGE_STYLE.setFilter(filter);
	}

	public void setLineWidth(GraphicMeasure width) {
		EDGE_STYLE.setStrokeWidth(width);
	}

	public boolean isActive() {
		return activeAction.isSelected();
	}

	public ToggleEditorActiveAction getToggleActiveAction() {
		return activeAction;
	}

	protected LineMarker createEdgeMarker(Edge e) {
		return new LineMarker(e, e.isLocked() ? LOCKED_EDGE_STYLE : EDGE_STYLE);
	}

	protected Marker createMidEdgeMarker(MidEdge e) {
		return new Marker(e.e.isLocked() ? MID_EDGE_SIGN_LOCKED : MID_EDGE_SIGN, e);
	}

	protected Marker createNodeMarker(Node n) {
		return new Marker(n.isLocked() ? NODE_SIGN_LOCKED : NODE_SIGN, n);
	}

	public void moveSelected(Position2D worldPos) {
		try {
			topoModel.setNodeLocation((Node)selectedPoint.worldPos, worldPos);
		} catch(TopologyException e) {
			throw new RuntimeException(e);
		}
	}

	public Marker getNearestMarker(HasCoordinate worldPos, double minDistSq, boolean excludeSelected) {
		Marker nd = getNearestNode(worldPos, minDistSq, excludeSelected);
		if (nd != null) {
			minDistSq = GeomUtil.distanceSq(nd, worldPos);
		}

		Marker mk = getMidPointOfNearestEdge(worldPos, minDistSq, excludeSelected);
		if (mk != null && (nd == null || GeomUtil.distanceSq(mk, worldPos) < minDistSq)) {
			return mk;
		}
		return nd;
	}

	Marker getNearestNode(HasCoordinate worldPos, double minDistSq, boolean excludeSelected) {
		Set<Node> excluded = (excludeSelected && selectedPoint != null && selectedPoint.worldPos instanceof Node)
			? Collections.singleton((Node)selectedPoint.worldPos)
			: Collections.<Node> emptySet();

		Node nd = topoModel.getNearestNode(worldPos, minDistSq, excluded);
		if (nd != null) {
			return points.get(nd);
		}
		return null;
	}

	private Marker getMidPointOfNearestEdge(HasCoordinate worldPos, double minDistSq, boolean excludeSelected) {
		Set<Edge> excluded = (excludeSelected && selectedPoint != null && selectedPoint.worldPos instanceof Node)
			? new HashSet<Edge>(topoModel.getTopoMap().getNodeStar((Node)selectedPoint.worldPos))
			: Collections.<Edge> emptySet();
			
		Edge e = topoModel.getNearestEdge(worldPos, minDistSq, excluded);
		return midPoints.get(e);
//		checkMidPointsIndex();
//		minPointsIndex.findNearest(worldPos, minDistSq, Collections.<Marker> emptySet());
	}

	public void checkMidPointsIndex() {
		if (midPointsIndex == null) {
			midPointsIndex = new PointQuadtree<Marker>();
			midPointsIndex.addAll(midPoints.values());
		}
	}

	public void updateMarker(Marker m) {
		ovr.updatePointLocation(m);
	}

	private void updateEdgeMarker(LineMarker lineMarker) {
		ovr.updateLineLocation(lineMarker);
		CoordinatePair lineLoc = lineMarker.getLocationData();
		LineLengthTextMarker tm = texts.get(lineLoc);
		if (tm != null) {
			updateTextMarker(tm);
		}
		Marker midM = midPoints.get(lineLoc);
		if (midM != null) {
			updateMarker(midM);
		}
	}


	private void updateTextMarker(LineLengthTextMarker textMarker) {
		updateTextMarker(textMarker, shouldShowEdgeText(textMarker.getEdge()));
	}

	private void updateTextMarker(AbstractTextMarker textMarker, boolean markerVisible) {
		textMarker.setVisible(markerVisible);
		ovr.updateTextLocation(textMarker);
	}

	@Override
	public void nodeLocationChanged(Node node) {
		Marker m = points.get(node);
		updateMarker(m);
		ovr.updateNodeForScale(m);
		NodeStar nodeStar = topoModel.getTopoMap().getNodeStar(node);
		for (Edge e : nodeStar) {
			edgeChanged(e);
		}
	}


	@Override
	public void edgeAdded(Edge e) {
		if (!shouldShowEdge(e)) {
			return;
		}
		LineMarker lm = createEdgeMarker(e);
		edges.put(e, lm);
		ovr.addLine(lm);

		if (shouldShowEdgeText(e)) {
			ensureTextMarker(e);
		}

		if (shouldShowMidEdge(e)) {
			Marker m = createMidEdgeMarker(new MidEdge(e));
			ovr.updateMidEdgeForScale(m);
			midPoints.put(e, m);
			ovr.addPoint(m);
			if (midPointsIndex != null) {
				midPointsIndex.add(m);
			}
		}
	}

	boolean shouldShowEdge(Edge e) {
		if (settings.shouldShowLockedEdges() || !e.isLocked()) {
			return true;
		}
		if (settings.shouldShowLockedAdjacents()) {
			if (!e.getStartNode().isLocked() || !e.getEndNode().isLocked()) {
				return true;
			}
		}
		return false;
	}

	boolean shouldShowEdgeText(Edge e) {
		return displayLabelsSelectable.isSelected() && shouldShowEdge(e);
	}

	boolean shouldShowMidEdge(Edge e) {
		return (!e.isLocked() || e.isAllowedAttachNodeWhenLocked()) && shouldShowEdge(e);
	}

	private void ensureTextMarker(Edge e) {
		LineLengthTextMarker atm = texts.get(e);
		if (atm == null) {
			atm = new LineLengthTextMarker(edges.get(e), settings.getTextStyle());
			atm.setVisible(false);
			texts.put(e, atm);
			ovr.addText(atm);
		}
		updateTextMarker(atm);
	}
	
	private void removeTextMarkers() {
		for (LineLengthTextMarker t : texts.values()) {
			ovr.removeShape(t);
		}
		texts = new HashMap<Edge, LineLengthTextMarker>();
	}

	public void setTextMarkerStyle(VectorFilter filter, String color) {
		settings.textStyle = new TextMarkerStyle(color, filter);
	}

	@Override
	public void edgeRemoved(Edge e) {
		
		LineMarker em = edges.remove(e);
		if (em != null) {
			ovr.removeShape(em);
		}
		
		LineLengthTextMarker tm = texts.remove(e);
		if (tm != null) {
			ovr.removeShape(tm);
		}
		
		Marker midPointMarker = midPoints.remove(e);
		if (midPointMarker != null) {
			ovr.removePoint(midPointMarker);
			if (midPointsIndex != null && midPointsIndex.contains(midPointMarker)) {
				midPointsIndex.remove(midPointMarker);
			}
		}
	}

	@Override
	public void nodeAdded(Node n) {
		if (!shouldShowNode(n)) {
			return;
		}
		Marker m = createNodeMarker(n);
		ovr.updateNodeForScale(m);
		ovr.addPoint(m);
		points.put(n, m);
	}

	boolean shouldShowNode(Node n) {
		if (settings.shouldShowLockedNodes() || !n.isLocked()) {
			return true;
		}
		if (settings.shouldShowLockedAdjacents()) {
			for (Edge e : topoModel.getTopoMap().getNodeStar(n)) {
				if (shouldShowEdge(e)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void nodeRemoved(Node n) {
		ovr.removePoint(points.remove(n));
	}

	@Override
	public void topoReset() {
		ovr.clear();
		points.clear();
		midPoints.clear();
		midPointsIndex = null;
		edges.clear();
		texts.clear();

		hoverPoint = null;
		selectedPoint = null;

		ovr.clear();
		topoModel.reset();

		fireTopologyModified();
	}

	@Override
	public void edgeChanged(Edge e) {
		LineMarker lineMarker = edges.get(e);
		if (lineMarker != null) {
			updateEdgeMarker(lineMarker);
			Marker midM = midPoints.get(e);
			if (midM != null) {
				ovr.updateMidEdgeForScale(midM);
			}
		}
		
		Marker startM = points.get(e.getStartNode());
		if (startM != null) {
			ovr.updateNodeForScale(startM);
			updateMarker(startM);
		}
		
		Marker endM = points.get(e.getEndNode());
		if (endM != null) {
			ovr.updateNodeForScale(endM);
			updateMarker(endM);
		}
	}

	@Override
	public int getTabIndex() {
		return map.getOverlaysHolder().getTabIndex();
	}

	@Override
	public void setTabIndex(int index) {
		map.getOverlaysHolder().setTabIndex(index);
	}

	@Override
	public void setAccessKey(char key) {
		map.getOverlaysHolder().setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		map.getOverlaysHolder().setFocus(focused);
	}

	public Envelope getEnvelope() {
		if (isEmpty()) {
			return new Envelope();
		}
		return topoModel.getEnvelope();
	}

	public boolean isEmpty() {
		return CollectionUtil.isNullOrEmpty(points);
	}

	public void snapNode(HasCoordinate point) {
		if (snapProvider != null) {
			snapProvider.snapPoint(point, snapCallbackImpl);
		}
	}

	public final SnapProviderCallback snapCallbackImpl = new SnapProviderCallbackAdapter() {
		@Override
		public void onPointSnapped(HasCoordinate point, HasCoordinate snapLocation) {
			if (point instanceof Node) {
				try {
					//check if node still on the map (could be deleted before callback returned)
					if (topoModel.getTopoMap().getNodes().contains(point)) {
						topoModel.setNodeLocation((Node)point, snapLocation);
						fireTopologyModified();
					}
				} catch(TopologyException e) {
					throw new RuntimeException(e);
				}
			}
		}
	};


	public void addMarkerMoveListener(MarkerMoveListener l) {
		if (markerMoveListeners == null) {
			markerMoveListeners = new ArrayList<MarkerMoveListener>();
		}
		markerMoveListeners.add(l);
	}

	public void removeMarkerMoveListener(MarkerMoveListener l) {
		if (markerMoveListeners == null) {
			return;
		}
		markerMoveListeners.remove(l);
	}

	protected void fireOnMarkerMoveStart(Marker marker) {
		if (markerMoveListeners != null) {
			for (MarkerMoveListener l : markerMoveListeners) {
				l.onMarkerMoveStart(marker);
			}
		}
	}

	protected void fireOnMarkerMoveEnd(Marker marker) {
		if (markerMoveListeners != null) {
			for (MarkerMoveListener l : markerMoveListeners) {
				l.onMarkerMoveEnd(marker);
			}
		}
	}


	public void addTopology(ITopoMap iTopoMap) {
		try {
			this.topoModel.addTopology(iTopoMap);
			afterTopoLoad();
		} catch(TopologyException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isVisible() {
		return visible;
	}

}