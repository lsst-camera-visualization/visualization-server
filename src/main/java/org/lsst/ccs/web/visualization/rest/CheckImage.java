package org.lsst.ccs.web.visualization.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Check if a new image is available
 */
@Path("checkImage")
public class CheckImage {

    private final ImageQueue queue;
    
    CheckImage(ImageQueue queue) {
        this.queue = queue;
    }

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "application/json" media type.
     *
     * @param since If specified, only return events newer than this.
     * @param waitSeconds If specified, and no suitable event is immediately available, 
     * wait a maximum of this many seconds for event to become available.
     * @return image that will be returned as a text/plain response.
     * @throws java.lang.InterruptedException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIt(@QueryParam("since") long since, @QueryParam("wait") int waitSeconds) throws InterruptedException {
        Image image = queue.waitForImageNewerThan(since, waitSeconds);
        return Response.status(image == null ? 204 : 200).entity(image).header("Access-Control-Allow-Origin", "*").build();
    }
}
