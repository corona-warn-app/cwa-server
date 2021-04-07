package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.protocols.internal.pt.QRCodePosterTemplateAndroid;
import app.coronawarn.server.common.protocols.internal.pt.QRCodePosterTemplateAndroid.QRCodeTextBoxAndroid;
import app.coronawarn.server.common.protocols.internal.pt.QRCodePosterTemplateIOS;
import app.coronawarn.server.common.protocols.internal.pt.QRCodePosterTemplateIOS.QRCodeTextBoxIOS;
import app.coronawarn.server.services.distribution.assembly.qrcode.QrCodeTemplateLoader;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.QrCodePosterTemplate;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.QrCodePosterTemplate.DescriptionTextBox;
import java.util.function.Function;
import org.springframework.stereotype.Component;

/**
 * Provide the export files that carry information about the QR Code Poster Template used by clients for event
 * registration.
 */
@Component
public class QrCodePosterTemplateStructureProvider {

  private final DistributionServiceConfig distributionServiceConfig;
  private final QrCodeTemplateLoader qrTemplateLoader;

  /**
   * Create an instance.
   */
  public QrCodePosterTemplateStructureProvider(DistributionServiceConfig distributionServiceConfig,
      QrCodeTemplateLoader qrTemplateLoader) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.qrTemplateLoader = qrTemplateLoader;
  }

  /**
   * Returns the publishable archive associated with the QR code poster template for Android mobile clients.
   */
  public WritableOnDisk getQrCodeTemplateForAndroid() {
    return constructArchiveToPublish(distributionServiceConfig.getAndroidQrCodePosterTemplate(),
        this::buildAndroidProtoStructure,
        distributionServiceConfig.getAndroidQrCodePosterTemplate().getPublishedArchiveName());
  }

  /**
   * Returns the publishable archive associated with the QR code poster template for IOS mobile clients.
   */
  public WritableOnDisk getQrCodeTemplateForIos() {
    return constructArchiveToPublish(distributionServiceConfig.getIosQrCodePosterTemplate(),
        this::buildIosProtoStructure,
        distributionServiceConfig.getIosQrCodePosterTemplate().getPublishedArchiveName());
  }

  private <T extends com.google.protobuf.GeneratedMessageV3> WritableOnDisk constructArchiveToPublish(
      QrCodePosterTemplate qrTemplateConfig, Function<QrCodePosterTemplate, T> protoBuilderFunction,
      String archiveName) {
    T templateProto = protoBuilderFunction.apply(qrTemplateConfig);
    ArchiveOnDisk archiveToPublish = new ArchiveOnDisk(archiveName);
    archiveToPublish.addWritable(new FileOnDisk("export.bin", templateProto.toByteArray()));
    return archiveToPublish;
  }

  private QRCodePosterTemplateAndroid buildAndroidProtoStructure(
      QrCodePosterTemplate templateConfig) {
    DescriptionTextBox textBoxConfig = templateConfig.getDescriptionTextBox();
    return QRCodePosterTemplateAndroid.newBuilder().setOffsetX(templateConfig.getOffsetX().floatValue())
        .setOffsetY(templateConfig.getOffsetY().floatValue())
        .setTemplate(qrTemplateLoader.loadAndroidTemplateAsBytes())
        .setQrCodeSideLength(templateConfig.getQrCodeSideLength())
        .setDescriptionTextBox(QRCodeTextBoxAndroid.newBuilder()
            .setOffsetX(textBoxConfig.getOffsetX().intValue()).setOffsetY(textBoxConfig.getOffsetY().intValue())
            .setFontSize(textBoxConfig.getFontSize()).setHeight(textBoxConfig.getHeight())
            .setFontColor(textBoxConfig.getFontColor()).build())
        .build();
  }

  private QRCodePosterTemplateIOS buildIosProtoStructure(QrCodePosterTemplate templateConfig) {
    DescriptionTextBox textBoxConfig = templateConfig.getDescriptionTextBox();
    return QRCodePosterTemplateIOS.newBuilder().setOffsetX(templateConfig.getOffsetX().intValue())
        .setOffsetY(templateConfig.getOffsetY().intValue())
        .setTemplate(qrTemplateLoader.loadIosTemplateAsBytes())
        .setQrCodeSideLength(templateConfig.getQrCodeSideLength())
        .setDescriptionTextBox(QRCodeTextBoxIOS.newBuilder().setOffsetX(textBoxConfig.getOffsetX().intValue())
            .setOffsetY(textBoxConfig.getOffsetY().intValue()).setFontSize(textBoxConfig.getFontSize())
            .setHeight(textBoxConfig.getHeight()).setFontColor(textBoxConfig.getFontColor())
            .build())
        .build();
  }
}
