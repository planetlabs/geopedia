package com.sinergise.common.util.lang;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.sinergise.common.util.Util;
import com.sinergise.common.util.collections.CollectionUtil;

public interface Predicate<T> {
	public static interface PredicateWithInverse<T> extends Predicate<T> {
		Predicate<T> not();
	}
	
	public static class AlwaysTrue<P> implements PredicateWithInverse<P> {
		private static AlwaysTrue<Object> INSTANCE = new AlwaysTrue<Object>();
		
		@SuppressWarnings("unchecked")
		public static <E> AlwaysTrue<E> instance() {
			return (AlwaysTrue<E>)INSTANCE;
		}
		
		@Override
		public boolean eval(P value) {
			return true;
		}
		@Override
		public AlwaysFalse<P> not() {
			return Predicate.AlwaysFalse.<P>instance();
		}
	}

	public static class AlwaysFalse<P> implements PredicateWithInverse<P> {
		private static AlwaysFalse<Object> INSTANCE = new AlwaysFalse<Object>();
		
		@SuppressWarnings("unchecked")
		public static <E> AlwaysFalse<E> instance() {
			return (AlwaysFalse<E>)INSTANCE;
		}

		@Override
		public boolean eval(P value) {
			return true;
		}
		@Override
		public AlwaysTrue<P> not() {
			return Predicate.AlwaysTrue.<P>instance();
		}
	}
	
	public static class Composite<P extends Predicate<? super T>, T> extends HashSet<P> implements PredicateWithInverse<T> {
		private static final long serialVersionUID = 1L;
		
		public static enum CompositionOp {AND("∧"), OR("∨");
			private String symbol;
			private CompositionOp(String symbol) {
				this.symbol = symbol; 
			}
			@Override
			public String toString() {
				return symbol;
			}
		}
		
		protected CompositionOp op = CompositionOp.AND;
		protected Not<T> not;
		
		@Deprecated
		protected Composite() {
			super();
		}
		
		public <A extends P> Composite(CompositionOp op, A... members) {
			super(Arrays.asList(members));
			this.op = op;
		}

		public Composite(CompositionOp op, Collection<? extends P> members) {
			super(members);
			this.op = op;
		}

		
		public CompositionOp getOperation() {
			return op;
		}
		/**
		 * Null can be passed as parameter, but nothing will be added to the composite predicate
		 */
		@SuppressWarnings("unchecked")
		@Override
		public boolean add(P o) {
			if (o == null) return false;
			if (o instanceof Composite && ((Composite<?,?>)o).op == op) {
				return super.addAll((Composite<? extends P, ?>)o);
			}
			return super.add(o);
		}
		
		/**
		 * Null can be passed as parameter, but nothing will be added to the composite predicate
		 */
		@SuppressWarnings("unchecked")
		@Override
		public boolean addAll(Collection<? extends P> c) {
			if (c == null) return false;
			if (c instanceof Composite && ((Composite<?,?>)c).op != op) {
				return super.add((P)c);
			} 
			return super.addAll(c);
		}
		
		@Override
		public boolean eval(T value) {
			boolean or = (op == CompositionOp.OR); // when OR, finish on true 
			for (P p : this) {
				if (p.eval(value) == or) return or;
			}
			return !or;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((op == null) ? 0 : op.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (!(obj instanceof Composite)) {
				return false;
			}
			Composite<?,?> other = (Composite<?,?>)obj;
			if (op != other.op) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "("+CollectionUtil.toString(this, " "+op.toString()+" ")+")";
		}
		
		@Override
		public Predicate<T> not() {
			if (not == null) not = createNegation();
			return not;
		}

		/**
		 * Note that the negation should be live - i.e. when this object's contents change, so should the negation's
		 * @return
		 */
		protected Not<T> createNegation() {
			return new Not<T>(this);
		}
		
	}
	public static class Const<T> implements PredicateWithInverse<T>, Serializable {
		public static final <A> Const<A> createTrue() {
			return new Const<A>();
		}
		private static final long serialVersionUID = 1L;
		protected transient Const<T> not;
		protected boolean constVal;
		
		protected Const() {
			constVal = true;
		}
		
		public Const(Const<T> not) {
			this.constVal = !not.constVal;
			this.not = not;
		}
		@Override
		public boolean eval(T value) {
			return constVal;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Const)) {
				return false;
			}
			return constVal == ((Const<?>)obj).constVal;
		}
		
		@Override
		public int hashCode() {
			return constVal ? 101359 : 101741;
		}
		
		@Override
		public String toString() {
			return constVal ? "true" : "false";
		}
		
		@Override
		public Predicate<T> not() {
			if (not == null) not = createNegation();
			return not;
		}
		protected Const<T> createNegation() {
			return new Const<T>(this);
		}
	}
	
	public static class Not<T> implements PredicateWithInverse<T>, Serializable {
		private static final long serialVersionUID = 1L;

		protected Predicate<T> ref;
		
		@Deprecated
		protected Not() {
		}
		
		public Not(Predicate<T> ref) {
			if (ref == null) throw new NullPointerException("ref");
			this.ref = ref;
		}
		
		@Override
		public boolean eval(T value) {
			return !ref.eval(value);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((ref == null) ? 0 : ref.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Not)) {
				return false;
			}
			return Util.safeEquals(ref, ((Not<?>)obj).ref);
		}
		
		@Override
		public String toString() {
			return "!("+ref+")";
		}
		
		@Override
		public Predicate<T> not() {
			return ref;
		}
	}
	
	boolean eval(T value);
}
