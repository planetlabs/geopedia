/**
 * 
 */
package com.sinergise.common.util.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.RandomAccess;
import java.util.Set;

import com.sinergise.common.util.CheckUtil;
import com.sinergise.common.util.collections.sort.SortLoop;
import com.sinergise.common.util.collections.sort.VirtualSorter;
import com.sinergise.common.util.lang.Function;
import com.sinergise.common.util.lang.NullSafeComparator;
import com.sinergise.common.util.lang.Pair;
import com.sinergise.common.util.lang.Predicate;

/**
 * @author tcerovski
 */
public class CollectionUtil {
	
	@SuppressWarnings("rawtypes")
	private static final Iterator EMPTY_ITERATOR = new Iterator() {
		@Override
		public boolean hasNext() {
			return false;
		}
		@Override
		public Object next() {
			throw new NoSuchElementException("No element in the empty iterator");
		}
		@Override
		public void remove() {
			throw new IllegalStateException("Can't remove from empty iterator");
		}
	};
	
	private static final int FILL_THRESHOLD = 25;
	public static final class ListByIndexGetter<E> implements Function<Integer, E> {
		private final List<E> list;
		public ListByIndexGetter(List<E> list) {
			this.list = list;
		}
		@Override
		public E execute(Integer param) {
			return list.get(param.intValue());
		}
	}
	
	private static final class FunctionComparator<E, T> implements Comparator<E> {
		public FunctionComparator(Function<E, T> fun, Comparator<? super T> comp) {
			super();
			this.fun = fun;
			this.comp = comp;
		}

		final Function<E, T> fun;
		final Comparator<? super T> comp; 
		
		@Override
		public int compare(E o1, E o2) {
			return comp.compare(fun.execute(o1), fun.execute(o2));
		}
	}

	private static final class FunctionComparatorWithComparable<E, T extends Comparable<? super T>> implements Comparator<E> {
		public FunctionComparatorWithComparable(Function<E, T> fun) {
			super();
			this.fun = fun;
		}

		final Function<E, T> fun;
		
		@Override
		public int compare(E o1, E o2) {
			return fun.execute(o1).compareTo(fun.execute(o2));
		}
	}
	
	/**
	 * Adds <code>element</code> to coll iff it is not null.
	 * 
	 * @param coll target
	 * @param element to add
	 * 
	 * @throws IllegalArgumentException if <code>coll</code> is null
	 */
	public static <T> boolean addTo(Collection<? super T> coll, T element) {
		if (element == null) {
			return false;
		}
		CheckUtil.checkArgumentNotNull(coll, "coll");
		return coll.add(element);
	}
	
	/**
	 * Adds all elements in data to coll. If <code>data</code> is null, does nothing.
	 * 
	 * @param <T> type of elements
	 * @param coll receiving collection
	 * @param data data to add
	 * @throws IllegalArgumentException if <code>coll</code> is null
	 */
	public static <T> boolean addTo(final Collection<? super T> coll, final T[] data) {
		if (data == null) {
			return false;
		}
		CheckUtil.checkArgumentNotNull(coll, "coll");
		return coll.addAll(Arrays.asList(data));
	}
	
	/**
	 * Adds elements from data to coll.
	 * 
	 * @param <T> type of elements
	 * @param coll receiving collection
	 * @param data data
	 * @param offset starting offset
	 * @param length number of elements to add
	 */
	public static <T> boolean addTo(final Collection<? super T> coll, final T[] data, final int offset, final int length) {
		if (data == null) {
			return false;
		}
		CheckUtil.checkArgumentNotNull(coll, "coll");
		return coll.addAll(Arrays.asList(data).subList(offset, offset + length));
	}
	
	public static <E extends Comparable<? super E>> Comparator<E> comparableComparator(boolean nullFirst) {
		return NullSafeComparator.<E>get(nullFirst);
	}

	public static <E, T> Comparator<E> createFunctionComparator(Function<E, T> fun, Comparator<? super T> comp) {
		return new FunctionComparator<E, T>(fun, comp);
	}
	
