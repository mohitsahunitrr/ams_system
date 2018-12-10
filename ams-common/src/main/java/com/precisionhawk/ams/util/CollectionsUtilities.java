package com.precisionhawk.ams.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Philip A. Chapman
 */
public class CollectionsUtilities {
    
    private CollectionsUtilities() {}
    
    public static <T> T firstItemIn(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }
    
    public static <T> T firstItemIn(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        } else {
            return array[0];
        }
    }
    
    /**
     * Safely attempts to get an item by index.  If the index is out of range, null is returned rather than throwing an OutOfRangeException.
     * @param <T> The type in the list.
     * @param list The list to get the item from.
     * @param index The index to attempt to retrieve.
     * @return The item.  Null if the item at the requested index is null in the list or if the index beyond the high range of the index.
     */
    public static <T> T getItemSafely(List<T> list, int index) {
        if (list.size() <= index) {
            return null;
        } else {
            return list.get(index);
        }
    }
    
    public static <T> List<T> copyToMaxSize(List<T> source, List<T> target, int maxLength) {
        if (source != null && (!source.isEmpty())) {
            for (int i = 0; i < source.size() && target.size() < maxLength; i++) {
                target.add(source.get(i));
            }
        }
        return target;
    }
    
    public static <T> Set<T> difference(Collection<T> col1, Collection<T> col2) {
        Set<T> set1;
        if (col1 instanceof Set) {
            set1 = (Set<T>)col1;
        } else {
            set1 = new HashSet<>(col1);
        }
        Set<T> set2;
        if (col2 instanceof Set) {
            set2 = (Set<T>)col2;
        } else {
            set2 = new HashSet<>(col2);
        }
        Set results = new HashSet<>();
        for (T o : set1) {
            if (!set2.contains(o)) {
                results.add(o);
            } else {
                set2.remove(o); // Don't look at this again when processing elements of set2
            }
        }
        for (T o : set2) {
            if (!set1.contains(o)) {
                results.add(0);
            }
        }
        return results;
    }
    
    public static <T> Set<T> union(Collection<T> col1, Collection<T> col2) {
        Set<T> set = new HashSet<>();
        set.addAll(col1);
        set.addAll(col2);
        return set;
    }
}
