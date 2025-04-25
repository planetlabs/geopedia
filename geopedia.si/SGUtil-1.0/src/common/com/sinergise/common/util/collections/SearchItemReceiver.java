package com.sinergise.common.util.collections;

import static java.lang.Boolean.TRUE;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sinergise.common.util.lang.Function;
import com.sinergise.common.util.lang.Predicate;

/**
 * Used in searching, traversals and iteration to process items found in the process and 
 * signal completion of the search if required.  
 * 
 * @author Miha
 *
 * @param <T>
 */
public interface SearchItemReceiver<T> extends Function<T, Boolean> {
	
	public static class SetCollector<E> extends SetWrapper<E> implements SearchItemReceiver<E> {
		public SetCollector() {
			this(new HashSet<E>(1));
		}
		public SetCollector(Set<E> target) {
			super(target);
		}
		@Override
		public Boolean execute(E item) {
			add(item);
			return TRUE;
		}
	}

	public static class ListCollector<E> extends ListWrapper<E> implements SearchItemReceiver<E> {
		public ListCollector() {
			this(new LinkedList<E>());
		}

		public ListCollector(List<E> store) {
			super(store);
		}
		
		@Override
		public Boolean execute(E item) {
			add(item);
			return TRUE;
		}
	}

	public static class FilteredListCollector<E> extends ListCollector<E> {
		final Predicate<E> filter;
		public FilteredListCollector(Predicate<E> filter) {
			super();
			this.filter = filter;
		}

		@Override
		public Boolean execute(E item) {
			if (filter.eval(item)) {
				add(item);
			}
			return TRUE;
		}
	}

	
	public static class Filter<E> implements SearchItemReceiver<E> {
		private final Set<? super E> excluded;
		private final Function<? super E, Boolean> target;
		public Filter(Function<? super E, Boolean> target, Set<? super E> excluded) {
			this.excluded = excluded;
			this.target = target;
		}
		@Override
		public Boolean execute(E item) {
			if (excluded.contains(item)) {
				return TRUE;
			}
			return target.execute(item);
		}
	}

	public static abstract class MinFinder<E> implements SearchItemReceiver<E> {
		private E best = null;
		private double bestValue;
		public MinFinder(double maxValue) {
			this.bestValue = maxValue;
		}
		@Override
		public Boolean execute(E item) {
			double curValue = calculate(item);
			if (curValue < bestValue) {
				best = item;
				bestValue = curValue;
			}
			return TRUE;
		}
		
		public final E getResult() {
			return best;
		}
		
		protected abstract double calculate(E item);
	}
	
	/**
	 * @param member the currently found item
	 * @return TRUE iff the iteration / traversal should continue
	 */
	@Override
	Boolean execute(T item);
}
