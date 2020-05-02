package org.ena.server.services.upload.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class UploadController {
  @GetMapping(value = "")
  public @ResponseBody ResponseEntity<String> hello() {
    return ResponseEntity.ok().body("Upload Endpoint v1");
  }
}
