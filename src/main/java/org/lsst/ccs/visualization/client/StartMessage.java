package org.lsst.ccs.visualization.client;

import java.nio.ByteBuffer;

/**
 * Message sent to indicate a new image is going to be sent to the server.
 * @author tonyj
 */
public class StartMessage extends Message {

    private final int width;
    private final int height;
    private final int nHeaders;
    private final int nClients;

    /**
     * Create a new StartMessage
     * @param imageName The image name. This will be used in all subsequent messages concerning this image.
     * @param width The total width (including any gaps) of the image which will be sent.
     * @param height The total height (including any gaps) of the image which will be sent.
     * @param nHeaders The number of {@link HeaderMessage}s which will be sent with this image. Used to allocate space 
     * on the server. Sending more headers will result in an error, but overestimating the number of headers is OK.
     * @param nClients The number of IngestClients which will send the actual data. Each IngestClient must send an {@link EndMessage} when
     * it is done. The server will use the number of EndMessages received to tell when the image is complete, so this number must be accurate.
     */
    public StartMessage(String imageName, int width, int height, int nHeaders, int nClients) {
        super(Message.MessageType.START, 16, imageName);
        this.width = width;
        this.height = height;
        this.nHeaders = nHeaders;
        this.nClients = nClients;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getnHeaders() {
        return nHeaders;
    }

    public int getnClients() {
        return nClients;
    }

    @Override
    void encodeAdditional(ByteBuffer bb) {
        bb.putInt(width);
        bb.putInt(height);
        bb.putInt(nHeaders);
        bb.putInt(nClients);
    }
    
    static Message decode(ByteBuffer bb, CharSequence name) {
        int width = bb.getInt();
        int height = bb.getInt();
        int nHeaders = bb.getInt();
        int nClients = bb.getInt();
        return new StartMessage(name.toString(), width, height, nHeaders, nClients);
    }

    @Override
    public String toString() {
        return "StartMessage{" + "imageName=" + getImageName() + ", width=" + width + ", height=" + height + ", nHeaders=" + nHeaders + ", nClients=" + nClients+ '}';
    }

}
