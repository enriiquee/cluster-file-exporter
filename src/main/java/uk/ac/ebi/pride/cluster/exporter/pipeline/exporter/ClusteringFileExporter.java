package uk.ac.ebi.pride.cluster.exporter.pipeline.exporter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.pride.cluster.exporter.pipeline.model.Specie;
import uk.ac.ebi.pride.cluster.exporter.pipeline.services.ClusterRepositoryServices;
import uk.ac.ebi.pride.cluster.exporter.pipeline.utils.PropertyUtils;
import uk.ac.ebi.pride.cluster.exporter.pipeline.utils.SummaryFactory;

import uk.ac.ebi.pride.spectracluster.repo.dao.cluster.IClusterReadDao;

import uk.ac.ebi.pride.spectracluster.repo.model.ClusterQuality;


import java.io.*;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * @author Yasset Perez-Riverol
 * @version $Id$
 */
public class ClusteringFileExporter {

    private static final Logger logger = LoggerFactory.getLogger(ClusteringFileExporter.class);

    public static void main(String[] args) {

        try {
            ApplicationContext ctx = new ClassPathXmlApplicationContext("spring/app-context.xml");
            IClusterReadDao clusterReadDao = (IClusterReadDao) ctx.getBean("clusterReaderDao");

            Map<String, Specie> specieMap = PropertyUtils.loadSpeciesPropertyFile("prop/species_metadata.conf");

            Properties properties         = PropertyUtils.loadProperties("prop/prop.properties");

            CommandLineParser parser = new GnuParser();

            CommandLine commandLine = parser.parse(CliOptions.getOptions(), args);

            // HELP
            if (commandLine.hasOption(CliOptions.OPTIONS.HELP.getValue())) {
                printUsage();
                return;
            }

            // OUTPUT
            File file;
            String version;
            if (commandLine.hasOption(CliOptions.OPTIONS.FILE.getValue()) && commandLine.hasOption(CliOptions.OPTIONS.VERSION.getValue())){
                file = new File(commandLine.getOptionValue(CliOptions.OPTIONS.FILE.getValue()));
                version = commandLine.getOptionValue(CliOptions.OPTIONS.VERSION.getValue());
            }
            else
                throw new Exception("Missing required parameter '" + CliOptions.OPTIONS.FILE.getValue() + " or " + CliOptions.OPTIONS.VERSION.getValue() + "'");

            ClusterQuality quality = ClusterQuality.HIGH;

            if(commandLine.hasOption(CliOptions.OPTIONS.QUALITY.getValue())){
                 quality = parseClusterQuality(commandLine.getOptionValue(CliOptions.OPTIONS.QUALITY.getValue()));
            }

            if (!file.exists())
                logger.info("Output .tsv file must be will be re-write with new data");

            writeClusteringResultFile(clusterReadDao, quality, file.getAbsolutePath(), properties, specieMap, version);

        } catch (Exception e) {
            logger.error("Error while running cluster importer", e);
            System.exit(1);
        }
    }

    /**
     * Parse the cluster quality Options
     * @param optionValue number representation of cluster quality
     * @return ClusterQuality
     */
    private static ClusterQuality parseClusterQuality(String optionValue) {
        if(optionValue.equalsIgnoreCase("0"))
            return ClusterQuality.LOW;
        if(optionValue.equalsIgnoreCase("1"))
            return ClusterQuality.MEDIUM;
        if(optionValue.equalsIgnoreCase("2"))
            return ClusterQuality.HIGH;
        return ClusterQuality.HIGH;
    }

    /**
     * This function allows to write all the output files for the cluster release in the present path.
     *
     * @param clusterReaderDao The cluster instance
     * @param path The path to write all the output files
     * @param properties The property files contains all metadata that should be provided in the file
     * @param species    The species that PRIDE CLuster will export at the very begining
     * @throws Exception
     */
    private static void writeClusteringResultFile(IClusterReadDao clusterReaderDao, ClusterQuality quality,
                                                  String path, Properties properties,
                                                  Map<String, Specie> species,
                                                  String version) throws Exception {

        logger.info("Loading clustering file: {}", clusterReaderDao.toString());

        ClusterRepositoryServices service = new ClusterRepositoryServices(clusterReaderDao);

        service.buildPeptidePSMReportLists(quality);

        logger.info("Number of HighQuality Clusters: ");

        SummaryFactory.printFile(service, null, path, properties, version);

        for(Specie specie: species.values()){
            SummaryFactory.printFile(service, specie, path, properties, version);
        }

        logger.info("All projects where exported!!!!");
    }

    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("PRIDE Cluster - Cluster importer", "Imports cluster results into the PRIDE Cluster database.\n", CliOptions.getOptions(), "\n\n", true);
    }

}
