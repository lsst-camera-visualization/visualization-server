package org.lsst.ccs.visualization.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Client used to send messages to the ingest server.
 * @author tonyj
 */
public class IngestClient implements Closeable {

    private final SocketChannel socket;

    /**
     * Create an ingest client which will connect to the specified server. 
     * @param hostname The server host name.
     * @param port The server port number.
     * @throws IOException If an error occurs while establishing the connection.
     */
    public IngestClient(String hostname, int port) throws IOException  {
        this(new InetSocketAddress(hostname, port));
    }
    /**
     * Create an ingest client which will connect to the specified server.
     * @param address The server address.
     * @throws IOException If an error occurs while establishing the connection.
     */
    public IngestClient(SocketAddress address) throws IOException {
        socket = SocketChannel.open(address);
        socket.shutdownInput();
    }

    /**
     * Send a message to the server.
     * @param msg The message to send.
     * @throws IOException If an error occurs while sending the message.
     */
    public void send(Message msg) throws IOException {
        msg.encode(socket);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

}
