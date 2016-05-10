package org.lsst.ccs.web.visualization.rest;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author tonyj
 */
public class ImageQueue {

    private Image latestImage;
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    /**
     * Add this image to the queue, if necessary replacing any existing image.
     *
     * @param image
     */
    public void put(Image image) {
        lock.lock();
        try {
            latestImage = image;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public Image waitForImageNewerThan(long since, int timeout) throws InterruptedException {
        lock.lock();
        try {
            Image image = getImageNewerThan(since);
            if (image == null && timeout > 0) {
                condition.await(timeout, TimeUnit.SECONDS);
                image = getImageNewerThan(since);
            }
            return image;
        } finally {
            lock.unlock();
        }
    }

    private Image getImageNewerThan(long since) {
        Image image = latestImage;
        if (image!=null && since > 0 && image.getTimestamp() <= since) {
            image = null;
        }
        return image;
    }
}
