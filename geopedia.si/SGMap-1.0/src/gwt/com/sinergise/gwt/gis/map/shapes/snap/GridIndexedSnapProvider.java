package com.sinergise.gwt.gis.map.shapes.snap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.gis.map.shapes.snap.SnapProvider;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.event.selection.Selectable;
import com.sinergise.common.util.event.selection.SelectableImpl;
import com.sinergise.common.util.geom.CoordinatePair;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.gwt.gis.map.ui.MapComponent;

public class GridIndexedSnapProvider implements SnapProvider {
	
	private static final int DEFAULT_GRID_INDEX_CELL_SIZE = 100;
	
	private final int DEFAULT_POINT_SNAP_PX_TOLERANCE = 12;
	private final int DEFAULT_EDGE_SNAP_PX_TOLERANCE = 8;
	
	private final Selectable enabled;
	private final CellDataProvider dataProvider;
	private final DisplayCoordinateAdapter dca;
	private final double cellSize;
	
	private final Envelope worldBounds;
	private final long gridWidth;
	
	private int pointSnapTolerancePx = DEFAULT_POINT_SNAP_PX_TOLERANCE;
	private int edgeSnapTolerancePx = DEFAULT_EDGE_SNAP_PX_TOLERANCE;
	
	private Map<Long, GridIndexCell> cachedCells = new HashMap<Long, GridIndexCell>();
	
	public GridIndexedSnapProvider(MapComponent map) {
		this (new FeatureLayersCellDataProvider(map.getLayers()), 
			  map.getCoordinateAdapter(), DEFAULT_GRID_INDEX_CELL_SIZE);
	}
	
	public GridIndexedSnapProvider(CellDataProvider dataProvider, DisplayCoordinateAdapter dca, double cellSize) {
		this(dataProvider, dca, cellSize, new SelectableImpl(false));
	}
	
	public GridIndexedSnapProvider(CellDataProvider dataProvider, DisplayCoordinateAdapter dca, double cellSize, Selectable enabled) {
		this.dataProvider = dataProvider;
		this.dca = dca;
		this.cellSize = cellSize;
		this.enabled = enabled;
		
		worldBounds = dca.bounds.mbr;
		gridWidth = (long) Math.ceil(worldBounds.getWidth()/cellSize);
	}

	@Override
	public void snapPoint(HasCoordinate point, SnapProviderCallback callback) {
		if (enabled.isSelected()) {
			snapPoint(getCellIndicesAround(point), point, callback);
		}
	}
	
	@Override
	public Selectable getEnabled() {
		return enabled;
	}
	
	public void setPointSnapTolerance(int pixels) {
		this.pointSnapTolerancePx = pixels;
	}
	
	public void setEdgeSnapTolerance(int pixels) {
		this.edgeSnapTolerancePx = pixels;
	}
	
	private void snapPoint(long[] gridCellIdxs, final HasCoordinate point, final SnapProviderCallback callback) {
		
		List<GridIndexCell> toFetch = new ArrayList<GridIndexCell>();
		final List<GridIndexCell> cells = new ArrayList<GridIndexCell>(gridCellIdxs.length);
		
		for (long cellIdx : gridCellIdxs) {
			GridIndexCell cell = getCellForIdx(cellIdx);
			if (cell == null) {
				continue;
			}
			
			if (dataProvider.isCellDataValid(cell)) {
				cells.add(cell);
			} else {
				cell.clearData();
				toFetch.add(cell);
			}
		}
		
		dataProvider.fetchCellData(toFetch, new CellDataFetchedCallback() {
			@Override
			public void onCellDataFetched(Collection<GridIndexCell> fetchedCells) {
				for (GridIndexCell cell : fetchedCells) {
					cells.add(cell);
				}
				snapPoint(cells, point, callback);
			}
		});
	}
	
	private void snapPoint(List<GridIndexCell> gridCells, HasCoordinate point, SnapProviderCallback callback) {

		HasCoordinate c = getNearestPoint(point, gridCells);
		if (c != null && GeomUtil.distance(point, c) < dca.worldFromPix.length(pointSnapTolerancePx)) {
			callback.onPointSnapped(point, c);
			return;
		}
		
		c = getNearestPointOnEdge(point, gridCells);
		if (c != null && GeomUtil.distance(point, c) < dca.worldFromPix.length(edgeSnapTolerancePx)) {
			callback.onPointSnapped(point, c);
			return;
		}
		callback.onPointNotSnapped(point);
	}
	
	private static HasCoordinate getNearestPoint(HasCoordinate point, List<GridIndexCell> gridCells) {
		//TODO: use spatial index
		
		double minDistSq = Double.MAX_VALUE;
		HasCoordinate nearest = null;
		for (GridIndexCell cell : gridCells) {
			for (HasCoordinate n : cell.nodes.values()) {
				double distSq = GeomUtil.distanceSq(point, n);
				if (distSq < minDistSq) {
					minDistSq = distSq;
					nearest = n;
				}
			}
		}
		
		return nearest;
	}
	
