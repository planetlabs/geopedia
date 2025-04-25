/**
 * 
 */
package com.sinergise.common.geometry.topo;

import com.sinergise.common.util.lang.DeepCopyable;
import com.sinergise.common.util.naming.EntityIdentifier;
import com.sinergise.common.util.naming.IdentifiableEntityImpl;

/**
 * 
 * <b>!!IMPORTANT!!</b> 
 * <p>All TopoElement subclasses should override deepCopy and 
 * newInstance methods to enable correct deep copying of elements.
 * See deepCopy and newInstance methods for additional information.</p>
 * 
 * @author tcerovski
 */
public abstract class TopoElement extends IdentifiableEntityImpl implements DeepCopyable<TopoElement> {
	private static final long serialVersionUID = 1L;
	protected int srid = 1;
	protected boolean locked = false;
	protected boolean dirty = false;
	
	/**
	 * To be used for cloning and serialization only; setId should be called immediately after construction
	 */
	protected TopoElement() {
		super();
	}
	
	protected TopoElement(EntityIdentifier id) {
		super(id);
	}
	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public void setSRID(int srid) {
		this.srid = srid;
	}
	
	public int getSRID() {
		return srid;
    }
	
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public abstract String getName();
	
	/**
	 * By extending this method, subclasses can check for more than just valid id.
	 */
	public boolean exists() {
		return hasPermanentId();
	}
	
//	@Override //Don't override - GWT doesn't have clone() on Object
	@SuppressWarnings("all")
	public TopoElement clone() {
		return deepCopy();
	}
	
	/**
	 * Makes a deep copy of this topology element. 
	 * All subclasses should override this method to make an appropriate copy by
	 * calling super.deepCopy() and copying any additional elements.
	 * 
	 * @return A deep copy of this topology element.
	 */
	@Override
	public TopoElement deepCopy() {
		TopoElement copy = newInstance();
		copy.setId(getQualifiedID());
		copy.locked = locked;
		copy.srid = srid;
		copy.dirty = dirty;
		return copy;
	}
	
	/**
	 * Creates new empty instance of this element using an empty constructor.
	 * It should be used only by the deepCopy method to provide correct instance class.
	 * All subclasses should override this method to return an instance of its class. 
	 * @return New empty instance of this class.
	 */
	protected abstract TopoElement newInstance();
}
