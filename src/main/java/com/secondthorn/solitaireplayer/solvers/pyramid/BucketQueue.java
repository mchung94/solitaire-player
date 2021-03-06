package com.secondthorn.solitaireplayer.solvers.pyramid;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A Bucket Queue is a type of priority queue for situations where the priorities are small integers,
 * and you only need to insert items and remove only the item with the lowest priority.
 * <p>
 * It's basically an array of queues where the index into the array is the priority.  So
 * when you add items you just look up the queue for the given priority and add it, and when
 * you remove something you remove it from the queue for the lowest priority.  In both cases you
 * just keep updating the lowest priority.
 *
 * @param <E> the type of elements held in this queue
 */
class BucketQueue<E> {
    private int capacity;
    private int currentPriority;
    private List<Deque<E>> buckets;

    /**
     * Create a bucket queue that allows priorities from 0 to maximumPriority inclusive.
     *
     * @param maximumPriority the maximum priority an element in the queue can have
     */
    BucketQueue(int maximumPriority) {
        capacity = maximumPriority + 1;
        currentPriority = capacity;
        buckets = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            buckets.add(new ArrayDeque<>());
        }
    }

    /**
     * Add an item to the bucket queue with the given priority.
     *
     * @param e        an element to be added to the queue
     * @param priority the priority of the element
     */
    void add(E e, int priority) {
        if (priority < currentPriority) {
            currentPriority = priority;
        }
        buckets.get(priority).add(e);
    }

    /**
     * Check if the bucket queue is empty.
     *
     * @return true if the bucket queue is empty, false otherwise.
     */
    boolean isEmpty() {
        return currentPriority == capacity;
    }

    /**
     * Remove and return the lowest priority element in the bucket queue.
     *
     * @return the lowest priority element in the bucket queue
     */
    E remove() {
        Deque<E> currentBucket = buckets.get(currentPriority);
        E e = currentBucket.remove();
        if (currentBucket.size() == 0) {
            do {
                currentPriority++;
            } while ((currentPriority < capacity) && (buckets.get(currentPriority).size() == 0));
        }
        return e;
    }
}