	public static <E, T extends Comparable<? super T>> Comparator<E> createFunctionComparator(Function<E, T> fun)  {
		return new FunctionComparatorWithComparable<E, T>(fun);
	}
	
	public static int[] toIntArray(final Collection<? extends Number> collection) {
		final int[] ids = new int[collection.size()];
		int idx = 0;
		for (final Number val : collection) {
			ids[idx++] = val != null ? val.intValue() : 0;
		}
		return ids;
	}
	
	public static boolean[] toBoolArray(final Collection<Boolean> collection) {
		final boolean[] bools = new boolean[collection.size()];
		int idx = 0;
		for (final Boolean val : collection) {
			bools[idx++] = val != null ? val.booleanValue() : false;
		}
		return bools;
	}
	
	public static <T> Collection<Collection<T>> splitCollection(final Collection<T> toSplit, final int maxSize) {
		
		final int cnt = (int)Math.ceil((double)toSplit.size() / (double)maxSize);
		
		final ArrayList<Collection<T>> result = new ArrayList<Collection<T>>(cnt);
		
		int i = 0;
		final Iterator<T> iter = toSplit.iterator();
		while (iter.hasNext()) {
			final int size = Math.min(maxSize, toSplit.size() - i * maxSize);
			final ArrayList<T> list = new ArrayList<T>();
			for (int j = 0; j < size; j++) {
				list.add(iter.next());
			}
			result.add(list);
			i++;
		}
		
		return result;
	}
	
	public static <T, E extends T> Set<T> asSet(E...e) {
		HashSet<T> set = new HashSet<T>(e.length);
		for (T o : e) {
			set.add(o);
		}
		return set;
	}
	
	@SuppressWarnings("unchecked")
	public static <T, E extends T> Set<T> asSet(Iterable<E> e) {
		if (e instanceof Set) {
			return (Set<T>)e;
		}
		HashSet<T> set = new HashSet<T>();
		for (T o : e) {
			set.add(o);
		}
		return set;
	}
	
	/**
     * Fills the specified list with the specified element.
     * The list can be empty as the size is specified. <p>
     * This method runs in linear time.
     *
     * @param  list the list to be filled with the specified element.
     * @param  obj The element with which to fill the specified list.
     * @param  size Number of elements to fill into the list
     */
    public static <T> void fill(List<? super T> list, T obj, int size) {
    	//implementation from java.util.Collections but the size can be specified
    	
    	int initSize = list.size();
    	
        int i=0;
    	int nToSet = Math.min(initSize, size);
        if (size < FILL_THRESHOLD || list instanceof RandomAccess) {
        	for (i=0; i<nToSet; i++) {
        		list.set(i, obj);
        	}
        } else {
            ListIterator<? super T> itr = list.listIterator();
        	for (i=0; i<nToSet; i++) {
                itr.next();
                itr.set(obj);
            }
        }
        for(;i<size; i++) {
        	list.add(obj);
        }
    }

	public static String toString(Map<?,?> map) {
		if (map==null) return null;
		StringBuilder sb = new StringBuilder();
		boolean comma = false;
		for (Map.Entry<?, ?> e : map.entrySet()) {
			if (comma) sb.append(", ");
			else comma = true;
			sb.append(e.getKey()).append("=").append(e.getValue());
		}
		return sb.toString();
	}
	
	public static String toString(Iterable<?> col) {
		return toString(col, ", ");
	}
	
	public static void reduceList(List<?> list, int maxSize) {
		while (list.size() > maxSize) {
			list.remove(list.size()-1);
		}
	}
	
	public static <T> int moveNullsToBack(List<T> data) {
		if (data == null || data.size()==0) return 0;
		
		int cnt = 0;
		for (int i = 0; i < data.size(); i++) {
			T val = data.get(i);
			if (val == null) {
				cnt++;
			} else if (cnt>0) {
				data.set(i-cnt, val);
			}
		}
		return data.size() - cnt;
	}
	
	public static <T> List<T> mergeCollections(Collection<T> ...collections) {
		int size = 0;
		for (Collection<T> c : collections) {
			size += c.size();
		}
		
		List<T> merged = new ArrayList<T>(size);
		for (Collection<T> c : collections) {
			merged.addAll(c);
		}
		
		return merged;
	}
	
