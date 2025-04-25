/**
 * 
 */
package com.sinergise.common.geometry.topo;

import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.HasCoordinateMutable;
import com.sinergise.common.util.naming.EntityIdentifier;
import com.sinergise.common.util.naming.Identifier;

/**
 * Simple topology face. This is mutable only if the centroid is mutable.
 * 
 * @see TopoElement
 *
 * @author tcerovski
 */
public class Face extends TopoElement implements HasCoordinate {
	private static final Identifier TEMP_FACE_PREFIX = new Identifier(Identifier.ROOT, "tempFace");
	
	public static final EntityIdentifier OUTER_FACE_ID = new EntityIdentifier(TEMP_FACE_PREFIX, "0");
	public static final Face OUTER_FACE = new Face(OUTER_FACE_ID, null);

	private static final long serialVersionUID = 1L;
	
	private HasCoordinate centroid;
	
	protected Face() {
		this(null);
	}
	
	public Face(HasCoordinate centroid) {
		this(null, centroid);
	}
	
	public Face(EntityIdentifier id, HasCoordinate centroid) {
		super(id == null ? new EntityIdentifier(TEMP_FACE_PREFIX) : id);
		this.centroid = centroid;
	}
	
	public boolean isStub() {
		return centroid == null;
	}
	
	@Override
	public String getName() {
		if(hasPermanentId()) {
			return "Face "+getLocalID();
		}
		return "New face "+getLocalID();
	}
	
	public HasCoordinate getCentroid() {
		return centroid;
	}
	
	public void setCentroid(HasCoordinate p) {
		this.centroid = p;
	}
	
	public double cenX() {
		if (centroid == null) return Double.NaN;
		return centroid.x();
	}
	
	public double cenY() {
		if (centroid == null) return Double.NaN;
		return centroid.y();
	}
	
	@Override
	public double x() {
		return cenX();
	}
	
	@Override
	public double y() {
		return cenY();
	}
	
	@Override
	public String toString() {
		return "Face [centroid=" + centroid + ", id=" + getQualifiedID() + "]";
	}

	
	
	@Override
	public Face deepCopy() {
		Face copy = (Face)super.deepCopy();
		copy.centroid = (centroid instanceof HasCoordinateMutable) ? ((HasCoordinateMutable)centroid).clone() : null;
		return copy;
	}
	
	@Override
	/**
	 * Override because new faces have temp_face_prefix as parents; other topo elements have null id
	 */
	protected void setId(EntityIdentifier id) {
		assert getQualifiedID().getParent().equals(TEMP_FACE_PREFIX);
		assert id != null;
		this.id = id;
	}
	
	@Override
	protected TopoElement newInstance() {
		return new Face();
	}
	
}
