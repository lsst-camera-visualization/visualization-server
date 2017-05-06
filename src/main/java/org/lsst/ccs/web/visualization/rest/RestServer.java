package org.lsst.ccs.web.visualization.rest;

import com.sun.net.httpserver.HttpServer;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.lsst.ccs.web.visualization.rest.demo.Demo;

/**
 *
 * @author tonyj
 */
public class RestServer implements Closeable {

    private HttpServer server;

    public void start(ImageQueue imageQueue, URI baseURI) throws IOException {
        final ResourceConfig rc = new ResourceConfig()
                .register(new CheckImage(imageQueue))
                .register(new Demo())
                .register(JacksonFeature.class);

        // create and start a new instance of http server
        //return GrizzlyHttpServerFactory.createHttpServer(baseURI, rc);
        server = JdkHttpServerFactory.createHttpServer(baseURI, rc);
    }

    @Override
    public void close() throws IOException {
        if (server != null) {
            server.stop(0);
        }
    }
}
