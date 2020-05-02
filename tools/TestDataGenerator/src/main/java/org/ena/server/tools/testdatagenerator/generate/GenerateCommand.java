package org.ena.server.tools.testdatagenerator.generate;

import java.io.File;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@SuppressWarnings("unused")
@Command(name = "generate",
    description = "Generate test data.",
    version = "0.2",
    mixinStandardHelpOptions = true)
public class GenerateCommand implements Runnable {

  @Option(names = {"--hours"},
      description = ""
          + "Number of hours for which to generate exposure keys.\n\n"
          + "A daily digest file will be generated for every 24h chunk into the 'days' directory.\n"
          + "Leftover hours (n mod 24) will be generated into the 'hours' directory.\n\n"
          + "Example: A value of 181 will result in the generation of 7 day files and 13 hour files"
          + "(20 files total).",
      required = true)
  private int hours;

  @Option(names = {"--avg_exposures_per_hour"},
      description = "Number of average exposure keys to generate per hour"
          + "(or avg_exposures_per_hour * 24 per day respectively).\n"
          + "Exposures per hour are randomized by poisson distribution with"
          + "epsilon = avg_exposures_per_hour (rounded to the nearest value).",
      required = true)
  private int exposures_per_hour;

  @Option(names = {"--force_empty"},
      description = ""
          + "This will force the generation of at least one hourly file that will not contain any"
          + "exposure keys.\n"
          + "Requires '--hours' to be set to a value n for which n mod 24 >= 1.")
  private boolean force_empty;

  @Option(names = {"--out"},
      description = ""
          + "Target directory (will be wiped before data generation).\n"
          + "Will contain a 'days' and an 'hours' directory.",
      required = true)
  private File out_directory;

  @Option(names = {"--private_key"},
      description = ""
          + "The Ed25519 private key that shall be used for signing.\n"
          + "Supported format: PEM PKCS1/PKCS8",
      required = true)
  private File private_key_file;

  @Option(names = {"--certificate"},
      description = ""
          + "The Ed25519 certificate chain to attach to the payload.\n"
          + "Supported Format: X.509",
      required = true)
  private File certificate_file;

  @Override
  public void run() {
    try {
      Generator.generate(hours, exposures_per_hour, force_empty, out_directory, private_key_file,
          certificate_file);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
