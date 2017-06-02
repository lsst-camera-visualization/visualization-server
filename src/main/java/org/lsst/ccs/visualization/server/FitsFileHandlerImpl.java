package org.lsst.ccs.visualization.server;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.FitsUtil;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.header.Standard;
import nom.tam.util.BufferedFile;
import org.lsst.ccs.visualization.client.DataMessage;
import org.lsst.ccs.visualization.client.HeaderMessage;
import org.lsst.ccs.visualization.client.Message;
import org.lsst.ccs.visualization.client.StartMessage;

/**
 * This handler deals with opening, writing and closing fits files.
 *
 * @author tonyj
 */
class FitsFileHandlerImpl implements FitsFileHandler {

    private static final int MAX_CARDS_PER_HEADER = FitsFactory.FITS_BLOCK_SIZE / HeaderCard.FITS_HEADER_CARD_SIZE;

    private final BufferedFile bf;
    private final Header header;
    private final long dataPointer;
    private final File file;
    private final int nHeaders;
    private static final Logger logger = Logger.getLogger(FitsFileHandlerImpl.class.getName());

    static {
        FitsFactory.setUseHierarch(true);
    }

    FitsFileHandlerImpl(File dir, StartMessage start) throws IOException {
        try {
            file = new File(dir, start.getImageName() + ".fits");
            bf = new BufferedFile(file, "rw");
            BasicHDU primary = BasicHDU.getDummyHDU();
            header = primary.getHeader();
            header.setNaxis(1, start.getWidth());
            header.setNaxis(2, start.getHeight());
            primary.addValue(Standard.BITPIX, 32);
            primary.addValue(Standard.NAXIS, 2);
            header.write(bf);
            // Reserve space for extra headers
            nHeaders = start.getnHeaders();
            long filePointer = bf.getFilePointer();
            int extraBlocks
                    = (start.getnHeaders()+header.getNumberOfPhysicalCards() + (MAX_CARDS_PER_HEADER - 1)) / MAX_CARDS_PER_HEADER
                    - 1; // The block we already allocated.
            dataPointer = filePointer + extraBlocks * FitsFactory.FITS_BLOCK_SIZE;
            logger.log(Level.INFO,String.format("nHeaders=%d extraBlocks=%d physicalCards=%d MAX_CARDS_PER_HEADER=%d",nHeaders,extraBlocks,header.getNumberOfPhysicalCards(),MAX_CARDS_PER_HEADER));
            logger.log(Level.INFO,String.format("dataPointer=%d",dataPointer));

            // reserve space for data (necessary?)
            long imageSize = 4 * start.getWidth() * start.getHeight();
            bf.seek(dataPointer + imageSize);
            FitsUtil.pad(bf, imageSize);
            logger.log(Level.INFO, "Created {0} imagesize={1} nClients={2} nHeaders={3}", new Object[]{file, imageSize, start.getnClients(), start.getnHeaders()});
        } catch (FitsException fx) {
            throw new IOException("Fits error during IO", fx);
        }
    }

    @Override
    public void handle(Message msg, SocketChannel socket) throws IOException {
        switch (msg.getType()) {
            case DATA:
                DataMessage data = (DataMessage) msg;
                FileChannel channel = bf.getChannel();
                if (data.getStepLength() == 0) {
                    channel.transferFrom(socket, dataPointer + 4 * data.getOffset(), data.getDataLength());
                } else {
                    int stepLength = data.getStepLength();
                    int stepOffset = data.getStepOffset();
                    int limit = data.getDataLength();
                    long position = dataPointer + 4 * data.getOffset();
                    while (limit > 0) {
                        channel.transferFrom(socket, position, 4 * stepLength);
                        position += 4 * stepOffset;
                        limit -= 4 * stepLength;
                    }
                }
                break;

            case HEADER:
                HeaderMessage hdr = (HeaderMessage) msg;
                // FIXME: We do not want to have to synchronize here
                // Would be better to have reserved space for each channel and keep the items
                // ordered by channel
                synchronized (header) {
                    header.addLine(HeaderCard.create(hdr.getCard()));
                }
                break;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            bf.seek(0);
            // FIXME: We need to deal with case where we did not 
            // receive enough headers to fill the header block
            logger.log(Level.INFO,String.format("Received %d/%d headers",header.getNumberOfCards(),nHeaders));
            header.write(bf);
            logger.log(Level.INFO,String.format("File Pointer after writing headers %d", bf.getFilePointer()));            
            bf.close();
            logger.log(Level.INFO, "Closed {0}", file);
        } catch (FitsException fx) {
            throw new IOException("Fits error during IO", fx);
        }
    }

    @Override
    public File getFile() {
        return file;
    }
}
