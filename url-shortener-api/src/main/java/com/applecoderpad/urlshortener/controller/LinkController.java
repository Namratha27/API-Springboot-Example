package com.applecoderpad.urlshortener.controller;

import com.applecoderpad.urlshortener.dto.CreateLinkRequest;
import com.applecoderpad.urlshortener.dto.LinkResponse;
import com.applecoderpad.urlshortener.service.LinkService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Collection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LinkController {
  private final LinkService links;

  public LinkController(LinkService links) {
    this.links = links;
  }

  @PostMapping("/links")
  public ResponseEntity<LinkResponse> create(@Valid @RequestBody CreateLinkRequest request) {
    LinkResponse response = links.create(request);
    return ResponseEntity.created(URI.create("/links/" + response.code())).body(response);
  }

  @GetMapping("/links/{code}")
  public LinkResponse get(@PathVariable String code) {
    return links.get(code);
  }

  @GetMapping("/links")
  public Collection<LinkResponse> list() {
    return links.list();
  }

  @DeleteMapping("/links/{code}")
  public ResponseEntity<Void> disable(@PathVariable String code) {
    links.disable(code);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{code}")
  public ResponseEntity<Void> redirect(@PathVariable String code) {
    LinkResponse link = links.resolve(code);
    return ResponseEntity.status(HttpStatus.FOUND)
        .header(HttpHeaders.LOCATION, link.originalUrl())
        .build();
  }
}
