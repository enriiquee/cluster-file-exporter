package uk.ac.ebi.pride.cluster.exporter.pipeline.exporter;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;


@SuppressWarnings("static-access")
public class CliOptions {

    public enum OPTIONS {
        // VARIABLES
        FILE("out"),

        VERSION("version"),

        QUALITY("quality"),

        // ACTIONS
        HELP("help");

        private String value;

        OPTIONS(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static final Options options = new Options();

    static {
        // VARIABLES
        Option outPath = OptionBuilder
                .hasArg()
                .withDescription("The file path to export the cluster results.")
                .create(OPTIONS.FILE.getValue());
        options.addOption(outPath);

        Option version = OptionBuilder
                .hasArg()
                .withDescription("The version of PRIDE Cluster to be included in the result files.")
                .create(OPTIONS.VERSION.getValue());
        options.addOption(version);

        Option quality = OptionBuilder
                .hasArg()
                .withDescription("Quality of the clusters to be exported:\n" +
                        "\t 0 : for all clusters low-quality to high-quality\n" +
                        "\t 1 : for all cluster from medium-quality to high-quality\n" +
                        "\t 2 : for all high-quality cluster\n")
                .create(OPTIONS.QUALITY.getValue());
        options.addOption(quality);

        // ACTIONS
        Option help = new Option(
                OPTIONS.HELP.toString(),
                "print this message.");
        options.addOption(help);

    }

    public static Options getOptions() {
        return options;
    }
}
