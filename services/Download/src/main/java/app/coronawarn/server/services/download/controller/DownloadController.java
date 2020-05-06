package app.coronawarn.server.services.download.controller;

import app.coronawarn.server.services.common.persistence.service.DiagnosisKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class DownloadController {

  @Autowired
  private DiagnosisKeyService exposureKeyService;

  @GetMapping(value = "")
  public ResponseEntity<String> hello() {
    return ResponseEntity.ok().body("Download Endpoint v1");
  }
}
