package org.lsst.ccs.visualization.server;

import java.io.File;

/**
 * An interface to be implemented by clients wishing to be notified of image
 * receipt.
 * @author tonyj
 */
public interface ImageListener {
    void imageReceived(String name, long timeStamp, File file);
}
