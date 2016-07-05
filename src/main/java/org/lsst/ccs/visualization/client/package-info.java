/**
 * Client code for sending images to the ingest server. Sending an image
 * to the server involves using one or more {@link IngestClient}s. To send a complete image:
 * <ul>
 * <li>One client must send the {@link StartMessage} indicating the size of the image to be sent
 * and the number of clients which will send data.
 * <li>One or more clients (as indicated in the StartMessage) will send {@link DataMessage}s and {@link HeaderMessage}s.
 * <li>Each client which sent data and/or headers will send an {@link EndMessage}.
 * </ul>
 */
package org.lsst.ccs.visualization.client;
