package com.sinergise.common.util.math;

import static com.sinergise.common.util.lang.TypeUtil.boxI;
import static com.sinergise.common.util.math.Interval.BoundaryType.CLOSED;
import static com.sinergise.common.util.math.Interval.BoundaryType.OPEN;
import static com.sinergise.common.util.math.Interval.IntervalBoundary.NEGATIVE_INFINITY;
import static com.sinergise.common.util.math.Interval.IntervalBoundary.POSITIVE_INFINITY;

public class Interval<T extends Comparable<? super T>> implements Comparable<Interval<? extends T>> {
	public static enum BoundaryType {
		CLOSED {
			@Override
			public boolean checkPositive(int comparisonInt) {
				return comparisonInt >= 0;
			}

			@Override
			public String toStringLeft() {
				return "[";
			}

			@Override
			public String toStringRight() {
				return "]";
			}
		},
		OPEN {
			@Override
			public boolean checkPositive(int comparisonInt) {
				return comparisonInt > 0;
			}

			@Override
			public String toStringLeft() {
				return "(";
			}

			@Override
			public String toStringRight() {
				return ")";
			}
		};

		public abstract boolean checkPositive(int comparisonInt);

		public abstract String toStringLeft();

		public abstract String toStringRight();
	}

	public static class IntervalBoundary<E extends Comparable<? super E>> {
		public static <S extends Comparable<? super S>> IntervalBoundary<S> createClosed(S value) {
			return create(CLOSED, value);
		}

		public static <S extends Comparable<? super S>> IntervalBoundary<S> createOpen(S value) {
			return create(OPEN, value);
		}

		public static <S extends Comparable<? super S>> IntervalBoundary<S> create(BoundaryType type, S value) {
			return new IntervalBoundary<S>(type, value);
		}

		private static class Infinite extends IntervalBoundary<Comparable<Object>> {
			public Infinite(Comparable<Object> value) {
				super(OPEN, value);
			}

			@Override
			public Comparable<Object> getValue() {
				throw new UnsupportedOperationException("Can't call getValue() on an infinite boundary");
			}

			@Override
			public boolean isInfinite() {
				return true;
			}
		}

		private static Comparable<Object> PLUS_INF = new Comparable<Object>() {
			@Override
			public final int compareTo(Object o) {
				return o == this ? 0 : 1;
			}

			@Override
			public String toString() {
				return "∞";
			}
		};
		public static IntervalBoundary<Comparable<Object>> POSITIVE_INFINITY = new Infinite(PLUS_INF);
		private static Comparable<Object> MINUS_INF = new Comparable<Object>() {
			@Override
			public final int compareTo(Object o) {
				return o == this ? 0 : -1;
			}

			@Override
			public String toString() {
				return "−∞";
			}
		};
		public static IntervalBoundary<Comparable<Object>> NEGATIVE_INFINITY = new Infinite(MINUS_INF);

		private BoundaryType type;
		private E value;

		IntervalBoundary(BoundaryType type, E value) {
			this.type = type;
			this.value = value;
		}

		public E getValue() {
			return value;
		}

		public BoundaryType getType() {
			return type;
		}

		/**
		 * For CLOSED, returns this.value <= compValue <br />
		 * For OPEN, returns this.value < compValue
		 */
		public boolean isSmallerThan(E compValue) {
			return type.checkPositive(-value.compareTo(compValue));
		}
		
		/**
		 * When value is the same, CLOSED boundary is smaller than OPEN
		 */
		public boolean isSmallerThan(IntervalBoundary<? extends E> other) {
			int comp = value.compareTo(other.value);
			if (comp == 0) {
				return type == other.type ? false : type == CLOSED;
			}
			return comp < 0;
		}

		/**
		 * For CLOSED, returns this.value >= compValue <br />
		 * For OPEN, returns this.value > compValue
		 */
		public boolean isLargerThan(E compValue) {
			return type.checkPositive(value.compareTo(compValue));
		}

		/**
		 * When value is the same, CLOSED boundary is larger than OPEN
		 */
		public boolean isLargerThan(IntervalBoundary<? extends E> other) {
			int comp = value.compareTo(other.value);
			if (comp == 0) {
				return type == other.type ? false : type == CLOSED;
			}
			return comp > 0;
		}

		
		public String toStringLeft() {
			return type.toStringLeft() + value;
		}

		public String toStringRight() {
			return value + type.toStringRight();
		}

		public boolean isInfinite() {
			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
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
			if (!(obj instanceof IntervalBoundary)) {
				return false;
			}
			IntervalBoundary<?> other = (IntervalBoundary<?>)obj;
			if (type != other.type) {
				return false;
			}
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}

