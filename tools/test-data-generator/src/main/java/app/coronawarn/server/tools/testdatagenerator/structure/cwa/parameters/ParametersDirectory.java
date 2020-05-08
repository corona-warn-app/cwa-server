package app.coronawarn.server.tools.testdatagenerator.structure.cwa.parameters;

import app.coronawarn.server.common.protocols.internal.RiskLevel;
import app.coronawarn.server.common.protocols.internal.RiskScoreParameters;
import app.coronawarn.server.common.protocols.internal.RiskScoreParameters.AttenuationRiskParameters;
import app.coronawarn.server.common.protocols.internal.RiskScoreParameters.DaysSinceLastExposureRiskParameters;
import app.coronawarn.server.common.protocols.internal.RiskScoreParameters.DurationRiskParameters;
import app.coronawarn.server.common.protocols.internal.RiskScoreParameters.TransmissionRiskParameters;
import app.coronawarn.server.tools.testdatagenerator.structure.Directory;
import app.coronawarn.server.tools.testdatagenerator.structure.IndexDirectory;
import app.coronawarn.server.tools.testdatagenerator.structure.SigningDirectory;
import app.coronawarn.server.tools.testdatagenerator.util.Crypto;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ParametersDirectory extends Directory implements SigningDirectory {

  final Crypto crypto;

  public ParametersDirectory(String region, Crypto crypto) {
    super("parameters");
    this.crypto = crypto;
    this.addDirectory(new IndexDirectory<>("country", __ -> List.of(region))
        .addFileToAll("index", __ -> generateParameters().toByteArray())
    );
  }

  private static RiskScoreParameters generateParameters() {
    return RiskScoreParameters.newBuilder()
        .setAttenuation(AttenuationRiskParameters.newBuilder()
            .setGt73Dbm(RiskLevel.RISK_LEVEL_LOWEST)
            .setGt63Le73Dbm(RiskLevel.RISK_LEVEL_LOW)
            .setGt51Le63Dbm(RiskLevel.RISK_LEVEL_LOW_MEDIUM)
            .setGt33Le51Dbm(RiskLevel.RISK_LEVEL_MEDIUM)
            .setGt27Le33Dbm(RiskLevel.RISK_LEVEL_MEDIUM_HIGH)
            .setGt10Le15Dbm(RiskLevel.RISK_LEVEL_HIGH)
            .setGt10Le15Dbm(RiskLevel.RISK_LEVEL_VERY_HIGH)
            .setLt10Dbm(RiskLevel.RISK_LEVEL_HIGHEST)
            .build())
        .setDaysSinceLastExposure(DaysSinceLastExposureRiskParameters.newBuilder()
            .setGe14Days(RiskLevel.RISK_LEVEL_LOWEST)
            .setGe12Lt14Days(RiskLevel.RISK_LEVEL_LOW)
            .setGe10Lt12Days(RiskLevel.RISK_LEVEL_LOW_MEDIUM)
            .setGe8Lt10Days(RiskLevel.RISK_LEVEL_MEDIUM)
            .setGe6Lt8Days(RiskLevel.RISK_LEVEL_MEDIUM_HIGH)
            .setGe4Lt6Days(RiskLevel.RISK_LEVEL_HIGH)
            .setGe2Lt4Days(RiskLevel.RISK_LEVEL_VERY_HIGH)
            .setGe0Lt2Days(RiskLevel.RISK_LEVEL_HIGHEST)
            .build())
        .setDuration(DurationRiskParameters.newBuilder()
            .setEq0Min(RiskLevel.RISK_LEVEL_LOWEST)
            .setGt0Le5Min(RiskLevel.RISK_LEVEL_LOW)
            .setGt5Le10Min(RiskLevel.RISK_LEVEL_LOW_MEDIUM)
            .setGt10Le15Min(RiskLevel.RISK_LEVEL_MEDIUM)
            .setGt15Le20Min(RiskLevel.RISK_LEVEL_MEDIUM_HIGH)
            .setGt20Le25Min(RiskLevel.RISK_LEVEL_HIGH)
            .setGt25Le30Min(RiskLevel.RISK_LEVEL_VERY_HIGH)
            .setGt30Min(RiskLevel.RISK_LEVEL_HIGHEST)
            .build())
        .setTransmission(TransmissionRiskParameters.newBuilder()
            .setAppDefined1(RiskLevel.RISK_LEVEL_LOWEST)
            .setAppDefined2(RiskLevel.RISK_LEVEL_LOW)
            .setAppDefined3(RiskLevel.RISK_LEVEL_LOW_MEDIUM)
            .setAppDefined4(RiskLevel.RISK_LEVEL_MEDIUM)
            .setAppDefined5(RiskLevel.RISK_LEVEL_MEDIUM_HIGH)
            .setAppDefined6(RiskLevel.RISK_LEVEL_HIGH)
            .setAppDefined7(RiskLevel.RISK_LEVEL_VERY_HIGH)
            .setAppDefined8(RiskLevel.RISK_LEVEL_HIGHEST)
            .build())
        .build();
  }

  @Override
  public void sign() {
    Arrays.stream(Objects.requireNonNull(this.getFile().listFiles()))
        .map(countryDirectory -> Arrays.stream(Objects.requireNonNull(countryDirectory.listFiles()))
            .filter(File::isDirectory)
            .findFirst().orElseThrow())
        .forEach(file -> this.signFiles(file, this.crypto));
  }
}
