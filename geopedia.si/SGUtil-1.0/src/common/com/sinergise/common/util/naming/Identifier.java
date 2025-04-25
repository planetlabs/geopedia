package com.sinergise.common.util.naming;

import static com.sinergise.common.util.Util.safeEquals;
import static com.sinergise.common.util.Util.safeHashCode;
import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.sinergise.common.util.CheckUtil;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.lang.Function;
import com.sinergise.common.util.string.HasCanonicalStringRepresentation;

public class Identifier implements Serializable, HasCanonicalStringRepresentation {
	
	public static final Function<Identifier, String> FUNC_LOCAL_ID_GETTER = new Function<Identifier, String>() {
		@Override
		public String execute(Identifier param) {
			return param.getLocalID();
		}
	};
	
	private static final long serialVersionUID = 1L;
	public static final Identifier ROOT = new Identifier();
	
	
	protected Identifier parent;
	protected /*final*/ String localID;
	
	//don't serialize the hashcode as it will cause problems due to different handling of integer overflows in GWT
	protected transient int localHashCode = 0;
	
	/**
	 * @deprecated Serialization only
	 */
	@Deprecated
	public Identifier() {
		this.localID = null;
		this.parent = null;
	}
	
	public Identifier(final Identifier parent, final String localID) {
		CheckUtil.checkArgumentNotNull(parent, "parent");
		this.parent = parent;
		this.localID = localID;
	}
	
	public String getLocalID() {
		return localID;
	}
	
	public Identifier getParent() {
		return parent;
	}
	
	public boolean isBound() {
		return parent != null && !parent.isEmpty();
	}
	
	public void bindTo(final Identifier newParent) {
		CheckUtil.checkState(!isBound(), "This identifier is already bound");
		this.parent = newParent;
	}
	
	public boolean isEmpty() {
		return this.parent == null && this.localID == null;
	}
	
	public void unbind() {
		if (this.parent == null) {
			return;
		}
		this.parent = null;
	}
	
	@Override
	public String toString() {
		return toCanonicalString();
	}
	
	@Override
	public String toCanonicalString() {
		if (isEmpty()) {
			return "<root>";
		}
		if (parent == null || parent.isEmpty()) {
			return localID;
		}
		return parent.toString() + "." + localID;
	}
	
	@Override
	public int hashCode() {
		if (localHashCode == 0) {
			localHashCode = computeLocalHashCode();
			if (localHashCode >= 0) {
				localHashCode++;
			}
		}
		return safeHashCode(parent) + localHashCode;
	}

	protected int computeLocalHashCode() {
		return 31*(31 + safeHashCode(localID));
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Identifier other = (Identifier)obj;
		if (localHashCode != 0 && other.localHashCode != 0 && localHashCode != other.localHashCode) {
			return false;
		}
		if (!safeEquals(localID, other.localID)) {
			return false;
		}
		if (!safeEquals(parent, other.parent)) {
			return false;
		}
		return true;
	}

	public static List<String> extractLocalIds(Identifier ... ids) {
		return CollectionUtil.mapToList(ids, FUNC_LOCAL_ID_GETTER);
	}

	public static List<String> extractLocalIds(Collection<? extends Identifier> ids) {
		return CollectionUtil.mapToList(ids, FUNC_LOCAL_ID_GETTER);
	}

	public static List<String> extractLocalIdsFromIdentifiable(Collection<? extends Identifiable> ids) {
		return CollectionUtil.mapToList(ids, Identifiable.FUNC_LOCAL_ID_GETTER);
	}

	public static List<Identifier> extractIdentifiers(Collection<? extends Identifiable> items) {
		return CollectionUtil.mapToList(items, Identifiable.FUNC_QUALIFIED_ID_GETTER);
	}
	
	public static List<Identifier> extractIdentifiers(Identifiable ...items) {
		return CollectionUtil.mapToList(items, Identifiable.FUNC_QUALIFIED_ID_GETTER);
	}

	public static Identifier[] extractIdentifiersArray(Collection<? extends Identifiable> items) {
		return CollectionUtil.map(items, new Identifier[items.size()], Identifiable.FUNC_QUALIFIED_ID_GETTER);
	}
	
	public static final Integer localIdAsInteger(Identifier id) {
		if (id == null) return null;
		String locID = id.getLocalID();
		return isNullOrEmpty(locID) ? null : Integer.valueOf(locID);
	}

	public static final Long localIdAsLong(Identifier id) {
		if (id == null) return null;
		String locID = id.getLocalID();
		return isNullOrEmpty(locID) ? null : Long.valueOf(locID);
	}

	public static Identifier chain(Identifier start, String ... components) {
		Identifier ret = start;
		for (String comp : components) {
			ret = new Identifier(ret, comp);
		}
		return ret;
	}
	public static Identifier[] buildQualifiedChildren(Identifier parent, String ... ids) {
		Identifier[] ret = new Identifier[ids.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = new Identifier(parent, ids[i]);
		}
		return ret;
	}

	public boolean isDescendantOf(Identifiable ancestor) {
		return isDescendantOf(ancestor.getQualifiedID());
	}
	/**
	 * @param entityOrTypeId
	 * @return true iff this identifier is the same as, or is a descendant of the parameter
	 */
	public boolean isDescendantOf(Identifier ancestor) {
		if (equals(ancestor)) {
			return true;
		}
		if (parent == null) {
			return false;
		}
		return parent.isDescendantOf(ancestor);
	}
}
