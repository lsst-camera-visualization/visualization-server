package org.lsst.ccs.visualization.server;

import java.io.File;
import java.time.Duration;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.OptionHandlerRegistry;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * Class for running visualization server from the command line
 *
 * @author tonyj
 */
public class Main {

    private final FitsFileManager defaultFitsFileManager = new FitsFileManager(new File("."));
    
    @Option(name = "-port", usage = "Set the port used by the visualization server")
    @SuppressWarnings("FieldMayBeFinal")
    private int port = 9999;

    @Option(name = "-dir", usage = "The directory where received files will be stored. Recommended to be a ramdisk")
    @SuppressWarnings("FieldMayBeFinal")
    private File dir = new File(".");

    @Option(name = "-startTimeout", usage = "The time after a start command that an idle connection will timeout")
    @SuppressWarnings("FieldMayBeFinal")
    private Duration startTimeout = defaultFitsFileManager.getStartTimeout();
    
    @Option(name = "-idleTimeout", usage = "Time since last message that an idle connection is considered to have timed out")
    @SuppressWarnings("FieldMayBeFinal")
    private Duration activeTimeout = defaultFitsFileManager.getActiveTimeout();

    @Option(name = "-startWait", usage = "The time we will wait for a start message after some other message has been received")
    @SuppressWarnings("FieldMayBeFinal")
    private Duration startWait = defaultFitsFileManager.getStartWait();
    
    public static void main(String[] args) {
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

    private void run() {
        VisualizationIngestServer server = new VisualizationIngestServer(port, dir);
        server.setStartWait(startWait);
        server.setActiveTimeout(activeTimeout);
        server.setStartTimeout(startTimeout);
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
