package com.secondthorn.solitaireplayer.solvers.tripeaks;

import java.util.NoSuchElementException;

/**
 * A FIFO (first in, first out) queue for primitive (unboxed) ints.
 */
public class IntFIFOQueue {
    private int[] elements;
    private int head;
    private int tail;

    /**
     * Creates a new FIFO queue for holding unboxed ints.
     */
    IntFIFOQueue() {
        elements = new int[128];
        head = 0;
        tail = 0;
    }

    /**
     * Add a new int to the end of the queue.
     */
    void enqueue(int item) {
        elements[tail] = item;
        tail = (tail + 1) & (elements.length - 1);
        if (tail == head) {
            doubleCapacity();
        }
    }

    /**
     * Returns true if the queue is empty
     */
    boolean isEmpty() {
        return head == tail;
    }

    /**
     * Removes and returns the first item in the queue.
     * @return the first item in the queue
     * @throws NoSuchElementException if the queue is empty
     */
    int dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        int item = elements[head];
        head = (head + 1) & (elements.length - 1);
        return item;
    }

    /**
     * Increases the size of the queue.  The size is always a power of two.
     * @throws IllegalStateException if the queue can't grow any bigger
     */
    private void doubleCapacity() {
        int p = head;
        int n = elements.length;
        int r = n - p;
        int newCapacity = n << 1;
        if (newCapacity < 0) {
            throw new IllegalStateException("IntFIFOQueue can't grow any further");
        }
        int[] a = new int[newCapacity];
        System.arraycopy(elements, p, a, 0, r);
        System.arraycopy(elements, 0, a, r, p);
        elements = a;
        head = 0;
        tail = n;
    }
}
