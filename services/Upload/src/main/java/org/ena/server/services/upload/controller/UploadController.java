package org.ena.server.services.upload.controller;

import org.ena.server.services.common.persistence.service.TemporaryExposureKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class UploadController {

  @Autowired
  private TemporaryExposureKeyService exposureKeyService;

  @GetMapping(value = "")
  @ResponseBody
  public ResponseEntity<String> hello() {
    return ResponseEntity.ok().body("Upload Endpoint v1");
  }
}
