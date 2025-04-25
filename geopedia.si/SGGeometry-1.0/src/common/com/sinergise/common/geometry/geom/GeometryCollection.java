package com.sinergise.common.geometry.geom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.sinergise.common.geometry.util.GeometryVisitor;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;

public class GeometryCollection<T extends Geometry> extends GeometryImpl implements List<T> {
	private static final long serialVersionUID = 1L;
	/* final */ List<T> elements;

	public GeometryCollection() {
		elements = Collections.emptyList();
	}

	public GeometryCollection(T... elements) {
		if (ArrayUtil.isNullOrEmpty(elements)) {
			this.elements = Collections.emptyList();
			return;
		}
		this.elements = Arrays.asList(elements);
		CrsIdentifier crsId = elements[0].getCrsId();
		if (crsId!=null) {
			for (T element:elements) {
				if (!crsId.equals(element.getCrsId())) {
					throw new IllegalArgumentException("Can't mix elements with different CRSIdentifiers in the same collection!");
				}
			}
			setCrsId(crsId);
		}
	}
	
	public GeometryCollection(List<? extends T> elements) {
		this.elements = new ArrayList<T>(elements);
	}

	@Override
	public int size() {
		return elements == null ? 0 : elements.size();
	}

	@Override
	public T get(int index) {
		return elements.get(index);
	}

	@Override
	public double getArea() {
		double sum = 0;
		for (T el : elements) {
			sum += el.getArea();
		}
		return sum;
	}

	@Override
	public double getLength() {
		double sum = 0;
		for (T el : elements) {
			sum += el.getLength();
		}
		return sum;
	}

	@Override
	public Envelope getEnvelope() {
		EnvelopeBuilder builder = new EnvelopeBuilder(crsRef);
		builder.expandToIncludeEnvelopes(elements);
		return builder.getEnvelope();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getCollectionName());
		sb.append("(");
		if (size() > 0) {
			sb.append(get(0));
			for (int i = 1; i < size(); i++) {
				sb.append(", ").append(get(i));
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	@Override
	public void setCrsId(CrsIdentifier crsRef) {
		super.setCrsId(crsRef);
		for (T element:elements) {
			element.setCrsId(crsRef);
		}
	}
	
	String getCollectionName() {
		return "GEOMETRYCOLLECTION";
	}

	@Override
	public Iterator<T> iterator() {
		return elements.iterator();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}

	@Override
	public Object[] toArray() {
		return elements.toArray();
	}

	@Override
	public <E> E[] toArray(E[] a) {
		return elements.toArray(a);
	}

	@Override
	public boolean add(T o) {
		throw new UnsupportedOperationException("Modification not supported");
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Modification not supported");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException("Modification not supported");
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException("Modification not supported");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Modification not supported");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Modification not supported");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Modification not supported");
	}

	@Override
	public T set(int index, T element) {
		throw new UnsupportedOperationException("Modification not supported");
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException("Modification not supported");
	}

	@Override
	public T remove(int index) {
		throw new UnsupportedOperationException("Modification not supported");
	}

	@Override
	public int indexOf(Object o) {
		return elements.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return elements.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return elements.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return elements.listIterator(index);
	}

	@Override
	public GeometryCollection<T> subList(int fromIndex, int toIndex) {
		return new GeometryCollection<T>(elements.subList(fromIndex, toIndex));
	}

	@Override
	public int hashCode() {
		return elements.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeometryCollection<?> other = (GeometryCollection<?>)obj;
		return elements.equals(other.elements);
	}
	
	@Override
	public void accept(GeometryVisitor visitor) {
		visitor.visitCollection(this);
	}
	
	//Should not have @Override annotation as GWT does not have clone() implemented
	@SuppressWarnings("all")
	public GeometryCollection<T> clone() {
		GeometryCollection<T> col = new GeometryCollection<T>();
		cloneInto(col);
		return col;
	}
	
	@SuppressWarnings("unchecked")
	public void cloneInto(GeometryCollection<T> col) {
		col.crsRef = crsRef;
		
		if (elements != null) {
			ArrayList<T> list = new ArrayList<T>(elements.size());
			for (T el : elements) {
				list.add((T)el.clone());
			}
			col.elements = Collections.unmodifiableList(list);
		}
	}
	
	
}
