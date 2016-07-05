package org.lsst.ccs.visualization.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * Base class for all messages.
 *
 * @author tonyj
 */
public class Message {

    private final int headerLength;

    /**
     * The message type. Each superclass must have a message type defined here.
     */
    public enum MessageType {
        /**
         * The type for the {@link StartMessage}
         */
        START,
        /**
         * The type for the {@link EndMessage}
         */
        END,
        /**
         * The type for the {@link DataMessage}
         */
        DATA,
        /**
         * The type for the {@link HeaderMessage}
         */
        HEADER
    }
    private final MessageType type;
    private final String imageName;

    /**
     * Constructor used by super classes.
     *
     * @param type The message type.
     * @param length The length in bytes of any additional data sent by the
     * super class as part of the message header using the
     * {@link #encodeAdditional()} method.
     * @param imageName The name of the image which this message is associated
     * with.
     */
    Message(MessageType type, int length, String imageName) {
        this.type = type;
        this.imageName = imageName;
        this.headerLength = length + 2 + imageName.length();
    }

    /**
     * Get the image name.
     *
     * @return The image name.
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * Get the message type.
     *
     * @return The message type.
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Called to encode a message for sending on a socket.
     *
     * @param socket The socket on which the message should be sent.
     * @throws IOException If an error occurs while transferring the message.
     */
    void encode(SocketChannel socket) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(headerLength + 4);
        bb.putInt(headerLength);
        bb.put((byte) type.ordinal());
        bb.put((byte) imageName.length());
        bb.put(imageName.getBytes(StandardCharsets.US_ASCII));
        encodeAdditional(bb);
        bb.flip();
        socket.write(bb);
    }

    /**
     * Allows subclasses to write additional data which will come after the
     * header provided by the Message class itself.
     *
     * @param bb The ByteBuffer where additional data should be written. The
     * ByteBuffer will have been allocated to have space requested in the
     * constructor.
     */
    void encodeAdditional(ByteBuffer bb) {
        // NOOP by default
    }

    /**
     * Decode a byte buffer and return the corresponding message.
     *
     * @param bb The byte buffer containing the encoded message.
     * @return The decoded message.
     * @throws IOException If the message cannot be decoded.
     */
    public static Message decode(ByteBuffer bb) throws IOException {
        int len = bb.getInt();
        MessageType type = MessageType.values()[bb.get()];
        int nameLength = bb.get();
        byte[] bytes = new byte[nameLength];
        bb.get(bytes);
        String name = new String(bytes, StandardCharsets.US_ASCII);
        switch (type) {
            case START:
                return StartMessage.decode(bb, name);
            case END:
                return EndMessage.decode(bb, name);
            case DATA:
                return DataMessage.decode(bb, name);
            case HEADER:
                return HeaderMessage.decode(bb, name);
            default:
                throw new IOException("Unknown message type" + type);
        }
    }
}
