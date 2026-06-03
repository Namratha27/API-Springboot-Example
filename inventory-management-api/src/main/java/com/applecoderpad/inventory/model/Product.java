package com.applecoderpad.inventory.model;

import com.applecoderpad.inventory.exception.ConflictException;
import java.time.Instant;

public class Product {
  private final String sku;
  private final String name;
  private final int reorderThreshold;
  private int onHand;
  private int reserved;
  private long version;
  private Instant updatedAt;

  private Product(String sku, String name, int onHand, int reorderThreshold) {
    this.sku = sku;
    this.name = name;
    this.onHand = onHand;
    this.reorderThreshold = reorderThreshold;
    this.updatedAt = Instant.now();
  }

  public static Product create(String sku, String name, int onHand, int reorderThreshold) {
    return new Product(sku, name, onHand, reorderThreshold);
  }

  public synchronized void adjust(int delta) {
    if (onHand + delta < reserved)
      throw new ConflictException("adjustment would put on-hand below reserved stock");
    onHand += delta;
    touch();
  }

  public synchronized void reserve(int qty) {
    if (available() < qty) throw new ConflictException("insufficient available stock");
    reserved += qty;
    touch();
  }

  public synchronized void release(int qty) {
    reserved = Math.max(0, reserved - qty);
    touch();
  }

  public synchronized void touch() {
    version++;
    updatedAt = Instant.now();
  }

  public synchronized int available() {
    return onHand - reserved;
  }

  public String sku() {
    return sku;
  }

  public String name() {
    return name;
  }

  public synchronized int onHand() {
    return onHand;
  }

  public synchronized int reserved() {
    return reserved;
  }

  public int reorderThreshold() {
    return reorderThreshold;
  }

  public synchronized long version() {
    return version;
  }

  public synchronized Instant updatedAt() {
    return updatedAt;
  }
}
