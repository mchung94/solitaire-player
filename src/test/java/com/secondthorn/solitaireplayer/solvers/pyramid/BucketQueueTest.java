package com.secondthorn.solitaireplayer.solvers.pyramid;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BucketQueueTest {
    @Test
    public void newBucketQueueIsEmpty() {
        BucketQueue<Integer> bq = new BucketQueue<>(100);
        assertEquals(0, bq.size());
    }

    @Test
    public void addAndRemoveLeavesQueueEmpty() {
        BucketQueue<String> bq = new BucketQueue(100);
        bq.add("One", 1);
        assertEquals("One", bq.remove());
        assertEquals(0, bq.size());
    }

    @Test
    public void addAndRemoveUpdatesPriorityCorrectly() {
        BucketQueue<String> bq = new BucketQueue<>(20);

        bq.add("twelve", 12);
        bq.add("fourteen", 14);
        bq.add("twelve-two", 12);
        assertEquals(3, bq.size());
        assertTrue(bq.remove().startsWith("twelve"));
        assertTrue(bq.remove().startsWith("twelve"));
        assertEquals("fourteen", bq.remove());
        assertEquals(0, bq.size());

        bq.add("fourteen", 14);
        bq.add("twelve", 12);
        bq.add("twelve-two", 12);
        assertEquals(3, bq.size());
        assertTrue(bq.remove().startsWith("twelve"));
        assertTrue(bq.remove().startsWith("twelve"));
        assertEquals("fourteen", bq.remove());
        assertEquals(0, bq.size());

        bq.add("twenty", 20);
        assertEquals(1, bq.size());
        assertEquals("twenty", bq.remove());
        assertEquals(0, bq.size());
    }
}
