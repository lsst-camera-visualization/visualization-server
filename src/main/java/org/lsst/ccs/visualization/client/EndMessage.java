package org.lsst.ccs.visualization.client;

import java.nio.ByteBuffer;


/**
 * Message sent to indicate that image sending on each client is complete.
 * @author tonyj
 */
public class EndMessage extends Message {

    /** 
     * Create an EndMessage for the specified image.
     * @param imageName The imageName as specified in the {@link StartMessage}
     */
    public EndMessage(String imageName) {
        super(Message.MessageType.END, 0, imageName);
    }

    static Message decode(ByteBuffer bb, CharSequence name) {
        return new EndMessage(name.toString());
    }

    @Override
    public String toString() {
        return "EndMessage{" + "imageName=" + getImageName() + '}';
    }
    
}
