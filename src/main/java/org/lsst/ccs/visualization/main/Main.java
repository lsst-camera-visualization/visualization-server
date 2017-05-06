package org.lsst.ccs.visualization.main;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.OptionHandlerRegistry;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.lsst.ccs.visualization.server.ImageListener;
import org.lsst.ccs.visualization.server.VisualizationIngestServer;
import org.lsst.ccs.web.visualization.rest.Image;
import org.lsst.ccs.web.visualization.rest.ImageQueue;
import org.lsst.ccs.web.visualization.rest.RestServer;

/**
 * Class for running visualization server from the command line
 *
 * @author tonyj
 */
public class Main {

    private final VisualizationIngestServer defaultServer = new VisualizationIngestServer(9999, (new File(".")));

    @Option(name = "-ingestPort", usage = "Set the port used by the visualization ingest server")
    @SuppressWarnings("FieldMayBeFinal")
    private int ingestPort = 9999;

    @Option(name = "-restPort", usage = "Set the port used by the visualization restful interface")
    @SuppressWarnings("FieldMayBeFinal")
    private int restPort = 8888;

    @Option(name = "-dir", usage = "The directory where received files will be stored. Recommended to be a ramdisk")
    @SuppressWarnings("FieldMayBeFinal")
    private File dir = new File(".");

    @Option(name = "-startTimeout", usage = "The time after a start command that an idle connection will timeout")
    @SuppressWarnings("FieldMayBeFinal")
    private Duration startTimeout = defaultServer.getStartTimeout();

    @Option(name = "-idleTimeout", usage = "Time since last message that an idle connection is considered to have timed out")
    @SuppressWarnings("FieldMayBeFinal")
    private Duration activeTimeout = defaultServer.getActiveTimeout();

    @Option(name = "-startWait", usage = "The time we will wait for a start message after some other message has been received")
    @SuppressWarnings("FieldMayBeFinal")
    private Duration startWait = defaultServer.getStartWait();

    public static void main(String[] args) throws IOException {
        OptionHandlerRegistry.getRegistry().registerHandler(Duration.class, DurationOptionHandler.class);
        Main main = new Main();
        CmdLineParser parser = new CmdLineParser(main);
        try {
            parser.parseArgument(args);
            main.run();
        } catch (CmdLineException x) {
            System.err.println(x.getMessage());
            parser.printUsage(System.err);
        }
    }

    private void run() throws IOException {
        
        ImageQueue imageQueue = new ImageQueue();
        
        RestServer restServer = new RestServer();
        URI baseURI = URI.create("http://localhost:" + restPort + "/vis/");
        restServer.start(imageQueue, baseURI);
        
        VisualizationIngestServer server = new VisualizationIngestServer(ingestPort, dir);
        server.setStartWait(startWait);
        server.setActiveTimeout(activeTimeout);
        server.setStartTimeout(startTimeout);
        server.addImageListener((String name, long timeStamp, File file) -> {
            imageQueue.put(new Image(file.toURI(), name, timeStamp));
        });
        server.run();
    }

    public static class DurationOptionHandler extends OptionHandler<Duration> {

        public DurationOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Duration> setter) {
            super(parser, option, setter);
        }

        @Override
        public int parseArguments(Parameters prmtrs) throws CmdLineException {
            Duration.parse(prmtrs.getParameter(0));
            return 1;
        }

        @Override
        public String getDefaultMetaVariable() {
            return "duration";
        }

    }

}
