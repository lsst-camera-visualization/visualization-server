package org.lsst.ccs.visualization.server;

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

    @Option(name = "-port", usage = "Set the port used by the visualization server")
    private int port;

    @Option(name = "-startTimeout", usage = "The time after a start command that an idle connection will timeout")
    private Duration startTimeout;

    public static void main(String[] args) {
        OptionHandlerRegistry.getRegistry().registerHandler(Duration.class, DurationOptionHandler.class);
        Main main = new Main();
        CmdLineParser parser = new CmdLineParser(main);
        try {
            parser.parseArgument(args);
            parser.printUsage(System.err);

        } catch (CmdLineException x) {
            System.err.println(x.getMessage());
            parser.printUsage(System.err);
        }
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