	public static int hashCode(final Collection<?> c) {
		if (c == null) {
			return 0;
		}
		int result = 1;
		int idx = 0;
		for (final Object element : c) {
			result ^= (idx++)+(element == null ? 0 : element.hashCode());
		}
		return result;
	}
	
	public static <T> List<T> toList(Iterable<T> iterable) {
		return toList(iterable.iterator());
	}
	
	public static <T> List<T> toList(Iterator<T> iter) {
		List<T> list = new ArrayList<T>();
		while(iter.hasNext()) {
			list.add(iter.next());
		}
		return list;
	}
	
	public static List<Integer> toList(int[] ints) {
		List<Integer> list = new ArrayList<Integer>(ints.length);
		for (int i : ints) {
			list.add(Integer.valueOf(i));
		}
		return list;
	}
	
	public static boolean isNullOrEmpty(Iterable<?> col) {
		return col == null || !col.iterator().hasNext();
	}
	
	public static boolean isNullOrEmpty(Collection<?> col) {
		return col == null || col.isEmpty();
	}
	
	public static boolean isNullOrEmpty(Map<?,?> map) {
		return map == null || map.isEmpty();
	}

	public static <P, R> List<R> mapToList(P[] arr, Function<? super P, R> function) {
		ArrayList<R> ret = new ArrayList<R>(arr.length);
		for (P p : arr) {
			ret.add(function.execute(p));
		}
		return ret;
	}
	
	public static <P, R> R[] mapToArray(P[] srcArr, R[] tgtArr, Function<? super P, R> function) {
		for (int i=0; i<srcArr.length; i++) {
			tgtArr[i] = function.execute(srcArr[i]);
		}
		return tgtArr;
	}
	
	public static <P, R> R[] mapToArray(Collection<P> srcList, R[] tgtArr, Function<? super P, R> function) {
		int i=0;
		for (P p : srcList) {
			tgtArr[i++] = function.execute(p);
		}
		return tgtArr;
	}
	
	public static <P, R> List<R> mapToList(Iterable<? extends P> col, Function<? super P, R> func) {
		return map(col, new ArrayList<R>(), func);
	}
	
	public static <P, R> List<R> mapToList(Collection<? extends P> col, Function<? super P, R> func) {
		if (col.isEmpty()) {
			return Collections.emptyList();
		}
		return map(col, new ArrayList<R>(col.size()), func);
	}
	
	public static <P, R> Set<R> mapToSet(Iterable<? extends P> col, Function<? super P, R> func) {
		return map(col, new HashSet<R>(), func);
	}

	public static <P, R> Set<R> mapToSet(Collection<? extends P> col, Function<? super P, R> func) {
		if (col.isEmpty()) {
			return Collections.emptySet();
		}
		return map(col, new HashSet<R>(), func);
	}
	
	public static <P, R, C extends Collection<R>> C map(Iterable<P> src, C tgt, Function<? super P, ? extends R> func) {
		for (P p : src) {
			tgt.add(func.execute(p));
		}
		return tgt;
	}

	public static <P, C, R extends C> C[] map(Iterable<P> src, C[] tgt, Function<? super P, ? extends R> func) {
		int i=0;
		for (P p : src) {
			tgt[i++] = func.execute(p);
		}
		return tgt;
	}
	
	/**
	 * @return the positions in data at which each successive element of collections.sort(data) appears
	 */
	public static <E> List<Integer> ordering(List<E> data, Comparator<? super E> comp) {
		int len = data.size();
		Integer[] ret = new Integer[len];
		for (int i = len; i >= 0; i--) {
			ret[i] = Integer.valueOf(i);
		}
		Arrays.sort(ret, createFunctionComparator(new ListByIndexGetter<E>(data), comp));
		return Arrays.asList(ret);
	}
	
	public static int partitionByKthSmallest(VirtualSorter s, int left, int right, int k) {
		return SortLoop.partitionByKthSmallest(s, left, right, k);
	}