		public static <S extends Comparable<? super S>> IntervalBoundary<S> min(IntervalBoundary<S> b1, IntervalBoundary<S> b2) {
			if (b1.isLargerThan(b2)) {
				return b2;
			}
			return b1;
		}

		public static <S extends Comparable<? super S>> IntervalBoundary<S> max(IntervalBoundary<S> b1, IntervalBoundary<S> b2) {
			if (b1.isSmallerThan(b2)) {
				return b2;
			}
			return b1;
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static final Interval EMPTY =  new Interval(null, null) {
		@Override
		public boolean contains(Comparable value) {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public Comparable getMinValue() {
			return throwBoundaryException();
		}

		@Override
		public Comparable getMaxValue() {
			return throwBoundaryException();
		}

		private <A> A throwBoundaryException() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Can't get boundary on empty interval");
		}

		@Override
		public Interval expandToInclude(Comparable val) {
			return closed(val, val);
		}
		
		@Override
		public String toString() {
			return OPEN.toStringLeft() + OPEN.toStringRight();
		}
	};

	private static final Interval<Comparable<Object>> ALL = new Interval<Comparable<Object>>(NEGATIVE_INFINITY,
		POSITIVE_INFINITY);

	@SuppressWarnings("unchecked")
	public static <E extends Comparable<? super E>> Interval<E> createEmpty() {
		return EMPTY;
	}

	@SuppressWarnings({"unused", "unchecked"})
	public static <E extends Comparable<? super E>> Interval<E> createEmpty(Class<E> type) {
		return EMPTY;
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends Comparable<? super E>> Interval<E> createInfinite() {
		return (Interval<E>)ALL;
	}

	public static <E extends Comparable<? super E>> Interval<E> closed(E lowerBound, E upperBound) {
		assert lowerBound.compareTo(upperBound) <= 0;
		return new Interval<E>(IntervalBoundary.createClosed(lowerBound), IntervalBoundary.createClosed(upperBound));
	}

	public static Interval<Integer> closed(int lowerBound, int upperBound) {
		assert lowerBound <= upperBound;
		return closed(boxI(lowerBound), boxI(upperBound));
	}
	
	public static <E extends Comparable<? super E>> Interval<E> createOpen(E lowerBound, E upperBound) {
		assert lowerBound.compareTo(upperBound) < 0;
		return new Interval<E>(IntervalBoundary.createOpen(lowerBound), IntervalBoundary.createOpen(upperBound));
	}

	private IntervalBoundary<T> min;
	private IntervalBoundary<T> max;

	Interval(IntervalBoundary<T> leftBound, IntervalBoundary<T> rightBound) {
		this.min = leftBound;
		this.max = rightBound;
	}

	public boolean isEmpty() {
		return false;
	}

	@Override
	public int compareTo(Interval<? extends T> o) {
		return 0;
	}

	public T getMinValue() {
		return min.getValue();
	}

	public T getMaxValue() {
		return max.getValue();
	}

	public boolean contains(T value) {
		return min.isSmallerThan(value) && max.isLargerThan(value);
	}
	
	@Override
	public String toString() {
		return min.toStringLeft() +", "+max.toStringRight();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((max == null) ? 0 : max.hashCode());
		result = prime * result + ((min == null) ? 0 : min.hashCode());
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
		if (!(obj instanceof Interval)) {
			return false;
		}
		Interval<?> other = (Interval<?>)obj;
		if (max == null) {
			if (other.max != null) {
				return false;
			}
		} else if (!max.equals(other.max)) {
			return false;
		}
		if (min == null) {
			if (other.min != null) {
				return false;
			}
		} else if (!min.equals(other.min)) {
			return false;
		}
		return true;
	}

	public Interval<T> expandToInclude(T val) {
		if (min.isLargerThan(val)) {
			if (max.isSmallerThan(val)) {
				return closed(val, val);
			}
			return new Interval<T>(IntervalBoundary.createClosed(val), max);
		}
		if (max.isSmallerThan(val)) {
			return new Interval<T>(min, IntervalBoundary.createClosed(val));
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public Interval<T> intersection(Interval<T> other) {
		IntervalBoundary<T> outMin = IntervalBoundary.max(this.min, other.min);
		IntervalBoundary<T> outMax = IntervalBoundary.min(this.max, other.max);
		if (outMin.isSmallerThan(outMax)) {
			return new Interval<T>(outMin, outMax);
		}
		if (outMin.equals(outMax) && outMin.getType() == CLOSED) {
			return new Interval<T>(outMin, outMin);
		}
		return EMPTY;
	}
	
}
