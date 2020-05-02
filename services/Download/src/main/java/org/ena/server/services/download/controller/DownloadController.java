package org.ena.server.services.download.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class DownloadController {
  @GetMapping(value = "")
  public @ResponseBody ResponseEntity<String> hello() {
    return ResponseEntity.ok().body("Download Endpoint v1");
  }
}
