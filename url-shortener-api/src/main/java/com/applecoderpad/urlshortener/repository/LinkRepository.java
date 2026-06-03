package com.applecoderpad.urlshortener.repository;

import com.applecoderpad.urlshortener.exception.ConflictException;
import com.applecoderpad.urlshortener.exception.NotFoundException;
import com.applecoderpad.urlshortener.model.LinkRecord;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class LinkRepository {
  private final Map<String, LinkRecord> byCode = new ConcurrentHashMap<>();

  public void insert(LinkRecord link) {
    if (byCode.putIfAbsent(link.code(), link) != null) {
      throw new ConflictException("short code already exists");
    }
  }

  public LinkRecord get(String code) {
    LinkRecord link = byCode.get(code);
    if (link == null) throw new NotFoundException("link not found: " + code);
    return link;
  }

  public boolean exists(String code) {
    return byCode.containsKey(code);
  }

  public Collection<LinkRecord> findAll() {
    return byCode.values();
  }
}
