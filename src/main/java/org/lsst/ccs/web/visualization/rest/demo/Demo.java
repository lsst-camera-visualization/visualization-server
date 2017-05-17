package org.lsst.ccs.web.visualization.rest.demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
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

    public Demo() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("demo.html")) {
            Scanner s = new Scanner(in).useDelimiter("\\A");
            staticHTML = s.hasNext() ? s.next() : "";
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getIt() {
        return staticHTML;
    }
}
