package org.lsst.ccs.web.visualization.rest.demo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author tonyj
 */
@Path("demo")

public class Demo {

    private final String staticHTML;
    public Demo() throws IOException, URISyntaxException {
        staticHTML = new String(Files.readAllBytes(Paths.get(getClass().getResource("demo.html").toURI())));
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getIt()  {
        return staticHTML;
    }    
}
