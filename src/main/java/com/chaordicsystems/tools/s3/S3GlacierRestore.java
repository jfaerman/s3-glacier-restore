package com.chaordicsystems.tools.s3;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class S3GlacierRestore {

    private static final String PROGRAM_NAME = "s3-glacier-restore";
    private static final String RECURSIVE_OPT = "recursive";
    private static final String HELP_OPT = "help";

    public static void main(String[] args) throws IOException {
        CommandLine line = parse(args);
        String[] parsedArgs = line.getArgs();
        if (parsedArgs.length != 2 || line.hasOption(HELP_OPT)) {
            printHelpAndExit();
        }

        S3Object s3PathPrefix = S3Utils.parseS3Path(parsedArgs[0]);
        Integer expirationInDays = Integer.valueOf(parsedArgs[1]);

        List<S3Object> s3Objects = S3Utils.listObjects(s3PathPrefix);
        if (s3Objects.size() > 1 && !line.hasOption(RECURSIVE_OPT)) {
            System.out.printf("- FAILED: Found %d objects to restore.\n" +
            		          "- Please use --recursive (-r) option if you " +
            		          "wish to restore multiple files.\n", s3Objects.size());
            System.exit(1);
        }

        S3Utils.restoreObjects(s3Objects, expirationInDays);
    }

    private static void printHelpAndExit() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( PROGRAM_NAME + " [OPTS] <s3PathPrefix> <expirationInDays>", getOptions() );
        System.exit(1);
    }

    private static CommandLine parse(String[] args) {
        try {
            // create the parser
            CommandLineParser parser = new GnuParser();
            Options options = getOptions();
            // parse the command line arguments
            return parser.parse( options, args );
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            System.exit(1);
        }
        return null;
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption( "h", HELP_OPT, false, "Print help." );
        options.addOption( "r", RECURSIVE_OPT, false, "Recursively delete all objects with prefix matching <s3PathPrefix>." );
        return options;
    }
}