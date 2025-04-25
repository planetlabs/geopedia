package com.sinergise.common.geometry.io.wkt;

import static com.sinergise.common.geometry.io.OgcShapeType.GEOMETRYCOLLECTION;
import static com.sinergise.common.geometry.io.OgcShapeType.LINESTRING;
import static com.sinergise.common.geometry.io.OgcShapeType.MULTILINESTRING;
import static com.sinergise.common.geometry.io.OgcShapeType.MULTIPOINT;
import static com.sinergise.common.geometry.io.OgcShapeType.MULTIPOLYGON;
import static com.sinergise.common.geometry.io.OgcShapeType.POINT;
import static com.sinergise.common.geometry.io.OgcShapeType.POLYGON;
import static com.sinergise.common.geometry.io.wkt.WKTConstants.COORD_SEPARATOR;
import static com.sinergise.common.geometry.io.wkt.WKTConstants.LIST_END;
import static com.sinergise.common.geometry.io.wkt.WKTConstants.LIST_SEPARATOR;
import static com.sinergise.common.geometry.io.wkt.WKTConstants.LIST_START;
import static com.sinergise.common.geometry.io.wkt.WKTConstants.WKT_EMPTY;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.io.OgcShapeType;
import com.sinergise.common.geometry.util.GeometryVisitor;
import com.sinergise.common.util.io.ObjectWriter;
import com.sinergise.common.util.string.ListStringBuilder;

/**
 * Advanced implementation with the use of some object-oriented principles:
 * <ul>
 * 	<li>fields to store intermediate state (buffer, auto type to determine whether to write tag or not)</li>
 * 	<li>methods modify internal state of the object ('void append(..)' instead of 'String write(..)')</li>
 * 	<li>visitor pattern separates the different Geometry subclasses</li>
 * 	<li>separation of concerns - the logic involing comma and parentheses in lists was separated into ListStringBuilder class</li>
 * </ul>
 * 
 * <p>The <code>curAutoType</code> is introduced to handle multi* collections where members' tag text is not printed out.
 * This was chosen instead of simple boolean writeTag to enable extension to e.g. MultiCurve where some members are printed
 * with tags and some without.</p>  
 * 
 * @author mkadunc
 */
public class WKTWriter implements ObjectWriter<Geometry>, GeometryVisitor {
	private final ListStringBuilder buffer;
	private OgcShapeType curAutoType;
	
	public WKTWriter() {
		this(new StringBuilder());
	}
	
	public WKTWriter(Appendable out) {
		this.buffer = new ListStringBuilder(out, LIST_START, LIST_SEPARATOR, LIST_END);
	}
	
	@Override
	public void append(Geometry o) {
		if (o == null) {
			return;
		}
		curAutoType = null;
		processGeometry(o);
	}

	private void processGeometry(Geometry g) {
		g.accept(this);
	}

	@Override
	public void visitPoint(Point point) {
		if (startGeometry(POINT, point)) {
			appendCoord(point.x(), point.y());
			endGeometry();
		}
	}

	@Override
	public void visitLineString(LineString line) {
		if (startGeometry(LINESTRING, line)) {
			for (int i = 0; i < line.getNumCoords(); i++) {
				buffer.beforeListItem();
				appendCoord(line.getX(i), line.getY(i));
			}
			endGeometry();
		}
	}

	@Override
	public void visitPolygon(Polygon poly) {
		if (startGeometry(POLYGON, poly)) {
			curAutoType = LINESTRING;
			visitLineString(poly.getOuter());
			for (int i = 0; i < poly.getNumHoles(); i++) {
				buffer.appendChar(LIST_SEPARATOR);
				visitLineString(poly.getHole(i));
			}
			endGeometry();
		}
	}

	@Override
	public void visitMultiPoint(MultiPoint mp) {
		appendCollection(MULTIPOINT, mp);
	}

	@Override
	public void visitMultiLineString(MultiLineString mls) {
		appendCollection(MULTILINESTRING, mls);
	}

	@Override
	public void visitMultiPolygon(MultiPolygon mp) {
		appendCollection(MULTIPOLYGON, mp);
	}

	@Override
	public void visitCollection(GeometryCollection<?> collection) {
		appendCollection(GEOMETRYCOLLECTION, collection);
	}

	/**
	 * Appends start of geometry as the next item of the current list into the buffer
	 * and starts a new list for the geometry's contents if the geometry is not empty.
	 * <p>In other words, this method appends:
	 * <ol>
	 * <li>list separator (",") iff this is not the first element in the list</li>
	 * <li>the geometry type tag (if the current auto type differs from the supplied tag)</li>
	 * <li>either "EMPTY" if the geometry is empty or</li>
	 * <li>"(" if it isn't.</li>
	 * </ol>
	 * 
	 * @param tag
	 * @param g
	 * @return true iff the geometry is not empty
	 */
	private boolean startGeometry(OgcShapeType tag, Geometry g) {
		if (tag != curAutoType) {
			buffer.appendString(tag.getWktTag());
		}
		if (g.isEmpty()) {
			buffer.appendChar(' ').appendString(WKT_EMPTY);
			return false;
		}
		buffer.startList(); // add initial parenthesis
		return true;
	}

	private void appendCoord(double x, double y) {
		buffer.appendString(formatNumber(x)).appendChar(COORD_SEPARATOR).appendString(formatNumber(y));
	}

	private void endGeometry() {
		buffer.endList();
	}

	private void appendCollection(OgcShapeType tag, GeometryCollection<?> collection) {
		if (startGeometry(tag, collection)) {
			for (Geometry g : collection) {
				buffer.beforeListItem(); // add comma if this is not the first child
				curAutoType = tag.getAutomaticMemberType();
				processGeometry(g);
			}
			endGeometry();
		}
	}

	/**
	 * Assumes that the internal output is StringBuilder. It is reset and used to output the geometry.
	 * The output's toString method is used to get at the result.
	 * @param o
	 * @return
	 */
	public String writeToString(Geometry o) {
		((StringBuilder)buffer.getOutput()).setLength(0);
		append(o);
		return buffer.toString();
	}
	
	@Override
	/**
	 * Delegates to the output object's toString(). Typically (e.g. in case output is StringBuilder) this will produce
	 * the text written to the output thus far. 
	 */
	public String toString() {
		return buffer.toString();
	}

	/**
	 * NOTE: This does not close the underlying output appendable.
	 */
	@Override
	public void close() {
		//nothing to close; output should be handled by the one that constructed this object
	}

	private static String formatNumber(double x) {
		String ret = Double.toString(x);
		if (ret.endsWith(".0")) {
			return ret.substring(0, ret.length() - 2);
		}
		return ret;
	}

	public static String write(Geometry geom) {
		return new WKTWriter().writeToString(geom);
	}
	
	/**
	 * @deprecated use {@link #write(Geometry)}
	 */
	@Deprecated
	public static String writeSilent(Geometry geom) {
		return write(geom);
	}
}
