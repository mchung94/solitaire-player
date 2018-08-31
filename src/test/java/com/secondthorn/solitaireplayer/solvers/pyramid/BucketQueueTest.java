package com.secondthorn.solitaireplayer.solvers.pyramid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BucketQueueTest {
    @Test
    public void newBucketQueueIsEmpty() {
        BucketQueue<Integer> bq = new BucketQueue<>(102);
        assertTrue(bq.isEmpty());
    }

    @Test
    public void addAndRemoveLeavesQueueEmpty() {
        BucketQueue<String> bq = new BucketQueue<>(102);
        bq.add("One", 1);
        assertEquals("One", bq.remove());
        assertTrue(bq.isEmpty());
    }

    @Test
    public void addAndRemoveUpdatesPriorityCorrectly() {
        BucketQueue<String> bq = new BucketQueue<>(20);

        bq.add("twelve", 12);
        bq.add("fourteen", 14);
        bq.add("twelve-two", 12);
        assertFalse(bq.isEmpty());
        assertTrue(bq.remove().startsWith("twelve"));
        assertTrue(bq.remove().startsWith("twelve"));
        assertEquals("fourteen", bq.remove());
        assertTrue(bq.isEmpty());

        bq.add("fourteen", 14);
        bq.add("twelve", 12);
        bq.add("twelve-two", 12);
        assertFalse(bq.isEmpty());
        assertTrue(bq.remove().startsWith("twelve"));
        assertTrue(bq.remove().startsWith("twelve"));
        assertEquals("fourteen", bq.remove());
        assertTrue(bq.isEmpty());

        bq.add("twenty", 20);
        assertFalse(bq.isEmpty());
        assertEquals("twenty", bq.remove());
        assertTrue(bq.isEmpty());
    }
}
