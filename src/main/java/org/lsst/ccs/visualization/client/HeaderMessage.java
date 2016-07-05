package org.lsst.ccs.visualization.client;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Used to send FITS headers to be added to the image built by the ingest server.
 * @author tonyj
 */
public class HeaderMessage extends Message {

    private final String card;

    /**
     * Create a HeaderMessage.
     * @param imageName The imageName as specified in the {@link StartMessage}
     * @param card The card image (maximum 80 characters).
     */
    public HeaderMessage(String imageName, String card) {
        super(MessageType.HEADER, card.length(), imageName);
        this.card = card;
    }

    @Override
    void encodeAdditional(ByteBuffer bb) {
        bb.put(card.getBytes(StandardCharsets.US_ASCII));
    }


    static Message decode(ByteBuffer bb, CharSequence name) {
        byte[] bytes = new byte[bb.remaining()];
        bb.get(bytes);
        String card = new String(bytes, StandardCharsets.US_ASCII);
        return new HeaderMessage(name.toString(), card);
    }

    public String getCard() {
        return card;
    }

    @Override
    public String toString() {
        return "HeaderMessage{" + "imageName=" + getImageName() + ", card=" + card + '}';
    }

}
