package dev.nvp.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Fixed-capacity FIFO buffer backed by an array. O(1) add; oldest entries are
 * evicted automatically. Index 0 is the oldest element, size-1 is the newest.
 */
public class RingBuffer<E> extends AbstractList<E> {

    private final Object[] data;
    private int head;       // index of next write
    private int size;
    private final int capacity;

    public RingBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.capacity = capacity;
        this.data = new Object[capacity];
    }

    public void push(E e) {
        data[head] = e;
        head = (head + 1) % capacity;
        if (size < capacity) size++;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException(index);
        int start = (head - size + capacity) % capacity;
        return (E) data[(start + index) % capacity];
    }

    @SuppressWarnings("unchecked")
    public E newest() { return size == 0 ? null : (E) data[(head - 1 + capacity) % capacity]; }

    @SuppressWarnings("unchecked")
    public E oldest() { return size == 0 ? null : (E) data[(head - size + capacity) % capacity]; }

    @Override public int size() { return size; }
    public int capacity() { return capacity; }
    public boolean isFull() { return size == capacity; }

    public void clear() {
        for (int i = 0; i < capacity; i++) data[i] = null;
        size = 0;
        head = 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            int i = 0;
            @Override public boolean hasNext() { return i < size; }
            @SuppressWarnings("unchecked")
            @Override public E next() {
                if (i >= size) throw new NoSuchElementException();
                return get(i++);
            }
        };
    }
}
