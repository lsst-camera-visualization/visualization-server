This module contains a simple restful interface for testing if new images are available from the CCS test stand. It is 
currently only partially implemented, returning fake fits files generated every 10 seconds, 
but what is here should be enough to begin to test the "Check for latest event"
functionality to be added to the firefly visualization client.

To run the project:

```bash
git clone https://github.com/lsst-camera-visualization/visualization-server.git
cd visualization-server
mvn install
mvn exec:java
```

Now point your browser to:

http://localhost:8080/vis/demo

The actual restful service is available can be viewed using curl:

```bash
curl -i "http://localhost:8080/vis/checkImage"
HTTP/1.1 204 No Content
Date: Tue, 10 May 2016 15:53:38 GMT
Access-control-allow-origin: *
Content-length: 0
```

The 204 No Content response indicates that no image is currently available, trying again:

```bash
curl -i "http://localhost:8080/vis/checkImage"
HTTP/1.1 200 OK
Date: Tue, 10 May 2016 15:54:49 GMT
Content-type: application/json
Access-control-allow-origin: *
Content-length: 109

{"uri":"file:/tmp/empty9029145216770999997.fits","name":"2016-05-10T15:54:40.898Z","timestamp":1462895680898}
```
Shows that there is now an image available. The json response includes

field|meaning
-----|-------
uri|The URI from which the image can be loaded (this is currently fake and does not actually contain a fits file)
name|The image name (to be shown to the user)
timestamp|The image timestamp (in milliseconds since epoch)

Additional parameters can be specified with the request

parameter|meaning
---------|-------
since|Only return images with a timestamp newer than that given (in milliseconds since epoch). The intention is that the timestamp of the previous image be specified as the value of this parameter
wait|Wait up to this number of seconds for a valid image to become available, before returning a "204 No Content" response.

For example:

```bash
curl -i "http://localhost:8080/vis/checkImage?since=1462836349955&wait=10"
HTTP/1.1 200 OK
Date: Tue, 10 May 2016 16:51:08 GMT
Content-type: application/json
Access-control-allow-origin: *
Content-length: 109

{"uri":"file:/tmp/empty7418385194369988201.fits","name":"2016-05-10T16:51:07.393Z","timestamp":1462899067393}
