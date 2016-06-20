package org.lsst.ccs.web.visualization.rest;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.lsst.ccs.web.visualization.rest.demo.Demo;

/**
 * Main class.
 *
 */
public class Main {

    // Base URI the HTTP server will listen on
	private static String PORT = "8080";
    public static String BASE_URI = "http://localhost:" + PORT + "/vis/";

    /**
     * Starts HTTP server exposing JAX-RS resources defined in this application.
     *
     * @param imageQueue
     * @return HTTP server.
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    public static HttpServer startServer(ImageQueue imageQueue) throws IOException, URISyntaxException {
        // create a resource config 
        final ResourceConfig rc = new ResourceConfig()
                .register(new CheckImage(imageQueue))
                .register(new Demo())
                .register(JacksonFeature.class);

        // create and start a new instance of http server
        // exposing the Jersey application at BASE_URI
        //return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
        return JdkHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     *
     * @param args
     * @throws IOException
     * @throws java.net.URISyntaxException
     */
    public static void main(String[] args) throws IOException, URISyntaxException {
		
		if (args.length == 1)
		{
			PORT = args[0];
			BASE_URI = "http://localhost:" + PORT + "/vis/";
		}

		System.out.println("PORT: " + PORT);
		System.out.println("BASE_URI: " + BASE_URI);

        ImageQueue imageQueue = new ImageQueue();
        final HttpServer server = startServer(imageQueue);
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        FakeImageProvider provider = new FakeImageProvider(imageQueue);
        provider.start(Duration.ofSeconds(10));
        System.in.read();
        server.stop(0);
        provider.stop();
    }
}
