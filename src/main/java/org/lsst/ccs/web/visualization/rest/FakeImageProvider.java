package org.lsst.ccs.web.visualization.rest;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class which provides a fake image every n seconds.
 *
 * @author tonyj
 */
public class FakeImageProvider {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final static Logger logger = Logger.getLogger(FakeImageProvider.class.getName());
    private final ImageQueue queue;

    FakeImageProvider(final ImageQueue queue) {
        this.queue = queue;
    }

    public void start(Duration duration) {
        Runnable deliverImage = new Runnable() {
            @Override
            public void run() {
                try {
                    Instant instance = Instant.now();
                    File file = File.createTempFile("empty", ".fits");
                    Image latestImage = new Image(file.toURI(), instance.toString(), instance.toEpochMilli());
                    queue.put(latestImage);
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "Eror while fetching image", ex);
                }
            }
        };
        scheduler.scheduleAtFixedRate(deliverImage, duration.toMillis(), duration.toMillis(), TimeUnit.MILLISECONDS);
    }
    public void stop() {
        scheduler.shutdownNow();
    }
}
