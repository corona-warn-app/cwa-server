package org.ena.server.tools.testdatagenerator;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ena.server.tools.testdatagenerator.generate.GenerateCommand;
import org.ena.server.tools.testdatagenerator.verify.VerifyCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "testDataGenerator",
    subcommands = {GenerateCommand.class, VerifyCommand.class},
    synopsisSubcommandLabel = "(generate | verify)",
    version = "0.2",
    mixinStandardHelpOptions = true,
    description = "Generates and verifies example exposure keys.")
public class TestDataGeneratorCLI implements Runnable {

  public static void main(String... args) {
    Security.addProvider(new BouncyCastleProvider());
    new CommandLine(new TestDataGeneratorCLI()).execute(args);
  }

  public void run() {
    new CommandLine(this).usage(System.err);
  }
}

