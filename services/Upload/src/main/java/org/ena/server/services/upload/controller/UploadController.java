package org.ena.server.services.upload.controller;

import java.util.Collection;
import java.util.stream.Collectors;
import org.ena.server.services.common.persistence.domain.DiagnosisKey;
import org.ena.server.services.common.persistence.service.DiagnosisKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ena.server.common.protocols.generated.ExposureKeys;

@RestController
@RequestMapping("/v1")
public class UploadController {

  @Autowired
  private DiagnosisKeyService exposureKeyService;

  @GetMapping(value = "")
  public ResponseEntity<String> hello() {
    return ResponseEntity.ok().body("Upload Endpoint v1");
  }

  @PostMapping(value = "/infections/country/{country}")
  public ResponseEntity<String> submitDiagnosisKey(@PathVariable String country,
      @RequestBody Collection<ExposureKeys.TemporaryExposureKey> exposureKeys) {
    // TODO handle fake requests appropriately
    // TODO verification
    Collection<DiagnosisKey> diagnosisKeys = exposureKeys.stream()
        .map((aProtoBufKey) -> DiagnosisKey.builder().fromProtoBuf(aProtoBufKey).build())
        .collect(Collectors.toList());

    this.exposureKeyService.saveDiagnosisKey(diagnosisKeys);

    return ResponseEntity.ok().build();
  }
}
