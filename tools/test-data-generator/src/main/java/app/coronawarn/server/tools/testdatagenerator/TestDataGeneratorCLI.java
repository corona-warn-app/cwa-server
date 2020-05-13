package app.coronawarn.server.tools.testdatagenerator;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "testDataGenerator",
    subcommands = {GenerateCommand.class},
    synopsisSubcommandLabel = "(generate)",
    version = "0.2",
    mixinStandardHelpOptions = true,
    description = "Generates example exposure keys.")
public class TestDataGeneratorCLI implements Runnable {

  public static void main(String... args) {
    Security.addProvider(new BouncyCastleProvider());
    new CommandLine(new TestDataGeneratorCLI()).execute(args);
  }

  public void run() {
    new CommandLine(this).usage(System.err);
  }
}

