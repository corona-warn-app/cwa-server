package org.ena.server.tools.testdatagenerator.verify;

import java.io.File;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@SuppressWarnings("unused")
@Command(name = "verify",
    description = "Verify test data",
    version = "0.2",
    mixinStandardHelpOptions = true)
public class VerifyCommand implements Runnable {

  @Option(names = {"--in"},
      description = "Directory of files to be verified.",
      required = true)
  private File in_directory;

  @Option(names = {"--certificate"},
      description = "The Ed25519 certificate chain to use for verification. Supported Format: X.509",
      required = true)
  private File certificate_file;

  @Override
  public void run() {
    try {
      Verifier.verify(in_directory, certificate_file);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