	public static <T> Pair<T,T> medianPair(List<T> list, Comparator<? super T> comp) {
		Collections.sort(list,comp);
		final int totSize = list.size();
		final int k = totSize/2;
		if (totSize % 2 == 0) {
			return Pair.newPair(list.get(k-1), list.get(k));
		}
		T elem = list.get(k);
		return Pair.newPair(elem, elem);
	}
	
	public static <T> List<T>[] partition(List<T> list, int pSize) {
		final int totSize = list.size();
		@SuppressWarnings("unchecked")
		List<T>[] ret = new List[totSize/pSize];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = list.subList(i*5, Math.min(totSize, (i+1)*5));
		}
		return ret;
	}

	public static String toString(Iterable<?> col, String separator) {
		if (col == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (Object e : col) {
			if (sb.length() > 0) {
				sb.append(separator);
			}
			sb.append(e);
		}
		return sb.toString();
	}

	public static <T> T firstOrNullIfEmpty(Iterable<T> col) {
		if (col == null || !col.iterator().hasNext()) {
			return null;
		}
		return first(col);
	}
	
	public static <T> T first(Iterable<T> col) {
		if (col instanceof LinkedList) {
			return ((LinkedList<T>)col).getFirst();
		}
		if (col instanceof List) {
			return ((List<T>)col).get(0);
		}
		if (col instanceof Queue) {
			return ((Queue<T>)col).element();
		}
		return col.iterator().next();
	}
	
	public static <T> T last(Iterable<T> col) {
		if (col instanceof LinkedList) {
			return ((LinkedList<T>)col).getLast();
		}
		if (col instanceof List) {
			List<T> list = (List<T>)col;
			return list.get(list.size()-1);
		}
//not supported	in GWT	
//		if (col instanceof Deque) {
//			return ((Deque<T>)col).getLast();
//		}
		return last(col.iterator());
	}

	public static <T> T last(Iterator<T> it) {
		T ret = null;
		while (it.hasNext()) {
			ret = it.next();
		}
		return ret;
	}

	public static <T> Set<T> singleton(T obj, boolean emptyIfNull) {
		if (obj == null && emptyIfNull) {
			return Collections.emptySet();
		}
		return Collections.singleton(obj);
	}

	public static <T> List<T> filter(Iterable<T> in, Predicate<? super T> predicate) {
		ArrayList<T> ret = new ArrayList<T>();
		for (T t : in) {
			if (predicate.eval(t)) {
				ret.add(t);
			}
		}
		return ret;
	}
	
	public static <T> Set<T> filter(Set<T> in, Predicate<? super T> predicate) {
		Set<T> ret = new HashSet<T>();
		return filter(in, predicate, ret);
	}

	public static <T> Set<T> filterLinked(Set<T> in, Predicate<? super T> predicate) {
		Set<T> ret = new LinkedHashSet<T>();
		return filter(in, predicate, ret);
	}
	
	private static <T> Set<T> filter(Set<T> in, Predicate<? super T> predicate, Set<T> ret) {
		for (T t : in) {
			if (predicate.eval(t)) {
				ret.add(t);
			}
		}
		return ret;
	}
	
	public static <T> void filterInPlace(Iterable<T> in, Predicate<? super T> predicate) {
		for (Iterator<T> it = in.iterator(); it.hasNext();) {
			if (!predicate.eval(it.next())) {
				it.remove();
			}
		}
	}

	public static <T> T findFirst(Iterable<T> collection, Predicate<? super T> predicate) {
		for (T t : collection) {
			if (predicate.eval(t)) {
				return t;
			}
		}
		return null;
	}
	
	public static <T> boolean findAll(Iterable<T> collection, Predicate<? super T> predicate, SearchItemReceiver<? super T> output) {
		boolean ret = false;
		for (T t : collection) {
			if (!predicate.eval(t)) {
				continue;
			}
			ret = true; 
			if (output.execute(t).booleanValue()) {
				continue; 
			}
		}
		return ret; 
	}

	public static <T> Iterable<T> emptyIfNull(Iterable<T> value) {
		return value == null ? Collections.<T>emptyList() : value;
	}
	
	public static <K,V> Map<K, V> mapPairs(Iterable<Pair<K, V>> c) {
		Map<K, V> map = new HashMap<K, V>(extractSize(c, 16));
		for (Pair<K, V> p : c) {
			map.put(p.getFirst(), p.getSecond());
		}
		return map;
	}

	public static <T, S> Map<T, List<S>> group(Iterable<S> source, Function<S, T> function) {
		HashMap<T, List<S>> ret = new HashMap<T, List<S>>(extractSize(source, 16));
		for (S obj : source) {
			T key = function.execute(obj);
			List<S> group = ret.get(key);
			if (group == null) {
				ret.put(key, group = new ArrayList<S>());
			}
			group.add(obj);
		}
		return ret;
	}

	public static <K, V> Map<K, V> mapValues(Iterable<V> collection, Function<V, K> keyGenerator) {
		HashMap<K, V> ret = new HashMap<K, V>(extractSize(collection, 16));
		for (V v : collection) {
			ret.put(keyGenerator.execute(v), v);
		}
		return ret;
	}
	
	public static <K, V> Map<K, V> mapKeys(Iterable<K> collection, Function<K, V> valueGenerator) {
		HashMap<K, V> ret = new HashMap<K, V>(extractSize(collection, 16));
		for (K k : collection) {
			ret.put(k, valueGenerator.execute(k));
		}
		return ret;
	}
	
	private static int extractSize(Iterable<?> iterable, int ifNotAvailable) {
		if (iterable instanceof Collection<?>) {
			return ((Collection<?>)iterable).size();
		}
		return ifNotAvailable;
	}

	public static <K, V, R> Map<K, R> mapToValues(Map<K, V> map, Function<V, R> valueMapper) {
		HashMap<K, R> ret = new HashMap<K, R>(map.size());
		for (Map.Entry<K, V> e : map.entrySet()) {
			ret.put(e.getKey(), valueMapper.execute(e.getValue()));
		}
		return ret;
	}
	
	public static <K, V, R> Map<R, V> mapToKeys(Map<K, V> map, Function<K, R> keyMapper) {
		HashMap<R, V> ret = new HashMap<R, V>(map.size());
		for (Map.Entry<K, V> e : map.entrySet()) {
			ret.put(keyMapper.execute(e.getKey()), e.getValue());
		}
		return ret;
	}
	
	public static <E extends Comparable<? super E>> E findSmallest(Iterable<E> elements) {
		return findSmallest(elements, CollectionUtil.<E>comparableComparator(false));
	}

	public static <E> E findSmallest(Iterable<E> elements, Comparator<? super E> comp) {
		Iterator<E> it = elements.iterator();
		E best = it.next();
		while (it.hasNext()) {
			E cur = it.next();
			if (comp.compare(cur, best) < 0) {
				best = cur;
			}
		}
		return best;
	}
	
	public static <E, V extends Comparable<? super V>> E findSmallest(Iterable<E> elements, Function<E, V> fun) {
		Iterator<E> it = elements.iterator();
		E best = it.next();
		V minVal = fun.execute(best);
		while (it.hasNext()) {
			E cur = it.next();
			V curVal = fun.execute(cur);
			if (curVal.compareTo(minVal) < 0) {
				best = cur;
				minVal = curVal;
			}
		}
		return best;
	}

	@SuppressWarnings("unchecked")
	public static <E> Iterator<E> emptyIterator() {
		return EMPTY_ITERATOR;
	}

	public static <E extends Comparable<? super E>> E findLargest(Iterable<E> elements) {
		return findLargest(elements, CollectionUtil.<E>comparableComparator(false));
	}

	public static <E> E findLargest(Iterable<E> elements, Comparator<? super E> comp) {
		Iterator<E> it = elements.iterator();
		E best = it.next();
		while (it.hasNext()) {
			E cur = it.next();
			if (comp.compare(cur, best) > 0) {
				best = cur;
			}
		}
		return best;
	}

	public static <T> boolean contains(Iterable<T> elements, Predicate<? super T> predicate) {
		return findFirst(elements, predicate) != null;
	}

}