	private static HasCoordinate getNearestPointOnEdge(HasCoordinate point, List<GridIndexCell> gridCells) {
		//TODO: use spatial index
		
		double minDistSq = Double.MAX_VALUE;
		CoordinatePair nearest = null;
		for (GridIndexCell cell : gridCells) {
			for (CoordinatePair e : cell.edges.values()) {
				double distSq = GeomUtil.distancePointLineSegmentSq(point.x(), point.y(), e.x1(), e.y1(), e.x2(), e.y2());
				if (distSq < minDistSq) {
					minDistSq = distSq;
					nearest = e;
				}
			}
		}
		
		if (nearest != null) {
			Point out = new Point(point);
			GeomUtil.pointLineStringNearest(nearest.x1(), nearest.y1(), nearest.x2(), nearest.y2(), point.x(), point.y(), out);
			return out;
		}
		
		return null;
	}
	
	private long[] getCellIndicesAround(HasCoordinate c) {
		double buffer = getMaxToleranceInMetres();
		
		Set<Long> indices = new HashSet<Long>(5);
		indices.add(Long.valueOf(getCellIndexAt(c)));
		indices.add(Long.valueOf(getCellIndexAt(new Position2D(c.x()+buffer, c.y()))));
		indices.add(Long.valueOf(getCellIndexAt(new Position2D(c.x()-buffer, c.y()))));
		indices.add(Long.valueOf(getCellIndexAt(new Position2D(c.x(), c.y()+buffer))));
		indices.add(Long.valueOf(getCellIndexAt(new Position2D(c.x(), c.y()-buffer))));
		
		return ArrayUtil.toLongArray(indices);
	}
	
	private long getCellIndexAt(HasCoordinate c) {
		long left = (long) Math.ceil((c.x()-worldBounds.getMinX())/cellSize);
		long top  = (long) Math.floor((c.y()-worldBounds.getMinY())/cellSize);
		return top*gridWidth + left;
	}
	
	private Envelope getEnvelopeForCellIdx(long cellIdx) {
		long top = (long) Math.floor(cellIdx/gridWidth);
		long left = cellIdx - top*gridWidth -1;
		
		return new Envelope(
			worldBounds.getMinX() + cellSize*left,
			worldBounds.getMinY() + cellSize*top,
			worldBounds.getMinX() + cellSize*(left+1),
			worldBounds.getMinY() + cellSize*(top+1));
	}
	
	private double getMaxToleranceInMetres() {
		return dca.worldFromPix.length(Math.max(pointSnapTolerancePx, edgeSnapTolerancePx));
	}
	
	private GridIndexCell getCellForIdx(long cellIdx) {
		GridIndexCell cell = cachedCells.get(Long.valueOf(cellIdx));
		if (cell == null) {
			cachedCells.put(Long.valueOf(cellIdx), cell = new GridIndexCell(cellIdx, getEnvelopeForCellIdx(cellIdx)));
		}
		return cell;
	}

	private static String nodeKey(double x, double y) {
		return x+"-"+y;
	}
	
	private static String edgeKey(double x1, double y1, double x2, double y2) {
		return Math.min(x1, x2)+"-"+Math.min(y1, y2)+","+Math.max(x1, x2)+"-"+Math.max(y1, y2);
	}
	
	
	
	public static class GridIndexCell {
		final long index;
		final Envelope env;
		
		Map<String, HasCoordinate> nodes = new HashMap<String, HasCoordinate>();
		Map<String, CoordinatePair> edges = new HashMap<String, CoordinatePair>();
		long dataHash = 0;
		
		
		private GridIndexCell(long index, Envelope env) {
			this.index = index;
			this.env = env;
		}
		
		public Envelope getEnvelope() {
			return env;
		}
		
		public long getDataHash() {
			return dataHash;
		}
		
		private void addNode(HasCoordinate n) {
			if (env.contains(n.x(), n.y())) {
				nodes.put(nodeKey(n.x(), n.y()), n);
			}
		}
		
		private void addEdge(CoordinatePair e) {
			if (env.intersects(Envelope.create(e.c1(), e.c2()))) {
				edges.put(edgeKey(e.x1(), e.y1(), e.x2(), e.y2()), e);
			}
		}
		
		public void clearData() {
			nodes.clear();
			edges.clear();
		}
		
		public void setData(Collection<HasCoordinate> nodes, Collection<CoordinatePair> edges, long dataHash) {
			this.dataHash = dataHash;
			for (HasCoordinate n : nodes) {
				addNode(n);
			}
			for (CoordinatePair e : edges) {
				addEdge(e);
			}
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (index ^ (index >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GridIndexCell other = (GridIndexCell) obj;
			if (index != other.index)
				return false;
			return true;
		}
		
	}
	
	public static interface CellDataProvider {
		void fetchCellData(Collection<GridIndexCell> cells, CellDataFetchedCallback callback);
		boolean isCellDataValid(GridIndexCell cell);
	}
	
	public static interface CellDataFetchedCallback {
		void onCellDataFetched(Collection<GridIndexCell> cells);
	}
}
