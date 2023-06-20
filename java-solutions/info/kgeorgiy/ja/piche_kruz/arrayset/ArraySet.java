package info.kgeorgiy.ja.piche_kruz.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {
    private final List<E> elements;
    private final Comparator<? super E> comparator;

    //Empty
    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    //Empty list
    public ArraySet(Comparator<? super E> comparator) {
        this(Collections.emptyList(), comparator);
    }

    //Natural order
    public ArraySet(Collection<? extends E> data) {
        this(data, null);
    }

    public ArraySet(Collection<? extends E> data, Comparator<? super E> comparator) {
        this.comparator = comparator;
        TreeSet<E> set = new TreeSet<>(comparator);
        set.addAll(data);
        elements = List.copyOf(set);
    }

    private ArraySet(List<E> data, Comparator<? super E> comparator) {
        elements = data;
        this.comparator = comparator;
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    int insertionPoint(E key) {
        return Collections.binarySearch(elements, key, comparator);
    }

    private int lowerBound(E key) {
        int ans = insertionPoint(key);
        if (ans < 0) {
            return -ans - 1;
        }
        return ans;
    }

    private SortedSet<E> subSetByIndices(int fromIndex, int toIndex) {
        return new ArraySet<>(elements.subList(fromIndex, toIndex), comparator);
    }

    @Override
    // :NOTE: duplicate code
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if (comparator.compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("Failed attempt to build a subset from an incorrect range\n");
        }
        int fromIndex = lowerBound(fromElement);
        int toIndex = lowerBound(toElement);
        // :NOTE: can work O(1)
        return subSetByIndices(fromIndex, toIndex);
    }


    @Override
    public SortedSet<E> headSet(E toElement) {
        return subSetByIndices(0, lowerBound(toElement));
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return subSetByIndices(lowerBound(fromElement), size());
    }

    private void emptyCheck(String message) {
        if (isEmpty()) {
            throw new NoSuchElementException(message);
        }
    }

    @Override
    public E first() {
        emptyCheck("Can't get smallest element from an empty list");
        return elements.get(0);
    }

    @Override
    public E last() {
        emptyCheck("Can't get largest element from an empty list");
        return elements.get(elements.size() - 1);
    }

    @Override
    public int size() {
        return isEmpty() ? 0 : elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(elements, (E) Objects.requireNonNull(o), comparator) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }
}
