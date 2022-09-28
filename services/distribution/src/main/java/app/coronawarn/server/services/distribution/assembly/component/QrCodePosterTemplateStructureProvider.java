package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.protocols.internal.pt.QRCodePosterTemplateAndroid;
import app.coronawarn.server.common.protocols.internal.pt.QRCodePosterTemplateAndroid.QRCodeTextBoxAndroid;
import app.coronawarn.server.common.protocols.internal.pt.QRCodePosterTemplateIOS;
import app.coronawarn.server.common.protocols.internal.pt.QRCodePosterTemplateIOS.QRCodeTextBoxIOS;
import app.coronawarn.server.services.distribution.assembly.qrcode.QrCodeTemplateLoader;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.QrCodePosterTemplate;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.QrCodePosterTemplate.DescriptionTextBox;
import java.util.function.Function;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Provide the export files that carry information about the QR Code Poster Template used by clients for event
 * registration.
 */
@Component
@Profile("!revocation")
public class QrCodePosterTemplateStructureProvider {

  private final DistributionServiceConfig distributionServiceConfig;
  private final CryptoProvider cryptoProvider;
  private final QrCodeTemplateLoader qrTemplateLoader;

  /**
   * Create an instance.
   */
  public QrCodePosterTemplateStructureProvider(DistributionServiceConfig distributionServiceConfig,
      CryptoProvider cryptoProvider, QrCodeTemplateLoader qrTemplateLoader) {
    this.distributionServiceConfig = distributionServiceConfig;
    this.cryptoProvider = cryptoProvider;
    this.qrTemplateLoader = qrTemplateLoader;
  }

  /**
   * Returns the publishable archive associated with the QR code poster template for Android mobile clients.
   */
  public Writable<WritableOnDisk> getQrCodeTemplateForAndroid() {
    return constructArchiveToPublish(distributionServiceConfig.getAndroidQrCodePosterTemplate(),
        this::buildAndroidProtoStructure,
        distributionServiceConfig.getAndroidQrCodePosterTemplate().getPublishedArchiveName());
  }

  /**
   * Returns the publishable archive associated with the QR code poster template for IOS mobile clients.
   */
  public Writable<WritableOnDisk> getQrCodeTemplateForIos() {
    return constructArchiveToPublish(distributionServiceConfig.getIosQrCodePosterTemplate(),
        this::buildIosProtoStructure,
        distributionServiceConfig.getIosQrCodePosterTemplate().getPublishedArchiveName());
  }

  private <T extends com.google.protobuf.GeneratedMessageV3> Writable<WritableOnDisk> constructArchiveToPublish(
      QrCodePosterTemplate qrTemplateConfig, Function<QrCodePosterTemplate, T> protoBuilderFunction,
      String archiveName) {
    T templateProto = protoBuilderFunction.apply(qrTemplateConfig);
    ArchiveOnDisk archiveToPublish = new ArchiveOnDisk(archiveName);
    archiveToPublish.addWritable(new FileOnDisk("export.bin", templateProto.toByteArray()));
    return new DistributionArchiveSigningDecorator(archiveToPublish, cryptoProvider, distributionServiceConfig);
  }

  private QRCodePosterTemplateAndroid buildAndroidProtoStructure(
      QrCodePosterTemplate templateConfig) {
    DescriptionTextBox textBoxConfig = templateConfig.getDescriptionTextBox();
    return QRCodePosterTemplateAndroid.newBuilder().setOffsetX(templateConfig.getOffsetX().floatValue())
        .setOffsetY(templateConfig.getOffsetY().floatValue())
        .setTemplate(qrTemplateLoader.loadAndroidTemplateAsBytes())
        .setQrCodeSideLength(templateConfig.getQrCodeSideLength())
        .setDescriptionTextBox(QRCodeTextBoxAndroid.newBuilder()
            .setOffsetX(textBoxConfig.getOffsetX().floatValue()).setOffsetY(textBoxConfig.getOffsetY().floatValue())
            .setWidth(textBoxConfig.getWidth())
            .setFontSize(textBoxConfig.getFontSize()).setHeight(textBoxConfig.getHeight())
            .setFontColor(textBoxConfig.getFontColor()).build())
        .build();
  }

  private QRCodePosterTemplateIOS buildIosProtoStructure(QrCodePosterTemplate templateConfig) {
    DescriptionTextBox textBoxConfig = templateConfig.getDescriptionTextBox();
    DescriptionTextBox textBoxIosConfig = templateConfig.getAddressTextBox();
    return QRCodePosterTemplateIOS.newBuilder().setOffsetX(templateConfig.getOffsetX().intValue())
        .setOffsetY(templateConfig.getOffsetY().intValue())
        .setTemplate(qrTemplateLoader.loadIosTemplateAsBytes())
        .setQrCodeSideLength(templateConfig.getQrCodeSideLength())
        .setDescriptionTextBox(QRCodeTextBoxIOS.newBuilder().setOffsetX(textBoxConfig.getOffsetX().intValue())
            .setOffsetY(textBoxConfig.getOffsetY().intValue()).setFontSize(textBoxConfig.getFontSize())
            .setWidth(textBoxConfig.getWidth())
            .setHeight(textBoxConfig.getHeight()).setFontColor(textBoxConfig.getFontColor())
            .build())
        .setAddressTextBox(QRCodeTextBoxIOS.newBuilder().setOffsetX(textBoxIosConfig.getOffsetX().intValue())
            .setOffsetY(textBoxIosConfig.getOffsetY().intValue()).setFontSize(textBoxIosConfig.getFontSize())
            .setWidth(textBoxIosConfig.getWidth())
            .setHeight(textBoxIosConfig.getHeight()).setFontColor(textBoxIosConfig.getFontColor())
            .build())
        .build();
  }
}
