package org.lsst.ccs.web.visualization.rest;

import java.net.URI;

/**
 *
 * @author tonyj
 */
public class Image {
    private final URI uri;
    private final String name;
    private final long millis;

    public Image(URI uri, String name, long millis) {
        this.uri = uri;
        this.name = name;
        this.millis = millis;
    }

    public URI getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public long getTimestamp() {
        return millis;
    }
    
    
}
