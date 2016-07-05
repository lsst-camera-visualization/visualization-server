package org.lsst.ccs.visualization.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Used to send pixel data to the ingest server.
 * @author tonyj
 */
public class DataMessage extends Message {

    private final int offset;
    private final int stepLength;
    private final int stepOffset;
    private final int dataLength;
    private ByteBuffer data;

    /**
     * Create a new DataMessage containing pixel data.
     * @param imageName The imageName as specified in the {@link StartMessage}
     * @param offset The offset (in pixels) within the full image at which this data should be placed.
     * @param stepLength The length (in pixels) of each line of data.
     * @param stepOffset The number of pixels to be added to the offset for each new line of data.
     * @param data The pixel data. Must be encoded as 4-byte per pixel.
     */
    public DataMessage(String imageName, int offset, int stepLength, int stepOffset, ByteBuffer data) {
        this(imageName, offset, stepLength, stepOffset, data.remaining());
        this.data = data;
    }

    private DataMessage(String imageName, int offset, int stepLength, int stepOffset, int dataLength) {
        super(Message.MessageType.DATA, 16, imageName);
        this.offset = offset;
        this.stepLength = stepLength;
        this.stepOffset = stepOffset;
        this.dataLength = dataLength;
    }

    public int getOffset() {
        return offset;
    }

    public int getStepLength() {
        return stepLength;
    }

    public int getStepOffset() {
        return stepOffset;
    }

    public int getDataLength() {
        return dataLength;
    }

    @Override
    void encodeAdditional(ByteBuffer bb) {
        bb.putInt(offset);
        bb.putInt(stepLength);
        bb.putInt(stepOffset);
        bb.putInt(dataLength);
    }

    @Override
    void encode(SocketChannel out) throws IOException {
        super.encode(out);
        out.write(data);
    }

    static Message decode(ByteBuffer bb, CharSequence name) {
        int offset = bb.getInt();
        int stepLength = bb.getInt();
        int stepOffset = bb.getInt();
        int dataLength = bb.getInt();
        return new DataMessage(name.toString(), offset, stepLength, stepOffset, dataLength);
    }

    @Override
    public String toString() {
        return "DataMessage{" + "imageName=" + getImageName() + ", offset=" + offset + ", stepLength=" + stepLength + ", stepOffset=" + stepOffset + ", dataLength=" + dataLength + '}';
    }

}
