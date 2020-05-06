package app.coronawarn.server.tools.testdatagenerator.generate;

import java.io.File;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@SuppressWarnings("unused")
@Command(name = "generate",
    description = "Generate test data according to the API spec.",
    version = "0.2",
    mixinStandardHelpOptions = true)
public class GenerateCommand implements Runnable {

  @Option(names = {"--hours"},
      description = ""
          + "Number of hours for which to generate exposure keys, starting at 00:00 on the "
          + "date defined by --start_date. A daily digest file will be generated for every "
          + "complete 24h chunk. Leftover hours (n mod 24) will be generated into the 'hour' "
          + "directory of the last day, but no daily digest file will be generated.\n\n"
          + "Example: A value of 181 will result in the generation of 181 hourly digest files "
          + "(24 for each of the seven days plus 13 leftover for the last day) and 7 daily digest "
          + "files (188 files total).",
      required = true)
  private int hours;

  @Option(names = {"--start_date"},
      description = ""
          + "Date on which to start generating hourly and daily files. Generation will start at "
          + "00:00 on the start date and proceed forwards in time from there on. "
          + "Format: ISO-8601\n\n"
          + "Example: 2020-05-01",
      required = true)
  private String start_date;

  @Option(names = {"--avg_exposures_per_hour"},
      description = "Number of average exposure keys to generate per hour"
          + "(or avg_exposures_per_hour * 24 per day respectively).\n"
          + "Exposures per hour are randomized by poisson distribution with"
          + "epsilon = avg_exposures_per_hour (rounded to the nearest value).\n\n"
          + "Example: 300",
      required = true)
  private int exposures_per_hour;

  /*
  // TODO Also one full empty day?
  @Option(names = {"--force_empty"},
      description = ""
          + "This will force the generation of at least one hourly file per day that will not "
          + "contain any exposure keys.\n"
          + "Requires '--hours' to be set to a value n for which n mod 24 >= 1.")
  private boolean force_empty;
  */

  @Option(names = {"--openapi"},
      description = ""
          + "Optional: An OpenAPI definition file to include.\n\n"
          + "Example: ../../services/Download/api_v1.json")
  private File openapi;

  @Option(names = {"--out"},
      description = ""
          + "Target directory (will be wiped before data generation).\n"
          + "Will contain directories that match the REST API definition "
          + "of the Download service.\n\n"
          + "Example: ./out",
      required = true)
  private File out_directory;

  @Option(names = {"--private_key"},
      description = ""
          + "The Ed25519 private key that shall be used for signing.\n"
          + "Supported format: PEM PKCS1/PKCS8\n\n"
          + "Example: ./certificates/client/private.pem",
      required = true)
  private File private_key_file;

  @Option(names = {"--certificate"},
      description = ""
          + "The Ed25519 certificate chain to attach to the payload.\n"
          + "Supported Format: X.509\n\n"
          + "Example: ./certificates/chain/certificate.crt",
      required = true)
  private File certificate_file;

  @Option(names = {"--seed"},
      description = ""
          + "A seed for random data generation.\n\n"
          + "Example: 123456 \n\n"
          + "Default: 123456")
  private int seed = 123456;

  @Override
  public void run() {
    try {
      Generator.generate(hours, start_date, exposures_per_hour, openapi, out_directory,
          private_key_file, certificate_file, seed);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
