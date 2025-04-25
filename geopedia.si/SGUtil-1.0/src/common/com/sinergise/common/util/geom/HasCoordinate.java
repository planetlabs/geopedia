/**
 * 
 */
package com.sinergise.common.util.geom;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.sinergise.common.util.math.MathUtil;

/**
 * @author tcerovski
 */
public interface HasCoordinate {
	static class Util {
		@SuppressWarnings("unchecked")
		public static <T extends HasCoordinate> List<T> sortByX(Collection<T> coords) {
			HasCoordinate[] arr = coords.toArray(new HasCoordinate[coords.size()]);
			Arrays.sort(arr, HasCoordinate.X_COMPARATOR);
			return (List<T>)Arrays.asList(arr); 
		}
		
		@SuppressWarnings("unchecked")
		public static <T extends HasCoordinate> List<T> sortByY(Collection<T> coords) {
			HasCoordinate[] arr = coords.toArray(new HasCoordinate[coords.size()]);
			Arrays.sort(arr, HasCoordinate.Y_COMPARATOR);
			return (List<T>)Arrays.asList(arr); 
		}
	}
	
	Comparator<HasCoordinate> X_COMPARATOR = new Comparator<HasCoordinate>() {
		@Override
		public int compare(HasCoordinate o1, HasCoordinate o2) {
			return MathUtil.fastCompare(o1.x(), o2.x());
		}
	};

	Comparator<HasCoordinate> Y_COMPARATOR = new Comparator<HasCoordinate>() {
		@Override
		public int compare(HasCoordinate o1, HasCoordinate o2) {
			return MathUtil.fastCompare(o1.y(), o2.y());
		}
	};
	double x();
	double y();
}
