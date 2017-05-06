package org.lsst.ccs.web.visualization.rest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

/**
 * Main class.
 *
 */
public class Main {

    // Base URI the HTTP server will listen on
    private static String PORT = "8080";

    /**
     * Main method.
     *
     * @param args
     * @throws IOException
     * @throws java.net.URISyntaxException
     */
    public static void main(String[] args) throws IOException {

        if (args.length == 1) {
            PORT = args[0];
        }
        URI baseURI = URI.create("http://localhost:" + PORT + "/vis/");

        System.out.println("PORT: " + PORT);
        System.out.println("BASE_URI: " + baseURI);

        ImageQueue imageQueue = new ImageQueue();
        try (RestServer restServer = new RestServer()) {
            restServer.start(imageQueue, baseURI);
            System.out.println(String.format("Jersey app started with WADL available at "
                    + "%sapplication.wadl\nHit enter to stop it...", baseURI));
            try (FakeImageProvider provider = new FakeImageProvider(imageQueue)) {
                provider.start(Duration.ofSeconds(10));
                System.in.read();
            }
        }
    }
}
